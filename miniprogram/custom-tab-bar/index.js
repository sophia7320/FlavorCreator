// custom-tab-bar/index.js
const { API_CONFIG } = require('../config/api')
const { request } = require('../utils/request')
const tabBarCreateState = require('../utils/tabBarCreateState')

const INDEX_ROUTE = 'pages/index/index'
const COMMUNITY_ROUTE = 'pages/community/community'
const MINE_ROUTE = 'pages/mine/mine'
const CREATE_ANIM_MS = 300

Component({

	data: {
		selected: 0,
		showCreate: false,
		noTransition: true,
		createAnimate: false,
		isOnCommunity: false,
		postBgAnimate: false,
		postIconUrl: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/release.svg',
		isOnMine: false,
		mineBgAnimate: false,
		mineIconUrl: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/selected-mine.svg',
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

	lifetimes: {
		/**
		 * 新实例首帧：仅 priming，不消费 handoff（消费在页面 onShow → onTabPageShow）
		 * 自定义 Tab 各页是独立实例，首次进入时 pageLifetimes.show 可能晚于/不触发
		 */
		attached() {
			this.primeHandoffFirstFrame()
		}
	},

	methods: {
		parseTargetTab(url) {
			if (!url) return ''
			if (url.indexOf('community') !== -1) return 'community'
			if (url.indexOf('mine') !== -1) return 'mine'
			if (url.indexOf('index') !== -1) return 'index'
			return ''
		},

		getCurrentTabKey() {
			const pages = getCurrentPages()
			const route = pages.length ? pages[pages.length - 1].route : ''
			if (route === INDEX_ROUTE) return 'index'
			if (route === COMMUNITY_ROUTE) return 'community'
			if (route === MINE_ROUTE) return 'mine'
			return ''
		},

		isIndexPage() {
			return this.getCurrentTabKey() === 'index'
		},

		getIndexCreateVisualState() {
			const pages = getCurrentPages()
			const indexPage = pages.find((p) => p.route === INDEX_ROUTE)
			if (!indexPage) {
				return tabBarCreateState.getShowCreate()
			}
			const ingredients = indexPage.data.selectedIngredients
			return Array.isArray(ingredients) && ingredients.length >= 1
		},

		getTargetShowCreate() {
			if (!this.isIndexPage()) {
				return false
			}
			return this.getIndexCreateVisualState()
		},

		shouldPlayHandoffAnimation(entryFrame, target) {
			if (this.isIndexPage()) {
				return target === true
			}
			return entryFrame === true
		},

		/** attached：与当前页匹配的接力目标 Tab 才 priming */
		primeHandoffFirstFrame() {
			const handoff = tabBarCreateState.peekHandoff()
			if (!handoff) {
				return
			}
			const currentTab = this.getCurrentTabKey()
			if (!currentTab || handoff.targetTab !== currentTab) {
				return
			}
			this.setData({
				showCreate: handoff.snapshot,
				noTransition: true,
				createAnimate: false
			})
		},

		/**
		 * 官方推荐：由各 Tab 页 onShow 里 getTabBar().onTabPageShow() 调用
		 * 确保「当前可见页」对应 TabBar 实例消费 handoff 并播动画
		 */
		onTabPageShow() {
			const currentTab = this.getCurrentTabKey()
			this.setData({
				isOnCommunity: currentTab === 'community',
				isOnMine: currentTab === 'mine'
			})

			// 进入社区页时触发背景胶囊动画（每次进入都触发）
			if (currentTab === 'community') {
				if (this._postBgTimer) {
					clearTimeout(this._postBgTimer)
					this._postBgTimer = null
				}
				this.setData({ postBgAnimate: false })
				wx.nextTick(() => {
					this.setData({ postBgAnimate: true })
					this._postBgTimer = setTimeout(() => {
						this._postBgTimer = null
						this.setData({ postBgAnimate: false })
					}, 400)
				})
			}

			// 进入我的页时触发背景胶囊动画（每次进入都触发）
			if (currentTab === 'mine') {
				if (this._mineBgTimer) {
					clearTimeout(this._mineBgTimer)
					this._mineBgTimer = null
				}
				this.setData({ mineBgAnimate: false })
				wx.nextTick(() => {
					this.setData({ mineBgAnimate: true })
					this._mineBgTimer = setTimeout(() => {
						this._mineBgTimer = null
						this.setData({ mineBgAnimate: false })
					}, 400)
				})
			}

			if (!currentTab) {
				return
			}

			const target = this.getTargetShowCreate()
			const handoff = tabBarCreateState.peekHandoff()

			if (handoff) {
				if (handoff.targetTab !== currentTab) {
					return
				}
				if (this._lastHandoffId === handoff.id) {
					return
				}
				const payload = tabBarCreateState.consumeHandoff()
				if (!payload) {
					return
				}
				this._lastHandoffId = payload.id
				this._runHandoffSequence(payload.snapshot, target)
				return
			}

			tabBarCreateState.setShowCreate(target)
			this._setCreateVisualState(target, {
				noTransition: false,
				createAnimate: false
			})
		},

		_playCreateKeyframeAnimation(showCreate, callback) {
			if (typeof showCreate === 'function') {
				callback = showCreate
				showCreate = undefined
			}
			const patch = {
				createAnimate: true,
				noTransition: false
			}
			if (showCreate !== undefined) {
				patch.showCreate = !!showCreate
			}
			this.setData(patch, () => {
				if (this._createAnimTimer) {
					clearTimeout(this._createAnimTimer)
				}
				this._createAnimTimer = setTimeout(() => {
					this._createAnimTimer = null
					this.setData({ createAnimate: false })
					if (typeof callback === 'function') {
						callback()
					}
				}, CREATE_ANIM_MS)
			})
		},

		_setCreateVisualState(showCreate, { noTransition, createAnimate }, callback) {
			this.setData({
				showCreate: !!showCreate,
				noTransition: !!noTransition,
				createAnimate: !!createAnimate
			}, callback)
		},

		_finishHandoff(target) {
			tabBarCreateState.resetHandoff(target)
		},

		_runHandoffSequence(entryFrame, target) {
			const shouldAnimate = this.shouldPlayHandoffAnimation(entryFrame, target)

			this._setCreateVisualState(entryFrame, {
				noTransition: true,
				createAnimate: false
			}, () => {
				const runTargetPhase = () => {
					if (!shouldAnimate) {
						this._setCreateVisualState(target, {
							noTransition: false,
							createAnimate: false
						}, () => this._finishHandoff(target))
						return
					}
					this._playCreateKeyframeAnimation(target, () => this._finishHandoff(target))
				}
				wx.nextTick(() => {
					wx.nextTick(runTargetPhase)
				})
			})
		},

		captureSnapshotBeforeSwitch(targetTab) {
			if (targetTab === 'community' || targetTab === 'mine') {
				return this.getIndexCreateVisualState()
			}
			if (targetTab === 'index') {
				return !!this.data.showCreate
			}
			return !!this.data.showCreate
		},

		navigateTab(url) {
			const targetTab = this.parseTargetTab(url)
			const snapshot = this.captureSnapshotBeforeSwitch(targetTab)
			tabBarCreateState.beginHandoff(snapshot, targetTab)
			wx.switchTab({ url })
		},

		switchTab(e) {
			const url = e.currentTarget.dataset.path
			this.navigateTab(url)
		},

		onCommunityTap() {
			if (this.data.isOnCommunity) {
				this.setData({ isOnCommunity: false })
				wx.navigateTo({ url: '/pages/post/post' })
			} else {
				this.navigateTab('/pages/community/community')
			}
		},

		updateCreateVisibility(show) {
			const value = !!show
			if (value === this.data.showCreate) {
				return
			}
			tabBarCreateState.setShowCreate(value)
			this._playCreateKeyframeAnimation(value)
		},

		onCreateClick() {
			const pages = getCurrentPages()
			const currentPage = pages[pages.length - 1]

			if (currentPage.route !== INDEX_ROUTE) {
				this.navigateTab('/pages/index/index')
				return
			}

			if (!this.data.showCreate) {
				wx.showToast({
					title: '请先选择食材',
					icon: 'none'
				})
				return
			}

			const { selectedIngredients, selectedPreferences, isDefaultMode, aiMode } = currentPage.data

			// 默认模式开启时，使用保存的默认偏好
			let preferences
			if (isDefaultMode) {
				const defaultPrefs = wx.getStorageSync('defaultPreferences') || {}
				const timeValueMap = {
					'快手菜': 15,
					'普通': 30,
					'慢炖': 60
				}
				const cookTimeValue = timeValueMap[defaultPrefs.time] || 30
				const mappedPortion = defaultPrefs.portion === '4人及以上' ? '4人以上' : (defaultPrefs.portion || null)

				preferences = {
					taste: (defaultPrefs.taste && defaultPrefs.taste.length > 0) ? defaultPrefs.taste : ['清淡'],
					portion: mappedPortion,
					cookTime: cookTimeValue
				}
			} else {
				// 非默认模式，使用主页手动选择的偏好
				preferences = {
					taste: selectedPreferences.taste.length > 0 ? selectedPreferences.taste : ['清淡'],
					portion: selectedPreferences.portion || null,
					cookTime: selectedPreferences.cookTime || 30
				}
			}

			// ======== AI 模式：调用 AI 生成接口 ========
			if (aiMode) {
				const requestData = {
					ingredients: selectedIngredients,
					preferences: {
						taste: preferences.taste,
						portion: preferences.portion,
						cookTime: preferences.cookTime
					}
				}

				wx.showLoading({ title: 'AI 生成中...' })

				request(API_CONFIG.recipe.aiGenerate, requestData)
					.then(res => {
						wx.hideLoading()
						const recipe = res.recipe || res
						wx.setStorageSync('aiRecipeResult', recipe)
						wx.navigateTo({
							url: '/pages/recipe-detail/recipe-detail?ai=true'
						})
					})
					.catch(err => {
						wx.hideLoading()
						console.error('AI 生成失败:', err)
						wx.showToast({
							title: 'AI 生成失败，请重试',
							icon: 'none'
						})
					})
				return
			}

		const requestData = {
			ingredients: selectedIngredients,
			preferences: {
				taste: preferences.taste,
				portion: preferences.portion,
				cookTime: preferences.cookTime
			}
		}

		request(API_CONFIG.recipe.apply, requestData)
				.then(res => {
					wx.setStorageSync('recipeRequest', requestData)
					wx.setStorageSync('recipeResult', res)
					wx.navigateTo({
						url: '/pages/choose-recipes/choose-recipes'
					})
				})
				.catch(err => {
					console.error('菜谱匹配失败:', err)
					wx.showToast({
						title: '菜谱匹配失败，请重试',
						icon: 'none'
					})
				})
		}
	}
})
