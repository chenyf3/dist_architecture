const routes = [
  {
    name: 'UserManage',
    path: '/sysManage/sysSetup/userManage',
    component: () => import('@/views/userCenter/system/UserManage.vue')
  },
  {
    name: 'RoleManage',
    path: '/sysManage/sysSetup/roleManage',
    component: () => import('@/views/userCenter/system/RoleManage.vue')
  },
  {
    name: 'OperateLog',
    path: '/sysManage/sysSetup/operateLog',
    component: () => import('@/views/userCenter/system/OperateLog.vue')
  }
];

export default routes;
