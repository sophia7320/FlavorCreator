// components/AI-btn/AI-btn.js
Component({
  data: {
    selected: false
  },

  methods: {
    onTap() {
      const newSelected = !this.data.selected
      this.setData({ selected: newSelected })
      this.triggerEvent('aitap', { selected: newSelected })
    }
  }
})