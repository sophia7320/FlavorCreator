// pages/settings/settings.js
const { request } = require('../../utils/request')
const { API_CONFIG } = require('../../config/api')

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
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/profile.svg',
        desc: '完善你的个人信息'
      },
      {
        id: 'taste',
        title: '口味偏好设置',
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/palate.svg',
        desc: '记录你的专属口味标签'
      },
      {
        id: 'history',
        title: '浏览历史记录',
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/history.svg',
        desc: '查看你看过的所有内容'
      },
      {
        id: 'security',
        title: '账号安全',
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/security.svg',
        desc: '保护你的账号与隐私安全'
      },
      {
        id: 'help',
        title: '帮助中心',
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/help-center.svg',
        desc: '常见问题与使用指南'
      },
      {
        id: 'feedback',
        title: '反馈',
        icon: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/feedback.svg',
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
   * 点击设置项
   */
  onSettingTap(e) {
    const id = e.currentTarget.dataset.id
    console.log('点击设置项:', id)
    
    switch(id) {
      case 'profile':
        wx.navigateTo({
          url: '/pages/profile-setting/profile'
        })
        break
      case 'taste':
        wx.navigateTo({
          url: '/pages/preference-setting/preference'
        })
        break
      case 'history':
				wx.navigateTo({
          url: '/pages/history/history'
        })
        break
      case 'security':
        wx.navigateTo({
          url: '/pages/account-management/account'
        })
        break
      case 'help':
        wx.navigateTo({
          url: '/pages/help-center/help'
        })
        break
      case 'feedback':
        wx.navigateTo({
          url: '/pages/feedback/feedback'
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
          const app = getApp()

          // 调用后端登出接口，使服务端 token 失效
          request(API_CONFIG.auth.logout, {}, { showLoading: false, showError: false })
            .catch(() => {})
            .finally(() => {
              // 无论后端请求成功与否，都清除本地登录状态
              app.clearLoginState()

              wx.showToast({
                title: '已退出登录',
                icon: 'success'
              })
              
              setTimeout(() => {
                wx.reLaunch({
                  url: '/pages/welcome/welcome'
                })
              }, 1500)
            })
        }
      }
    })
  }
})
