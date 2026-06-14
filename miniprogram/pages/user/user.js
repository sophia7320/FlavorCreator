// pages/user/user.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

Page({

  data: {
    userId: '',
    userInfo: {
      nickname: '',
      avatar: ''
    },
    recipes: [],
    loading: true,
    hasMore: false,
    page: 1,
    pageSize: 10,
    loadingStatus: ''
  },

  onLoad(options) {
    const { id, name, avatar } = options
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      wx.navigateBack()
      return
    }

    this.setData({
      userId: id,
      userInfo: {
        nickname: decodeURIComponent(name || ''),
        avatar: decodeURIComponent(avatar || '')
      }
    })

    this.loadUserRecipes(true)
  },

  loadUserRecipes(isRefresh = false) {
    if (this.data._requesting) return
    if (!isRefresh && !this.data.hasMore) return

    this.setData({ _requesting: true, loadingStatus: 'loading' })

    const page = isRefresh ? 1 : this.data.page

    // 获取指定用户的已发布菜谱
    request(API_CONFIG.userCenter.published, {
      page,
      size: this.data.pageSize,
      userId: this.data.userId
    }, { showLoading: false })
      .then(res => {
        const list = res.list || res.data || []
        const hasMore = list.length >= this.data.pageSize

        const cards = list.map((item, index) => {
          const hasRealId = !!(item.id || item._id)
          return {
            id: hasRealId ? (item.id || item._id) : '',
            isPlaceholder: !hasRealId,
            authorId: item.author?.id || item.authorId || '',
            userName: item.author?.nickname || item.userName || '',
            userImg: item.author?.avatar || item.userImg || '',
            recipeName: item.name || item.recipeName || item.title || '',
            recipeImage: item.cover || item.recipeImage || item.image || '',
            collectionCount: item.collectionCount || 0
          }
        })

        const newRecipes = isRefresh ? cards : [...this.data.recipes, ...cards]

        // 同步收藏状态
        const favorites = wx.getStorageSync('favorites') || []
        const collectedIds = new Set(favorites.map(f => f.id))
        const cardsWithCollected = newRecipes.map(c => ({ ...c, isCollected: collectedIds.has(c.id) }))

        this.setData({
          recipes: cardsWithCollected,
          page: isRefresh ? 2 : page + 1,
          hasMore,
          loading: false,
          loadingStatus: hasMore ? '' : 'noMore',
          _requesting: false
        })
      })
      .catch(() => {
        this.setData({
          loading: false,
          loadingStatus: 'error',
          _requesting: false
        })
      })
  },

  onReady() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().hide()
    }
  },

  onPullDownRefresh() {
    this.loadUserRecipes(true)
  },

  onReachBottom() {
    this.loadUserRecipes(false)
  },

  onCardTap(e) {
    const { cardId } = e.detail
    if (!cardId || cardId === '' || String(cardId).startsWith('ph_')) return
    wx.navigateTo({
      url: `/pages/recipe-detail/recipe-detail?id=${cardId}`
    })
  },

  onCollect(e) {
    const { cardId } = e.detail
    const card = this.data.recipes.find(c => c.id === cardId)
    if (!card) return

    // 根据当前收藏状态决定调用收藏还是取消收藏
    const isCollecting = !card.isCollected
    const apiConfig = isCollecting ? API_CONFIG.community.collect : API_CONFIG.community.uncollect
    const api = { ...apiConfig, path: apiConfig.path.replace('{id}', cardId) }

    request(api, {}, { showLoading: false })
      .then(() => {
        let favorites = wx.getStorageSync('favorites') || []
        const index = favorites.findIndex(f => f.id === cardId)

        if (index > -1) {
          favorites.splice(index, 1)
          wx.showToast({ title: '已取消收藏', icon: 'none' })
        } else {
          // 拒绝对无真实 ID 的卡片执行收藏写入
          if (!card.id || card.id === '' || String(card.id).startsWith('ph_')) {
            wx.showToast({ title: '该菜谱暂无法收藏', icon: 'none' })
            return
          }
          favorites.unshift({ ...card, collectedAt: Date.now() })
          wx.showToast({ title: '已收藏', icon: 'success' })
        }

        wx.setStorageSync('favorites', favorites)

        const collectedIds = new Set(favorites.map(f => f.id))
        this.setData({
          recipes: this.data.recipes.map(c => ({ ...c, isCollected: collectedIds.has(c.id) }))
        })
      })
      .catch(() => {
        wx.showToast({ title: '操作失败', icon: 'none' })
      })
  },

  onShare(e) {
    const { cardId } = e.detail
    this.setData({ currentShareCardId: cardId })
  },

  onShareAppMessage() {
    const { userInfo } = this.data
    return {
      title: `${userInfo.nickname} 的菜谱`,
      path: `/pages/user/user?id=${this.data.userId}&name=${encodeURIComponent(userInfo.nickname)}&avatar=${encodeURIComponent(userInfo.avatar)}`
    }
  }
})
