<template>
  <div id="app">
    <layout></layout>
  </div>
</template>

<script>
import Layout from '@/components/layout/Layout';
export default {
  name: 'App',
  components: {
    Layout
  }
};
</script>

<style lang="stylus">
@import '../../assets/style/reset.styl';
@import '../../assets/style/common.styl';
// *:not(path):not(g)
//   color:                    hsla(210, 100%, 100%, 0.9) !important;
//   background:               hsla(210, 100%,  50%, 0.5) !important;
//   outline:    solid 0.25rem hsla(210, 100%, 100%, 0.5) !important;
//   box-shadow: none !important;
</style>
