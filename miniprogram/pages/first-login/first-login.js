// pages/first-login/first-login.js
Page({

  //页面数据
  data: {
    showPopup: false,
    popupType: 'agreement'
  },
	
	onPhoneLogin() {
		const agreementComponent = this.selectComponent('#agreement')
		if(agreementComponent && !agreementComponent.isAgreed()) {
			agreementComponent.triggerShake()
			return
		}

		wx.navigateTo({
			url: '/pages/phone-number-login/login',
		})
	},

	onWxLogin() {
		const agreementComponent = this.selectComponent('#agreement')
		if(agreementComponent && !agreementComponent.isAgreed()) {
			agreementComponent.triggerShake()
			return
		}

		wx.navigateTo({
			url: '/pages/wait-for-weixin/wait',
		})
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