const { CLOUD_CONFIG, REQUEST_CONFIG } = require('../config/api')
const { getUseCallContainer } = require('./globalState')

let loadingCount = 0

/**
 * 显示 loading
 */
function showLoading() {
  if (loadingCount === 0) {
    wx.showLoading({
      title: '加载中...',
      mask: true
    })
  }
  loadingCount++
}

/**
 * 隐藏 loading
 */
function hideLoading() {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    wx.hideLoading()
  }
}

// Token 刷新相关状态
let isRefreshing = false
let refreshQueue = []
let isRetrying = false  // 标记是否正在重试队列中的请求，避免重试时再次触发静默刷新

/**
 * 检查是否是刷新 token 的请求路径
 */
function isRefreshPath(apiConfig) {
  return apiConfig && apiConfig.path === '/api/auth/refresh'
}

/**
 * 执行 token 刷新
 */
function doRefreshToken() {
  if (isRefreshing) return
  isRefreshing = true

  const app = getApp()
  app.refreshAccessToken()
    .then((success) => {
      processRefreshQueue(success)
    })
    .catch(() => {
      processRefreshQueue(false)
    })
}

/**
 * 处理刷新后的请求队列
 */
function processRefreshQueue(success) {
  isRetrying = true  // 标记正在重试，避免重试时再次触发静默刷新

  // 提前判断是否需要通知用户（在清空队列之前）
  const shouldNotify = !success && refreshQueue.some(({ options }) => options.showError !== false)

  refreshQueue.forEach(({ apiConfig, data, options, resolve, reject, isCloud }) => {
    if (success) {
      const requestFn = isCloud ? cloudRequest : directRequest
      requestFn(apiConfig, data, options)
        .then(resolve)
        .catch(reject)
    } else {
      reject({ code: 401, message: '登录已过期' })
    }
  })

  refreshQueue = []
  isRefreshing = false

  // 延迟重置 isRetrying，确保重试请求已完成
  setTimeout(() => {
    isRetrying = false
  }, 100)

  if (shouldNotify) {
    wx.showToast({ title: '登录已过期', icon: 'none' })
    setTimeout(() => {
      wx.reLaunch({ url: '/pages/phone-number-login/login' })
    }, 1500)
  }
}

/**
 * 获取 Authorization header
 */
function getAuthorization() {
  const token = wx.getStorageSync('token')
  return token ? `Bearer ${token}` : ''
}

/**
 * 检查 token 是否即将过期
 * 在过期时间前主动触发刷新，避免请求发出后收到 401
 */
function isTokenExpiringSoon() {
  const app = getApp()
  const expiresAt = app.globalData.tokenExpiresAt
                 || wx.getStorageSync('tokenExpiresAt')
                 || 0
  // 提前 5 分钟视为即将过期，留出缓冲时间
  return expiresAt > 0 && Date.now() >= (expiresAt - 5 * 60 * 1000)
}

/**
 * 使用 callContainer 的请求方法
 * @param {Object} apiConfig API 配置对象 { path, method }
 * @param {Object} data 请求数据
 * @param {Object} options 其他选项 { showLoading = true, showError = true }
 */
