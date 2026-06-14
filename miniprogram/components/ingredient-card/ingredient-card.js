// components/ingredient-card/ingredient-card.js
const { getIngredientCover } = require('../../config/ingredient-images')

Component({
  /**
   * 组件的属性列表
   */
  properties: {
    item: {
      type: Object,
      value: {}
    }
  },

  lifetimes: {
    attached() {
      this.computeStatus()
      this.computeCover()
    }
  },

  observers: {
    'item.daysLeft': function () {
      this.computeStatus()
    },
    'item.readed': function () {
      this.computeStatus()
    },
    'item.name': function (name) {
      this.computeCover(name)
    }
  },

  /**
   * 组件的方法列表
   */
  methods: {
    // daysLeft → CSS 类名 red/yellow/green + 状态标签文本
    // readed + _labelShown → 控制标签"粘性"（本次会话显示后保持，下次进入才消失）
    computeStatus() {
      const days = this.data.item.daysLeft
      let statusClass = 'green'
      let statusLabelText = ''
      let showStatusLabel = false

      if (days <= 0) {
        statusClass = 'red'
        statusLabelText = '已过期'
      } else if (days <= 15) {
        statusClass = days <= 3 ? 'red' : 'yellow'
        statusLabelText = '已转灯'
      }

      // 标签显示逻辑：新通知显示 + 本次会话粘性保持
      if (statusLabelText) {
        if (!this.data.item.readed) {
          // 新通知 → 显示并锁定本会话
          showStatusLabel = true
          this._labelShown = true
        } else if (this._labelShown) {
          // 本会话已显示过 → 继续显示（即使 readed 已变 true）
          showStatusLabel = true
        }
        // readed=true 且未在本会话显示过 → 不显示
      }

      this.setData({ statusClass, statusLabelText, showStatusLabel })
    },

    // 根据食材名称匹配封面图
    computeCover(name) {
      const cover = getIngredientCover(name || this.data.item.name)
      this.setData({ cover })
    },

    onTap() {
      this.triggerEvent('tap', { item: this.data.item })
    },
    onDelete() {
      this.triggerEvent('delete', { id: this.data.item.id })
    }
  }
})
