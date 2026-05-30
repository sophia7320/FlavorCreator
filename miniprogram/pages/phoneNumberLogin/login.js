// pages/login/login.js

//正则判断手机号码合理
const reg = /^1[3-9]\d{9}$/

Page({
    data: {
        phone: '',
        code: '',
        isAgree: true,

        isMsgTagActive: false,
        time: 60,
        isDisable: false,
        timer: null,

        wrongFormat: false,
        verifyCode: '',
        showToast: false,

        showPopup: false,
        popupType: 'agreement'
    },

    //手机号码输入事件
    inputPhone(e) {
        this.setData({
            phone: e.detail.value
        })
    },

		//验证码输入事件
		inputVerifyCode(e) {
			this.setData({
				verifyCode: e.detail.value
			})
		},

		//检查手机号码格式
		checkPhone(e) {
			const phoneNumber = this.data.phone;
			if(phoneNumber.length > 0 && !reg.test(phoneNumber)) {
				this.setData({
					wrongFormat: true
				})
				return
			} else {
				this.setData({
					wrongFormat: false
				})
			}
		},

    //点击登录按钮事件
    handleLogin() {

		if(this.data.wrongFormat || this.data.phone === '') return

		//暂定一个确定的验证码
		if(this.data.verifyCode !== '2077') {
			this.setData({
				showToast: true,
				toastText: '验证码错误 请重新输入！'
			})

			//控制toast显示时间
			setTimeout(() => {
				this.setData({
					showToast: false
				})
			}, 1400)
			return
		}

		//组件方法引用--完成动效
		const agreementComp = this.selectComponent('#agreement')
		if (!agreementComp.isAgreed()) {
			agreementComp.triggerShake()
			return
		}

		wx.redirectTo({
			url: '/pages/startGuide/guide'
		})
    },

    // 点击第三方登录按钮事件
    handleWxLogin() {
        const agreementComp = this.selectComponent('#agreement');
        if (!agreementComp.isAgreed()) {
            agreementComp.triggerShake();
            return;
        }

        wx.navigateTo({
            url: '/pages/waitForWeixin/wait'
        })
    },

    //协议勾选事件（由组件触发）
    onAgreeChange(e) {
        this.setData({
            isAgree: e.detail.isAgree
        })
    },

    //验证码按钮事件
    sendMsg() {
        //若已经开始倒计时则不执行
        if (this.data.isDisable || this.data.wrongFormat || this.data.phone === '') return

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
            if (t <= 0) {
                clearInterval(timer)
                this.setData({
                    time: 'OK',
                    isDisable: false
                })
            }
        }, 1000)

        this.setData({ timer: timer }) //后续直接关闭页面时要清除定时器
    },

    onUnload() {
        //页面关闭后要清除定时器
        clearInterval(this.data.timer)
    },

    onShowAgreement(e) {
        this.setData({
            showPopup: true,
            popupType: e.detail.type
        })
    },

    onClosePopup() {
        this.setData({
            showPopup: false
        })
    }
})
