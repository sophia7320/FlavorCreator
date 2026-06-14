// pages/mine/mine.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')
const { formatPublishDate } = require('../../utils/util')

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
    collectionCards: [],
    currentCards: [],

    // 分页状态（分 Tab 独立维护，避免互相污染）
    publishedPage: 1,
    collectionsPage: 1,
    pageSize: 10,
    publishedHasMore: false,
    collectionsHasMore: false,
    loadingStatus: '',
    isRequesting: false,

  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    const userInfo = this.normalizeUserInfo(wx.getStorageSync('userInfo'))
    this.setData({ userInfo, backgroundUrl: userInfo.backgroundUrl })
    this.cleanDirtyFavorites()
    this.cleanDirtyPublished()
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
    // 从后端刷新最新数据（其他页面可能修改了收藏状态）
    this.loadData(true)
  },

  /**
   * 清理本地收藏中的历史无效数据
   */
  cleanDirtyFavorites() {
    const favorites = wx.getStorageSync('favorites') || []
    const valid = favorites.filter(c => {
      const id = c.recipeid || c.id || c._id
      if (id === null || id === undefined || id === '' || id === 0 || id === '0') return false
      if (String(id).startsWith('ph_')) return false
      return true
    })
    if (valid.length !== favorites.length) {
      wx.setStorageSync('favorites', valid)
      console.log(`[mine] 清理了 ${favorites.length - valid.length} 条无效收藏`)
    }
  },

  /**
   * 清理本地已发布中的历史无效数据
   */
  cleanDirtyPublished() {
    const published = wx.getStorageSync('published') || []
    const valid = published.filter(c => {
      const id = c.recipeid || c.id || c._id
      if (id === null || id === undefined || id === '' || id === 0 || id === '0') return false
      if (String(id).startsWith('ph_')) return false
      return true
    })
    if (valid.length !== published.length) {
      wx.setStorageSync('published', valid)
      console.log(`[mine] 清理了 ${published.length - valid.length} 条无效已发布`)
    }
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
   * scroll-view 触底加载更多
   */
  onScrollToLower() {
    this.loadData(false)
  },

  // ========== 数据加载 ==========

  /**
   * 统一加载入口，根据当前 tagIndex 路由到不同 API
   */
  loadData(isRefresh = false) {
    // 使用同步标志防竞态（不经过 setData，立即生效）
    if (this._requesting) return

    const { tagIndex } = this.data
    const hasMoreKey = tagIndex === 0 ? 'publishedHasMore' : 'collectionsHasMore'
    if (!isRefresh && !this.data[hasMoreKey]) return
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
    this._requesting = true
    this.setData({ isRequesting: true, loadingStatus: 'loading' })

    const page = isRefresh ? 1 : this.data.publishedPage

    return request(
      API_CONFIG.userCenter.published,
      { page, size: this.data.pageSize },
      { showLoading: false }
    )
      .then(res => {
        const list = res.list || res.records || res.data || []
        const transformed = list.map(item => this.transformCardData(item)).filter(card => !card.isPlaceholder)
        const hasMore = list.length >= this.data.pageSize

        let newCards
        if (isRefresh) {
          newCards = transformed
        } else {
          // 去重：基于 id 过滤掉已存在的卡片
          const existingIds = new Set(this.data.sharedCards.map(c => String(c.id)))
          const uniqueNew = transformed.filter(c => !existingIds.has(String(c.id)))
          newCards = [...this.data.sharedCards, ...uniqueNew]
        }

        const cardsWithCollected = this.applyCollectedStatus(newCards)

        this.setData({
          sharedCards: cardsWithCollected,
          publishedPage: isRefresh ? 2 : page + 1,
          publishedHasMore: hasMore,
          loadingStatus: hasMore ? '' : 'noMore',
          isRequesting: false
        }, () => {
          this._requesting = false
          this.updateCurrentCards()
        })
      })
      .catch(() => {
        this._requesting = false
        console.warn('已发布 API 暂不可用，使用本地数据')
        this.loadPublishedFromLocal(isRefresh)
      })
  },

  /**
   * 加载收藏菜谱（优先后端 API，失败降级到本地存储）
   */
  loadCollectionsData(isRefresh) {
    this._requesting = true
    this.setData({ isRequesting: true, loadingStatus: 'loading' })

    const page = isRefresh ? 1 : this.data.collectionsPage

    return request(
      API_CONFIG.userCenter.collections,
      { page, size: this.data.pageSize },
      { showLoading: false }
    )
      .then(res => {
        const list = res.list || res.records || res.data || []
        const transformed = list.map(item => this.transformCardData(item)).filter(card => !card.isPlaceholder)
        const hasMore = list.length >= this.data.pageSize

        let newCards
        if (isRefresh) {
          newCards = transformed
        } else {
          // 去重：基于 id 过滤掉已存在的卡片
          const existingIds = new Set(this.data.collectionCards.map(c => String(c.id)))
          const uniqueNew = transformed.filter(c => !existingIds.has(String(c.id)))
          newCards = [...this.data.collectionCards, ...uniqueNew]
        }

        const cardsWithCollected = this.applyCollectedStatus(newCards, true)

        this.setData({
          collectionCards: cardsWithCollected,
          collectionsPage: isRefresh ? 2 : page + 1,
          collectionsHasMore: hasMore,
          loadingStatus: hasMore ? '' : 'noMore',
          isRequesting: false
        }, () => {
          this._requesting = false
          this.updateCurrentCards()
        })
      })
      .catch(() => {
        this._requesting = false
        console.warn('收藏 API 暂不可用，使用本地数据')
        this.loadCollectionsFromLocal(isRefresh)
      })
  },

  /**
   * 降级方案：从本地存储加载收藏
   */
  loadCollectionsFromLocal(isRefresh) {
    const favorites = wx.getStorageSync('favorites') || []
    // 过滤掉 ID 无效的本地数据
    const valid = favorites.filter(c => {
      const id = c.recipeid || c.id || c._id
      if (id === null || id === undefined || id === '' || id === 0 || id === '0') return false
      if (String(id).startsWith('ph_')) return false
      return true
    })
    const transformed = valid.map(c => this.transformCardData({ ...c, isCollected: true }))

    this.setData({
      collectionCards: transformed,
      collectionsPage: 1,
      collectionsHasMore: false,
      loadingStatus: '',
      isRequesting: false
    }, () => this.updateCurrentCards())
  },

  /**
   * 为卡片列表批量标记收藏状态
   */
  applyCollectedStatus(cards, forceCollected = false) {
    if (forceCollected) {
      return cards.map(c => ({ ...c, isCollected: true }))
    }
    const favorites = wx.getStorageSync('favorites') || []
    const collectedIds = new Set(favorites.map(f => String(f.id)))
    return cards.map(c => {
      // 如果卡片本身已经有 isCollected 字段（来自后端），优先使用
      if (c.isCollected !== undefined) {
        return c
      }
      return { ...c, isCollected: collectedIds.has(String(c.id)) }
    })
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
   * 社区列表: { name, cover, author: { nickname, avatar } }
   * 收藏列表: { recipeId, recipeName, cover, authorName, ... }
   * 已发布列表: { id, name, cover, ... }
   * 组件: { recipeName, recipeImage, userName, userImg }
   */
  transformCardData(item) {
    // 统一使用 recipeid 作为菜谱唯一标识
    const id = item.recipeid || item.recipeId || item.id || item._id
    if (!id) {
      console.warn('[mine] transformCardData: item missing valid id', item)
    }
    return {
      id: id || '',
      isPlaceholder: !id,
      authorId: item.author?.id || item.authorId || '',
      // 社区列表: author.nickname，收藏列表: authorName，已发布列表: userName
      userName: item.author?.nickname || item.authorName || item.userName || '匿名用户',
      userImg: item.author?.avatar || item.userImg || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
      recipeName: item.name || item.recipeName || '',
      publishDate: formatPublishDate(item.createdAt || item.collectedAt),
      recipeImage: item.cover || item.recipeImage || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
      collectionCount: item.stats?.collections ?? item.collectionCount ?? item.collectCount ?? 0
    }
  },

  /**
   * 降级方案：从本地存储加载已发布菜谱
   */
  loadPublishedFromLocal(isRefresh) {
    const published = wx.getStorageSync('published') || []
    const transformed = published.map(c => this.transformCardData(c)).filter(card => !card.isPlaceholder)
    const cardsWithCollected = this.applyCollectedStatus(transformed)

    this.setData({
      sharedCards: cardsWithCollected,
      publishedPage: 1,
      publishedHasMore: false,
      loadingStatus: '',
      isRequesting: false
    }, () => this.updateCurrentCards())
  },

  /**
   * 同步所有卡片列表的收藏状态（从其他页面回来后刷新）
   */
  syncCollectedStatus() {
    const favorites = wx.getStorageSync('favorites') || []
    const collectedIds = new Set(favorites.map(f => String(f.id)))
    this.setData({
      sharedCards: this.data.sharedCards.map(c => ({ ...c, isCollected: collectedIds.has(String(c.id)) })),
      collectionCards: this.data.collectionCards.map(c => ({ ...c, isCollected: collectedIds.has(String(c.id)) }))
    }, () => this.updateCurrentCards())
  },

  /**
   * 更新当前显示的卡片列表
   */
  updateCurrentCards() {
    const { tagIndex, sharedCards, collectionCards } = this.data
    this.setData({
      currentCards: tagIndex === 0 ? sharedCards : collectionCards
    })
  },

  // ========== 卡片点击 ==========

  /**
   * 点击卡片跳转到菜谱详情
   */
  onCardTap(e) {
    const { cardId } = e.detail
    if (!cardId || cardId === '' || String(cardId).startsWith('ph_')) return
    wx.navigateTo({
      url: `/pages/recipe-detail/recipe-detail?recipeid=${cardId}`
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
      // 切换标签时始终从后端刷新最新数据
      this.loadData(true)
    })
  },

  // ========== 收藏/取消收藏 ==========

  /**
   * 收藏/取消收藏（以组件传递的 isCollected 为准，用 API 响应更新 UI）
   */
  onCollect(e) {
    const { cardId, isCollected } = e.detail

    const apiConfig = isCollected
      ? { ...API_CONFIG.community.uncollect, path: API_CONFIG.community.uncollect.path.replace('{id}', cardId) }
      : { ...API_CONFIG.community.collect, path: API_CONFIG.community.collect.path.replace('{id}', cardId) }

    request(apiConfig, {}, { showLoading: false })
      .then((res) => {
        const newCollected = res.isCollected
        const newCount = res.collectionCount

        // 同步本地存储
        const favs = wx.getStorageSync('favorites') || []
        if (newCollected) {
          const card = [...this.data.sharedCards, ...this.data.collectionCards].find(c => String(c.id) === String(cardId))
          if (card && card.id && card.id !== '' && !String(card.id).startsWith('ph_')) {
            const idx = favs.findIndex(f => String(f.id) === String(cardId))
            if (idx > -1) favs.splice(idx, 1)
            favs.unshift({
              id: card.id,
              recipeName: card.recipeName,
              publishDate: card.publishDate,
              recipeImage: card.recipeImage,
              userName: card.userName,
              userImg: card.userImg,
              collectionCount: newCount,
              collectedAt: Date.now()
            })
          }
        } else {
          const idx = favs.findIndex(f => String(f.id) === String(cardId))
          if (idx > -1) favs.splice(idx, 1)
        }
        wx.setStorageSync('favorites', favs)

        // 如果在收藏标签页，直接从后端刷新整个列表
        if (this.data.tagIndex === 1) {
          this.loadData(true)
          wx.showToast({ title: newCollected ? '已收藏' : '已取消收藏', icon: 'none' })
          return
        }

        // 不在收藏标签页，只更新单张卡片状态
        const updateCard = (list) => list.map(c =>
          String(c.id) === String(cardId) ? { ...c, isCollected: newCollected, collectionCount: newCount } : c
        )

        this.setData({
          sharedCards: updateCard(this.data.sharedCards),
          collectionCards: updateCard(this.data.collectionCards)
        }, () => this.updateCurrentCards())

        wx.showToast({ title: newCollected ? '已收藏' : '已取消收藏', icon: 'none' })
      })
      .catch(() => {
        wx.showToast({ title: '操作失败', icon: 'none' })
      })
  },

  /**
   * 删除已发布的菜谱
   */
  onDelete(e) {
    const { cardId } = e.detail

    wx.showModal({
      title: '确认删除',
      content: '删除后不可恢复，确认删除该菜谱？',
      success: (res) => {
        if (!res.confirm) return

        wx.showLoading({ title: '删除中...' })

        const apiConfig = { ...API_CONFIG.userCenter.deleteRecipe }
        apiConfig.path = apiConfig.path.replace('{id}', cardId)

        request(apiConfig, {}, { showLoading: false })
          .then(() => {
            wx.hideLoading()
            wx.showToast({ title: '已删除', icon: 'success' })

            // 从列表中移除
            const sharedCards = this.data.sharedCards.filter(c => String(c.id) !== String(cardId))
            this.setData({ sharedCards }, () => this.updateCurrentCards())
          })
          .catch(() => {
            wx.hideLoading()
            wx.showToast({ title: '删除失败', icon: 'none' })
          })
      }
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
