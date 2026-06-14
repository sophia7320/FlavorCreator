// components/index-swiper-contianer/swiper-ingredients/ingredients-choose.js
Component({
	properties: {
		// 从父组件接收选中的食材列表
		selectedIngredientsList: {
			type: Array,
			value: []
		}
	},
  data: {
		tabList: [
			'谷薯类', '肉蛋类', '蔬菜类', '水果类', '豆类'
		],
		currentTab: 0,

		ingredientsData: [],

		allIngredientsData: {
			0: [
				{name: '大米', id: 1, selected: false, quantity: 1, unit: 'g'}, 
				{name: '面粉', id: 2, selected: false, quantity: 1, unit: 'g'}, 
				{name: '小米', id: 3, selected: false, quantity: 1, unit: 'g'}, 
				{name: '玉米', id: 4, selected: false, quantity: 1, unit: 'g'}, 
				{name: '燕麦', id: 5, selected: false, quantity: 1, unit: 'g'}, 
				{name: '红薯', id: 6, selected: false, quantity: 1, unit: 'g'}, 
				{name: '土豆', id: 7, selected: false, quantity: 1, unit: 'g'}, 
				{name: '山药', id: 8, selected: false, quantity: 1, unit: 'g'}, 
				{name: '荞麦', id: 9, selected: false, quantity: 1, unit: 'g'}, 
				{name: '薏米', id: 10, selected: false, quantity: 1, unit: 'g'}, 
				{name: '藜麦', id: 11, selected: false, quantity: 1, unit: 'g'}, 
				{name: '大麦', id: 12, selected: false, quantity: 1, unit: 'g'},
				{name: '糯米', id: 13, selected: false, quantity: 1, unit: 'g'}, 
				{name: '糙米', id: 14, selected: false, quantity: 1, unit: 'g'}, 
				{name: '黑米', id: 15, selected: false, quantity: 1, unit: 'g'}, 
				{name: '芋头', id: 16, selected: false, quantity: 1, unit: 'g'},
			],
			1: [
				{name: '鹅肉', id: 17, selected: false, quantity: 1, unit: 'g'}, 
				{name: '花甲', id: 18, selected: false, quantity: 1, unit: 'g'}, 
				{name: '火腿', id: 19, selected: false, quantity: 1, unit: 'g'}, 
				{name: '鸡翅', id: 20, selected: false, quantity: 1, unit: 'g'}, 
				{name: '鸡蛋', id: 21, selected: false, quantity: 1, unit: 'g'}, 
				{name: '鸡肉', id: 22, selected: false, quantity: 1, unit: 'g'}, 
				{name: '鸡腿', id: 23, selected: false, quantity: 1, unit: 'g'}, 
				{name: '鸡胸肉', id: 24, selected: false, quantity: 1, unit: 'g'}, 
				{name: '腊肉', id: 25, selected: false, quantity: 1, unit: 'g'}, 
				{name: '牛腩', id: 26, selected: false, quantity: 1, unit: 'g'}, 
				{name: '牛肉', id: 27, selected: false, quantity: 1, unit: 'g'}, 
				{name: '螃蟹', id: 28, selected: false, quantity: 1, unit: 'g'},
				{name: '三文鱼', id: 29, selected: false, quantity: 1, unit: 'g'}, 
				{name: '扇贝', id: 30, selected: false, quantity: 1, unit: 'g'}, 
				{name: '虾', id: 31, selected: false, quantity: 1, unit: 'g'}, 
				{name: '香肠', id: 32, selected: false, quantity: 1, unit: 'g'},
				{name: '鸭肉', id: 33, selected: false, quantity: 1, unit: 'g'}, 
				{name: '羊肉', id: 34, selected: false, quantity: 1, unit: 'g'}, 
				{name: '鱿鱼', id: 35, selected: false, quantity: 1, unit: 'g'}, 
				{name: '鱼', id: 36, selected: false, quantity: 1, unit: 'g'},
				{name: '猪肉', id: 37, selected: false, quantity: 1, unit: 'g'}
			],
			2: [
				{name: '白菜', id: 38, selected: false, quantity: 1, unit: 'g'}, 
				{name: '菠菜', id: 39, selected: false, quantity: 1, unit: 'g'}, 
				{name: '彩椒', id: 40, selected: false, quantity: 1, unit: 'g'}, 
				{name: '番茄', id: 41, selected: false, quantity: 1, unit: 'g'}, 
				{name: '花椰菜', id: 42, selected: false, quantity: 1, unit: 'g'}, 
				{name: '黄瓜', id: 43, selected: false, quantity: 1, unit: 'g'}, 
				{name: '韭菜', id: 44, selected: false, quantity: 1, unit: 'g'}, 
				{name: '苦瓜', id: 45, selected: false, quantity: 1, unit: 'g'}, 
				{name: '辣椒', id: 46, selected: false, quantity: 1, unit: 'g'}, 
				{name: '莲藕', id: 47, selected: false, quantity: 1, unit: 'g'}, 
				{name: '毛豆', id: 48, selected: false, quantity: 1, unit: 'g'}, 
				{name: '南瓜', id: 49, selected: false, quantity: 1, unit: 'g'},
				{name: '茄子', id: 50, selected: false, quantity: 1, unit: 'g'}, 
				{name: '芹菜', id: 51, selected: false, quantity: 1, unit: 'g'}, 
				{name: '青椒', id: 52, selected: false, quantity: 1, unit: 'g'}, 
				{name: '娃娃菜', id: 53, selected: false, quantity: 1, unit: 'g'},
				{name: '豌豆', id: 54, selected: false, quantity: 1, unit: 'g'}, 
				{name: '西兰花', id: 55, selected: false, quantity: 1, unit: 'g'}, 
				{name: '洋葱', id: 56, selected: false, quantity: 1, unit: 'g'}, 
				{name: '紫甘蓝', id: 57, selected: false, quantity: 1, unit: 'g'}
			],
			3: [
				{name: '草莓', id: 58, selected: false, quantity: 1, unit: 'g'}, 
				{name: '橘子', id: 59, selected: false, quantity: 1, unit: 'g'}, 
				{name: '苹果', id: 60, selected: false, quantity: 1, unit: 'g'}, 
				{name: '桃子', id: 61, selected: false, quantity: 1, unit: 'g'}, 
				{name: '芒果', id: 62, selected: false, quantity: 1, unit: 'g'}, 
				{name: '香蕉', id: 63, selected: false, quantity: 1, unit: 'g'}, 
				{name: '西瓜', id: 64, selected: false, quantity: 1, unit: 'g'}, 
				{name: '椰子', id: 65, selected: false, quantity: 1, unit: 'g'}, 
				{name: '柠檬', id: 66, selected: false, quantity: 1, unit: 'g'}, 
				{name: '蓝莓', id: 67, selected: false, quantity: 1, unit: 'g'}, 
				{name: '菠萝', id: 68, selected: false, quantity: 1, unit: 'g'}, 
				{name: '哈密瓜', id: 69, selected: false, quantity: 1, unit: 'g'},
				{name: '火龙果', id: 70, selected: false, quantity: 1, unit: 'g'}, 
				{name: '橙子', id: 71, selected: false, quantity: 1, unit: 'g'}, 
				{name: '木瓜', id: 72, selected: false, quantity: 1, unit: 'g'}, 
				{name: '牛油果', id: 73, selected: false, quantity: 1, unit: 'g'}
			],
			4: [
				{name: '黄豆', id: 74, selected: false, quantity: 1, unit: 'g'}, 
				{name: '黑豆', id: 75, selected: false, quantity: 1, unit: 'g'}, 
				{name: '红豆', id: 76, selected: false, quantity: 1, unit: 'g'}, 
				{name: '绿豆', id: 77, selected: false, quantity: 1, unit: 'g'}, 
				{name: '豆腐', id: 78, selected: false, quantity: 1, unit: 'g'}, 
				{name: '豆皮', id: 79, selected: false, quantity: 1, unit: 'g'}, 
				{name: '豆干', id: 80, selected: false, quantity: 1, unit: 'g'}, 
				{name: '腐竹', id: 81, selected: false, quantity: 1, unit: 'g'}, 
				{name: '蚕豆', id: 82, selected: false, quantity: 1, unit: 'g'}, 
				{name: '豌豆', id: 83, selected: false, quantity: 1, unit: 'g'}, 
				{name: '赤小豆', id: 84, selected: false, quantity: 1, unit: 'g'}, 
				{name: '芸豆', id: 85, selected: false, quantity: 1, unit: 'g'},
				{name: '油豆腐', id: 86, selected: false, quantity: 1, unit: 'g'}, 
				{name: '豆花', id: 87, selected: false, quantity: 1, unit: 'g'}, 
				{name: '鹰嘴豆', id: 88, selected: false, quantity: 1, unit: 'g'}, 
				{name: '腐乳', id: 89, selected: false, quantity: 1, unit: 'g'}	
			]
		}
  },

	lifetimes: {
		attached() {
			this.updateShowList()
			this.syncSelectedIngredients()
		}
	},

	observers: {
		// 监听选中食材列表的变化
		'selectedIngredientsList': function(newList) {
			this.syncSelectedIngredients(newList)
		}
	},

	methods: {
		// 同步选中的食材
		syncSelectedIngredients(selectedList) {
			const listToSync = selectedList || this.data.selectedIngredientsList || []
			const allIngredientsData = { ...this.data.allIngredientsData }
			
			// 先全部取消选中
			Object.keys(allIngredientsData).forEach(category => {
				allIngredientsData[category] = allIngredientsData[category].map(item => ({
					...item,
					selected: false
				}))
			})
			
			// 根据传入的列表标记选中的食材
			listToSync.forEach(selectedItem => {
				let found = false
				Object.keys(allIngredientsData).forEach(category => {
					const itemIndex = allIngredientsData[category].findIndex(item => 
						item.name === selectedItem.name
					)
					if (itemIndex !== -1) {
						allIngredientsData[category][itemIndex].selected = true
						allIngredientsData[category][itemIndex].quantity = selectedItem.quantity
						found = true
					}
				})
			})
			
			this.setData({ allIngredientsData })
			this.updateShowList()
		},

		onSwitchTab(e) {
			const currentTab = e.currentTarget.dataset.index
			this.setData({ currentTab })
			this.updateShowList()
		},

		onSelectedIngredients(e) {
			const index = e.currentTarget.dataset.index
			const list = [...this.data.ingredientsData]
			const ingredient = list[index]
			const isSelected = !list[index].selected
			list[index].selected = isSelected

			const allIngredientsData = { ...this.data.allIngredientsData }
			allIngredientsData[this.data.currentTab] = list

			this.setData({ 
				ingredientsData: list,
				allIngredientsData
			})

			this.sendSelectedIngredients(isSelected ? ingredient : null)
		},

		sendSelectedIngredients(changedIngredient) {
			const allList = Object.values(this.data.allIngredientsData).flat()
			const selectedIngredients = allList
				.filter(item => item.selected)
				.map(item => ({
					name: item.name
				}))

			this.triggerEvent('ingredientsChange', { 
				selectedIngredients,
				changedIngredient: changedIngredient ? {
					name: changedIngredient.name
				} : null
			})
		},
		
		updateShowList() {
			const list = this.data.allIngredientsData[this.data.currentTab]
			this.setData({ingredientsData: list})
		}
	}
})
