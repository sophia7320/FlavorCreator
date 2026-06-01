// pages/community/community.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

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
			{userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg'},
			{userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg'},
			{userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg'},
			{userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg'},
		],
		rightList: [
			{userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg'},
			{userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg'},
			{userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg'},
			{userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg'},
		],
		page: 1,
		pageSize: 10,
		loadingStatus: '',
		isRequesting: false,
		hasMore: true,
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

    request(API_CONFIG.community.list, { page, pageSize: this.data.pageSize }, { showLoading: false })
      .then(res => {
        const list = res.data.list || res.data || []
        const hasMore = list.length >= this.data.pageSize

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
      const card = {
        id: item.id || item._id || Date.now() + index,
        userName: item.userName || item.user_name || 'User Name',
        userImg: item.userImg || item.user_avatar || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
        recipeName: item.recipeName || item.title || 'Addtional info goes here',
        recipeImage: item.recipeImage || item.image || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg'
      }

      const totalCount = newLeftList.length + newRightList.length
      if (totalCount % 2 === 0) {
        newLeftList.push(card)
      } else {
        newRightList.push(card)
      }
    })

    this.setData({ leftList: newLeftList, rightList: newRightList })
  },

  onScrollToLower() {
    this.loadData(false)
  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  }
})
