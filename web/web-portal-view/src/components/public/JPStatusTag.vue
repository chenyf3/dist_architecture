<template>
  <div class="jp-status-tag-wrap">
    <span v-if="color" class="color" :style="{ background: tagColor(color) }"></span>
    <!-- <div v-if="imgUrl" class="jp-status-tag-img" :style="{ backgroundImage: 'url(' + imgUrl + ')' }"></div> -->
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: 'JPStatusTag',
  props: {
    // Tag 大小
    color: {
      type: String,
      default: () => {
        return '';
      }
    },
    // 图片链接
    imgUrl: {
      type: String,
      default: () => {
        return '';
      }
    }
  },
  methods: {
    tagColor: color => {
      switch (color) {
        case 'danger':
          return '#FF0000';
        case 'success':
          return '#29C30E';
        case 'primary':
          return '#0D89FC';
        case 'warning':
          return '#FFC20D';
        case 'info':
          return '#616166';
      }
    }
  }
};
</script>

<style lang="stylus">
.jp-status-tag-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  .jp-status-tag-img {
    width: 24px;
    height: 24px;
    background-size: 100%;
    background-repeat: no-repeat;
  }
  .color {
    margin-right: 6px;
    width: 8px;
    height: 8px;
    border-radius: 5px;
  }
}
</style>
