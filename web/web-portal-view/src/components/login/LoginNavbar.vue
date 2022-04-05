<template>
  <div class="login-navbar-container">
    <div class="login-navbar-main">
      <img src="@/assets/images/public/logo.png" alt="xpay" />
      <span class="sys-name">商户系统</span>
    </div>
  </div>
</template>

<script>
export default {
  name: 'LoginNavbar'
};
</script>

<style lang="stylus" scoped>
.login-navbar-container
  display flex
  height 80px;
  line-height 80px;
  background #fff;
  border-bottom 1px solid #FC9942;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.2);
  .login-navbar-main
    display flex
    margin-left 60px
    justify-content flex-start
    align-items center
    img
      width 150px
    .sys-name
      display: flex;
      justify-content: center;
      align-items: center;
      margin-left: 20px;
      padding-left: 20px;
      height: 30px;
      font-weight: 300;
      font-size: 20px;
      border-left: 1px solid #000;
</style>
