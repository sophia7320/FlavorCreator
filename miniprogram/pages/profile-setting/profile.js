// pages/profile-setting/profile.js
const { API_CONFIG, REQUEST_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

Page({

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
    this.fetchUserInfo()
  },

  // 后端字段 → 前端字段映射
  mapBackendUser(data) {
    return {
      nickName: data.nickname || data.nickName || '',
      nickname: data.nickname || data.nickName || '',
      avatarUrl: data.avatar || data.avatarUrl || '',
      gender: data.gender || '',
      age: data.age || '',
      region: data.region || '',
      background: data.background || ''
    }
  },

  // 从后端拉取用户信息
  fetchUserInfo() {
    const app = getApp()
    request(API_CONFIG.user.info, {}, { showLoading: false, showError: false })
      .then(res => {
        const data = res.data || res
        const mappedUser = this.mapBackendUser(data)
        this.setData({ userInfo: mappedUser })
        this.syncLocalUserInfo(mappedUser)
      })
      .catch(() => {
        // 降级：从本地缓存读取
        const cached = app.getUserInfo()
        if (cached) {
          this.setData({ userInfo: this.mapBackendUser(cached) })
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
  uploadImageFile(filePath) {
    const uploadConfig = this.buildUploadUrl(API_CONFIG.image.upload)
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: uploadConfig.url,
        filePath: filePath,
        name: 'file',
        header: uploadConfig.header,
        success(res) {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 0 || data.code === 200) {
              resolve(data.data || data.url)
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
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePath = res.tempFiles[0].tempFilePath

        that.uploadImageFile(tempFilePath)
          .then(imageUrl => {
            return request(API_CONFIG.user.uploadAvatar, { avatar: imageUrl }, { showLoading: true })
          })
          .then(() => {
            // 重新拉取确保数据一致
            return that.fetchUserInfo()
          })
          .then(() => {
            wx.showToast({ title: '头像更新成功', icon: 'success' })
          })
          .catch(err => {
            console.error('头像更新失败', err)
            wx.showToast({ title: typeof err === 'string' ? err : '头像更新失败', icon: 'none' })
          })
      }
    })
  },

  // 公共提交方法
  submitFieldUpdate(field, newValue) {
    const updateData = {}
    updateData[field] = newValue

    request(API_CONFIG.user.update, updateData, { showLoading: true })
      .then(() => {
        const updated = { ...this.data.userInfo }
        updated[field] = newValue
        if (field === 'nickname') updated.nickName = newValue
        this.setData({ userInfo: updated })
        this.syncLocalUserInfo(updated)
        wx.showToast({ title: '更新成功', icon: 'success' })
      })
      .catch(err => {
        console.error('更新失败', err)
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

  // 地区选择器回调
  onRegionChange(e) {
    const region = e.detail.value  // ['广东省', '广州市', '天河区']
    const regionStr = region.join(' ')
    this.submitFieldUpdate('region', regionStr)
  },

  // 选择背景图
  onChooseBackground() {
    const that = this
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePath = res.tempFiles[0].tempFilePath

        that.uploadImageFile(tempFilePath)
          .then(imageUrl => {
            return request(API_CONFIG.user.uploadBackground, { background: imageUrl }, { showLoading: true })
          })
          .then(() => {
            wx.showToast({ title: '背景图更新成功', icon: 'success' })
            return that.fetchUserInfo()
          })
          .catch(err => {
            console.error('背景图更新失败', err)
            wx.showToast({ title: typeof err === 'string' ? err : '背景图更新失败', icon: 'none' })
          })
      }
    })
  },

  goBack(e) {
    wx.navigateBack()
  }
})