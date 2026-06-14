// pages/post/post.js
const { API_CONFIG, REQUEST_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')
const { getUseCallContainer } = require('../../utils/globalState')

// 分类中文名 → API 值映射
const CATEGORY_MAP = {
  '快手菜': 'fast',
  '特色菜': 'special',
  '减脂餐': 'lowcal',
  '养生频道': 'health',
  '家常菜': 'home'
}

Page({
  data: {
    categories: ['快手菜', '特色菜', '减脂餐', '养生频道', '家常菜'],
    selectedCategory: '',

    // 封面
    cover: '',       // 本地临时路径（展示用）
    coverUrl: '',    // 已上传的云端 URL

    // 标题 & 描述
    title: '',
    description: '',

    // 用料
    ingredients: [],

    // 用料弹窗
    showIngredientModal: false,
    editingIngredient: { name: '', quantity: '', unit: '' },
    editingIngredientIndex: -1,  // -1 表示新增

    unitList: ['斤', 'g', 'kg', 'ml', 'l'],

    // 步骤
    procedures: [
      { index: 1, image: '', imageUrl: '', description: '' }
    ]
  },

  onLoad() {},

  // ===================== 标题 & 描述 =====================
  onTitleInput(e) {
    this.setData({ title: e.detail.value })
  },

  onDescriptionInput(e) {
    this.setData({ description: e.detail.value })
  },

  // ===================== 封面图 =====================
  onChooseCover() {
    const that = this
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePath = res.tempFiles[0].tempFilePath
        that.setData({ cover: tempFilePath })
      }
    })
  },

  // ===================== 上传图片（复刻 profile.js 模式） =====================
  buildUploadUrl(apiConfig) {
    const token = wx.getStorageSync('token') || ''
    return {
      url: REQUEST_CONFIG.baseUrl + apiConfig.path,
      header: {
        'Authorization': token ? 'Bearer ' + token : ''
      }
    }
  },

  uploadImageFile(filePath, scene = '') {
    const that = this
    const useCloud = getUseCallContainer()

    if (useCloud) {
      // 云模式：上传到云存储 → 获取临时 URL
      const ext = filePath.split('.').pop() || 'jpg'
      const timestamp = Date.now()
      const random = Math.random().toString(36).substring(2, 8)
      const cloudPath = `images/${scene || 'general'}/${timestamp}_${random}.${ext}`

      return wx.cloud.uploadFile({
        cloudPath: cloudPath,
        filePath: filePath
      }).then(res => {
        return wx.cloud.getTempFileURL({
          fileList: [res.fileID]
        })
      }).then(res => {
        return res.fileList[0].tempFileURL
      }).catch(err => {
        if (err && err.code === 401) {
          const app = getApp()
          return app.refreshAccessToken().then(success => {
            if (success) return that.uploadImageFile(filePath, scene)
            throw { code: 401, message: '登录已过期' }
          })
        }
        throw err
      })
    }

    // 本地直连模式
    const uploadConfig = this.buildUploadUrl(API_CONFIG.image.upload)
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
              const app = getApp()
              app.refreshAccessToken()
                .then(success => {
                  if (success) {
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
        fail(err) { reject(err) }
      })
    })
  },

  // ===================== 用料管理 =====================
  onAddIngredient() {
    this.setData({
      showIngredientModal: true,
      editingIngredient: { name: '', quantity: '', unit: '' },
      editingIngredientIndex: -1
    })
  },

  onEditIngredient(e) {
    const index = e.currentTarget.dataset.index
    const ing = this.data.ingredients[index]
    this.setData({
      showIngredientModal: true,
      editingIngredient: { ...ing },
      editingIngredientIndex: index
    })
  },

  onDeleteIngredient(e) {
    const index = e.currentTarget.dataset.index
    const ingredients = [...this.data.ingredients]
    ingredients.splice(index, 1)
    this.setData({ ingredients })
  },

  onIngredientNameInput(e) {
    this.setData({ 'editingIngredient.name': e.detail.value })
  },

  onIngredientQuantityInput(e) {
    this.setData({ 'editingIngredient.quantity': e.detail.value })
  },

  onIngredientUnitChange(e) {
    const index = e.detail.value
    const unit = this.data.unitList[index]
    this.setData({ 'editingIngredient.unit': unit })
  },

  onConfirmIngredient() {
    const { editingIngredient, editingIngredientIndex, ingredients } = this.data
    const name = editingIngredient.name.trim()
    const quantity = editingIngredient.quantity.trim()
    const unit = editingIngredient.unit.trim()

    if (!name) {
      wx.showToast({ title: '请输入食材名称', icon: 'none' })
      return
    }
    if (!quantity) {
      wx.showToast({ title: '请输入用量', icon: 'none' })
      return
    }
    if (!unit) {
      wx.showToast({ title: '请输入单位', icon: 'none' })
      return
    }

    const newIngredient = { name, quantity, unit }
    const newIngredients = [...ingredients]

    if (editingIngredientIndex >= 0) {
      newIngredients[editingIngredientIndex] = newIngredient
    } else {
      newIngredients.push(newIngredient)
    }

    this.setData({
      ingredients: newIngredients,
      showIngredientModal: false
    })
  },

  onCancelIngredient() {
    this.setData({ showIngredientModal: false })
  },

  // ===================== 步骤管理 =====================
  onAddProcedure() {
    const procedures = [...this.data.procedures]
    procedures.push({
      index: procedures.length + 1,
      image: '',
      imageUrl: '',
      description: ''
    })
    this.setData({ procedures })
  },

  onDeleteProcedure(e) {
    const index = e.currentTarget.dataset.index
    if (this.data.procedures.length <= 1) {
      wx.showToast({ title: '至少保留一个步骤', icon: 'none' })
      return
    }
    const procedures = [...this.data.procedures]
    procedures.splice(index, 1)
    // 重新编号
    procedures.forEach((p, i) => { p.index = i + 1 })
    this.setData({ procedures })
  },

  onChooseStepImage(e) {
    const index = e.currentTarget.dataset.index
    const that = this
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success(res) {
        const tempFilePath = res.tempFiles[0].tempFilePath
        const procedures = [...that.data.procedures]
        procedures[index].image = tempFilePath
        that.setData({ procedures })
      }
    })
  },

  onDeleteStepImage(e) {
    const index = e.currentTarget.dataset.index
    const procedures = [...this.data.procedures]
    procedures[index].image = ''
    procedures[index].imageUrl = ''
    this.setData({ procedures })
  },

  onStepDescriptionInput(e) {
    const index = e.currentTarget.dataset.index
    const procedures = [...this.data.procedures]
    procedures[index].description = e.detail.value
    this.setData({ procedures })
  },

  // ===================== 分类选择 =====================
  onSelectCategory(e) {
    const category = e.currentTarget.dataset.category
    this.setData({
      selectedCategory: this.data.selectedCategory === category ? '' : category
    })
  },

  // ===================== 发布 =====================
  async onPublish() {
    // 表单校验
    const { title, ingredients, procedures, selectedCategory, cover, description } = this.data

    if (!title.trim()) {
      wx.showToast({ title: '请输入菜谱标题', icon: 'none' })
      return
    }
    if (ingredients.length === 0) {
      wx.showToast({ title: '请至少添加一种用料', icon: 'none' })
      return
    }
    // 检查步骤是否都有描述
    const emptyStep = procedures.find(p => !p.description.trim())
    if (emptyStep) {
      wx.showToast({ title: `步骤${emptyStep.index}缺少说明`, icon: 'none' })
      return
    }

    wx.showLoading({ title: '发布中...', mask: true })

    try {
      // 1. 上传封面图
      let coverUrl = ''
      if (cover && !coverUrl) {
        try {
          coverUrl = await this.uploadImageFile(cover, 'recipe-cover')
          this.setData({ coverUrl })
        } catch (err) {
          console.error('封面上传失败', err)
        }
      }

      // 2. 上传步骤图片
      const imageUrls = []
      for (let i = 0; i < procedures.length; i++) {
        const p = procedures[i]
        if (p.image && !p.imageUrl) {
          try {
            const url = await this.uploadImageFile(p.image, 'recipe-image')
            imageUrls.push(url)
            const updatedProcedures = [...this.data.procedures]
            updatedProcedures[i].imageUrl = url
            this.setData({ procedures: updatedProcedures })
          } catch (err) {
            console.error(`步骤${i + 1}图片上传失败`, err)
          }
        } else if (p.imageUrl) {
          imageUrls.push(p.imageUrl)
        }
      }

      // 3. 构建请求数据
      const ingredientsJson = JSON.stringify(ingredients)
      const stepsJson = JSON.stringify(
        procedures.map(p => ({
          order: p.index,
          description: p.description.trim()
        }))
      )
      const tagsJson = selectedCategory ? JSON.stringify([selectedCategory]) : '[]'

      const postData = {
        name: title.trim(),
        ingredients: ingredientsJson,
        steps: stepsJson
      }

      if (coverUrl) postData.coverUrl = coverUrl
      if (imageUrls.length > 0) postData.imageUrls = imageUrls
      if (selectedCategory) postData.category = CATEGORY_MAP[selectedCategory] || selectedCategory
      if (tagsJson !== '[]') postData.tags = tagsJson
      if (description.trim()) postData.tips = description.trim()

      // 4. 提交菜谱
      await request(API_CONFIG.recipe.publish, postData, { showLoading: false })

      wx.hideLoading()
      wx.showToast({ title: '发布成功！', icon: 'success' })

      // 延迟返回上一页
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)

    } catch (err) {
      wx.hideLoading()
      console.error('发布失败', err)
      if (err && err.code === 401) {
        wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
      } else {
        wx.showToast({ title: '发布失败，请稍后重试', icon: 'none' })
      }
    }
  }
})
