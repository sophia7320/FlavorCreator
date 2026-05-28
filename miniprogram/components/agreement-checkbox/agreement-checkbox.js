Component({
    properties: {
        defaultChecked: {
            type: Boolean,
            value: false
        }
    },

    lifetimes: {
        attached() {
            this.setData({ isAgree: this.properties.defaultChecked });
        }
    },

    data: {
        isAgree: false,
        isShake: false,
        isShaking: false
    },

    methods: {
        agreementTick(e) {
            const ischeck = e.detail.value.length > 0;
            this.setData({ isAgree: ischeck });
            this.triggerEvent('agreechange', { isAgree: ischeck });
        },

        goAgreement() {
            this.triggerEvent('showagreement', { type: 'agreement' });
        },

        goPrivacy() {
            this.triggerEvent('showagreement', { type: 'privacy' });
        },

        isAgreed() {
            return this.data.isAgree;
        },

        triggerShake() {
            if (this.data.isShaking) return;
            this.setData({
                isShake: true,
                isShaking: true
            });
            setTimeout(() => {
                this.setData({
                    isShake: false,
                    isShaking: false
                });
            }, 1000);
        }
    }
});
