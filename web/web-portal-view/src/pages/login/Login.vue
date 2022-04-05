<template>
  <div id="login">
    <login-layout></login-layout>
  </div>
</template>

<script>
import LoginLayout from '@/components/login/LoginLayout';
export default {
  name: 'Login',
  components: {
    LoginLayout
  }
};
</script>

<style lang="stylus">
@import '../../assets/style/reset.styl';

.el-container.login-container
  width: 100%;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  height: 100%;

.el-header.login-header
  padding 0;

.el-main.login-main
  padding 0;
  flex-grow: 1;
  display flex;
  flex-direction column;
</style>
