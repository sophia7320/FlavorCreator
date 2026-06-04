Component({
  methods: {
    onTap() {
      this.triggerEvent('back')
      wx.navigateBack()
    }
  }
})
