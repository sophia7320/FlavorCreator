// pages/mine/mine.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo: {
      nickname: '默认用户',
      avatar: '',
      username: 'user001'
    }
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    // 从本地获取用户信息
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.setData({ userInfo })
    }
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().onTabPageShow()
    }
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().onTabPageShow()
    }
    // 每次显示页面时更新用户信息
    const userInfo = wx.getStorageSync('userInfo')
    if (userInfo) {
      this.setData({ userInfo })
    }
  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  },

  /**
   * 跳转到设置页面
   */
  goToSettings() {
    wx.navigateTo({
      url: '/pages/settings/settings'
    })
  },

  /**
   * 点击菜单项
   */
  onMenuTap(e) {
    const id = e.currentTarget.dataset.id
    console.log('点击菜单项:', id)
    
    switch(id) {
      case 'favorites':
        wx.showToast({
          title: '我的收藏功能开发中',
          icon: 'none'
        })
        break
      case 'works':
        wx.showToast({
          title: '我的作品功能开发中',
          icon: 'none'
        })
        break
      case 'notes':
        wx.showToast({
          title: '厨艺笔记功能开发中',
          icon: 'none'
        })
        break
      case 'help':
        wx.showToast({
          title: '帮助与反馈功能开发中',
          icon: 'none'
        })
        break
      case 'about':
        wx.showToast({
          title: '关于我们功能开发中',
          icon: 'none'
        })
        break
    }
  }
})
