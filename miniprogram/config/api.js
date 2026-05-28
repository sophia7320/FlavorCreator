// 配置文件
const CLOUD_CONFIG = {
  env: 'prod-1g8rulbw951115e9',
  service: 'flavor-creator'
}

const API_CONFIG = {
  // 认证相关
  auth: {
    // 发送验证码
    sendCode: {
      path: '/auth/send-code',
      method: 'POST'
    },
    // 手机号登录
    loginPhone: {
      path: '/auth/login-phone',
      method: 'POST'
    },
    // 微信一键登录
    loginWx: {
      path: '/auth/login-wx',
      method: 'POST'
    },
    // 刷新 Token
    refresh: {
      path: '/auth/refresh',
      method: 'POST'
    },
    // 退出登录
    logout: {
      path: '/auth/logout',
      method: 'POST'
    }
  },

  // 用户相关
  user: {
    // 获取用户信息
    info: {
      path: '/user/info',
      method: 'GET'
    },
    // 更新用户信息
    update: {
      path: '/user/info',
      method: 'POST'
    },
    // 上传头像
    uploadAvatar: {
      path: '/user/avatar',
      method: 'POST'
    },
    // 上传背景图
    uploadBackground: {
      path: '/user/background',
      method: 'POST'
    }
  },

  // 菜谱相关
  recipe: {
    // 提交食材获取菜谱
    apply: {
      path: '/recipe/apply',
      method: 'POST'
    }
  }
}

module.exports = {
  CLOUD_CONFIG,
  API_CONFIG
}
