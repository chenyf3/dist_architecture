<template>
  <el-footer class="layout-footer">
    <a
      target="_blank"
      href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=xxxxxxx"
      style="display:inline-block;text-decoration:none;height:15px;line-height:15px;"
    >
      <img src="@/assets/images/public/badge.png" style="float:left;" />
      <p style="float:left;height:15px;line-height:15px;margin: 0px 0px 0px 5px;">
        粤公网安备 xxxxxxxxxx 号
      </p>
    </a>
    <span>&nbsp;|&nbsp;</span>
    <a href="http://www.beian.miit.gov.cn/">
      粤ICP备xxxxxx号
    </a>
    <span>&nbsp;|&nbsp;</span>
    <span>Copyright © 2022 xpay 版权所有</span>
    &nbsp;
    <a href="http://www.cyberpolice.cn/wfjb/">
      <img src="@/assets/images/public/jinghui.png" class="footer-bottom-jinghui" alt="beian" />
    </a>
    &nbsp;
    <a href="http://www.315online.com.cn/">
      <img src="@/assets/images/public/safe.png" class="footer-bottom-safe" alt="beian" />
    </a>
  </el-footer>
</template>

<script>
export default {
  name: 'Footer'
};
</script>

<style lang="stylus" scoped>
.layout-footer
  border-top 1px solid #FC9942
  display: flex;
  justify-content: center;
  align-items: center;
</style>
