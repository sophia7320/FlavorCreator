// components/recipe-card/recipe-card.js
Component({
  /**
   * 组件的属性列表
   */
  properties: {
    cardData: {
      type: Object,
      value: {}
    },
    isFirst: {
      type: Boolean,
      value: false
    },
    isCollected: {
      type: Boolean,
      value: false
    }
  },

  /**
   * 组件的方法列表
   */
  methods: {
    onCardTap() {
      if (this.data.cardData.isPlaceholder) return
      this.triggerEvent('cardtap', { cardId: this.data.cardData.id })
    },
    onUserTap() {
      if (this.data.cardData.isPlaceholder) return
      const { authorId, userName, userImg } = this.data.cardData
      this.triggerEvent('usertap', { authorId, userName, userImg })
    },
    onCollectTap() {
      if (this.data.cardData.isPlaceholder) return
      this.triggerEvent('collecttap', { cardId: this.data.cardData.id })
    },
    onShareTap() {
      if (this.data.cardData.isPlaceholder) return
      this.triggerEvent('sharetap', { cardId: this.data.cardData.id })
    }
  }
})
