// pages/waitForWeixin/wait.js
Page({

  /**
   * 页面的初始数据
   */
  data: {

  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
		wx.login({
			success: (res) => {
				const code = res.code;
				console.log(code)
	
				//打包数据
				const loginParams = {
					code: code
				}
	
				wx.request({
					url: "https://api.it120.cc/",	 	 //后端接口，需要修改
					method: "POST",
					data: loginParams,    					 //将打包后的用户信息发送给后端接口
					
					success: (res) => {
						const resp = res.data          //这是后端返回的内容，先接收，后面判断错误码
						
						if (resp.code === 200) {
							const data = resp.data
							
							//将用户信息保存到本地
							wx.setStorageSync('token', data.token)
							wx.setStorageSync('refreshToken', data.refreshToken)
							wx.setStorageSync('userInfo', data.user)
							
							wx.switchTab({
								url: '/pages/index/index'
							})
							
							wx.showToast({
								title: resp.message || "登录成功",  		//如果后端返回的message无效，则显示默认的登录成功
								icon: "success"
							})
						} else {
							wx.showToast({
								title: resp.message || "登录失败",
								icon: "none"
							})
						}
					},
	
					fail: (err) => {
						wx.showToast({
							title: "网络异常，请稍后重试",
							icon: "none"
						})
					}
				})
			}
		})

  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
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