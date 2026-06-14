// pages/add-ingredients/add.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

Page({

  /**
   * 页面的初始数据
   */
  data: {
    editId: null,        // 编辑模式下的食材 ID
    isEdit: false,        // 是否为编辑模式
    pageTitle: '添加食材',
    categories: ['谷薯类', '肉蛋类', '蔬菜类', '水果类', '豆类', '调味品', '其他'],
    units: ["斤", "kg", "g", "ml", "L"],
    selectedUnitIndex: 0,
    selectedUnit: 'g',
    selectedCategoryIndex: -1,
    selectedCategory: '',
    name: '',
    quantity: '',
    reminderDate: '',
    entryTime: ''
  },

  onLoad(options) {
    if (options.id) {
      // 编辑模式
      this.initEditMode(options)
    } else {
      // 新增模式
      this.initDates()
    }
  },

  // 初始化日期
  initDates() {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    const today = `${year}-${month}-${day}`
    this.setData({
      reminderDate: today,
      entryTime: today
    })
  },

  // 编辑模式初始化
  initEditMode(options) {
    const category = decodeURIComponent(options.category || '')
    const categoryIndex = this.data.categories.indexOf(category)
    const unit = decodeURIComponent(options.unit || '')
    const unitIndex = this.data.units.indexOf(unit)

    this.setData({
      isEdit: true,
      pageTitle: '编辑食材',
      editId: options.id,
      name: decodeURIComponent(options.name || ''),
      quantity: decodeURIComponent(options.quantity || ''),
      selectedUnit: unitIndex >= 0 ? unit : 'g',
      selectedUnitIndex: unitIndex >= 0 ? unitIndex : 0,
      selectedCategory: categoryIndex >= 0 ? category : '',
      selectedCategoryIndex: categoryIndex,
      reminderDate: decodeURIComponent(options.expireDate || ''),
      entryTime: this.formatDate(options.createdAt) || this.getToday()
    })
  },

  // 格式化 ISO 日期为 YYYY-MM-DD
  formatDate(isoStr) {
    if (!isoStr) return ''
    const d = new Date(isoStr)
    if (isNaN(d.getTime())) return ''
    const year = d.getFullYear()
    const month = String(d.getMonth() + 1).padStart(2, '0')
    const day = String(d.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  },

  getToday() {
    const now = new Date()
    const year = now.getFullYear()
    const month = String(now.getMonth() + 1).padStart(2, '0')
    const day = String(now.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  },

  // 名称输入
  bindNameInput(e) {
    this.setData({ name: e.detail.value })
  },

  // 用量输入
  bindQuantityInput(e) {
    this.setData({ quantity: e.detail.value })
  },

  // 单位选择
  bindUnitChange(e) {
    const index = e.detail.value
    this.setData({
      selectedUnitIndex: index,
      selectedUnit: this.data.units[index]
    })
  },

  // 提醒日期选择
  bindReminderChange(e) {
    this.setData({ reminderDate: e.detail.value })
  },

  // 分类选择
  bindCategoryTap(e) {
    const index = e.currentTarget.dataset.index
    const category = this.data.categories[index]
    if (this.data.selectedCategoryIndex === index) {
      this.setData({
        selectedCategoryIndex: -1,
        selectedCategory: ''
      })
    } else {
      this.setData({
        selectedCategoryIndex: index,
        selectedCategory: category
      })
    }
  },

  // 确定提交
  async bindConfirm() {
    const { name, quantity, selectedUnit, reminderDate, selectedCategory, isEdit, editId } = this.data

    // 校验
    if (!name.trim()) {
      wx.showToast({ title: '请输入食材名称', icon: 'none' })
      return
    }
    if (!quantity.trim()) {
      wx.showToast({ title: '请输入用量', icon: 'none' })
      return
    }
    const qty = parseFloat(quantity)
    if (isNaN(qty) || qty <= 0) {
      wx.showToast({ title: '用量需为大于0的数字', icon: 'none' })
      return
    }
    if (!selectedUnit) {
      wx.showToast({ title: '请选择单位', icon: 'none' })
      return
    }
    if (!reminderDate) {
      wx.showToast({ title: '请选择提醒日期', icon: 'none' })
      return
    }
    if (!selectedCategory) {
      wx.showToast({ title: '请选择分类', icon: 'none' })
      return
    }

    const payload = {
      name: name.trim(),
      quantity: qty,
      unit: selectedUnit,
      category: selectedCategory,
      expireDate: reminderDate,
      storageCondition: '常温'
    }

    try {
      if (isEdit && editId) {
        // 编辑模式：PUT
        const updateConfig = {
          ...API_CONFIG.ingredient.update,
          path: API_CONFIG.ingredient.update.path.replace('{id}', editId)
        }
        await request(updateConfig, payload, { showLoading: true, showError: true })
        wx.showToast({ title: '修改成功', icon: 'success' })
      } else {
        // 新增模式：POST
        await request(API_CONFIG.ingredient.add, payload, { showLoading: true, showError: true })
        wx.showToast({ title: '添加成功', icon: 'success' })
      }
      setTimeout(() => {
        wx.navigateBack()
      }, 1500)
    } catch (err) {
      console.error(isEdit ? '修改食材失败:' : '添加食材失败:', err)
    }
  }
})
