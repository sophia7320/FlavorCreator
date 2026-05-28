// 配置文件
const BASE_URL = 'http://127.0.0.1:8080' // TODO: 替换为你的实际后端地址

const API_CONFIG = {
  // 认证相关
  auth: {
    // 发送验证码
    sendCode: {
      url: '/api/auth/send-code',
      method: 'POST'
    },
    // 手机号登录
    loginPhone: {
      url: '/api/auth/login-phone',
      method: 'POST'
    },
    // 微信一键登录
    loginWx: {
      url: '/api/auth/login-wx',
      method: 'POST'
    },
    // 刷新 Token
    refresh: {
      url: '/api/auth/refresh',
      method: 'POST'
    },
    // 退出登录
    logout: {
      url: '/api/auth/logout',
      method: 'POST'
    }
  },

  // 用户相关
  user: {
    // 获取用户信息
    info: {
      url: '/api/user/info',
      method: 'GET'
    },
    // 更新用户信息
    update: {
      url: '/api/user/info',
      method: 'POST'
    },
    // 上传头像
    uploadAvatar: {
      url: '/api/user/avatar',
      method: 'POST'
    },
    // 上传背景图
    uploadBackground: {
      url: '/api/user/background',
      method: 'POST'
    }
  },

  // 菜谱相关
  recipe: {
    // 提交食材获取菜谱
    apply: {
      url: '/api/recipe/apply',
      method: 'POST'
    }
  }
}

module.exports = {
  BASE_URL,
  API_CONFIG
}