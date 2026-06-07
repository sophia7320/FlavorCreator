// pages/community/community.js
const { API_CONFIG } = require('../../config/api')
const { request } = require('../../utils/request')

// 标签名 → API category 参数映射
const TAG_CATEGORY_MAP = {
  '快手菜': 'fast',
  '减脂餐': 'lowcal',
  '家常菜': 'home',
  '养生频道': 'health',
  '特色菜': 'special'
}

Page({

  /**
   * 页面的初始数据
   */
  data: {
		tags: [
			{name: '快手菜', selected: true},
			{name: '减脂餐', selected: false},
			{name: '家常菜', selected: false},
			{name: '养生频道', selected: false},
			{name: '特色菜', selected: false},
		],
		leftList: [
			{ id: 'ph_l0', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', likeCount: 0, isPlaceholder: true },
			{ id: 'ph_l1', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', likeCount: 0, isPlaceholder: true },
			{ id: 'ph_l2', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', likeCount: 0, isPlaceholder: true },
			{ id: 'ph_l3', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', likeCount: 0, isPlaceholder: true },
		],
		rightList: [
			{ id: 'ph_r0', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', likeCount: 0, isPlaceholder: true },
			{ id: 'ph_r1', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', likeCount: 0, isPlaceholder: true },
			{ id: 'ph_r2', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', likeCount: 0, isPlaceholder: true },
			{ id: 'ph_r3', authorId: '', userName: 'User Name', userImg: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg', recipeName: 'Addtional info goes here', recipeImage: 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg', likeCount: 0, isPlaceholder: true },
		],
		page: 1,
		pageSize: 10,
		loadingStatus: '',
		isRequesting: false,
		hasMore: true,
		currentShareCardId: null,
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    this.loadData(true)
  },

  onReady() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().onTabPageShow()
    }
  },

  onShow() {
    if (typeof this.getTabBar === 'function' && this.getTabBar()) {
      this.getTabBar().onTabPageShow()
    }
    // 同步收藏状态
    this.syncLikedStatus()
  },

  onHide() {

  },

  onUnload() {

  },

  onPullDownRefresh() {
    this.loadData(true)
  },

  onReachBottom() {
    this.loadData(false)
  },

  loadData(isRefresh = false) {
    if (this.data.isRequesting) {
      return
    }
    if (!isRefresh && !this.data.hasMore) {
      return
    }

    this.setData({ isRequesting: true, loadingStatus: 'loading' })

    const page = isRefresh ? 1 : this.data.page
    const selectedTag = this.data.tags.find(t => t.selected)
    const category = selectedTag ? TAG_CATEGORY_MAP[selectedTag.name] : 'fast'

    request(API_CONFIG.community.list, { page, size: this.data.pageSize, category }, { showLoading: false })
      .then(res => {
        const list = res.list || res.data || []
        const hasMore = res.hasMore ?? (list.length >= this.data.pageSize)

        this.appendCards(list, isRefresh)

        this.setData({
          page: isRefresh ? 2 : page + 1,
          hasMore,
          loadingStatus: hasMore ? '' : 'noMore',
          isRequesting: false
        })
      })
      .catch(() => {
        this.setData({ loadingStatus: 'error', isRequesting: false })
      })
  },

  appendCards(cards, isRefresh = false) {
    let newLeftList = isRefresh ? [] : [...this.data.leftList]
    let newRightList = isRefresh ? [] : [...this.data.rightList]

    cards.forEach((item, index) => {
      const card = {
        id: item.id || item._id || Date.now() + index,
        authorId: item.author?.id || item.authorId || '',
        userName: item.author?.nickname || item.userName || item.user_name || '匿名用户',
        userImg: item.author?.avatar || item.userImg || item.user_avatar || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/user.svg',
        recipeName: item.name || item.recipeName || item.title || '',
        recipeImage: item.cover || item.recipeImage || item.image || 'https://miniprogram-img-1422554268.cos.ap-guangzhou.myqcloud.com/icon/community/image.svg',
        likeCount: item.likeCount || 0
      }

      const totalCount = newLeftList.length + newRightList.length
      if (totalCount % 2 === 0) {
        newLeftList.push(card)
      } else {
        newRightList.push(card)
      }
    })

    // 同步收藏状态
    const favorites = wx.getStorageSync('favorites') || []
    const likedIds = new Set(favorites.map(f => f.id))
    newLeftList = newLeftList.map(c => ({ ...c, isLiked: likedIds.has(c.id) }))
    newRightList = newRightList.map(c => ({ ...c, isLiked: likedIds.has(c.id) }))

    this.setData({ leftList: newLeftList, rightList: newRightList })
  },

  /**
   * 同步所有卡片的收藏状态
   */
  syncLikedStatus() {
    const favorites = wx.getStorageSync('favorites') || []
    const likedIds = new Set(favorites.map(f => f.id))
    this.setData({
      leftList: this.data.leftList.map(c => ({ ...c, isLiked: likedIds.has(c.id) })),
      rightList: this.data.rightList.map(c => ({ ...c, isLiked: likedIds.has(c.id) }))
    })
  },

  onScrollToLower() {
    this.loadData(false)
  },

  onSwitchTag(e) {
    const index = parseInt(e.currentTarget.dataset.index)
    const currentTag = this.data.tags.find(t => t.selected)

    // 点击当前已选中的标签，不重复请求
    if (currentTag && this.data.tags.indexOf(currentTag) === index) return

    const tags = this.data.tags.map((t, i) => ({
      ...t,
      selected: i === index
    }))

    this.setData({
      tags,
      page: 1,
      leftList: [],
      rightList: [],
      hasMore: true
    }, () => {
      this.loadData(true)
    })
  },

  onLike(e) {
    const { cardId } = e.detail

    // 先判断当前收藏状态，决定调用收藏还是取消收藏 API
    let favorites = wx.getStorageSync('favorites') || []
    const index = favorites.findIndex(f => f.id === cardId)
    const isCurrentlyFavorited = index > -1

    const apiConfig = isCurrentlyFavorited
      ? API_CONFIG.community.uncollect
      : API_CONFIG.community.collect
    const api = { ...apiConfig, path: apiConfig.path.replace('{id}', cardId) }

    request(api, {}, { showLoading: false })
      .then(() => {
        // 重新读取本地收藏列表（防止并发问题）
        favorites = wx.getStorageSync('favorites') || []
        const idx = favorites.findIndex(f => f.id === cardId)

        if (idx > -1) {
          // 已收藏 → 取消收藏
          favorites.splice(idx, 1)
          wx.showToast({ title: '已取消收藏', icon: 'none' })
        } else {
          // 未收藏 → 添加收藏
          const card = [...this.data.leftList, ...this.data.rightList].find(c => c.id === cardId)
          if (!card) return
          favorites.unshift({ ...card, collectedAt: Date.now() })
          wx.showToast({ title: '已收藏', icon: 'success' })
        }

        // 保存到本地
        wx.setStorageSync('favorites', favorites)

        // 更新列表中对应卡片的收藏状态
        const cardIdSet = new Set(favorites.map(f => f.id))
        const updateLiked = (list) => list.map(c => ({ ...c, isLiked: cardIdSet.has(c.id) }))

        this.setData({
          leftList: updateLiked(this.data.leftList),
          rightList: updateLiked(this.data.rightList)
        })
      })
      .catch(() => {
        wx.showToast({ title: '操作失败', icon: 'none' })
      })
  },

  onShare(e) {
    const { cardId } = e.detail
    this.setData({ currentShareCardId: cardId })
  },

  onCardTap(e) {
    const { cardId } = e.detail
    wx.navigateTo({
      url: `/pages/detail/detail?id=${cardId}`
    })
  },

  onUserTap(e) {
    const { authorId, userName, userImg } = e.detail
    if (!authorId) return
    wx.navigateTo({
      url: `/pages/user/user?id=${authorId}&name=${encodeURIComponent(userName || '')}&avatar=${encodeURIComponent(userImg || '')}`
    })
  },

  /**
   * 用户点击右上角分享 / 转发
   */
  onShareAppMessage() {
    const cardId = this.data.currentShareCardId
    const card = [...this.data.leftList, ...this.data.rightList].find(c => c.id === cardId)

    return {
      title: card ? card.recipeName : '看看这道菜谱',
      path: `/pages/detail/detail?id=${cardId || ''}`,
      imageUrl: card ? card.recipeImage : ''
    }
  }
})
