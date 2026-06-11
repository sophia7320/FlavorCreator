// components/ingredient-card/ingredient-card.js
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

  /**
   * 组件的方法列表
   */
  methods: {
    onTap() {
      this.triggerEvent('tap', { item: this.data.item })
    },
    onDelete() {
      this.triggerEvent('delete', { id: this.data.item.id })
    }
  }
})
