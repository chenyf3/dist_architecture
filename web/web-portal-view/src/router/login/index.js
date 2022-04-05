import Vue from 'vue';
import VueRouter from 'vue-router';

Vue.use(VueRouter);

const routes = [
	{
		path: '/',
		alias: ['/login', '*'],
		name: 'Login',
		component: () => import('@/views/login/Index.vue')
	},
	{
		path: '/forgetPwd',
		name: 'ForgetPwd',
		component: () => import('@/views/login/ForgetPwd.vue')
	}
];

const router = new VueRouter({
	routes
});

export default router;
