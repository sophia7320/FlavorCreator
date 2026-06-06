// pages/mine/mine.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo: {
      nickName: '默认用户',
      avatar: '',
      userName: 'user001'
    },
    backgroundUrl: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/bg/mine-background.png',
    tagIndex: 0,
    sharedCards: [],
    likeCards: [],
    currentCards: [],

    // 分页状态
    publishedPage: 1,
    collectionsPage: 1,
    pageSize: 10,
    hasMore: false,
    loadingStatus: '',
    isRequesting: false
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    const userInfo = this.normalizeUserInfo(wx.getStorageSync('userInfo'))
    this.setData({ userInfo, backgroundUrl: userInfo.backgroundUrl })
    this.loadData(true)
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().onTabPageShow()
    }
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().onTabPageShow()
    }
    const userInfo = this.normalizeUserInfo(wx.getStorageSync('userInfo'))
    this.setData({ userInfo, backgroundUrl: userInfo.backgroundUrl })
    // 同步收藏状态（从其他页面回来后更新 isLiked）
    this.syncLikedStatus()
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadData(true).then(() => {
      wx.stopPullDownRefresh()
    })
  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {
    this.loadData(false)
  },

  // ========== 数据加载 ==========

  /**
   * 统一加载入口，根据当前 tagIndex 路由到不同 API
   */
  loadData(isRefresh = false) {
    if (this.data.isRequesting) return
    if (!isRefresh && !this.data.hasMore) return

    const { tagIndex } = this.data
    if (tagIndex === 0) {
      return this.loadPublishedData(isRefresh)
    } else {
      return this.loadCollectionsData(isRefresh)
    }
  },

  /**
   * 加载已发布菜谱（从后端 API）
   */
  loadPublishedData(isRefresh) {
    this.setData({ isRequesting: true, loadingStatus: 'loading' })

    const page = isRefresh ? 1 : this.data.publishedPage

    return request(
      API_CONFIG.userCenter.published,
      { page, size: this.data.pageSize },
      { showLoading: false }
    )
      .then(res => {
        const list = res.data.list || res.data || []
        const transformed = list.map(item => this.transformCardData(item))
        const hasMore = list.length >= this.data.pageSize
        const newCards = isRefresh ? transformed : [...this.data.sharedCards, ...transformed]
        const cardsWithLiked = this.applyLikedStatus(newCards)

        this.setData({
          sharedCards: cardsWithLiked,
          publishedPage: isRefresh ? 2 : page + 1,
          hasMore,
          loadingStatus: hasMore ? '' : 'noMore',
          isRequesting: false
        }, () => this.updateCurrentCards())
      })
      .catch(() => {
        // 后端不可用时，降级到本地存储
        console.warn('已发布 API 暂不可用，使用本地数据')
        this.loadPublishedFromLocal(isRefresh)
      })
  },

  /**
   * 加载收藏菜谱（优先后端 API，失败降级到本地存储）
   */
  loadCollectionsData(isRefresh) {
    this.setData({ isRequesting: true, loadingStatus: 'loading' })

    const page = isRefresh ? 1 : this.data.collectionsPage

    return request(
      API_CONFIG.userCenter.collections,
      { page, size: this.data.pageSize },
      { showLoading: false }
    )
      .then(res => {
        const list = res.data.list || res.data || []
        const transformed = list.map(item => this.transformCardData(item))
        const hasMore = list.length >= this.data.pageSize
        const newCards = isRefresh ? transformed : [...this.data.likeCards, ...transformed]
        const cardsWithLiked = this.applyLikedStatus(newCards, true)

        this.setData({
          likeCards: cardsWithLiked,
          collectionsPage: isRefresh ? 2 : page + 1,
          hasMore,
          loadingStatus: hasMore ? '' : 'noMore',
          isRequesting: false
        }, () => this.updateCurrentCards())
      })
      .catch(() => {
        // 后端不可用时，降级到本地存储
        console.warn('收藏 API 暂不可用，使用本地数据')
        this.loadCollectionsFromLocal(isRefresh)
      })
  },

  /**
   * 降级方案：从本地存储加载收藏
   */
  loadCollectionsFromLocal(isRefresh) {
    const favorites = wx.getStorageSync('favorites') || []
    const transformed = favorites.map(c => this.transformCardData({ ...c, isLiked: true }))

    this.setData({
      likeCards: transformed,
      collectionsPage: 1,
      hasMore: false,
      loadingStatus: '',
      isRequesting: false
    }, () => this.updateCurrentCards())
  },

  /**
   * 为卡片列表批量标记收藏状态
   */
  applyLikedStatus(cards, forceLiked = false) {
    if (forceLiked) {
      return cards.map(c => ({ ...c, isLiked: true }))
    }
    const favorites = wx.getStorageSync('favorites') || []
    const likedIds = new Set(favorites.map(f => f.id))
    return cards.map(c => ({ ...c, isLiked: likedIds.has(c.id) }))
  },

  /**
   * 规范化用户信息：将后端字段映射到 WXML 绑定字段
   * 后端: { nickname, avatar } → 前端: { nickName, avatarUrl }
   */
  normalizeUserInfo(raw) {
    const DEFAULT_BG = 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/bg/mine-background.png'
    if (!raw) {
      return { nickName: '默认用户', avatarUrl: '', backgroundUrl: DEFAULT_BG }
    }
    return {
      ...raw,
      nickName: raw.nickName || raw.nickname || '默认用户',
      avatarUrl: raw.avatarUrl || raw.avatar || '',
      backgroundUrl: raw.background || DEFAULT_BG
    }
  },

  /**
   * 将后端卡片数据转换为 recipe-card 组件所需格式
   * 后端: { name, cover, author: { nickname, avatar } }
   * 组件: { recipeName, recipeImage, userName, userImg }
   */
  transformCardData(item) {
    return {
      id: item.id || item._id,
      authorId: item.author?.id || item.authorId || '',
      userName: item.author?.nickname || item.userName || '匿名用户',
      userImg: item.author?.avatar || item.userImg || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
      recipeName: item.name || item.recipeName || '',
      recipeImage: item.cover || item.recipeImage || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
      likeCount: item.likeCount || 0
    }
  },

  /**
   * 降级方案：从本地存储加载已发布菜谱
   */
  loadPublishedFromLocal(isRefresh) {
    const published = wx.getStorageSync('published') || []
    const transformed = published.map(c => this.transformCardData(c))
    const cardsWithLiked = this.applyLikedStatus(transformed)

    this.setData({
      sharedCards: cardsWithLiked,
      publishedPage: 1,
      hasMore: false,
      loadingStatus: '',
      isRequesting: false
    }, () => this.updateCurrentCards())
  },

  /**
   * 同步所有卡片列表的收藏状态（从其他页面回来后刷新）
   */
  syncLikedStatus() {
    const favorites = wx.getStorageSync('favorites') || []
    const likedIds = new Set(favorites.map(f => f.id))
    this.setData({
      sharedCards: this.data.sharedCards.map(c => ({ ...c, isLiked: likedIds.has(c.id) })),
      likeCards: this.data.likeCards.map(c => ({ ...c, isLiked: likedIds.has(c.id) }))
    }, () => this.updateCurrentCards())
  },

  /**
   * 更新当前显示的卡片列表
   */
  updateCurrentCards() {
    const { tagIndex, sharedCards, likeCards } = this.data
    this.setData({
      currentCards: tagIndex === 0 ? sharedCards : likeCards
    })
  },

  // ========== 标签切换 ==========

  /**
   * 切换标签
   */
  onSwitchTag(e) {
    const index = parseInt(e.currentTarget.dataset.index)
    if (index === this.data.tagIndex) return

    this.setData({ tagIndex: index }, () => {
      this.updateCurrentCards()
      // 切换标签时，如果目标列表为空则自动加载
      const targetCards = index === 0 ? this.data.sharedCards : this.data.likeCards
      if (targetCards.length === 0) {
        this.loadData(true)
      }
    })
  },

  // ========== 收藏/取消收藏 ==========

  /**
   * 收藏/取消收藏（调用后端 API + 同步本地存储）
   */
  onLike(e) {
    const { cardId } = e.detail

    // 在所有列表中查找对应卡片
    const card = [...this.data.sharedCards, ...this.data.likeCards].find(c => c.id === cardId)
    if (!card) return

    // 判断操作类型
    const isCollecting = !card.isLiked
    const apiConfig = isCollecting
      ? { ...API_CONFIG.community.collect, path: API_CONFIG.community.collect.path.replace('{id}', cardId) }
      : { ...API_CONFIG.community.uncollect, path: API_CONFIG.community.uncollect.path.replace('{id}', cardId) }

    request(apiConfig, {}, { showLoading: false })
      .then(() => {
        // 同步本地存储
        let favorites = wx.getStorageSync('favorites') || []

        if (isCollecting) {
          favorites.unshift({ ...card, isLiked: true, likedAt: Date.now() })
        } else {
          favorites = favorites.filter(f => f.id !== cardId)
        }

        wx.setStorageSync('favorites', favorites)

        // 更新所有列表中的 isLiked 状态
        const likedIds = new Set(favorites.map(f => f.id))
        const updateLiked = (list) => list.map(c => ({ ...c, isLiked: likedIds.has(c.id) }))

        this.setData({
          sharedCards: updateLiked(this.data.sharedCards),
          likeCards: updateLiked(this.data.likeCards)
        }, () => this.updateCurrentCards())

        wx.showToast({
          title: isCollecting ? '已收藏' : '已取消收藏',
          icon: isCollecting ? 'success' : 'none'
        })
      })
      .catch(() => {
        wx.showToast({ title: '操作失败', icon: 'none' })
      })
  },

  // ========== 分享 ==========

  /**
   * 分享
   */
  onShare(e) {
    const { cardId } = e.detail
    console.log('share card:', cardId)
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  // ========== 导航 ==========

  /**
   * 跳转到设置页面
   */
  goToSettings() {
    wx.navigateTo({
      url: '/pages/settings/settings'
    })
  },

  /**
   * 点击菜单项
   */
  onMenuTap(e) {
    const id = e.currentTarget.dataset.id
    console.log('点击菜单项:', id)
    
    switch(id) {
      case 'favorites':
        wx.showToast({
          title: '我的收藏功能开发中',
          icon: 'none'
        })
        break
      case 'works':
        wx.showToast({
          title: '我的作品功能开发中',
          icon: 'none'
        })
        break
      case 'notes':
        wx.showToast({
          title: '厨艺笔记功能开发中',
          icon: 'none'
        })
        break
      case 'help':
        wx.showToast({
          title: '帮助与反馈功能开发中',
          icon: 'none'
        })
        break
      case 'about':
        wx.showToast({
          title: '关于我们功能开发中',
          icon: 'none'
        })
        break
    }
  }
})