function cloudRequest(apiConfig, data = {}, options = {}) {
  const { showLoading: needLoading = true, showError = true } = options

  return new Promise((resolve, reject) => {
    // 主动刷新：非刷新请求本身、非重试中，且 token 即将过期时静默刷新
    if (!isRefreshPath(apiConfig) && !isRetrying && isTokenExpiringSoon()) {
      if (!isRefreshing) {
        isRefreshing = true
        // 将当前请求加入队列，等待刷新完成后重试
        refreshQueue.push({ apiConfig, data, options, resolve, reject, isCloud: true })
        const app = getApp()
        app.silentRefreshToken()
          .then((success) => {
            if (success) {
              processRefreshQueue(true)
            } else {
              // 静默刷新失败，放行队列请求让服务端判 401
              isRefreshing = false
              isRetrying = true
              const pending = refreshQueue.splice(0)
              pending.forEach(({ apiConfig: cfg, data: d, options: opt, resolve: r, reject: j, isCloud: ic }) => {
                const fn = ic ? cloudRequest : directRequest
                fn(cfg, d, opt).then(r).catch(j)
              })
              setTimeout(() => { isRetrying = false }, 100)
            }
          })
          .catch(() => {
            // 静默刷新异常，同样放行队列请求
            isRefreshing = false
            isRetrying = true
            const pending = refreshQueue.splice(0)
            pending.forEach(({ apiConfig: cfg, data: d, options: opt, resolve: r, reject: j, isCloud: ic }) => {
              const fn = ic ? cloudRequest : directRequest
              fn(cfg, d, opt).then(r).catch(j)
            })
            setTimeout(() => { isRetrying = false }, 100)
          })
        // 直接 return，不执行后续请求逻辑，等待刷新完成后重试
        return
      } else {
        // 已经在刷新中，将当前请求加入队列等待
        refreshQueue.push({ apiConfig, data, options, resolve, reject, isCloud: true })
        return
      }
    }

    if (needLoading) {
      showLoading()
    }

    const headers = {
      'X-WX-SERVICE': CLOUD_CONFIG.service,
      'Content-Type': 'application/json'
    }

    const token = getAuthorization()
    if (token && !isRefreshPath(apiConfig)) {
      headers['Authorization'] = token
    }

    wx.cloud.callContainer({
      config: {
        env: CLOUD_CONFIG.env
      },
      path: apiConfig.path,
      method: apiConfig.method,
      header: headers,
      data: data,
      success: (res) => {
        if (needLoading) {
          hideLoading()
        }
        handleResponse(res, resolve, reject, showError, apiConfig, data, options, true)
      },
      fail: (err) => {
        if (needLoading) {
          hideLoading()
        }
        if (showError) {
          wx.showToast({
            title: '网络请求失败',
            icon: 'none'
          })
        }
        reject(err)
      }
    })
  })
}

/**
 * 统一请求方法 - 根据全局状态选择请求方式
 * @param {Object} apiConfig API 配置对象 { path, method }
 * @param {Object} data 请求数据
 * @param {Object} options 其他选项 { showLoading = true, showError = true }
 */
function request(apiConfig, data = {}, options = {}) {
  const useCallContainer = getUseCallContainer()
  
  if (useCallContainer) {
    return cloudRequest(apiConfig, data, options)
  } else {
    return directRequest(apiConfig, data, options)
  }
}

/**
 * 处理响应
 */
function handleResponse(res, resolve, reject, showError, apiConfig, data, options, isCloud) {
  if (res.statusCode === 200) {
    if (res.data.code === 0 || res.data.code === 200) {
      resolve(res.data)
    } else if (res.data.code === 401) {
      // 业务 code 401：token 过期，与 HTTP 401 同样处理
      if (isRefreshPath(apiConfig)) {
        // 刷新请求本身返回 401，说明 refreshToken 也过期了
        if (showError) {
          wx.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          })
          setTimeout(() => {
            wx.reLaunch({
              url: '/pages/phone-number-login/login'
            })
          }, 1500)
        }
        reject(res.data)
        return
      }

      // 将当前请求加入重试队列，等待 token 刷新后重试
      refreshQueue.push({ apiConfig, data, options, resolve, reject, isCloud })

      if (!isRefreshing) {
        doRefreshToken()
      }
      // 不调用 reject/resolve，等待刷新完成后处理
    } else {
      if (showError) {
        wx.showToast({
          title: res.data.message || '请求失败',
          icon: 'none'
        })
      }
      reject(res.data)
    }
  } else if (res.statusCode === 401) {
    // HTTP 401：token 过期（兜底兼容）
    if (isRefreshPath(apiConfig)) {
      if (showError) {
        wx.showToast({
          title: '登录已过期，请重新登录',
          icon: 'none'
        })
        setTimeout(() => {
          wx.reLaunch({
            url: '/pages/phone-number-login/login'
          })
        }, 1500)
      }
      reject(res)
      return
    }

    // 将当前请求加入重试队列，等待 token 刷新后重试
    refreshQueue.push({ apiConfig, data, options, resolve, reject, isCloud })

    if (!isRefreshing) {
      doRefreshToken()
    }
    // 不调用 reject/resolve，等待刷新完成后处理
  } else {
    if (showError) {
      wx.showToast({
        title: '网络错误',
        icon: 'none'
      })
    }
    reject(res)
  }
}

