// pages/community/community.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')
const { formatPublishDate } = require('../../utils/util')

// 标签名 → API category 参数映射
const TAG_CATEGORY_MAP = {
  '快手菜': 'fast',
  '减脂餐': 'lowcal',
  '家常菜': 'home',
  '养生频道': 'health',
  '特色菜': 'special'
}

Page({

  /**
   * 页面的初始数据
   */
  data: {
		tags: [
			{name: '快手菜', selected: true},
			{name: '减脂餐', selected: false},
			{name: '家常菜', selected: false},
			{name: '养生频道', selected: false},
			{name: '特色菜', selected: false},
		],
		leftList: [
			{ id: 'ph_l0', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: '', publishDate: '', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', collectionCount: 0, isPlaceholder: true },
			{ id: 'ph_l1', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: '', publishDate: '', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', collectionCount: 0, isPlaceholder: true },
			{ id: 'ph_l2', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: '', publishDate: '', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', collectionCount: 0, isPlaceholder: true },
			{ id: 'ph_l3', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: '', publishDate: '', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', collectionCount: 0, isPlaceholder: true },
		],
		rightList: [
			{ id: 'ph_r0', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: '', publishDate: '', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', collectionCount: 0, isPlaceholder: true },
			{ id: 'ph_r1', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: '', publishDate: '', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', collectionCount: 0, isPlaceholder: true },
			{ id: 'ph_r2', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: '', publishDate: '', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', collectionCount: 0, isPlaceholder: true },
			{ id: 'ph_r3', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: '', publishDate: '', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', collectionCount: 0, isPlaceholder: true },
		],
		page: 1,
		pageSize: 10,
		loadingStatus: '',
		isRequesting: false,
		hasMore: true,
		currentShareCardId: null,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.loadData(true)
  },

  onReady() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().onTabPageShow()
    }
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().onTabPageShow()
    }
    // 同步收藏状态
    this.syncCollectedStatus()
  },

  onHide() {

  },

  onUnload() {

  },

  onPullDownRefresh() {
    this.loadData(true)
  },

  onReachBottom() {
    this.loadData(false)
  },

  loadData(isRefresh = false) {
    if (this.data.isRequesting) {
      return
    }
    if (!isRefresh && !this.data.hasMore) {
      return
    }

    this.setData({ isRequesting: true, loadingStatus: 'loading' })

    const page = isRefresh ? 1 : this.data.page
    const selectedTag = this.data.tags.find(t => t.selected)
    const category = selectedTag ? TAG_CATEGORY_MAP[selectedTag.name] : 'fast'

    request(API_CONFIG.community.list, { page, size: this.data.pageSize, category }, { showLoading: false })
      .then(res => {
        const list = res.list || res.records || res.data || []
        const hasMore = res.hasMore ?? (list.length >= this.data.pageSize)

        this.appendCards(list, isRefresh)

        this.setData({
          page: isRefresh ? 2 : page + 1,
          hasMore,
          loadingStatus: hasMore ? '' : 'noMore',
          isRequesting: false
        })
      })
      .catch(() => {
        this.setData({ loadingStatus: 'error', isRequesting: false })
      })
  },

  appendCards(cards, isRefresh = false) {
    let newLeftList = isRefresh ? [] : [...this.data.leftList]
    let newRightList = isRefresh ? [] : [...this.data.rightList]

    cards.forEach((item, index) => {
      const recipeId = item.recipeid || item.id || item._id
      const card = {
        id: recipeId || Date.now() + index,
        isPlaceholder: !recipeId,
        authorId: item.author?.id || item.authorId || '',
        userName: item.author?.nickname || item.userName || item.user_name || '匿名用户',
        userImg: item.author?.avatar || item.userImg || item.user_avatar || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
        recipeName: item.name || item.recipeName || item.title || '',
        publishDate: formatPublishDate(item.createdAt),
        recipeImage: item.cover || item.recipeImage || item.image || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
        collectionCount: item.collectionCount || 0
      }

      const totalCount = newLeftList.length + newRightList.length
      if (totalCount % 2 === 0) {
        newLeftList.push(card)
      } else {
        newRightList.push(card)
      }
    })

    // 同步收藏状态
    const favorites = wx.getStorageSync('favorites') || []
    const collectedIds = new Set(favorites.map(f => String(f.id)))
    newLeftList = newLeftList.map(c => ({ ...c, isCollected: collectedIds.has(String(c.id)) }))
    newRightList = newRightList.map(c => ({ ...c, isCollected: collectedIds.has(String(c.id)) }))

    this.setData({ leftList: newLeftList, rightList: newRightList })
  },

  /**
   * 同步所有卡片的收藏状态
   */
  syncCollectedStatus() {
    const favorites = wx.getStorageSync('favorites') || []
    const collectedIds = new Set(favorites.map(f => String(f.id)))
    this.setData({
      leftList: this.data.leftList.map(c => ({ ...c, isCollected: collectedIds.has(String(c.id)) })),
      rightList: this.data.rightList.map(c => ({ ...c, isCollected: collectedIds.has(String(c.id)) }))
    })
  },

  onScrollToLower() {
    this.loadData(false)
  },

  onSwitchTag(e) {
    const index = parseInt(e.currentTarget.dataset.index)
    const currentTag = this.data.tags.find(t => t.selected)

    // 点击当前已选中的标签，不重复请求
    if (currentTag && this.data.tags.indexOf(currentTag) === index) return

    const tags = this.data.tags.map((t, i) => ({
      ...t,
      selected: i === index
    }))

    this.setData({
      tags,
      page: 1,
      leftList: [],
      rightList: [],
      hasMore: true
    }, () => {
      this.loadData(true)
    })
  },

  onCollect(e) {
    const { cardId, isCollected } = e.detail

    const apiConfig = isCollected
      ? API_CONFIG.community.uncollect
      : API_CONFIG.community.collect
    const api = { ...apiConfig, path: apiConfig.path.replace('{id}', cardId) }

    request(api, {}, { showLoading: false })
      .then((res) => {
        const newCollected = res.isCollected
        const newCount = res.collectionCount

        // 同步本地存储
        let favorites = wx.getStorageSync('favorites') || []
        if (newCollected) {
          const card = [...this.data.leftList, ...this.data.rightList].find(c => c.id === cardId)
          if (card) {
            favorites = favorites.filter(f => String(f.id) !== String(cardId))
            favorites.unshift({
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
          favorites = favorites.filter(f => String(f.id) !== String(cardId))
        }
        wx.setStorageSync('favorites', favorites)

        // 以 API 响应为准更新 UI
        const updateCard = (list) => list.map(c =>
          c.id === cardId ? { ...c, isCollected: newCollected, collectionCount: newCount } : c
        )

        this.setData({
          leftList: updateCard(this.data.leftList),
          rightList: updateCard(this.data.rightList)
        })

        wx.showToast({ title: newCollected ? '已收藏' : '已取消收藏', icon: 'none' })
      })
      .catch(() => {
        wx.showToast({ title: '操作失败', icon: 'none' })
      })
  },

  onShare(e) {
    const { cardId } = e.detail
    this.setData({ currentShareCardId: cardId })
  },

  onCardTap(e) {
    const { cardId } = e.detail
    if (!cardId) return
    const card = [...this.data.leftList, ...this.data.rightList].find(c => c.id === cardId)
    if (card && card.isPlaceholder) return
    wx.navigateTo({
      url: `/pages/recipe-detail/recipe-detail?recipeid=${cardId}`
    })
  },

  onUserTap(e) {
    const { authorId, userName, userImg } = e.detail
    if (!authorId) return
    wx.navigateTo({
      url: `/pages/user/user?id=${authorId}&name=${encodeURIComponent(userName || '')}&avatar=${encodeURIComponent(userImg || '')}`
    })
  },

  /**
   * 用户点击右上角分享 / 转发
   */
  onShareAppMessage() {
    const cardId = this.data.currentShareCardId
    const card = [...this.data.leftList, ...this.data.rightList].find(c => c.id === cardId)

    return {
      title: card ? card.recipeName : '看看这道菜谱',
      path: `/pages/recipe-detail/recipe-detail?recipeid=${cardId || ''}`,
      imageUrl: card ? card.recipeImage : ''
    }
  }
})
