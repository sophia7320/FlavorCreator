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

        const cards = list.map((item, index) => ({
          id: item.id || item._id || Date.now() + index,
          authorId: item.author?.id || item.authorId || '',
          userName: item.author?.nickname || item.userName || '',
          userImg: item.author?.avatar || item.userImg || '',
          recipeName: item.name || item.recipeName || item.title || '',
          recipeImage: item.cover || item.recipeImage || item.image || '',
          likeCount: item.likeCount || 0
        }))

        const newRecipes = isRefresh ? cards : [...this.data.recipes, ...cards]

        // 同步收藏状态
        const favorites = wx.getStorageSync('favorites') || []
        const likedIds = new Set(favorites.map(f => f.id))
        const cardsWithLiked = newRecipes.map(c => ({ ...c, isLiked: likedIds.has(c.id) }))

        this.setData({
          recipes: cardsWithLiked,
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
    wx.navigateTo({
      url: `/pages/detail/detail?id=${cardId}`
    })
  },

  onLike(e) {
    const { cardId } = e.detail
    const likeApi = { ...API_CONFIG.community.like, path: API_CONFIG.community.like.path.replace('{id}', cardId) }

    request(likeApi, {}, { showLoading: false })
      .then(() => {
        const card = this.data.recipes.find(c => c.id === cardId)
        if (!card) return

        let favorites = wx.getStorageSync('favorites') || []
        const index = favorites.findIndex(f => f.id === cardId)

        if (index > -1) {
          favorites.splice(index, 1)
          wx.showToast({ title: '已取消收藏', icon: 'none' })
        } else {
          favorites.unshift({ ...card, likedAt: Date.now() })
          wx.showToast({ title: '已收藏', icon: 'success' })
        }

        wx.setStorageSync('favorites', favorites)

        const likedIds = new Set(favorites.map(f => f.id))
        this.setData({
          recipes: this.data.recipes.map(c => ({ ...c, isLiked: likedIds.has(c.id) }))
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
