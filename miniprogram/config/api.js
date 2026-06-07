// 配置文件
const CLOUD_CONFIG = {
  env: 'prod-1g8rulbw951115e9',
  service: 'flcr2'
}

// 直连请求配置
const REQUEST_CONFIG = {
  baseUrl: 'http://127.0.0.1:8080'
}

const API_CONFIG = {
  // 认证相关
  auth: {
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

  // 图片上传
  image: {
    upload: {
      path: '/api/image/upload',
      method: 'POST'
    },
    // 云模式上传（传 fileID 而非文件）
    uploadCloud: {
      path: '/api/image/upload',
      method: 'POST'
    }
  },

  // 菜谱相关
  recipe: {
    // 发布菜谱
    publish: {
      path: '/api/recipe',
      method: 'POST'
    },
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
    },
    // 菜谱分类列表
    list: {
      path: '/api/recipe/list',
      method: 'GET'
    },
    // 搜索菜谱
    search: {
      path: '/api/recipe/search',
      method: 'GET'
    },
    // 今日推荐
    recommend: {
      path: '/api/recipe/recommend',
      method: 'GET'
    }
  },

  // 用户个人中心相关
  userCenter: {
    // 获取已发布菜谱
    published: {
      path: '/api/user/recipes',
      method: 'GET'
    },
    // 获取收藏列表
    collections: {
      path: '/api/user/collections',
      method: 'GET'
    },
    // 获取点赞列表
    likes: {
      path: '/api/user/likes',
      method: 'GET'
    },
    // 浏览历史
    history: {
      path: '/api/user/history',
      method: 'GET'
    },
    // 清除浏览历史
    deleteHistory: {
      path: '/api/user/history',
      method: 'DELETE'
    },
    // 删除已发布菜谱
    deleteRecipe: {
      path: '/api/user/recipe/{id}',
      method: 'DELETE'
    },
    // 编辑已发布菜谱
    editRecipe: {
      path: '/api/user/recipe/{id}',
      method: 'PUT'
    }
  },

  // 社区相关
  community: {
    // 获取社区菜谱列表
    list: {
      path: '/api/recipe/list',
      method: 'GET'
    },
    // 点赞
    like: {
      path: '/api/community/recipe/{id}/like',
      method: 'POST'
    },
    // 取消点赞
    unlike: {
      path: '/api/community/recipe/{id}/like',
      method: 'DELETE'
    },
    // 收藏菜谱
    collect: {
      path: '/api/community/recipe/{id}/collect',
      method: 'POST'
    },
    // 取消收藏
    uncollect: {
      path: '/api/community/recipe/{id}/collect',
      method: 'DELETE'
    },
    // 评论列表
    comments: {
      path: '/api/community/recipe/{id}/comment',
      method: 'GET'
    },
    // 发表评论
    addComment: {
      path: '/api/community/recipe/{id}/comment',
      method: 'POST'
    },
    // 删除评论
    deleteComment: {
      path: '/api/community/comment/{id}',
      method: 'DELETE'
    },
    // 评论点赞
    likeComment: {
      path: '/api/community/comment/{id}/like',
      method: 'POST'
    },
    // 取消评论点赞
    unlikeComment: {
      path: '/api/community/comment/{id}/like',
      method: 'DELETE'
    }
  },

  // 食材管理相关
  ingredient: {
    // 食材列表
    list: {
      path: '/api/ingredient/list',
      method: 'GET'
    },
    // 添加食材
    add: {
      path: '/api/ingredient',
      method: 'POST'
    },
    // 更新食材
    update: {
      path: '/api/ingredient/{id}',
      method: 'PUT'
    },
    // 删除食材
    delete: {
      path: '/api/ingredient/{id}',
      method: 'DELETE'
    },
    // 批量添加食材
    batchAdd: {
      path: '/api/ingredient/batch',
      method: 'POST'
    },
    // 常用食材列表
    common: {
      path: '/api/ingredient/common',
      method: 'GET'
    },
    // 临期提醒
    expiringNotice: {
      path: '/api/ingredient/expiring-notice',
      method: 'GET'
    }
  },

  // 调味品管理相关
  condiment: {
    // 添加调味品
    add: {
      path: '/api/condiment',
      method: 'POST'
    },
    // 调味品列表
    list: {
      path: '/api/condiment/list',
      method: 'GET'
    },
    // 更新调味品
    update: {
      path: '/api/condiment/{id}',
      method: 'PUT'
    },
    // 删除调味品
    delete: {
      path: '/api/condiment/{id}',
      method: 'DELETE'
    }
  },

  // 系统相关
  system: {
    // 帮助中心
    help: {
      path: '/api/help',
      method: 'GET'
    },
    // 提交反馈
    feedback: {
      path: '/api/feedback',
      method: 'POST'
    },
    // 检查更新
    checkUpdate: {
      path: '/api/system/check-update',
      method: 'GET'
    }
  }
}

module.exports = {
  CLOUD_CONFIG,
  REQUEST_CONFIG,
  API_CONFIG
}
