// pages/detail/detail.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

Page({

  data: {
    recipeId: '',
    recipe: null,
    loading: true,
    isLiked: false,
    isCollected: false
  },

  onLoad(options) {
    const id = options.id
    if (!id) {
      wx.showToast({ title: '参数错误', icon: 'none' })
      wx.navigateBack()
      return
    }
    this.setData({ recipeId: id })
    this.loadDetail(id)
  },

  loadDetail(id) {
    this.setData({ loading: true })

    const apiConfig = {
      ...API_CONFIG.recipe.getDetail,
      path: API_CONFIG.recipe.getDetail.path.replace('{id}', id)
    }

    request(apiConfig, {}, { showLoading: true })
      .then(data => {
        // 同步本地收藏状态
        const favorites = wx.getStorageSync('favorites') || []
        const isLiked = favorites.some(f => f.id == id)
        const isCollected = data.isCollected || isLiked

        this.setData({
          recipe: data,
          isLiked: data.isLiked || isLiked,
          isCollected,
          loading: false
        })
      })
      .catch(() => {
        this.setData({ loading: false })
        wx.showToast({ title: '加载失败', icon: 'none' })
      })
  },

  // 点赞/取消点赞
  onLike() {
    const { recipeId, isLiked } = this.data

    request(
      { ...API_CONFIG.community.like, path: API_CONFIG.community.like.path.replace('{id}', recipeId) },
      {},
      { showLoading: false }
    )
      .then(() => {
        let favorites = wx.getStorageSync('favorites') || []
        if (isLiked) {
          favorites = favorites.filter(f => f.id != recipeId)
        } else {
          const card = this.buildCardFromRecipe()
          favorites.unshift({ ...card, likedAt: Date.now() })
        }
        wx.setStorageSync('favorites', favorites)

        this.setData({
          isLiked: !isLiked,
          'recipe.stats.likes': (this.data.recipe.stats?.likes || 0) + (isLiked ? -1 : 1)
        })

        wx.showToast({ title: isLiked ? '已取消收藏' : '已收藏', icon: isLiked ? 'none' : 'success' })
      })
      .catch(() => {
        wx.showToast({ title: '操作失败', icon: 'none' })
      })
  },

  // 收藏/取消收藏
  onCollect() {
    const { recipeId, isCollected } = this.data
    const apiConfig = isCollected
      ? { ...API_CONFIG.community.uncollect, path: API_CONFIG.community.uncollect.path.replace('{id}', recipeId) }
      : { ...API_CONFIG.community.collect, path: API_CONFIG.community.collect.path.replace('{id}', recipeId) }

    request(apiConfig, {}, { showLoading: false })
      .then(() => {
        this.setData({
          isCollected: !isCollected,
          'recipe.stats.collections': (this.data.recipe.stats?.collections || 0) + (isCollected ? -1 : 1)
        })
        wx.showToast({ title: isCollected ? '已取消收藏' : '已收藏', icon: isCollected ? 'none' : 'success' })
      })
      .catch(() => {
        wx.showToast({ title: '操作失败', icon: 'none' })
      })
  },

  buildCardFromRecipe() {
    const r = this.data.recipe
    return {
      id: this.data.recipeId,
      userName: r.author?.nickname || '',
      userImg: r.author?.avatar || '',
      recipeName: r.name || '',
      recipeImage: r.cover || '',
      likeCount: r.stats?.likes || 0
    }
  },

  /**
   * 分享
   */
  onShareAppMessage() {
    const { recipe } = this.data
    return {
      title: recipe ? recipe.name : '看看这道菜谱',
      path: `/pages/detail/detail?id=${this.data.recipeId}`,
      imageUrl: recipe ? recipe.cover : ''
    }
  }
})
