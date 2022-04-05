<template>
  <el-container class="login-container">
    <login-navbar></login-navbar>
    <el-main class="login-main">
      <router-view />
    </el-main>
    <login-footer v-if="false"></login-footer>
  </el-container>
</template>

<script>
import LoginNavbar from '@/components/login/LoginNavbar';
import LoginFooter from '@/components/login/LoginFooter';
export default {
  name: 'LoginLayout',
  components: {
    LoginNavbar,
    LoginFooter
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
