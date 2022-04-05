import Vue from 'vue';
import VueRouter from 'vue-router';
import { getToken } from '@/tools/token';
import { Message } from 'element-ui';

import homeRoutes from './home';
import userRoutes from './userCenter';
import exampleRoutes from './examples';

Vue.use(VueRouter);

const routes = [...homeRoutes, ...userRoutes, ...exampleRoutes];

const router = new VueRouter({ routes });

router.beforeEach((to, from, next) => {
  const TOKEN = getToken();
  if (TOKEN) {
    next();
  } else {
    from.path !== '/' && Message.error('登录状态已失效，请重新登录');
    setTimeout(() => {
      sessionStorage.removeItem('vuex');
      const loginPath =
        window.location.origin +
        (window.location.pathname.includes('index.html')
          ? window.location.pathname.replace('index', 'login')
          : window.location.pathname.includes('login.html')
          ? window.location.pathname
          : window.location.pathname + 'login.html')
          + '#/';
      window.location.href = loginPath;
    }, 1000);
  }
});

export default router;
