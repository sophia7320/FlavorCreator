// custom-tab-bar/index.js
const { API_CONFIG } = require('../config/api')
const { request } = require('../utils/request')

Component({

  data: {
		selected: 0,
		showCreate: false,
		list: [
			{
				pagePath: '/pages/index/index', 
				text: '首页',
				iconPath: '待改', 
				selectedIconPath: '待改'
			},
			{
				pagePath: '/pages/community/community',
				text: '社区',
				iconPath: '待改',
				selectedIconPath: '待改'
			},
			{
				pagePath: '/pages/mine/mine',
				text: '我的',
				iconPath: '待改',
				selectedIconPath: '待改'
			}
		]
  },

	methods: {
		switchTab(e) {
			const data = e.currentTarget.dataset
			const url = data.path
			wx.switchTab({ url })
		},

		updateCreateVisibility(show) {
			this.setData({ showCreate: show })
		},

		onCreateClick() {
			// 获取当前页面栈
			const pages = getCurrentPages()
			const currentPage = pages[pages.length - 1]

			// 检查是否在 index 页面
			if (currentPage.route !== 'pages/index/index') {
				// 不在主页，跳转到主页
				wx.switchTab({
					url: '/pages/index/index'
				})
				return
			}

			// 检查是否有选中的食材
			if (!this.data.showCreate) {
				wx.showToast({
					title: '请先选择食材',
					icon: 'none'
				})
				return
			}

			// 获取 index 页面的数据
			const { selectedIngredients, selectedPreferences } = currentPage.data

			// 构建请求数据
			const requestData = {
				ingredients: selectedIngredients,
				preferences: {
					taste: selectedPreferences.taste.length > 0 ? selectedPreferences.taste : ['清淡'],
					cookTime: selectedPreferences.cookTime || 30,
					difficulty: selectedPreferences.difficulty || '简单'
				}
			}

			// 调用接口
			request(API_CONFIG.recipe.apply, requestData)
				.then(res => {
					// 将请求数据和响应数据都存储到本地
					wx.setStorageSync('recipeRequest', requestData)
					wx.setStorageSync('recipeResult', res)
					
					// 跳转到 choose-recipes 页面
					wx.navigateTo({
						url: '/pages/choose-recipes/choose-recipes'
					})
				})
				.catch(err => {
					console.error('菜谱匹配失败:', err)
					wx.showToast({
						title: '匹配失败，请重试',
						icon: 'none'
					})
				})
		}
	}
})
