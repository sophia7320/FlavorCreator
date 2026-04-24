// pages/login/login.js
Page({
	data: {
		phone: '',
		code: '',
		isAgree: true,

		isMsgTagActive: false,
		time: 60,
		isDisable: false,
		timer: null,

		isShake: false,
		isShaking: false

	},

	//手机号码输入事件
	inputPhone(e) {
		this.setData({
			phone: e.detail.value
		})
		console.log("test")
	},

	//点击登录按钮事件
	handleLogin() {
		console.log("test")
		if(!this.data.isAgree && !this.data.isShaking) {
			this.setData({
				isShake: true,
				isShaking: true
			})
			setTimeout(() => {
				this.setData({
					isShake: false,
					isShaking: false
				})
			}, 1000)
			return
		}
	},

	// 点击第三方登录按钮事件
	handleWxLogin() {
		if(!this.data.isAgree) {
			this.setData({
				isShake: true
			})
			setTimeout(() => {
				this.setData({
					isShake: false
				})
			}, 600)
			return
		}

		navigator({
			url: '/pages/waitForWeixin/wait'
		})
	},
	
	//验证码按钮事件
	sendMsg() {
		if(this.data.isDisable) return //若已经开始倒计时则不执行

		// 先清空旧残留定时器，防止多个定时器同时跑
		clearInterval(this.data.timer)

		//支持反复触发动画
    this.setData({ isMsgTagActive: false })
    setTimeout(() => {
        this.setData({ isMsgTagActive: true })
    }, 50)  // 极短延迟让渲染引擎识别到变化

		//初始化定时器参数
		this.setData({
			isDisable: true,
			time: 60
		})

		//再次获取验证码的倒计时
		const timer = setInterval(() => {
			let t = this.data.time - 1
			this.setData({
				time: t
			})

			// 倒计时结束后的操作
			if(t <= 0) {
				clearInterval(timer)
				this.setData({
					time: 'OK',
					isDisable: false
				})
			}
		},1000)

		this.setData({ timer: timer }) //后续直接关闭页面时要清除定时器
	},

	//协议勾选事件
	agreementTick(e) {
		const ischeck = e.detail.value.length > 0  //当选中时，value的长度大于0，表示已勾选
		this.setData({
			isAgree: ischeck
		})
	},

	//协议跳转事件
	goAgreement() {
		wx.navigateTo({
			url: '/pages/agreement/agreement?type=agreement'
		})
	},
	goPrivacy() {
		wx.navigateTo({
			url: '/pages/agreement/agreement?type=privacy'
		})
	},

	onUnload() {
		//页面关闭后要清除定时器
		clearInterval(this.data.timer)
	}
	
})
