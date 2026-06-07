const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

Page({

  data: {
    title: '',
    content: '',
    sending: false
  },

  onTitleInput(e) {
    this.setData({ title: e.detail.value })
  },

  onContentInput(e) {
    this.setData({ content: e.detail.value })
  },

  onSend() {
    const { title, content, sending } = this.data

    if (sending) return

    if (!title.trim()) {
      wx.showToast({ title: '请输入反馈标题', icon: 'none' })
      return
    }
    if (!content.trim()) {
      wx.showToast({ title: '请输入反馈内容', icon: 'none' })
      return
    }

    const fullContent = `【${title.trim()}】${content.trim()}`

    this.setData({ sending: true })

    request(API_CONFIG.system.feedback, {
      type: 'suggestion',
      content: fullContent,
      contact: 'xxxxxxxxxxx@xxx.com',
      images: []
    }, {
      showLoading: true,
      showError: true
    })
      .then(() => {
        wx.showToast({ title: '感谢您的反馈！', icon: 'success' })
        this.setData({ title: '', content: '' })
        setTimeout(() => wx.navigateBack(), 1500)
      })
      .catch((err) => {
        if (err && err.code !== 401) {
          wx.showToast({ title: '提交失败，请稍后重试', icon: 'none' })
        }
      })
      .finally(() => {
        this.setData({ sending: false })
      })
  }
})