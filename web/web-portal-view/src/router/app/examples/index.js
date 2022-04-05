
const routes = [
  {
    path: '/examples',
    name: 'ExampleModule',
    component: () => import('@/views/examples/Index.vue'),
    children: [
      {
        path: '/examples/audioPlay',
        name: 'AudioPlay',
        component: () => import('@/views/examples/AudioPlay.vue'),
      },
      {
        path: '/examples/fileOperate',
        name: 'fileOperate',
        component: () => import('@/views/examples/FileUpAndDown.vue'),
      },
    ]
  }
];

export default routes;
