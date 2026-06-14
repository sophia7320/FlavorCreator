// pages/history/history.js
const { request } = require('../../utils/request')
const { API_CONFIG } = require('../../config/api')

Page({

  /**
   * 页面的初始数据
   */
  data: {
    todayRecipes: [],
    recentRecipes: [],
    loading: false,
    // 占位数据，作为加载失败或空数据的兜底展示
    placeholders: {
      todayRecipes: [
        {
          id: 'ph_t0', authorId: '',
          userName: 'User Name',
          userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
          recipeName: 'Additional info goes here',
          recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
          collectionCount: 0, isPlaceholder: true
        },
        {
          id: 'ph_t1', authorId: '',
          userName: 'User Name',
          userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
          recipeName: 'Additional info goes here',
          recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
          collectionCount: 0, isPlaceholder: true
        },
        {
          id: 'ph_t2', authorId: '',
          userName: 'User Name',
          userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
          recipeName: 'Additional info goes here',
          recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
          collectionCount: 0, isPlaceholder: true
        },
        {
          id: 'ph_t3', authorId: '',
          userName: 'User Name',
          userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
          recipeName: 'Additional info goes here',
          recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
          collectionCount: 0, isPlaceholder: true
        }
      ],
      recentRecipes: [
        {
          id: 'ph_r0', authorId: '',
          userName: 'User Name',
          userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
          recipeName: 'Additional info goes here',
          recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
          collectionCount: 0, isPlaceholder: true
        },
        {
          id: 'ph_r1', authorId: '',
          userName: 'User Name',
          userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
          recipeName: 'Additional info goes here',
          recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
          collectionCount: 0, isPlaceholder: true
        },
        {
          id: 'ph_r2', authorId: '',
          userName: 'User Name',
          userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
          recipeName: 'Additional info goes here',
          recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
          collectionCount: 0, isPlaceholder: true
        },
        {
          id: 'ph_r3', authorId: '',
          userName: 'User Name',
          userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
          recipeName: 'Additional info goes here',
          recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
          collectionCount: 0, isPlaceholder: true
        }
      ]
    }
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.loadHistory()
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    this.loadHistory()
  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {
    this.loadHistory().then(() => {
      wx.stopPullDownRefresh()
    })
  },

  /**
   * 加载历史记录
   */
  loadHistory() {
    if (this.data.loading) return Promise.resolve()
    this.setData({ loading: true })

    return request(API_CONFIG.userCenter.history, { page: 1, size: 50 }).then(res => {
      const list = res.list || res.records || res?.data?.list || []
      if (list.length > 0) {
        const cards = list.map(item => this.transformCardData(item))
        const { today, recent } = this.splitByDate(cards)
        this.setData({
          todayRecipes: today.length > 0 ? today : this.data.placeholders.todayRecipes,
          recentRecipes: recent.length > 0 ? recent : this.data.placeholders.recentRecipes
        })
      } else {
        this.setData({
          todayRecipes: this.data.placeholders.todayRecipes,
          recentRecipes: this.data.placeholders.recentRecipes
        })
      }
    }).catch(() => {
      // 请求失败兜底：保留已有的占位数据
      if (this.data.todayRecipes.length === 0) {
        this.setData({
          todayRecipes: this.data.placeholders.todayRecipes,
          recentRecipes: this.data.placeholders.recentRecipes
        })
      }
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  /**
   * 将后端数据转换为 recipe-card 组件所需格式
   */
  transformCardData(item) {
    return {
      id: item.recipeid || item.id || item._id,
      authorId: item.authorId || '',
      userName: item.author?.nickname || 'User Name',
      userImg: item.author?.avatar || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
      recipeName: item.name || item.recipeName || '',
      recipeImage: item.cover || item.recipeImage || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
      collectionCount: item.collectionCount || 0,
      viewedAt: item.viewedAt
    }
  },

  /**
   * 按浏览日期拆分为"今天"和"近三天"
   */
  splitByDate(cards) {
    const now = new Date()
    const todayStart = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
    const threeDaysAgo = todayStart - 2 * 24 * 60 * 60 * 1000

    const today = []
    const recent = []

    cards.forEach(card => {
      const viewedTime = new Date(card.viewedAt).getTime()
      if (viewedTime >= todayStart) {
        today.push(card)
      } else if (viewedTime >= threeDaysAgo) {
        recent.push(card)
      }
    })

    return { today, recent }
  }

})