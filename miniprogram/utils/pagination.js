/**
 * 全局分页 Behavior
 * 封装无限下滑的核心逻辑：请求锁、去重、分页状态机、错误降级
 * 
 * 使用方式：
 *   const paginationBehavior = require('../../utils/pagination')
 *   Page({ behaviors: [paginationBehavior], ... })
 * 
 * 核心方法：
 *   paginationInit(tagKey, pageSize)        - 初始化某个标签的分页状态
 *   paginationLoad(isRefresh, tagKey, fetchFn, transformFn) - 加载数据，返回 Promise<Array>
 *   paginationSwitchTag(newTagKey, pageSize) - 切换标签，自动重置分页状态
 */

module.exports = Behavior({
  methods: {
    /**
     * 初始化某个标签的分页状态
     * @param {string} tagKey    - 标签唯一标识（如 'fast', 'home'）
     * @param {number} pageSize  - 每页数量，默认 10
     */
    paginationInit(tagKey, pageSize = 10) {
      if (!this._paginationState) {
        this._paginationState = {}
      }
      this._paginationState[tagKey] = {
        page: 1,
        pageSize,
        hasMore: true,
        _requesting: false,
        _idSet: new Set()
      }
    },

    /**
     * 加载数据（刷新或追加）
     * @param {boolean}  isRefresh    - true=刷新（重新从第1页加载），false=追加下一页
     * @param {string}   tagKey       - 当前标签唯一标识
     * @param {Function} fetchFn      - 请求函数: (page, size) => Promise<{ list|records|data, hasMore? }>
     * @param {Function} transformFn  - 转换函数: (item) => cardData（返回 null 或 isPlaceholder 的项会被过滤）
     * @returns {Promise<Array>} 返回本次加载的新卡片列表（已去重）
     */
    paginationLoad(isRefresh, tagKey, fetchFn, transformFn) {
      const state = this._paginationState && this._paginationState[tagKey]
      if (!state) {
        console.warn(`[pagination] tagKey "${tagKey}" 未初始化`)
        return Promise.resolve([])
      }

      // 请求锁：防止重复请求
      if (state._requesting) return Promise.resolve([])

      // 非刷新模式下没有更多数据，直接返回
      if (!isRefresh && !state.hasMore) return Promise.resolve([])

      state._requesting = true
      this.setData({ loadingStatus: 'loading' })

      const page = isRefresh ? 1 : state.page

      return fetchFn(page, state.pageSize)
        .then(res => {
          const list = res.list || res.records || res.data || []
          // 修复 hasMore 判断：必须同时满足 >= pageSize 且 > 0，防止最后恰好满页导致多余请求
          const hasMore = res.hasMore ?? (list.length >= state.pageSize && list.length > 0)

          // 数据转换 + 过滤无效项
          const transformed = list
            .map(item => transformFn(item))
            .filter(card => card && !card.isPlaceholder)

          // 去重 + 追加
          let newCards
          if (isRefresh) {
            state._idSet = new Set(transformed.map(c => String(c.id)))
            newCards = transformed
          } else {
            const existingIds = state._idSet
            const uniqueNew = transformed.filter(c => !existingIds.has(String(c.id)))
            uniqueNew.forEach(c => existingIds.add(String(c.id)))
            newCards = uniqueNew
          }

          // 更新分页状态
          state.page = isRefresh ? 2 : page + 1
          state.hasMore = hasMore
          state._requesting = false

          this.setData({
            loadingStatus: hasMore ? '' : 'noMore'
          })

          return newCards
        })
        .catch(err => {
          state._requesting = false
          this.setData({ loadingStatus: 'error' })
          console.error('[pagination] 加载失败:', err)
          return []
        })
    },

    /**
     * 切换标签：重置分页状态
     * @param {string} newTagKey - 新标签唯一标识
     * @param {number} pageSize  - 每页数量，默认 10
     */
    paginationSwitchTag(newTagKey, pageSize = 10) {
      if (!this._paginationState) {
        this._paginationState = {}
      }

      let state = this._paginationState[newTagKey]
      if (!state) {
        this._paginationState[newTagKey] = {
          page: 1,
          pageSize,
          hasMore: true,
          _requesting: false,
          _idSet: new Set()
        }
      } else {
        state.page = 1
        state.hasMore = true
        state._idSet = new Set()
        state._requesting = false
      }

      this.setData({ loadingStatus: '' })
    },

    /**
     * 查询指定标签是否还有更多数据
     * @param {string} tagKey
     * @returns {boolean}
     */
    paginationHasMore(tagKey) {
      const state = this._paginationState && this._paginationState[tagKey]
      return state ? state.hasMore : false
    },

    /**
     * 查询指定标签是否正在请求中
     * @param {string} tagKey
     * @returns {boolean}
     */
    paginationIsRequesting(tagKey) {
      const state = this._paginationState && this._paginationState[tagKey]
      return state ? state._requesting : false
    }
  }
})
