<template>
  <div class="jp-dialog-wrap" v-if="dialogVisible">
    <div :class="[theme ? 'jp-dialog-container-' + theme : '', 'jp-dialog-container']">
      <div class="jp-dialog-header" v-if="false">
        <i class="el-icon-circle-close" @click="handleClose"></i>
      </div>
      <div class="jp-dialog-body">
        <div class="jp-dialog-body-wrap">
          <div :class="[theme ? 'jp-dialog-body-img-' + theme : 'jp-dialog-body-img']"></div>
          <div class="jp-dialog-body-title">
            <span v-if="title">{{ title }}</span>
            <slot v-else name="title"></slot>
          </div>
        </div>
      </div>
      <template>
        <div v-if="content" class="dialog-content-box">
          <span class="jp-dialog-content" v-html="content"></span>
        </div>
        <slot v-else name="content"></slot>
      </template>
      <div class="jp-dialog-footer">
        <slot name="footer"></slot>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'JPDialog',
  props: {
    dialogVisible: {
      type: Boolean,
      default: () => {
        return false;
      }
    },
    theme: {
      type: String,
      default: () => {
        return '';
      }
    },
    title: {
      type: String,
      default: () => {
        return '';
      }
    },
    content: {
      type: String,
      default: () => {
        return '';
      }
    }
  },
  methods: {
    handleClose() {
      this.$emit('update:dialogVisible', false);
    }
  }
};
</script>

<style lang="stylus">
.jp-dialog-wrap {
  z-index: 2000;
  position: fixed;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.4);
  display: flex;
  align-content: center;
  justify-content: space-evenly;
  .jp-dialog-container {
    margin-top: 20vh;
    width: 460px;
    height: 200px;
    border: 1px solid black;
    border-radius: 8px;
    box-sizing: border-box;
    box-shadow: 0px 0px 16px rgba(25, 25, 26, 0.2);
    background: #ffffff;
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    .jp-dialog-header {
      display: flex;
      .el-icon-circle-close {
        margin-right: 10px;
        margin-top: 10px;
        text-align: right;
        width: 100%;
        opacity: 0.35;
        &:hover {
          opacity: 0.8;
        }
      }
    }
    .jp-dialog-body {
      display: flex;
      justify-content: center;
      align-items: center;
      .jp-dialog-body-wrap {
        height: 60px;
        width: 100%;
        display: flex;
        align-content: center;
      }
      .jp-dialog-body-img {
        margin-left: 70px;
        width: 36px;
        height: 36px;
      }
      .jp-dialog-body-title {
        display: block;
        margin: 10px 20px;
        font-size: 20px;
      }
    }
    .dialog-content-box {
      display: flex;
      flex-direction: column;
      width: 320px;
      margin: 0 70px;
      text-align: left;
      line-height: 18px;
      font-size: 14px;
      .dialog-content {
        line-height: 18px;
        font-size: 14px;
        color: #616166;
      }
    }
    .jp-dialog-footer {
      display: flex;
      height: 50px;
      margin: 10px 20px;
      justify-content: flex-end;
      align-items: center;
    }
  }
  .jp-dialog-container-success {
    border: 1px solid #29c30e;
    .jp-dialog-body {
      .jp-dialog-body-img-success {
        margin-left: 70px;
        width: 36px;
        height: 36px;
        background: url('../../assets/images/public/jp-ui/success-icon.png') center no-repeat;
        background-size: 100%;
      }
    }
  }
  .jp-dialog-container-warning {
    border: 1px solid #ff9808;
    .jp-dialog-body {
      .jp-dialog-body-img-warning {
        margin-left: 70px;
        width: 36px;
        height: 36px;
        background: url('../../assets/images/public/jp-ui/warning-icon.png') center no-repeat;
        background-size: 100%;
      }
    }
  }
  .jp-dialog-container-danger {
    border: 1px solid #ff0000;
    .jp-dialog-body {
      .jp-dialog-body-img-danger {
        margin-left: 70px;
        width: 36px;
        height: 36px;
        background: url('../../assets/images/public/jp-ui/danger-icon.png') center no-repeat;
        background-size: 100%;
      }
    }
  }
}
</style>