/**
 * GET 请求快捷方法
 */
function get(apiConfig, data, options) {
  return request({ ...apiConfig, method: 'GET' }, data, options)
}

/**
 * POST 请求快捷方法
 */
function post(apiConfig, data, options) {
  return request({ ...apiConfig, method: 'POST' }, data, options)
}

/**
 * 直连请求方法（使用 wx.request）
 * @param {Object} apiConfig API 配置对象 { path, method }
 * @param {Object} data 请求数据
 * @param {Object} options 其他选项 { showLoading = true, showError = true }
 */
function directRequest(apiConfig, data = {}, options = {}) {
  const { showLoading: needLoading = true, showError = true } = options

  return new Promise((resolve, reject) => {
    // 主动刷新：非刷新请求本身、非重试中，且 token 即将过期时静默刷新
    if (!isRefreshPath(apiConfig) && !isRetrying && isTokenExpiringSoon()) {
      if (!isRefreshing) {
        isRefreshing = true
        // 将当前请求加入队列，等待刷新完成后重试
        refreshQueue.push({ apiConfig, data, options, resolve, reject, isCloud: false })
        const app = getApp()
        app.silentRefreshToken()
          .then((success) => {
            if (success) {
              processRefreshQueue(true)
            } else {
              // 静默刷新失败，放行队列请求让服务端判 401
              isRefreshing = false
              isRetrying = true
              const pending = refreshQueue.splice(0)
              pending.forEach(({ apiConfig: cfg, data: d, options: opt, resolve: r, reject: j, isCloud: ic }) => {
                const fn = ic ? cloudRequest : directRequest
                fn(cfg, d, opt).then(r).catch(j)
              })
              setTimeout(() => { isRetrying = false }, 100)
            }
          })
          .catch(() => {
            // 静默刷新异常，同样放行队列请求
            isRefreshing = false
            isRetrying = true
            const pending = refreshQueue.splice(0)
            pending.forEach(({ apiConfig: cfg, data: d, options: opt, resolve: r, reject: j, isCloud: ic }) => {
              const fn = ic ? cloudRequest : directRequest
              fn(cfg, d, opt).then(r).catch(j)
            })
            setTimeout(() => { isRetrying = false }, 100)
          })
        // 直接 return，不执行后续请求逻辑，等待刷新完成后重试
        return
      } else {
        // 已经在刷新中，将当前请求加入队列等待
        refreshQueue.push({ apiConfig, data, options, resolve, reject, isCloud: false })
        return
      }
    }

    if (needLoading) {
      showLoading()
    }

    const headers = {
      'Content-Type': 'application/json'
    }

    const token = getAuthorization()
    if (token && !isRefreshPath(apiConfig)) {
      headers['Authorization'] = token
    }

    wx.request({
      url: REQUEST_CONFIG.baseUrl + apiConfig.path,
      method: apiConfig.method,
      header: headers,
      data: data,
      success: (res) => {
        if (needLoading) {
          hideLoading()
        }
        handleResponse(res, resolve, reject, showError, apiConfig, data, options, false)
      },
      fail: (err) => {
        if (needLoading) {
          hideLoading()
        }
        if (showError) {
          wx.showToast({
            title: '网络请求失败',
            icon: 'none'
          })
        }
        reject(err)
      }
    })
  })
}

module.exports = {
  request,
  directRequest,
  get,
  post
}
