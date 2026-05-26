// pages/welcome/welcome.js
const app = getApp()

Page({
  data: {},

  onLoad(options) {
    setTimeout(() => {
      const hasRefreshToken = !!app.getRefreshToken()

      if (!hasRefreshToken) {
        // 没有 refreshToken，直接去登录
        wx.reLaunch({ url: '/pages/firstLogin/firstLogin' })
        return
      }

      // 有 refreshToken，尝试刷新恢复登录态
      app.refreshAccessToken().then(success => {
        if (success) {
          const hasSeenGuide = wx.getStorageSync('hasSeenGuide')
          if (!hasSeenGuide) {
            wx.reLaunch({ url: '/pages/startGuide/guide' })
          } else {
            wx.switchTab({ url: '/pages/index/index' })
          }
        } else {
          wx.reLaunch({ url: '/pages/firstLogin/firstLogin' })
        }
      })
    }, 800)
  },
})