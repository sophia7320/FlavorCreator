// pages/settings/settings.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    userInfo: {
      nickname: '默认用户',
      avatar: '',
      username: 'user001'
    },
    settingsList: [
      {
        id: 'profile',
        title: '个人资料设置',
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/profile.png',
        desc: '完善你的个人信息'
      },
      {
        id: 'taste',
        title: '口味偏好设置',
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/taste.png',
        desc: '记录你的专属口味标签'
      },
      {
        id: 'history',
        title: '浏览历史记录',
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/history.png',
        desc: '查看你看过的所有内容'
      },
      {
        id: 'security',
        title: '账号安全',
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/security.png',
        desc: '保护你的账号与隐私安全'
      },
      {
        id: 'help',
        title: '帮助中心',
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/help.png',
        desc: '常见问题与使用指南'
      },
      {
        id: 'feedback',
        title: '反馈',
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/feedback.png',
        desc: '提交建议或问题给我们'
      }
    ]
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

  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

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
   * 返回上一页
   */
  goBack() {
    wx.navigateBack()
  },

  /**
   * 点击设置项
   */
  onSettingTap(e) {
    const id = e.currentTarget.dataset.id
    console.log('点击设置项:', id)
    
    switch(id) {
      case 'profile':
        wx.showToast({
          title: '个人资料功能开发中',
          icon: 'none'
        })
        break
      case 'taste':
        wx.showToast({
          title: '口味偏好功能开发中',
          icon: 'none'
        })
        break
      case 'history':
        wx.showToast({
          title: '浏览历史功能开发中',
          icon: 'none'
        })
        break
      case 'security':
        wx.showToast({
          title: '账号安全功能开发中',
          icon: 'none'
        })
        break
      case 'help':
        wx.showToast({
          title: '帮助中心功能开发中',
          icon: 'none'
        })
        break
      case 'feedback':
        wx.showToast({
          title: '反馈功能开发中',
          icon: 'none'
        })
        break
    }
  },

  /**
   * 切换账号
   */
  switchAccount() {
    wx.showToast({
      title: '切换账号功能开发中',
      icon: 'none'
    })
  },

  /**
   * 退出登录
   */
  logout() {
    wx.showModal({
      title: '确认退出',
      content: '确定要退出当前账号吗？',
      success: (res) => {
        if (res.confirm) {
          // 清除登录状态
          wx.removeStorageSync('token')
          wx.removeStorageSync('userInfo')
          
          wx.showToast({
            title: '已退出登录',
            icon: 'success'
          })
          
          setTimeout(() => {
            wx.reLaunch({
              url: '/pages/welcome/welcome'
            })
          }, 1500)
        }
      }
    })
  }
})
