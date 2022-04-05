const routes = [
  // 安全中心
  {
    name: 'SecurityCenter',
    path: '/sysManage/mchInfo/security',
    component: () => import('@/views/userCenter/security/SecurityCenter.vue')
  },
  // 密钥管理
  {
    name: 'SecurityKeyManage',
    path: '/sysManage/mchInfo/secretKey',
    component: () => import('@/views/userCenter/security/SecretKey.vue')
  }
];

export default routes;
