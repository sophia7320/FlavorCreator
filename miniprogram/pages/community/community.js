// pages/community/community.js
const { get, post } = require('../../utils/request')
const { API_CONFIG } = require('../../config/api')

Page({

  data: {
    tags: [
      { name: '快手菜', selected: true },
      { name: '减脂餐', selected: false },
      { name: '家常菜', selected: false },
      { name: '养生频道', selected: false },
      { name: '特色菜', selected: false },
    ],
    leftList: [],
    rightList: [],

    // 无限滚动
    isLoading: false,
    hasMore: true,
    currentPage: 1,
    pageSize: 10,
    currentTag: '快手菜',

    // scroll-view 高度
    scrollHeight: 0
  },

  onLoad(options) {
    this.loadRecipes()
    this.calcScrollHeight()
  },

  // 计算 scroll-view 高度
  calcScrollHeight() {
    const { windowHeight, statusBarHeight } = wx.getSystemInfoSync()
    // 减去顶部栏(201rpx≈134px) + 标签区(70rpx≈47px) + 分割线 + 底部tabbar(50px)
    const topBarHeight = Math.round(201 / 750 * windowHeight)
    const tagAreaHeight = Math.round(76 / 750 * windowHeight)
    const scrollHeight = windowHeight - 50 // 扣掉底部 tabbar 即可，scroll-view 从 top-title 后开始
    this.setData({ scrollHeight })
  },

  // 加载菜谱列表
  async loadRecipes(isRefresh = false) {
    if (this.data.isLoading) return
    if (!isRefresh && !this.data.hasMore) return

    const page = isRefresh ? 1 : this.data.currentPage
    this.setData({ isLoading: true })

    try {
      const res = await get(API_CONFIG.community.list, {
        page,
        pageSize: this.data.pageSize,
        tag: this.data.currentTag
      }, { showLoading: false })

      const newRecipes = res.data.list || []
      const hasMore = res.data.hasMore !== false && res.data.total > (page * this.data.pageSize)

      const leftList = isRefresh ? [] : [...this.data.leftList]
      const rightList = isRefresh ? [] : [...this.data.rightList]

      newRecipes.forEach((item, index) => {
        if (index % 2 === 0) {
          leftList.push(item)
        } else {
          rightList.push(item)
        }
      })

      this.setData({
        leftList,
        rightList,
        currentPage: page + 1,
        hasMore,
        isLoading: false
      })
    } catch (err) {
      console.error('加载菜谱失败:', err)
      this.setData({ isLoading: false })
      if (!isRefresh) {
        wx.showToast({ title: '加载失败，请重试', icon: 'none' })
      }
    }
  },

  // scroll-view 触底
  onScrollToLower() {
    this.loadRecipes()
  },

  // 切换标签
  onTagSwitch(e) {
    const { name } = e.currentTarget.dataset
    if (name === this.data.currentTag) return

    const tags = this.data.tags.map(tag => ({
      ...tag,
      selected: tag.name === name
    }))

    this.setData({
      tags,
      currentTag: name,
      currentPage: 1,
      hasMore: true,
      leftList: [],
      rightList: [],
      isLoading: false
    })

    this.loadRecipes(true)
  },

  // 点赞/取消点赞
  async onLike(e) {
    const { id } = e.currentTarget.dataset
    const { leftList, rightList } = this.data

    try {
      const likePath = API_CONFIG.community.like.path.replace('{id}', id)
      await post({ ...API_CONFIG.community.like, path: likePath }, {}, { showLoading: false })

      const updateItem = (list) => list.map(item => {
        if (item.id == id) {
          return {
            ...item,
            isLiked: !item.isLiked,
            likeCount: item.isLiked ? (item.likeCount || 1) - 1 : (item.likeCount || 0) + 1
          }
        }
        return item
      })

      this.setData({
        leftList: updateItem(leftList),
        rightList: updateItem(rightList)
      })
    } catch (err) {
      console.error('点赞失败:', err)
    }
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {
    return {
      title: '快来发现美味菜谱！',
      path: '/pages/community/community'
    }
  },

  // 页面隐藏
  onHide() {},

  // 页面卸载
  onUnload() {}
})
