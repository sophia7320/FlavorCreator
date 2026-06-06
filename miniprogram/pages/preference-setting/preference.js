// pages/preference-setting/preference.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
    flavorList: [
      { name: "清淡", selected: false },
      { name: "偏咸", selected: false },
      { name: "酸甜", selected: false },
      { name: "微辣", selected: false },
      { name: "中辣", selected: false },
      { name: "重辣", selected: false }
    ],
    portionList: ["1人", "2-3人", "4人及以上"],
    timeList: ["快手菜", "普通", "慢炖"],

    // 已选中的偏好
    selectedFlavors: [],
    selectedPortion: '',
    selectedTime: ''
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    const saved = wx.getStorageSync('defaultPreferences')
    if (saved) {
      this.setData({
        selectedPortion: saved.portion || '',
        selectedTime: saved.time || ''
      })
      this.syncFlavorList(saved.taste || [])
    }
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    const saved = wx.getStorageSync('defaultPreferences')
    if (saved) {
      this.setData({
        selectedPortion: saved.portion || '',
        selectedTime: saved.time || ''
      })
      this.syncFlavorList(saved.taste || [])
    }
  },

  // 同步 flavorList 的 selected 状态
  syncFlavorList(tasteArr) {
    const flavorList = this.data.flavorList.map(item => ({
      ...item,
      selected: tasteArr.includes(item.name)
    }))
    this.setData({ flavorList, selectedFlavors: tasteArr })
  },

  // 选择口味（多选）
  onFlavorTap(e) {
    const flavorName = e.currentTarget.dataset.item
    const flavorList = this.data.flavorList.map(item => {
      if (item.name === flavorName) {
        return { ...item, selected: !item.selected }
      }
      return item
    })
    const selectedFlavors = flavorList.filter(i => i.selected).map(i => i.name)
    this.setData({ flavorList, selectedFlavors })
  },

  // 选择分量（单选）
  onPortionTap(e) {
    const portion = e.currentTarget.dataset.item
    this.setData({
      selectedPortion: this.data.selectedPortion === portion ? '' : portion
    })
  },

  // 选择时长（单选）
  onTimeTap(e) {
    const time = e.currentTarget.dataset.item
    this.setData({
      selectedTime: this.data.selectedTime === time ? '' : time
    })
  },

  // 保存偏好
  onSave() {
    const { selectedFlavors, selectedPortion, selectedTime } = this.data
    const preferences = {
      taste: selectedFlavors,
      portion: selectedPortion,
      time: selectedTime
    }
    wx.setStorageSync('defaultPreferences', preferences)
    wx.showToast({
      title: '偏好已保存',
      icon: 'success'
    })
    // 返回上一页
    setTimeout(() => {
      wx.navigateBack()
    }, 1000)
  }
})