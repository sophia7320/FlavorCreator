// pages/ingredients-storage/storage.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

const DEFAULT_COVER = 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/bg/top-bar-dish-bg.png'

Page({

  /**
   * 页面的初始数据
   */
  data: {
    ready: false,
    currentIndex: 0,
    selectedTop: 0,
    typeList: [
      { name: '全部', redPoint: false },
      { name: '谷薯类', redPoint: false },
      { name: '肉蛋类', redPoint: false },
      { name: '蔬菜类', redPoint: false },
      { name: '水果类', redPoint: false },
      { name: '豆类', redPoint: false },
      { name: '调味品', redPoint: false },
      { name: '其他', redPoint: false },
    ],
    ingredientList: [],
    allIngredients: [],
    currentCategory: '全部',
    sortBy: 'createTime',
    sortOrder: 'desc',
    keyword: '',
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    const windowInfo = wx.getWindowInfo();
    const rpxRatio = windowInfo.windowWidth / 750;
    const estimatedTop = 35.5 * rpxRatio;
    this.setData({
      selectedTop: estimatedTop,
      ready: true
    });
    this.fetchIngredients();
  },

  /**
   * 生命周期函数--监听页面初次渲染完成
   */
  onReady() {
    this.calcSelectedTop(0);
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    this.fetchIngredients();
  },

  // 获取食材列表
  async fetchIngredients() {
    try {
      const data = await request(API_CONFIG.ingredient.list, {
        sortBy: this.data.sortBy,
        sort: this.data.sortOrder
      }, { showLoading: false });

      const ingredients = (data.ingredients || []).map(item => ({
        ...item,
        cover: item.cover || DEFAULT_COVER,
        amount: item.quantity,
      }));

      this.setData({ allIngredients: ingredients });
      this.updateRedPoints(ingredients, this.data.typeList);
      this.filterByCategory();
    } catch (err) {
      console.error('获取食材列表失败', err);
    }
  },

  // 更新分类红点（根据食材状态判断）
  updateRedPoints(ingredients, typeList) {
    // 找出有未读临期/过期食材的分类
    // status: 0=已过期, 1=红灯(≤3天), 2=黄灯(≤15天), 3=绿灯(>15天)
    const problemCategories = new Set();
    ingredients.forEach(item => {
      if (item.category && item.daysLeft <= 15 && !item.readed) {
        problemCategories.add(item.category);
      }
    });

    const updatedList = typeList.map(cat => ({
      ...cat,
      // "全部"分类：只要有任何分类有红点，就亮红点
      redPoint: cat.name === '全部'
        ? problemCategories.size > 0
        : problemCategories.has(cat.name)
    }));

    this.setData({ typeList: updatedList });
  },

  // 按分类筛选
  filterByCategory() {
    const { allIngredients, currentCategory, keyword } = this.data;

    let filtered = currentCategory === '全部'
      ? allIngredients
      : allIngredients.filter(item => item.category === currentCategory);

    // 关键字搜索
    if (keyword) {
      filtered = filtered.filter(item => item.name.includes(keyword));
    }

    this.setData({ ingredientList: filtered });
  },

  // 点击分类项
  onTypeTap(e) {
    const index = e.currentTarget.dataset.index;
    const category = this.data.typeList[index];

    // 如果当前分类有红点，先标记已读
    if (category.redPoint) {
      this.markCategoryAsRead(category.name);
    }

    this.setData({
      currentIndex: index,
      currentCategory: category.name,
      keyword: '',
    });
    this.calcSelectedTop(index);
    this.filterByCategory();
  },

  // 标记某个分类下的异常食材为已读
  async markCategoryAsRead(categoryName) {
    const { allIngredients } = this.data;

    // 找出该分类下需要标记的食材 ID
    const targetIds = allIngredients
      .filter(item => {
        const matchCategory = categoryName === '全部' || item.category === categoryName;
        return matchCategory && item.daysLeft <= 15 && !item.readed;
      })
      .map(item => item.id);

    if (targetIds.length === 0) return;

    try {
      await request(API_CONFIG.ingredient.batchRead, targetIds, { showLoading: false });

      // 本地更新 allIngredients 的 readed 状态
      const updatedIngredients = allIngredients.map(item =>
        targetIds.includes(item.id) ? { ...item, readed: true } : item
      );

      // 本地更新 typeList 红点状态
      const typeList = this.data.typeList.map(cat => {
        if (categoryName === '全部') {
          // 点击"全部" → 清除所有分类的红点
          return { ...cat, redPoint: false };
        }
        if (cat.name === categoryName) {
          // 清除该分类的红点
          return { ...cat, redPoint: false };
        }
        if (cat.name === '全部') {
          // 重新判断"全部"是否还有其他分类有红点
          const hasOther = updatedIngredients.some(
            item =>
              item.category &&
              item.category !== categoryName &&
              item.daysLeft <= 15 &&
              !item.readed
          );
          return { ...cat, redPoint: hasOther };
        }
        return cat;
      });

      this.setData({ allIngredients: updatedIngredients, typeList });
    } catch (err) {
      console.error('标记已读失败', err);
    }
  },

  // 计算 selected-background 的 top 值
  calcSelectedTop(index) {
    const query = wx.createSelectorQuery();
    query.select('.left-container').boundingClientRect();
    query.selectAll('.left-container .item').boundingClientRect();
    query.exec((res) => {
      const containerRect = res[0];
      const items = res[1];
      if (containerRect && items && items[index]) {
        const itemCenter = items[index].top - containerRect.top + items[index].height / 2;
        const windowInfo = wx.getWindowInfo();
        const rpxRatio = windowInfo.windowWidth / 750;
        const bgHalfHeight = (119 * rpxRatio) / 2;
        const top = itemCenter - bgHalfHeight;
        this.setData({ selectedTop: top, ready: true });
      }
    });
  },

  // 搜索输入
  onSearchInput(e) {
    const keyword = e.detail.value.trim();
    this.setData({ keyword });
    this.filterByCategory();
  },

  // 排序切换（按食材提醒时间正倒序）
  onSort() {
    const currentOrder = this.data.sortOrder;
    const nextOrder = currentOrder === 'asc' ? 'desc' : 'asc';
    const label = nextOrder === 'asc' ? '即将过期优先' : '保质期最长优先';

    this.setData({
      sortBy: 'expireDate',
      sortOrder: nextOrder
    });

    wx.showToast({ title: label, icon: 'none', duration: 1000 });
    this.fetchIngredients();
  },

  // 点击食材卡片 → 编辑
  onIngredientTap(e) {
    const item = e.detail.item
    if (!item) return
    const params = this.buildItemParams(item)
    wx.navigateTo({
      url: '/pages/add-ingredients/add?' + params
    })
  },

  // 删除食材
  async onDelete(e) {
    const id = e.detail.id;
    if (!id) return;

    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个食材吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            const deleteConfig = {
              ...API_CONFIG.ingredient.delete,
              path: API_CONFIG.ingredient.delete.path.replace('{id}', id)
            };
            await request(deleteConfig, {}, { showLoading: true });
            wx.showToast({ title: '删除成功', icon: 'success' });
            this.fetchIngredients();
          } catch (err) {
            console.error('删除失败', err);
          }
        }
      }
    });
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

  },

	buildItemParams(item) {
		const parts = []
		if (item.id) parts.push('id=' + encodeURIComponent(item.id))
		if (item.name) parts.push('name=' + encodeURIComponent(item.name))
		if (item.quantity != null) parts.push('quantity=' + encodeURIComponent(item.quantity))
		if (item.unit) parts.push('unit=' + encodeURIComponent(item.unit))
		if (item.category) parts.push('category=' + encodeURIComponent(item.category))
		if (item.expireDate) parts.push('expireDate=' + encodeURIComponent(item.expireDate))
		if (item.createdAt) parts.push('createdAt=' + encodeURIComponent(item.createdAt))
		return parts.join('&')
	},

	onMore() {
		wx.navigateTo({
			url: '/pages/add-ingredients/add'
		})
	}
})