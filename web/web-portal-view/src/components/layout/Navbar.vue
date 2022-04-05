<template>
  <div>
    <el-header class="navbar-container">
      <div class="navbar-menus">
        <template v-for="(item, index) in topMenuList">
          <div
              :class="['navbar-menus-item', activeNavbarMenu === item.id && 'navbar-menus-item-active']"
              :key="index"
              @click="changeNavbarMenu(item)"
          >
            <i :class="item.icon"></i>
            {{ item.name }}
          </div>
        </template>
      </div>
      <div class="navbar-actions">
        <el-dropdown @command="handleCommand">
          <span class="el-dropdown-link" style="cursor: pointer; ">
            {{ userInfo.realName }}
            <i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <el-dropdown-menu slot="dropdown" style="font-size: medium">
            <el-dropdown-item command="showUserInfo">个人信息</el-dropdown-item>
            <el-dropdown-item command="changeLoginPwd">修改密码</el-dropdown-item>
            <el-dropdown-item command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </el-header>
    <ViewUserInfo ref="viewUserInfoDialog" :show.sync="showUserDialog"></ViewUserInfo>
    <ChangeLoginPwd ref="changePwdDialog" :show.sync="showChangePwdDialog"></ChangeLoginPwd>
  </div>
</template>

<script>
import { removeToken } from '@/tools/token';
import { logoutSys } from '@/api/public/login';
import ChangeLoginPwd from '@/components/userCenter/ChangeLoginPwd';
import ViewUserInfo from '@/components/userCenter/ViewUserInfo';

export default {
  name: 'Navbar',
  components: {
    ViewUserInfo,
    ChangeLoginPwd
  },
  props: {
    activeNavbarMenu: {
      type: Number,
      default: () => -1
    },
    topMenuList: {
      type: Array,
      default: () => []
    }
  },
  data(){
    return {
      showUserDialog: false,
      showChangePwdDialog: false
    }
  },
  computed: {
    userInfo() {
      return this.$store.state.userInfo;
    }
  },
  methods: {
    changeNavbarMenu(item) {
      this.$emit('update:activeNavbarMenu', item.id);
    },
    handleCommand(command) {
      command === 'showUserInfo' && this.showUserInfo();
      command === 'changeLoginPwd' && this.changeLoginPwd();
      command === 'logout' && this.logout();
    },
    showUserInfo() {
      const userId = this.userInfo.userId
      this.showUserDialog = true
      this.$refs['viewUserInfoDialog'].initAndShow(userId);
    },
    changeLoginPwd() {
      this.showChangePwdDialog = true
    },
    logout() {
      logoutSys().then(res => {
        if (res.code === 200) {
          this.$message.success('退出成功');
          setTimeout(() => {
            removeToken();
            sessionStorage.removeItem('vuex');
            const loginPath =
              window.location.origin +
              (window.location.pathname.includes('index.html')
                ? window.location.pathname.replace('index', 'login')
                : window.location.pathname.includes('login.html')
                ? window.location.pathname
                : window.location.pathname + 'login.html') +
              '#/';
            window.location.href = loginPath;
          }, 1000);
        }
      });
    }
  }
};
</script>

<style lang="stylus" scoped>
.navbar-container
  display flex;
  justify-content space-between;
  align-items center;
  padding-left 0
  height 8vh !important;
  max-height: 80px;
  box-sizing: content-box;
  border-bottom 1px solid #409eff;
  box-shadow: 4px 4px 10px 0px rgba(252,153,66,0.1)
  .navbar-menus
    display flex
    &-item
      height: 8vh;
      line-height 7.5vh
      border-bottom 0.5vh
      display flex
      justify-content center
      align-items center
      min-width 80px
      font-size 16px
      padding 0 20px
      cursor pointer
      max-height: 74px;
      &:hover
        border-bottom 0.5vh
        background #ecf5ff
      &-active
        border-bottom 0.5vh solid rgb(64,202,255,0.9);
        //background #ecf5ff //rgba(64,167,255,0.1)
</style>
