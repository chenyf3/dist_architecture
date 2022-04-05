import systemRoutes from './system';
import securityRoutes from './security';

const routes = [
  {
    name: 'UserCenter',
    path: '/sysManage',
    component: () => import('@/views/userCenter/Index.vue'),
    children: [
      ...systemRoutes,
      ...securityRoutes
    ]
  }
];

export default routes;
