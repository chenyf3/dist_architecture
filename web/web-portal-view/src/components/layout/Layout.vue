<template>
  <el-container class="layout-container">
    <el-container>
      <SideBar ref="sidebar" :activeNavbarMenu.sync="activeNavbarMenu" :sideMenuList.sync="sideMenuList" />
      <el-container class="layout-wrapper">
        <Navbar ref="navbar" :activeNavbarMenu.sync="activeNavbarMenu" :topMenuList.sync="topMenuList" />
        <el-main>
          <router-view />
        </el-main>
         <Footer></Footer>
      </el-container>
    </el-container>
  </el-container>
</template>

<script>
import SideBar from '@/components/layout/SideBar';
import Navbar from '@/components/layout/Navbar';
import Footer from '@/components/layout/Footer';

import { getToken } from '@/tools/token';
import { getSignedInInfo } from '@/api/public/login';
import { getProductPermit } from '@/api/public';

export default {
  name: 'Layout',
  components: {
    SideBar,
    Navbar,
    Footer
  },
  data() {
    return {
      activeNavbarMenu: -1,
      topMenuList: [],
      sideMenuList: []
    };
  },
  beforeMount() {
    if (getToken()) {
      this.initSignedInInfo();
      this.getProductPermission();
    }
  },
  methods: {
    async initSignedInInfo() {
      await getSignedInInfo()
        .then(res => {
          if (res.code === 200) {
            return res.data;
          } else {
            this.$message.error(res.msg);
          }
        })
        .then(({ userInfo, dictionary, authList }) => {
          //设置用户信息
          this.$store.dispatch('setUserInfo', userInfo);
          //设置数据字典
          localStorage.setItem('dictionary', JSON.stringify(dictionary));
          //设置菜单
          const allMenuList = authList;
          this.topMenuList = allMenuList.filter(m => m.parentId === 0);
          this.sideMenuList = allMenuList.filter(m => m.authType === 1);
          const permissionList = allMenuList.filter(m => m.authType === 2);
          this.$store.dispatch('setAuthorization', permissionList);
          this.activeNavbarMenu = this.topMenuList && this.topMenuList.length > 0 ? this.topMenuList[0].id : -1;
        });
    },
    getProductPermission() {
      getProductPermit().then(res => {
        if (res.code === 200) {
          this.$store.dispatch('setProducts', res.data);
        }
      });
    }
  }
};
</script>

<style lang="stylus" scoped>
.layout-container
  .layout-wrapper
    display flex;
    flex-direction column;
  .el-main
    padding 1vh 0.5vw;
    height: 91.6vh;
</style>
