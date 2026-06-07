// pages/profile-setting/profile.js
const { API_CONFIG, REQUEST_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

Page({

  _dataReady: false,
  _updating: false,

  data: {
    userInfo: {
      avatarUrl: '',
      nickName: '',
      gender: '',
      age: '',
      region: '',
      background: ''
    },
    infoList: [
      { title: "昵称", pointClass: '', field: 'nickname' },
      { title: "性别", pointClass: 'blue', field: 'gender' },
      { title: "年龄", pointClass: 'pink', field: 'age' },
      { title: "背景图", pointClass: '', field: 'background' },
      { title: "地区", pointClass: 'blue', field: 'region' }
    ]
  },

  onLoad() {
    this.fetchUserInfo()
  },

  onShow() {
    if (!this._updating) {
      this.fetchUserInfo()
    }
  },

  // 后端字段 → 前端字段映射
  mapBackendUser(data) {
    const genderMap = { 1: '男', 2: '女', 0: '保密' }
    return {
      nickName: data.nickname || data.nickName || '',
      nickname: data.nickname || data.nickName || '',
      avatarUrl: data.avatar || data.avatarUrl || '',
      gender: genderMap[data.gender] || data.gender || '',
      age: data.age != null ? String(data.age) : '',
      region: data.region || '',
      background: data.background || ''
    }
  },

  // 从后端拉取用户信息
  fetchUserInfo() {
    const app = getApp()
    return request(API_CONFIG.user.info, {}, { showLoading: false, showError: false })
      .then(res => {
        const data = res.data || res
        const mappedUser = this.mapBackendUser(data)
        // 合并数据：只更新后端返回的有值字段，保留本地修改
        const mergedUser = { ...this.data.userInfo }
        Object.keys(mappedUser).forEach(key => {
          if (mappedUser[key] !== '' && mappedUser[key] !== null && mappedUser[key] !== undefined) {
            mergedUser[key] = mappedUser[key]
          }
        })
        this.setData({ userInfo: mergedUser })
        this.syncLocalUserInfo(mergedUser)
        this._dataReady = true
      })
      .catch(() => {
        // 降级：从本地缓存读取
        const cached = app.getUserInfo()
        if (cached) {
          this.setData({ userInfo: this.mapBackendUser(cached) })
          this._dataReady = true
        }
      })
  },

  // 同步到 globalData + Storage
  syncLocalUserInfo(user) {
    const app = getApp()
    app.globalData.userInfo = user
    wx.setStorageSync('userInfo', user)
  },

  // 构建文件上传 URL
  buildUploadUrl(apiConfig) {
    const token = wx.getStorageSync('token') || ''
    // 云托管路径
    return {
      url: REQUEST_CONFIG.baseUrl + apiConfig.path,
      header: {
        'Authorization': token ? 'Bearer ' + token : ''
      }
    }
  },

  // 上传图片文件，返回图片 URL
  uploadImageFile(filePath, scene = '') {
    const uploadConfig = this.buildUploadUrl(API_CONFIG.image.upload)
    const that = this
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: uploadConfig.url,
        filePath: filePath,
        name: 'file',
        formData: scene ? { scene } : {},
        header: uploadConfig.header,
        success(res) {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 0 || data.code === 200) {
              const imageUrl = (data.data && data.data.url) || data.url || data.data
              resolve(imageUrl)
            } else if (data.code === 401) {
              // token 过期，触发刷新后重试
              const app = getApp()
              app.refreshAccessToken()
                .then((success) => {
                  if (success) {
                    // 刷新成功，重试上传
                    that.uploadImageFile(filePath, scene).then(resolve).catch(reject)
                  } else {
                    reject({ code: 401, message: '登录已过期' })
                  }
                })
                .catch(() => reject({ code: 401, message: '登录已过期' }))
            } else {
              reject(data.message || '上传失败')
            }
          } catch (e) {
            reject('解析上传结果失败')
          }
        },
        fail(err) {
          reject(err)
        }
      })
    })
  },

  // 选择头像
  onChooseAvatar() {
    const that = this
    this._updating = true
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePath = res.tempFiles[0].tempFilePath

        that.uploadImageFile(tempFilePath, 'avatar')
          .then(imageUrl => {
            return request(API_CONFIG.user.update, { avatar: imageUrl }, { showLoading: true })
          })
          .then(() => {
            return that.fetchUserInfo()
          })
          .then(() => {
            wx.showToast({ title: '头像更新成功', icon: 'success' })
          })
          .catch(err => {
            console.error('头像更新失败', err)
            if (err && err.code === 401) {
              wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
            } else {
              wx.showToast({ title: typeof err === 'string' ? err : '头像更新失败', icon: 'none' })
            }
          })
          .finally(() => {
            that._updating = false
          })
      },
      fail() {
        that._updating = false
      }
    })
  },

  // 公共提交方法
  submitFieldUpdate(field, newValue) {
    const updateData = {}
    // 性别后端期望数字：男=1，女=2，保密=0
    if (field === 'gender') {
      const genderMap = { '男': 1, '女': 2, '保密': 0 }
      updateData[field] = genderMap[newValue] !== undefined ? genderMap[newValue] : newValue
    } else {
      updateData[field] = newValue
    }

    const that = this
    this._updating = true

    // 乐观更新：先让 UI 立即响应
    const optimistic = { ...this.data.userInfo }
    optimistic[field] = newValue
    if (field === 'nickname') optimistic.nickName = newValue
    this.setData({ userInfo: optimistic })

    request(API_CONFIG.user.update, updateData, { showLoading: true })
      .then(() => {
        // 从后端重新拉取，确保数据一致
        return that.fetchUserInfo()
      })
      .then(() => {
        // fetchUserInfo 成功后，保留后端不支持的字段（如 age）
        const current = that.data.userInfo
        if (!current[field] && newValue) {
          const restored = { ...current }
          restored[field] = newValue
          if (field === 'nickname') restored.nickName = newValue
          that.setData({ userInfo: restored })
          that.syncLocalUserInfo(restored)
        }
        wx.showToast({ title: '更新成功', icon: 'success' })
      })
      .catch(err => {
        console.error('更新失败', err)
        if (err && err.code === 401) {
          wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
        } else {
          wx.showToast({ title: '更新失败，请稍后重试', icon: 'none' })
        }
        // 失败时从后端恢复真实数据
        that.fetchUserInfo()
      })
      .finally(() => {
        that._updating = false
      })
  },

  // 编辑路由
  onEditField(e) {
    const field = e.currentTarget.dataset.field

    switch (field) {
      case 'gender':     this.onEditGender(); break
      case 'age':        this.onEditAge(); break
      case 'nickname':   this.onEditNickname(); break
      case 'background': this.onChooseBackground(); break
      // region 由 picker bindchange 处理
    }
  },

  // 编辑性别
  onEditGender() {
    const currentValue = this.data.userInfo.gender || ''
    const options = ['男', '女', '保密']
    wx.showActionSheet({
      itemList: options,
      success: (res) => {
        const selected = options[res.tapIndex]
        if (selected === currentValue) return
        this.submitFieldUpdate('gender', selected)
      }
    })
  },

  // 编辑年龄
  onEditAge() {
    const currentValue = this.data.userInfo.age || ''
    wx.showModal({
      title: '修改年龄',
      editable: true,
      placeholderText: '请输入年龄 (1-150)',
      content: currentValue,
      success: (res) => {
        if (!res.confirm || res.content === undefined) return
        const val = parseInt(res.content.trim())
        if (isNaN(val) || val < 1 || val > 150) {
          wx.showToast({ title: '请输入有效年龄 (1-150)', icon: 'none' })
          return
        }
        const newValue = val.toString()
        if (newValue === currentValue) return
        this.submitFieldUpdate('age', newValue)
      }
    })
  },

  // 编辑昵称
  onEditNickname() {
    const currentValue = this.data.userInfo.nickname || this.data.userInfo.nickName || ''
    wx.showModal({
      title: '修改昵称',
      editable: true,
      placeholderText: '请输入昵称',
      content: currentValue,
      success: (res) => {
        if (res.confirm && res.content !== undefined) {
          const newValue = res.content.trim()
          if (!newValue || newValue === currentValue) return
          this.submitFieldUpdate('nickname', newValue)
        }
      }
    })
  },

  // 地区选择器回调（500ms 防抖）
  onRegionChange(e) {
    const region = e.detail.value  // ['广东省', '广州市', '天河区']
    const regionStr = region.join(' ')

    if (this._regionTimer) clearTimeout(this._regionTimer)
    this._regionTimer = setTimeout(() => {
      this.submitFieldUpdate('region', regionStr)
      this._regionTimer = null
    }, 500)
  },

  // 选择背景图
  onChooseBackground() {
    const that = this
    this._updating = true
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePath = res.tempFiles[0].tempFilePath

        that.uploadImageFile(tempFilePath, 'background')
          .then(imageUrl => {
            return request(API_CONFIG.user.update, { background: imageUrl }, { showLoading: true })
          })
          .then(() => {
            return that.fetchUserInfo()
          })
          .then(() => {
            wx.showToast({ title: '背景图更新成功', icon: 'success' })
          })
          .catch(err => {
            console.error('背景图更新失败', err)
            if (err && err.code === 401) {
              wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
            } else {
              wx.showToast({ title: typeof err === 'string' ? err : '背景图更新失败', icon: 'none' })
            }
          })
          .finally(() => {
            that._updating = false
          })
      },
      fail() {
        that._updating = false
      }
    })
  }
})