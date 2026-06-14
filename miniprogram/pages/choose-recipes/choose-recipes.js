// pages/choose-recipes/choose-recipes.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

Page({

  /**
   * 页面的初始数据
   */
  data: {
    matchDegree: 0,
    recipes: [],
    needAiGenerate: false,
    loading: true,
    aiGenerating: false,
    recipeRequest: null
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    // 从本地存储获取请求数据和响应数据
    const recipeRequest = wx.getStorageSync('recipeRequest')
    const recipeResult = wx.getStorageSync('recipeResult')
    
    this.setData({ recipeRequest })
    
    if (recipeResult) {
      this.setData({
        matchDegree: recipeResult.matchDegree || 0,
        recipes: recipeResult.recipes || [],
        needAiGenerate: recipeResult.needAiGenerate || false,
        loading: false
      })
      console.log('接收到的菜谱数据:', recipeResult)
    } else {
      // TODO: 后端接口就绪后，删除以下 mock 数据，恢复原来的空状态逻辑
      // ========== 临时 mock 数据（开始） ==========
      const mockRecipes = [
        {
          id: 'mock_1',
          name: '示例菜谱一',
          cover: '',
          cookTime: 30,
          matchDegree: 95
        },
        {
          id: 'mock_2',
          name: '示例菜谱二',
          cover: '',
          cookTime: 45,
          matchDegree: 88
        },
        {
          id: 'mock_3',
          name: '示例菜谱三',
          cover: '',
          cookTime: 20,
          matchDegree: 76
        }
      ]
      this.setData({
        recipes: mockRecipes,
        matchDegree: 85,
        needAiGenerate: true,
        loading: false
      })
      // ========== 临时 mock 数据（结束） ==========
    }
  },

  /**
   * 返回上一页
   */
  onBack() {
    wx.navigateBack()
  },

  /**
   * 点击菜谱卡片 - 获取详情
   */
  onRecipeTap(e) {
    const { id } = e.currentTarget.dataset
    console.log('点击菜谱:', id)

    // 直接跳转到菜谱详情页，由详情页自行获取数据
    wx.navigateTo({
      url: `/pages/recipe-detail/recipe-detail?recipeid=${id}`
    })
  },

  /**
   * 点击开始制作 - 获取详情并开始制作
   */
  onStartCook(e) {
    const { id } = e.currentTarget.dataset
    console.log('开始制作菜谱:', id)

    wx.navigateTo({
      url: `/pages/recipe-detail/recipe-detail?recipeid=${id}`
    })
  },

  /**
   * AI 生成菜谱
   */
  onAiGenerate() {
    const { recipeRequest } = this.data
    
    if (!recipeRequest) {
      wx.showToast({
        title: '缺少请求数据',
        icon: 'none'
      })
      return
    }
    
    console.log('AI 生成菜谱，请求数据:', recipeRequest)
    
    // 显示加载状态
    this.setData({ aiGenerating: true })
    
    // 调用 AI 生成接口
    request(API_CONFIG.recipe.aiGenerate, recipeRequest)
      .then(res => {
        this.setData({ aiGenerating: false })
        console.log('AI 生成成功:', res)
        
        if (res.recipe) {
          // 存储完整的 AI 菜谱数据
          wx.setStorageSync('aiRecipeResult', res.recipe)
          // 跳转时用 ai=true 参数，详情页会走 loadAIRecipe 路径
          wx.navigateTo({
            url: '/pages/recipe-detail/recipe-detail?ai=true'
          })
        }
      })
      .catch(err => {
        this.setData({ aiGenerating: false })
        console.error('AI 生成失败:', err)
        wx.showToast({
          title: 'AI 生成失败',
          icon: 'none'
        })
      })
  }
})
