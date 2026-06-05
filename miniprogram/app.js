// app.js
const { API_CONFIG, CLOUD_CONFIG } = require('./config/api')
const { request } = require('./utils/request')

App({
  onLaunch() {
    // 初始化云开发
    wx.cloud.init({
      env: CLOUD_CONFIG.env,
      traceUser: true
    })
    // 从本地存储同步登录状态
    this.syncFromStorage()
  },

  // 创建标准方法完成对本地存储内容的读写

	//存储登录信息
  saveLoginInfo(token, refreshToken, userInfo, expiresIn) {
    wx.setStorageSync('token', token)
    wx.setStorageSync('refreshToken', refreshToken)
    wx.setStorageSync('userInfo', userInfo)

    // 记录 token 过期时间，提前 60 秒视为过期留缓冲
    // 如果后端没返回 expiresIn，使用默认值 7200 秒（2小时）
    const validExpiresIn = expiresIn || 7200
    const expiresAt = Date.now() + (validExpiresIn - 60) * 1000
    wx.setStorageSync('tokenExpiresAt', expiresAt)
    this.globalData.tokenExpiresAt = expiresAt

    this.globalData.token = token
    this.globalData.refreshToken = refreshToken
    this.globalData.userInfo = userInfo
    this.globalData.isLoggedIn = true
  },

  // 获取 token
  getToken() {
    return this.globalData.token || wx.getStorageSync('token') || ''
  },

  // 获取 refreshToken
  getRefreshToken() {
    return this.globalData.refreshToken || wx.getStorageSync('refreshToken') || ''
  },

  // 获取用户信息
  getUserInfo() {
    return this.globalData.userInfo || wx.getStorageSync('userInfo') || null
  },

  // 检查是否已登录
  checkLogin() {
    const token = this.getToken()
    return !!token && token.length > 0
  },

  // 清除登录状态
  clearLoginState() {
    wx.removeStorageSync('token')
    wx.removeStorageSync('refreshToken')
    wx.removeStorageSync('userInfo')
    wx.removeStorageSync('tokenExpiresAt')
    this.globalData.token = ''
    this.globalData.refreshToken = ''
    this.globalData.userInfo = null
    this.globalData.tokenExpiresAt = 0
    this.globalData.isLoggedIn = false
  },

  // 启动时从本地存储同步数据到 globalData
  syncFromStorage() {
    const token = wx.getStorageSync('token') || ''
    const refreshToken = wx.getStorageSync('refreshToken') || ''
    const userInfo = wx.getStorageSync('userInfo') || null
    const tokenExpiresAt = wx.getStorageSync('tokenExpiresAt') || 0

    if (token) {
      this.globalData.token = token
      this.globalData.refreshToken = refreshToken
      this.globalData.userInfo = userInfo
      this.globalData.tokenExpiresAt = tokenExpiresAt
      this.globalData.isLoggedIn = true
    }
  },

  // 用 refreshToken 刷新 token（被动兜底，允许弹 toast）
  refreshAccessToken() {
    const refreshToken = this.getRefreshToken()
    if (!refreshToken) return Promise.resolve(false)

    return request(API_CONFIG.auth.refresh, { refreshToken }, { showLoading: false })
      .then((res) => {
        if (res.code === 0 || res.code === 200) {
          const data = res.data
          this.saveLoginInfo(data.token, data.refreshToken, this.getUserInfo(), data.expiresIn)
          return true
        } else {
          this.clearLoginState()
          return false
        }
      })
      .catch(() => {
        this.clearLoginState()
        return false
      })
  },

  // 静默刷新 token（主动预刷新，失败不弹 toast、不清除登录态）
  silentRefreshToken() {
    const refreshToken = this.getRefreshToken()
    if (!refreshToken) return Promise.resolve(false)

    return request(API_CONFIG.auth.refresh, { refreshToken }, {
      showLoading: false,
      showError: false
    })
      .then((res) => {
        if ((res.code === 0 || res.code === 200) && res.data) {
          const data = res.data
          this.saveLoginInfo(data.token, data.refreshToken, this.getUserInfo(), data.expiresIn)
          return true
        }
        return false
      })
      .catch(() => false)
  },

  globalData: {
    userInfo: null,
    token: '',
    refreshToken: '',
    tokenExpiresAt: 0,
    isLoggedIn: false
  }
})
