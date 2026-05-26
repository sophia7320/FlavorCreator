// pages/waitForWeixin/wait.js
const app = getApp()
const { BASE_URL } = require('../../config/api')

Page({
  data: {
    success: false
  },

  onLoad(options) {
    wx.login({
      success: (res) => {
        const code = res.code
        const loginParams = { code: code }

        wx.request({
          url: BASE_URL + '/api/auth/login-wx',
          method: "POST",
          data: loginParams,

          success: (res) => {
            const resp = res.data

            if (resp.code === 200) {
              const data = resp.data
              app.saveLoginInfo(data.token, data.refreshToken, data.user)

              wx.showToast({
                title: resp.message || "登录成功",
                icon: "success"
              })

              setTimeout(() => {
                const hasSeenGuide = wx.getStorageSync('hasSeenGuide')
                if (data.isNewUser && !hasSeenGuide) {
                  wx.reLaunch({ url: '/pages/startGuide/guide' })
                } else {
                  wx.switchTab({ url: '/pages/index/index' })
                }
              }, 1500)
            }

				else {
					wx.showToast({
						title: resp.message || "登录失败，请稍后重试",
						icon: "none"
					})
					setTimeout(function() {
						wx.reLaunch({ url: '/pages/phoneNumberLogin/login' })
					}, 2000)
				}
          },

          fail: (err) => {
            wx.showToast({
              title: "网络异常，请稍后重试",
              icon: "none"
            })
            setTimeout(function() {
              wx.reLaunch({ url: '/pages/phoneNumberLogin/login' })
            }, 2000)
          }
        })
      },
      fail: (err) => {
        wx.showToast({
          title: "获取登录凭证失败",
          icon: "none"
        })
        setTimeout(function() {
          wx.reLaunch({ url: '/pages/phoneNumberLogin/login' })
        }, 2000)
      }
    })
  }
})
