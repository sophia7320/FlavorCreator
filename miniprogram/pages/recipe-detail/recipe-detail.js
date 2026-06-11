// pages/recipe-detail/recipe-detail.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

Page({
  data: {
    recipeId: 0,
    name: '',
    cover: '',
    desc: '',
    author: null,
    ingredients: [],
    procedure: [],
    tags: [],
    cookTime: '',
    difficulty: '',
    calories: '',
    stats: null,
    isLiked: false,
    isCollected: false,
    images: []
  },

  onLoad(options) {
    const id = options.id
    if (!id) {
      wx.showToast({ title: '缺少菜谱ID', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    this.setData({ recipeId: id })
    this.fetchDetail(id)
  },

  fetchDetail(id) {
    const apiConfig = {
      ...API_CONFIG.recipe.getDetail,
      path: API_CONFIG.recipe.getDetail.path.replace('{id}', id)
    }

    request(apiConfig, {}, { showLoading: true })
      .then((res) => {
        this.setData({
          name: res.name || '',
          cover: res.cover || '',
          desc: res.tips || '',
          author: res.author || null,
          ingredients: (res.ingredients || []).map(item => ({
            name: item.name,
            quantity: item.quantity,
            unit: item.unit
          })),
          procedure: (res.steps || []).map((s, i) => ({
            step: s.order || i + 1,
            desc: s.description || '',
            image: (res.images && res.images[i]) || ''
          })),
          tags: res.tags || [],
          cookTime: res.cookTime || '',
          difficulty: res.difficulty || '',
          calories: res.calories || '',
          stats: res.stats || null,
          isLiked: res.isLiked || false,
          isCollected: res.isCollected || false,
          images: res.images || []
        })
      })
      .catch((err) => {
        console.error('获取菜谱详情失败', err)
        if (err && err.code !== 401) {
          wx.showToast({ title: '加载失败', icon: 'none' })
        }
      })
  },

  /** 收藏 / 取消收藏 */
  onCollectBtnTap() {
    const { recipeId, isCollected } = this.data
    const apiConfig = {
      ...(isCollected ? API_CONFIG.community.uncollect : API_CONFIG.community.collect),
      path: (isCollected ? API_CONFIG.community.uncollect.path : API_CONFIG.community.collect.path).replace('{id}', recipeId)
    }

    request(apiConfig, {}, { showLoading: false })
      .then((res) => {
        this.setData({
          isCollected: res.isCollected,
          'stats.collections': res.collectionCount
        })
        wx.showToast({
          title: isCollected ? '已取消收藏' : '已收藏',
          icon: 'none'
        })
      })
      .catch((err) => {
        console.error('收藏操作失败', err)
      })
  },

  /** 点赞 / 取消点赞 */
  onLikeBtnTap() {
    const { recipeId, isLiked } = this.data
    const apiConfig = {
      ...(isLiked ? API_CONFIG.community.unlike : API_CONFIG.community.like),
      path: (isLiked ? API_CONFIG.community.unlike.path : API_CONFIG.community.like.path).replace('{id}', recipeId)
    }

    request(apiConfig, {}, { showLoading: false })
      .then((res) => {
        this.setData({
          isLiked: res.isLiked,
          'stats.likes': res.likeCount
        })
        wx.showToast({
          title: isLiked ? '已取消点赞' : '已点赞',
          icon: 'none'
        })
      })
      .catch((err) => {
        console.error('点赞操作失败', err)
      })
  },

  onShareBtnTap() {
    // 触发页面分享
  },

  /** 同步清除仓库食材 */
  onFinishBtnTap() {
    wx.showModal({
      title: '提示',
      content: '标记完成后将从你的食材仓库中同步扣除对应食材，确认已完成？',
      success: (res) => {
        if (res.confirm) {
          // TODO: 调用后续后端接口同步扣除
          wx.showToast({ title: '已完成！', icon: 'success' })
        }
      }
    })
  },

  onShareAppMessage() {
    const { name, recipeId } = this.data
    return {
      title: name || '美味菜谱',
      path: `/pages/recipe-detail/recipe-detail?id=${recipeId}`
    }
  }
})
