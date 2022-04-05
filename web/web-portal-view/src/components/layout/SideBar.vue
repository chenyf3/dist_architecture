<template>
  <el-aside class="layout-sidebar" width="180px">
    <div class="sidebar-logo" width="100px">
      <img src="@/assets/images/public/logo.png" alt="Logo" />
    </div>
    <div class="sidebar-menus">
      <el-menu
        :default-active.sync="activeIndex"
        ref="menu"
        class="el-menu-vertical-demo"
        @open="handleOpen"
        @close="handleClose"
      >
        <template v-for="menuList in activeSideBarList">
          <template v-if="menuList.children.length > 0">
            <el-submenu :index="String(menuList.id)" :key="menuList.id">
              <template slot="title">
                <i :class="menuList.icon"></i>
                <span>{{ menuList.name }}</span>
              </template>
              <template v-if="menuList">
                <template v-for="menuItem in menuList.children">
                  <el-menu-item :key="menuItem.id" :index="String(menuItem.id)" @click="changeChildrenMenu(menuItem)">
                    {{ menuItem.name }}
                  </el-menu-item>
                </template>
              </template>
            </el-submenu>
          </template>
          <template v-else>
            <el-menu-item :key="menuList.id" :index="String(menuList.id)" @click="changeChildrenMenu(menuList)">
              <template slot="title">
                <i :class="menuList.icon"></i>
                <span>{{ menuList.name }}</span>
              </template>
            </el-menu-item>
          </template>
        </template>
      </el-menu>
    </div>
  </el-aside>
</template>

<script>
export default {
  name: 'SideBar',
  props: {
    activeNavbarMenu: {
      type: Number,
      default: () => -1
    },
    sideMenuList: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      sideBarMenuList: [],
      activeSideBarList: [],
      activeIndex: ''
    };
  },
  watch: {
    activeNavbarMenu(newValue = -1) {
      const activeListIndex = this.sideBarMenuList.findIndex(m => m.id === newValue);
      if (activeListIndex === -1) this.sideBarMenuList = this.renderTree(this.sideMenuList);
      this.activeSideBarList = this.sideBarMenuList[activeListIndex !== -1 ? activeListIndex : 0].children;
      // eslint-disable-next-line no-prototype-builtins
      this.activeSideBarList[0].hasOwnProperty('children')
        ? this.changeChildrenMenu(
            this.activeSideBarList[0].children.length > 0
              ? this.activeSideBarList[0].children[0]
              : this.activeSideBarList[0]
          )
        : this.changeChildrenMenu(this.activeSideBarList[0]);
      return newValue;
    },
    $route(newValue) {
      const menu = this.sideMenuList.find(item => item.url === newValue.fullPath);
      this.changeChildrenMenu(menu);
    }
  },
  methods: {
    // TODO: 默认打开第一个
    handleOpen() {
      // console.log(typeof key);
      // console.log(this.$refs['menu'].openedMenus, '00000');
    },
    handleClose() {
      // console.log(key, keyPath);
      // console.log(this.$refs['menu'].openedMenus, '11111');
    },
    renderTree(list, parentId = 0) {
      let node = [];
      list
        .filter(parent => parent.parentId === parentId)
        .forEach(children => {
          node.push(children);
          children && (node[node.length - 1].children = this.renderTree(list, children.id));
        });
      return node;
    },
    changeChildrenMenu(menu) {
      if (menu) {
        this.$refs['menu'].activeIndex = String(menu.id);
        this.$router.push({ path: menu.url }).catch(() => {});
      }
    }
  }
};
</script>

<style lang="stylus" scoped>
.layout-sidebar
  height: 100vh;
  &.el-aside
    min-height 100vh;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  .sidebar-logo
    display flex;
    justify-content center;
    align-items center;
    height: 8vh;
    max-height: 80px;
    border-bottom 1px solid #409eff;
    box-shadow: 4px 4px 10px 0px rgba(252,153,66,0.1)
    // min-height: 80px;
    img
      // width: 10vw;
      max-height: 80px;
      height: 8vh;
  .el-menu
    border-right: 0;
  .el-menu-item.is-active
    border-right: 6px solid;
</style>
