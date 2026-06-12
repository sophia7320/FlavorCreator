// index.js
const app = getApp()
const { request } = require('../../utils/request')
const { API_CONFIG } = require('../../config/api')

Page({
  data: {
    dishName: '加载中...',
		weekDay: '',
		solarDate: '',
		description: '',
		recommendCover: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/bg/top-bar-dish-bg.png',

		steps: [
			{ stepId: 1, name:'食材', selected: true },
			{ stepId: 2, name:'口味', selected: false },
			{ stepId: 3, name:'时长', selected: false },
			{ stepId: 4, name:'分量', selected: false },
			],

			isDefaultMode: false,

			// seleceted-line的位置
			indicatorLeft: 0,

			currentTab: 0,

			// 选中的食材
			selectedIngredients: [],

			// 搜索相关
			searchText: '',
			showDropdown: false,
			autocompleteList: [],

			// 所有食材数据（用于搜索）
			allIngredientsList: [],

			// 食材存储未读预警角标
			storageBadgeCount: 0,

			// 口味数据
			tasteOptions: [
				{ name: '清淡', id: 1, selected: false },
				{ name: '偏咸', id: 2, selected: false },
				{ name: '酸甜', id: 3, selected: false },
				{ name: '微辣', id: 4, selected: false },
				{ name: '中辣', id: 5, selected: false },
				{ name: '重辣', id: 6, selected: false }
			],

			// 时长数据
			cookTimeOptions: [
				{ name: '简单（小于15分钟）', id: 1, value: 15, selected: false },
				{ name: '普通（15到30分钟）', id: 2, value: 30, selected: false },
				{ name: '慢炖（大于30分钟）', id: 3, value: 60, selected: false }
			],

			// 份量数据
			servingsOptions: [
				{ name: '1人', id: 1, selected: false },
				{ name: '2-3人', id: 2, selected: false },
				{ name: '4人以上', id: 3, selected: false }
			],

			// 选中的偏好设置
			selectedPreferences: {
				taste: [],
				cookTime: null,
				difficulty: null
			},

			// basket 相关
			showBasketAnimation: false,
			showBasketTip: false,
			basketTipText: '',
			showBasketPanel: false,
		aiMode: false
  },

	timerId: null,

	// 点击设置
	onSettingTap() {
		wx.navigateTo({
			url: '/pages/settings/settings'
		})
	},

	// 时间更新 
	updateTime() {
		const now = new Date()

		const day = now.getDate()
		const month = now.getMonth() + 1
		const weekDays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']

		this.setData({
			weekDay: weekDays[now.getDay()],
			solarDate: 	`${month}月${day}日`
		})

	},

	// 拉取每日推荐
	fetchDailyRecommend() {
		const DEFAULT_COVER = 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/bg/top-bar-dish-bg.png'
		request(API_CONFIG.recipe.recommend, {}, { showLoading: false, showError: false })
			.then((res) => {
				const data = res.data || res
				// 接口返回 { title, recipes: [{ name, cover, reason, ... }] }
				const firstRecipe = data.recipes && data.recipes.length > 0 ? data.recipes[0] : null

				// 菜名：优先取真实菜名 recipes[0].name，没有则回退到 title
				const dishName = (firstRecipe && firstRecipe.name) || data.title || '今日推荐'

				// 描述：优先取推荐理由 recipes[0].reason，其次取 data.description，最后拼接兜底
				const description = (firstRecipe && firstRecipe.reason)
					|| data.description
					|| (firstRecipe
						? `${firstRecipe.name || '推荐菜谱'} · 共${data.recipes.length}道推荐`
						: '')

				// 背景图：使用 recipes[0].cover，无则保留 CSS 默认图
				const recommendCover = (firstRecipe && firstRecipe.cover) || DEFAULT_COVER

				this.setData({
					dishName,
					description,
					recommendCover
				})
			})
			.catch(() => {
				// 接口失败时使用默认文案
				this.setData({
					dishName: '今日推荐',
					description: '今天不知道吃什么？选选食材，让AI帮你搭配吧！',
					recommendCover: DEFAULT_COVER
				})
			})
	},

	onShow() {
		// 调用时间更新函数 
		this.updateTime()
		// 初始化时计算selected-line的位置
		this.updateIndicatorPosition()
		// 自定义 Tab 多实例：须在本页 onShow 驱动当前页 TabBar 接力（官方 getTabBar 用法）
		if (typeof this.getTabBar === 'function' && this.getTabBar()) {
			this.getTabBar().onTabPageShow()
		}
		// 初始化食材搜索数据
		this.initIngredientsList()
		// 拉取每日推荐
		this.fetchDailyRecommend()
		// 恢复默认模式开关状态
		const savedMode = wx.getStorageSync('isDefaultMode')
		if (savedMode !== undefined && savedMode !== '') {
			this.setData({ isDefaultMode: !!savedMode })
			if (!!savedMode) {
				this.applyDefaultPreferences()
			}
		}
		// 获取食材存储未读预警角标
		this.fetchStorageBadge()
	},

	onReady() {
		if (typeof this.getTabBar === 'function' && this.getTabBar()) {
			this.getTabBar().onTabPageShow()
		}
	},

	onDefaultModeTap(e) {
		const newMode = !this.data.isDefaultMode
		this.setData({
			isDefaultMode: newMode
		})
		// 持久化开关状态
		wx.setStorageSync('isDefaultMode', newMode)

		if (newMode) {
			this.applyDefaultPreferences()
		} else {
			this.resetPreferences()
		}
	},

	// 点击切换tab
	onSwitchTab(e) {
		const index = e.currentTarget.dataset.index
    
    // 更新选中状态
    const newSteps = this.data.steps.map((step, i) => ({
        ...step,
        selected: i === index
    }))
    
    this.setData({
        currentTab: index,
        steps: newSteps,
    })
    // 计算并更新selected-line的位置
    this.updateIndicatorPosition()
	},

	// 滑动切换tab
	onSlipSwitchTab(e) {
		const index = e.detail.current

		// 模拟点击事件(事件复用)
		this.onSwitchTab({
			// 传入index
			currentTarget: {
				dataset: {
					index
				}
			}
		})
	},

	// 更新selected-line的位置到选中项正下方
	updateIndicatorPosition() {
		// 获取窗口信息用于转换单位
		const windowInfo = wx.getWindowInfo()
		const scale = 750 / windowInfo.windowWidth

		// 使用createSelectorQuery获取选中项和bar的位置
		const query = this.createSelectorQuery()
		query.select('#step-' + this.data.currentTab).boundingClientRect()
		query.select('.steps-bar').boundingClientRect()
		query.exec((res) => {
			if (res && res[0] && res[1]) {
				const itemRect = res[0]
				const barRect = res[1]

				// 计算选中项相对于bar的位置（px转rpx）
				const itemLeft = (itemRect.left - barRect.left) * scale
				const itemWidth = itemRect.width * scale

				// 获取line的固定宽度（从wxss中知道是57rpx）
				const lineWidth = 57

				// 计算line的位置：选中项中心 - line宽度/2
				// 这样line就会精确居中于选中项下方
				const left = itemLeft + (itemWidth - lineWidth) / 2

				this.setData({
					indicatorLeft: left
				})
			}
		})
	},

	// 监听食材选择变化
	onIngredientsChange(e) {
		const selectedIngredients = e.detail.selectedIngredients
		const changedIngredient = e.detail.changedIngredient
		
		this.setData({
			selectedIngredients: selectedIngredients
		})

		// 更新 tab-bar 的 Create 显示状态
		if (typeof this.getTabBar === 'function' && this.getTabBar()) {
			this.getTabBar().updateCreateVisibility(selectedIngredients.length >= 1)
		}

		// 如果有食材变化，触发 basket 动画
		if (changedIngredient) {
			// 判断是添加还是取消
			const isAdded = selectedIngredients.some(item => item.name === changedIngredient.name)
			const message = isAdded ? `已添加${changedIngredient.name}` : `已取消${changedIngredient.name}`
			
			this.setData({
				basketTipText: message,
				showBasketTip: true
			})
			
			this.playBasketAnimation()
		}
	},

	// 选择口味
	onSelectTaste(e) {
		// 默认模式下手动修改，自动关闭默认模式
		if (this.data.isDefaultMode) {
			this.setData({ isDefaultMode: false })
			wx.setStorageSync('isDefaultMode', false)
		}

		const { index } = e.currentTarget.dataset
		const tasteOptions = [...this.data.tasteOptions]
		tasteOptions[index].selected = !tasteOptions[index].selected

		// 获取所有选中的口味
		const selectedTastes = tasteOptions
			.filter(item => item.selected)
			.map(item => item.name)

		this.setData({
			tasteOptions: tasteOptions,
			'selectedPreferences.taste': selectedTastes
		})
	},

	// 选择时长
	onSelectCookTime(e) {
		// 默认模式下手动修改，自动关闭默认模式
		if (this.data.isDefaultMode) {
			this.setData({ isDefaultMode: false })
			wx.setStorageSync('isDefaultMode', false)
		}

		const { index } = e.currentTarget.dataset
		const cookTimeOptions = [...this.data.cookTimeOptions]
		const isCurrentlySelected = cookTimeOptions[index].selected
		
		// 先取消所有选择
		cookTimeOptions.forEach(item => item.selected = false)

		// 如果当前项之前未选中，则选中它；否则保持全取消（即取消选择）
		if (!isCurrentlySelected) {
			cookTimeOptions[index].selected = true
		}

		const difficultyMap = ['简单', '普通', '困难']
		const selectedCookTime = isCurrentlySelected ? null : cookTimeOptions[index].value
		const difficulty = isCurrentlySelected ? null : difficultyMap[index]

		this.setData({
			cookTimeOptions: cookTimeOptions,
			'selectedPreferences.cookTime': selectedCookTime,
			'selectedPreferences.difficulty': difficulty
		})
	},

	// 选择份量
	onSelectServings(e) {
		// 默认模式下手动修改，自动关闭默认模式
		if (this.data.isDefaultMode) {
			this.setData({ isDefaultMode: false })
			wx.setStorageSync('isDefaultMode', false)
		}

		const { index } = e.currentTarget.dataset
		const servingsOptions = [...this.data.servingsOptions]
		const isCurrentlySelected = servingsOptions[index].selected
		
		// 先取消所有选择
		servingsOptions.forEach(item => item.selected = false)

		// 如果当前项之前未选中，则选中它；否则保持全取消（即取消选择）
		if (!isCurrentlySelected) {
			servingsOptions[index].selected = true
		}

		this.setData({
			servingsOptions: servingsOptions
		})
	},

	// 初始化食材搜索列表
	initIngredientsList() {
		const allIngredientsData = {
			0: [
				{name: '大米', id: 1, unit: 'g'}, 
				{name: '面粉', id: 2, unit: 'g'}, 
				{name: '小米', id: 3, unit: 'g'}, 
				{name: '玉米', id: 4, unit: 'g'}, 
				{name: '燕麦', id: 5, unit: 'g'}, 
				{name: '红薯', id: 6, unit: 'g'}, 
				{name: '土豆', id: 7, unit: 'g'}, 
				{name: '山药', id: 8, unit: 'g'}, 
				{name: '荞麦', id: 9, unit: 'g'}, 
				{name: '薏米', id: 10, unit: 'g'}, 
				{name: '藜麦', id: 11, unit: 'g'}, 
				{name: '大麦', id: 12, unit: 'g'},
				{name: '糯米', id: 13, unit: 'g'}, 
				{name: '糙米', id: 14, unit: 'g'}, 
				{name: '黑米', id: 15, unit: 'g'}, 
				{name: '芋头', id: 16, unit: 'g'},
			],
			1: [
				{name: '鹅肉', id: 17, unit: 'g'}, 
				{name: '花甲', id: 18, unit: 'g'}, 
				{name: '火腿', id: 19, unit: 'g'}, 
				{name: '鸡翅', id: 20, unit: 'g'}, 
				{name: '鸡蛋', id: 21, unit: 'g'}, 
				{name: '鸡肉', id: 22, unit: 'g'}, 
				{name: '鸡腿', id: 23, unit: 'g'}, 
				{name: '鸡胸肉', id: 24, unit: 'g'}, 
				{name: '腊肉', id: 25, unit: 'g'}, 
				{name: '牛腩', id: 26, unit: 'g'}, 
				{name: '牛肉', id: 27, unit: 'g'}, 
				{name: '螃蟹', id: 28, unit: 'g'},
				{name: '三文鱼', id: 29, unit: 'g'}, 
				{name: '扇贝', id: 30, unit: 'g'}, 
				{name: '虾', id: 31, unit: 'g'}, 
				{name: '香肠', id: 32, unit: 'g'},
				{name: '鸭肉', id: 33, unit: 'g'}, 
				{name: '羊肉', id: 34, unit: 'g'}, 
				{name: '鱿鱼', id: 35, unit: 'g'}, 
				{name: '鱼', id: 36, unit: 'g'},
				{name: '猪肉', id: 37, unit: 'g'}
			],
			2: [
				{name: '白菜', id: 38, unit: 'g'}, 
				{name: '菠菜', id: 39, unit: 'g'}, 
				{name: '彩椒', id: 40, unit: 'g'}, 
				{name: '番茄', id: 41, unit: 'g'}, 
				{name: '花椰菜', id: 42, unit: 'g'}, 
				{name: '黄瓜', id: 43, unit: 'g'}, 
				{name: '韭菜', id: 44, unit: 'g'}, 
				{name: '苦瓜', id: 45, unit: 'g'}, 
				{name: '辣椒', id: 46, unit: 'g'}, 
				{name: '莲藕', id: 47, unit: 'g'}, 
				{name: '毛豆', id: 48, unit: 'g'}, 
				{name: '南瓜', id: 49, unit: 'g'},
				{name: '茄子', id: 50, unit: 'g'}, 
				{name: '芹菜', id: 51, unit: 'g'}, 
				{name: '青椒', id: 52, unit: 'g'}, 
				{name: '娃娃菜', id: 53, unit: 'g'},
				{name: '豌豆', id: 54, unit: 'g'}, 
				{name: '西兰花', id: 55, unit: 'g'}, 
				{name: '洋葱', id: 56, unit: 'g'}, 
				{name: '紫甘蓝', id: 57, unit: 'g'}
			],
			3: [
				{name: '草莓', id: 58, unit: 'g'}, 
				{name: '橘子', id: 59, unit: 'g'}, 
				{name: '苹果', id: 60, unit: 'g'}, 
				{name: '桃子', id: 61, unit: 'g'}, 
				{name: '芒果', id: 62, unit: 'g'}, 
				{name: '香蕉', id: 63, unit: 'g'}, 
				{name: '西瓜', id: 64, unit: 'g'}, 
				{name: '椰子', id: 65, unit: 'g'}, 
				{name: '柠檬', id: 66, unit: 'g'}, 
				{name: '蓝莓', id: 67, unit: 'g'}, 
				{name: '菠萝', id: 68, unit: 'g'}, 
				{name: '哈密瓜', id: 69, unit: 'g'},
				{name: '火龙果', id: 70, unit: 'g'}, 
				{name: '橙子', id: 71, unit: 'g'}, 
				{name: '木瓜', id: 72, unit: 'g'}, 
				{name: '牛油果', id: 73, unit: 'g'}
			],
			4: [
				{name: '黄豆', id: 74, unit: 'g'}, 
				{name: '黑豆', id: 75, unit: 'g'}, 
				{name: '红豆', id: 76, unit: 'g'}, 
				{name: '绿豆', id: 77, unit: 'g'}, 
				{name: '豆腐', id: 78, unit: 'g'}, 
				{name: '豆皮', id: 79, unit: 'g'}, 
				{name: '豆干', id: 80, unit: 'g'}, 
				{name: '腐竹', id: 81, unit: 'g'}, 
				{name: '蚕豆', id: 82, unit: 'g'}, 
				{name: '豌豆', id: 83, unit: 'g'}, 
				{name: '赤小豆', id: 84, unit: 'g'}, 
				{name: '芸豆', id: 85, unit: 'g'},
				{name: '油豆腐', id: 86, unit: 'g'}, 
				{name: '豆花', id: 87, unit: 'g'}, 
				{name: '鹰嘴豆', id: 88, unit: 'g'}, 
				{name: '腐乳', id: 89, unit: 'g'}	
			]
		}
		
		// 展平所有食材
		const allIngredientsList = Object.values(allIngredientsData).flat()
		this.setData({ allIngredientsList })
	},

	// 搜索输入处理
	onSearchInput(e) {
		const searchText = e.detail.value.trim()
		this.setData({ searchText })

		if (searchText === '') {
			this.setData({ 
				autocompleteList: [],
				showDropdown: false 
			})
			return
		}

		// 搜索匹配的食材
		const allIngredientsList = this.data.allIngredientsList
		const autocompleteList = allIngredientsList.filter(item => 
			item.name.includes(searchText)
		)

		this.setData({ 
			autocompleteList,
			showDropdown: true 
		})
	},

	// 输入框获得焦点
	onInputFocus() {
		if (this.data.searchText && this.data.autocompleteList.length > 0) {
			this.setData({ showDropdown: true })
		}
	},

	// 输入框失去焦点
	onInputBlur() {
		// 延迟隐藏，让点击事件能够生效
		setTimeout(() => {
			this.setData({ showDropdown: false })
		}, 200)
	},

	// 选择自动补全项
	onSelectAutocomplete(e) {
		const item = e.currentTarget.dataset.item
		this.addIngredientToSelected(item)
	},

	// 点击添加按钮
	onAddIngredient() {
		const searchText = this.data.searchText.trim()
		if (!searchText) {
			wx.showToast({
				title: '请先输入食材名称',
				icon: 'none'
			})
			return
		}

		// 检查是否有匹配的食材
		const allIngredientsList = this.data.allIngredientsList
		const matchedItem = allIngredientsList.find(item => 
			item.name === searchText || item.name.includes(searchText)
		)

		if (matchedItem) {
			this.addIngredientToSelected(matchedItem)
		} else {
			// 没有匹配的，添加自定义食材
		this.addIngredientToSelected({
			name: searchText,
			id: Date.now(),
			unit: 'g'
		})
		}
	},

	// 添加食材到选中列表
	addIngredientToSelected(ingredient) {
		// 检查是否已添加
		const selectedIngredients = [...this.data.selectedIngredients]
		const existingIndex = selectedIngredients.findIndex(item => 
			item.name === ingredient.name
		)

		let message = ''
		if (existingIndex !== -1) {
			// 已存在，取消选中
			selectedIngredients.splice(existingIndex, 1)
			message = `已取消${ingredient.name}`
		} else {
			// 不存在，添加新项
			selectedIngredients.push({
				name: ingredient.name
			})
			message = `已添加${ingredient.name}`
		}

		this.setData({ 
			selectedIngredients,
			searchText: '',
			autocompleteList: [],
			showDropdown: false,
			basketTipText: message,
			showBasketTip: true
		})

		// 更新 tab-bar 的 Create 显示状态
		if (typeof this.getTabBar === 'function' && this.getTabBar()) {
			this.getTabBar().updateCreateVisibility(selectedIngredients.length >= 1)
		}

		// 播放 basket 动画
		this.playBasketAnimation()
	},

	// 播放 basket 动画
	playBasketAnimation() {
		this.setData({ showBasketAnimation: true })
		
		setTimeout(() => {
			this.setData({ showBasketAnimation: false })
		}, 400)
		
		// 清除旧的定时器
		if (this.timerId) {
			clearTimeout(this.timerId)
		}
		
		// 1.5秒后隐藏提示
		this.timerId = setTimeout(() => {
			this.setData({ showBasketTip: false })
			this.timerId = null
		}, 1500)
	},

	// 点击 basket 打开面板
	onBasketTap() {
		this.setData({
			showBasketPanel: true
		})
	},

	// 关闭 basket 面板
	onCloseBasketPanel() {
		this.setData({
			showBasketPanel: false
		})
	},

	// 清空已选食材
	onClearSelectedIngredients() {
		wx.showModal({
			title: '确认清空',
			content: '确定要清空所有已选食材吗？',
			success: (res) => {
				if (res.confirm) {
					this.setData({
						selectedIngredients: []
					})
					
					// 更新 tab-bar 的 Create 显示状态
					if (typeof this.getTabBar === 'function' && this.getTabBar()) {
						this.getTabBar().updateCreateVisibility(false)
					}
					
					wx.showToast({
						title: '已清空',
						icon: 'success'
					})
				}
			}
		})
	},

	// 应用默认偏好（从本地存储读取并自动选中对应按钮）
	applyDefaultPreferences() {
		const prefs = wx.getStorageSync('defaultPreferences')
		if (!prefs) return

		const updates = {}

		// 1. 应用口味（多选，按名称匹配）
		if (prefs.taste && prefs.taste.length > 0) {
			const tasteOptions = this.data.tasteOptions.map(item => ({
				...item,
				selected: prefs.taste.includes(item.name)
			}))
			updates.tasteOptions = tasteOptions
			updates['selectedPreferences.taste'] = prefs.taste
		}

		// 2. 应用分量（单选，"4人及以上" → "4人以上"）
		if (prefs.portion) {
			const mappedPortion = prefs.portion === '4人及以上' ? '4人以上' : prefs.portion
			const servingsOptions = this.data.servingsOptions.map(item => ({
				...item,
				selected: item.name === mappedPortion
			}))
			updates.servingsOptions = servingsOptions
		}

		// 3. 应用时长（单选，按偏好页顺序映射到 cookTimeOptions 索引）
		if (prefs.time) {
			const timeIndexMap = { '快手菜': 0, '普通': 1, '慢炖': 2 }
			const timeIndex = timeIndexMap[prefs.time]
			if (timeIndex !== undefined) {
				const cookTimeOptions = this.data.cookTimeOptions.map((item, i) => ({
					...item,
					selected: i === timeIndex
				}))
				const difficultyMap = ['简单', '普通', '困难']
				updates.cookTimeOptions = cookTimeOptions
				updates['selectedPreferences.cookTime'] = cookTimeOptions[timeIndex].value
				updates['selectedPreferences.difficulty'] = difficultyMap[timeIndex]
			}
		}

		if (Object.keys(updates).length > 0) {
			this.setData(updates)
		}
	},

	// 重置所有偏好选择（关闭默认模式时调用）
	resetPreferences() {
		const tasteOptions = this.data.tasteOptions.map(item => ({
			...item,
			selected: false
		}))
		const cookTimeOptions = this.data.cookTimeOptions.map(item => ({
			...item,
			selected: false
		}))
		const servingsOptions = this.data.servingsOptions.map(item => ({
			...item,
			selected: false
		}))

		this.setData({
			tasteOptions,
			cookTimeOptions,
			servingsOptions,
			selectedPreferences: {
				taste: [],
				cookTime: null,
				difficulty: null
			}
		})
	},

	// AI 模式切换
	onAITap(e) {
		const aiMode = e.detail.selected
		this.setData({ aiMode })
	},

	// 移除单个食材
	onRemoveIngredient(e) {
		const ingredient = e.currentTarget.dataset.item
		const selectedIngredients = [...this.data.selectedIngredients]
		const index = selectedIngredients.findIndex(item => item.name === ingredient.name)
		
		if (index !== -1) {
			selectedIngredients.splice(index, 1)
			
			this.setData({
				selectedIngredients,
				basketTipText: `已取消${ingredient.name}`,
				showBasketTip: true
			})
			
			// 更新 tab-bar 的 Create 显示状态
			if (typeof this.getTabBar === 'function' && this.getTabBar()) {
				this.getTabBar().updateCreateVisibility(selectedIngredients.length >= 1)
			}
		}
	},

	// 获取食材存储未读预警数量（角标）
	fetchStorageBadge() {
		request(API_CONFIG.ingredient.list, {
			sortBy: 'createTime',
			sort: 'desc'
		}, { showLoading: false, showError: false })
			.then((data) => {
				const ingredients = data.ingredients || []
				// 统计未读异常食材数（daysLeft ≤ 15 且 !readed）
				const badgeCount = ingredients.filter(
					item => item.daysLeft <= 15 && !item.readed
				).length
				this.setData({ storageBadgeCount: badgeCount })
			})
			.catch(() => {})
	},

	onIngredientsStorageTap() {
		wx.navigateTo({
			url: '/pages/ingredients-storage/storage'
		})
	},

})
