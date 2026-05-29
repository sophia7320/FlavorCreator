// pages/startGuide/guide.js
Page({

  /**
   * 页面的初始数据
   */
  data: {
		mainText: '食谱创作场景',
		secondText: 'AI智能匹配专属私房菜谱随意编写',
		index: 0
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {

  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
	},

	onChange(e) {
		this.setData({
			index: e.detail.current,
		})
		if(e.detail.current === 0) {
			this.setData({
				mainText: '食谱创作场景',
				secondText: 'AI智能匹配专属私房菜谱随意编写'
			})
		}
		if(e.detail.current === 1) {
			this.setData({
				mainText: '食材搭配',
				secondText: '智能优化搭配方案'
			})
		}
		if(e.detail.current === 2) {
			this.setData({
				mainText: '成品分享',
				secondText: '晒出专属家常烹饪日常'
			})
		}
	},

	onStart() {
		wx.setStorageSync('hasSeenGuide', true)
		wx.switchTab({
			url: '/pages/index/index',
		})
	},
	
  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {

  },

  /**
   * 生命周期函数--监听页面隐藏
   */
  onHide() {

  },

  /**
   * 生命周期函数--监听页面卸载
   */
  onUnload() {

  },

  /**
   * 页面相关事件处理函数--监听用户下拉动作
   */
  onPullDownRefresh() {

  },

  /**
   * 页面上拉触底事件的处理函数
   */
  onReachBottom() {

  },

  /**
   * 用户点击右上角分享
   */
  onShareAppMessage() {

  }
})