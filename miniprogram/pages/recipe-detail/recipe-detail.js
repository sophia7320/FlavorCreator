// pages/recipe-detail/recipe-detail.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')
const { formatPublishDate } = require('../../utils/util')

Page({
  data: {
    recipeId: 0,
    isAI: false,
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
    isCollected: false,
    images: []
  },

  onLoad(options) {
    const recipeid = options.recipeid
    const isAI = options.ai === 'true'

    if (isAI) {
      this.setData({ isAI: true })
      this.loadAIRecipe()
      return
    }

    if (!recipeid) {
      wx.showToast({ title: '缺少菜谱ID', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }
    this.setData({ recipeId: recipeid })
    this.fetchDetail(recipeid)
  },

  // 从本地存储加载 AI 生成的菜谱
  loadAIRecipe() {
    const recipe = wx.getStorageSync('aiRecipeResult')
    if (!recipe) {
      wx.showToast({ title: '获取菜谱数据失败', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1500)
      return
    }

    // 使用后端返回的 recipeid，无效时统一为 0 走纯本地逻辑
    const rawId = recipe.recipeid
    const recipeId = (rawId !== null && rawId !== undefined && rawId !== 0 && rawId !== '0') ? rawId : 0

    // 从本地存储恢复收藏状态
    const favorites = wx.getStorageSync('favorites') || []
    const isCollected = favorites.some(f => f.id === recipeId)

    this.setData({
      recipeId,
      isAI: true,
      name: recipe.name || '',
      cover: recipe.cover || '',
      desc: recipe.tips || '',
      author: recipe.author || null,
      ingredients: (recipe.ingredients || []).map(item => ({
        name: item.name,
        quantity: item.quantity,
        unit: item.unit
      })),
      procedure: (recipe.steps || []).map((s, i) => ({
        step: s.order || i + 1,
        desc: s.description || '',
        image: ''
      })),
      tags: recipe.tags || [],
      cookTime: recipe.cookTime || '',
      difficulty: recipe.difficulty || '',
      calories: recipe.calories || '',
      stats: recipe.stats || null,
      isCollected
    })
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
    const { recipeId, isCollected, isAI } = this.data

    // AI 菜谱且没有有效后端 ID 时，走纯本地收藏逻辑（不调后端 API）
    if (isAI && (!recipeId || recipeId === 0 || recipeId === '0')) {
      this.toggleLocalCollect()
      return
    }

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
        // 同步本地存储（让 mine 页面可以读取）
        this.syncFavoritesToStorage(res.isCollected, res.collectionCount)
        wx.showToast({
          title: res.isCollected ? '已收藏' : '已取消收藏',
          icon: 'none'
        })
      })
      .catch((err) => {
        console.error('收藏操作失败', err)
        // AI 菜谱在 API 不可用时降级为本地逻辑
        if (isAI) {
          this.toggleLocalCollect()
        } else {
          wx.showToast({ title: '操作失败', icon: 'none' })
        }
      })
  },

  /** 纯本地收藏/取消收藏（AI 菜谱无后端 ID 时使用） */
  toggleLocalCollect() {
    const { isCollected, stats } = this.data
    const newCollected = !isCollected
    const newCount = (stats?.collections || 0) + (newCollected ? 1 : -1)

    this.setData({
      isCollected: newCollected,
      'stats.collections': Math.max(0, newCount)
    })
    this.syncFavoritesToStorage(newCollected, Math.max(0, newCount))

    wx.showToast({
      title: newCollected ? '已收藏' : '已取消收藏',
      icon: 'none'
    })
  },

  /** 同步收藏状态到本地存储 */
  syncFavoritesToStorage(isCollected, collectionCount) {
    const { recipeId, name, cover, author } = this.data
    let favorites = wx.getStorageSync('favorites') || []

    if (isCollected) {
      // 拒绝对无效 ID 写入收藏
      if (!recipeId || recipeId === 0 || recipeId === '0') {
        console.warn('[recipe-detail] syncFavoritesToStorage: skipping invalid recipeId', recipeId)
        return
      }
      const card = {
        id: Number(recipeId),
        recipeName: name,
        publishDate: formatPublishDate(new Date().toISOString()),
        recipeImage: cover,
        userName: author?.nickname || '',
        userImg: author?.avatar || '',
        collectionCount: collectionCount || 1,
        collectedAt: Date.now()
      }
      favorites = favorites.filter(f => String(f.id) !== String(recipeId))
      favorites.unshift(card)
    } else {
      favorites = favorites.filter(f => String(f.id) !== String(recipeId))
    }
    wx.setStorageSync('favorites', favorites)
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
      path: `/pages/recipe-detail/recipe-detail?recipeid=${recipeId}`
    }
  }
})
