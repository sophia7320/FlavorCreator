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

/**
 * 获取 Authorization header
 */
function getAuthorization() {
  const token = wx.getStorageSync('token')
  return token ? `Bearer ${token}` : ''
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
    if (needLoading) {
      showLoading()
    }

    const headers = {
      'X-WX-SERVICE': CLOUD_CONFIG.service,
      'Content-Type': 'application/json'
    }

    const token = getAuthorization()
    if (token) {
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
        handleResponse(res, resolve, reject, showError)
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
function handleResponse(res, resolve, reject, showError) {
  if (res.statusCode === 200) {
    if (res.data.code === 0 || res.data.code === 200) {
      resolve(res.data)
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
    wx.showToast({
      title: '登录已过期',
      icon: 'none'
    })
    setTimeout(() => {
      wx.reLaunch({
        url: '/pages/phoneNumberLogin/login'
      })
    }, 1500)
    reject(res)
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
    if (needLoading) {
      showLoading()
    }

    const headers = {
      'Content-Type': 'application/json'
    }

    const token = getAuthorization()
    if (token) {
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
        handleResponse(res, resolve, reject, showError)
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
