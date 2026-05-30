// 配置文件
const CLOUD_CONFIG = {
  env: 'prod-1g8rulbw951115e9',
  service: 'flcr2'
}

// 直连请求配置
const REQUEST_CONFIG = {
  baseUrl: 'http://localhost:8080'
}


const API_CONFIG = {
  // 认证相关
  auth: {
    // 发送验证码
    sendCode: {
      path: '/api/auth/send-code',
      method: 'POST'
    },
    // 手机号登录
    loginPhone: {
      path: '/api/auth/login-phone',
      method: 'POST'
    },
    // 微信一键登录
    loginWx: {
      path: '/api/auth/login-wx',
      method: 'POST'
    },
    // 刷新 Token
    refresh: {
      path: '/api/auth/refresh',
      method: 'POST'
    },
    // 退出登录
    logout: {
      path: '/api/auth/logout',
      method: 'POST'
    }
  },

  // 用户相关
  user: {
    // 获取用户信息
    info: {
      path: '/api/user/info',
      method: 'GET'
    },
    // 更新用户信息
    update: {
      path: '/api/user/info',
      method: 'POST'
    },
    // 上传头像
    uploadAvatar: {
      path: '/api/user/avatar',
      method: 'POST'
    },
    // 上传背景图
    uploadBackground: {
      path: '/api/user/background',
      method: 'POST'
    }
  },

  // 菜谱相关
  recipe: {
    // 提交食材获取菜谱
    apply: {
      path: '/api/recipe/apply',
      method: 'POST'
    },
    // AI 生成菜谱
    aiGenerate: {
      path: '/api/recipe/ai-generate',
      method: 'POST'
    },
    // 获取菜谱详情
    getDetail: {
      path: '/api/recipe/{id}',
      method: 'GET'
    }
  }
}

module.exports = {
  CLOUD_CONFIG,
  REQUEST_CONFIG,
  API_CONFIG
}
