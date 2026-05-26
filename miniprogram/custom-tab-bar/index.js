// custom-tab-bar/index.js
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
			// 检查是否有选中的食材
			if (!this.data.showCreate) {
				wx.showToast({
					title: '请先选择食材',
					icon: 'none'
				})
				return
			}

			// 获取当前页面栈
			const pages = getCurrentPages()
			const currentPage = pages[pages.length - 1]

			// 检查是否在 index 页面
			if (currentPage.route !== 'pages/index/index') {
				wx.showToast({
					title: '请在首页选择食材',
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

			// 显示加载
			wx.showLoading({ title: '提交中...' })

			// 模拟请求成功
			setTimeout(() => {
				wx.hideLoading()
				
				// 将数据存储到本地，传递到 create 页面
				wx.setStorageSync('recipeData', requestData)
				
				// 跳转到 choose-recipes 页面
				wx.navigateTo({
					url: '/pages/choose-recipes/choose-recipes'
				})
			}, 500)
		}
	}
})
