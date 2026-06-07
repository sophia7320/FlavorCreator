// pages/wait-for-weixin/wait.js
const app = getApp()
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

Page({
  data: {
    success: false
  },

  onLoad(options) {
    wx.login({
      success: (res) => {
        const code = res.code
        const loginParams = { code: code }

        request(API_CONFIG.auth.loginWx, loginParams).then((data) => {
          app.saveLoginInfo(data.token, data.refreshToken, data.user, data.expiresIn)

          wx.showToast({
            title: "登录成功",
            icon: "success"
          })

          setTimeout(() => {
            const hasSeenGuide = wx.getStorageSync('hasSeenGuide')
            if (data.isNewUser && !hasSeenGuide) {
              wx.reLaunch({ url: '/pages/start-guide/guide' })
            } else {
              wx.switchTab({ url: '/pages/index/index' })
            }
          }, 1500)
        }).catch(() => {
          wx.showToast({
            title: "网络异常，请稍后重试",
            icon: "none"
          })
          setTimeout(() => {
            wx.reLaunch({ url: '/pages/phone-number-login/login' })
          }, 2000)
        })
      },
      fail: (err) => {
        wx.showToast({
          title: "获取登录凭证失败",
          icon: "none"
        })
        setTimeout(() => {
          wx.reLaunch({ url: '/pages/phone-number-login/login' })
        }, 2000)
      }
    })
  }
})
