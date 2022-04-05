<template>
  <el-button type="primary" @click='run' :disabled="disabled" :style="btnStyle">
    {{ btn_text }}
  </el-button>
</template>

<script>
export default {
  name: 'CountDown',
  data() {
    return {
      currCount: 0,
      disabled: false
    };
  },
  props: {
    count: { //倒数的初始值，默认60
      type: Number,
      default: 60
    },
    interval: { //倒数间隔，默认是1000毫秒
      type: Number,
      default: 1000
    },
    text: { //文本内容
      type: String,
      default: '获取验证码'
    },
    btnStyle: { //样式
      type: String,
    },
    execute: { //组件被点击后需要执行的函数
      required: true,
      type: Function,
    },
    onEnd: { //倒计时结束时的回调函数
      type: Function,
    }
  },
  computed: {
    btn_text: function() {
      return this.currCount > 0 ? this.currCount + '秒后重新获取' : this.text;
    }
  },
  methods: {
    run: function() {
      if (!this.disabled) {
        this.disabled = true
        this.currCount = this.count
        this.countDown()
        this.execute()
      }
    },
    countDown: function() {
      if (this.currCount > 0) {
        this.currCount--;
        setTimeout(this.countDown, this.interval);
      } else {
        this.disabled = false;
        if (this.onEnd) {
          this.onEnd() //执行回调函数
        }
      }
    },
  },
};
</script>

<style scoped>
</style>
