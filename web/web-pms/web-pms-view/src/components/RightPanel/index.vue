<template>
    <div ref="rightPanel" class="rightPanel-container" :class="{rightPanelShow:show}">
        <div class="rightPanel-background"/>
        <div class="rightPanel" @mouseenter="mouseEnter" @mouseleave="mouseLeave">
            <div class="handle-button" :style="{'top':buttonTop+'px','background-color':theme, 'opacity':buttonOpacity}"
                 @click="show=!show">
            </div>
            <div class="rightPanel-items">
                <slot/>
            </div>
        </div>
    </div>
</template>

<script>
export default {
    name: 'RightPanel',
    props: {
        clickNotClose: {
            default: false,
            type: Boolean
        },
        buttonTop: {
            default: 250,
            type: Number
        }
    },
    data() {
        return {
            show: false,
            delayLoadPanel: undefined,
            buttonOpacity: 0.2
        }
    },
    computed: {
        theme() {
            return this.$store.state.settings.theme
        }
    },
    watch: {
        show(value) {
            if (value && !this.clickNotClose) {
                this.addEventClick()
            }
        }
    },
    mounted() {
        this.insertToBody()
    },
    beforeDestroy() {
        const elx = this.$refs.rightPanel
        elx.remove()
    },
    methods: {
        addEventClick() {
            // window.addEventListener('click', this.closeSidebar)
        },
        closeSidebar(evt) {
            const parent = evt.target.closest('.rightPanel')
            if (!parent) {
                this.show = false
                window.removeEventListener('click', this.closeSidebar)
            }
        },
        insertToBody() {
            const elx = this.$refs.rightPanel
            const body = document.querySelector('body')
            body.insertBefore(elx, body.firstChild)
        },
        mouseEnter(){
            this.buttonOpacity = 1
            this.delayLoadPanel = setTimeout(() => {
                this.show = true
            }, 700)
        },
        mouseLeave(){
            this.show = false
            this.buttonOpacity = 0.2
            if(this.delayLoadPanel){
                clearTimeout(this.delayLoadPanel)
                this.delayLoadPanel = undefined
            }
        },
    }
}
</script>
<style lang="scss" scoped>
.rightPanel-container {
    overflow: hidden;
    position: relative;
    width: calc(100% - 15px);

    /* 实现效果：默认情况下，右边面板没有被弹出，背景框隐藏起来 */
    .rightPanel-background {
        position: fixed;
        top: 0;
        left: 0;
        opacity: 0;
        transition: opacity .3s cubic-bezier(.7, .3, .1, 1);
        background: rgba(0, 0, 0, .2);
        z-index: -1;
    }

    .rightPanel {
        width: 100%;
        max-width: 260px;
        height: 100vh;
        position: fixed;
        top: 0;
        right: 0;
        box-shadow: 0px 0px 15px 0px rgba(0, 0, 0, .05);
        transition: all .25s cubic-bezier(.7, .3, .1, 1);
        transform: translate(100%);
        background: #fff;
        z-index: 40000;

        .handle-button {
            width: 10px;
            height: 60px;
            line-height: 40px;
            position: absolute;
            left: -10px;
            text-align: center;
            font-size: 24px;
            border-radius: 3px 0 0 3px !important;
            z-index: 0;
            pointer-events: auto;
            cursor: pointer;
            color: #fff;

            i {
                font-size: 24px;
                margin-left: -5px;
            }
        }
    }
}

/* 实现效果：右边面板弹出时，左侧面板的内容会被带有一定透明度的画布遮盖，右边面板则正常悬浮在最上层 */
.rightPanelShow {
    transition: all .3s cubic-bezier(.7, .3, .1, 1);

    .rightPanel-background {
        z-index: 20000;
        opacity: 1;
        width: 100%;
        height: 100%;
    }

    .rightPanel {
        transform: translate(0);
    }
}
</style>
