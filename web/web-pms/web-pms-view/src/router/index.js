import Vue from 'vue'
import VueRouter from 'vue-router'
import Layout from '@/layout'
import store from '@/store';
import NProgress from 'nprogress';
import getPageTitle from '@/tools/get-page-title';
import {getToken, removeToken} from '@/tools/token';
import {routeBuilder, menuBuilder, permitBuilder} from './builder'
import {menuRoutes} from './menuRoutes'

Vue.use(VueRouter)

//不需要权限控制的那些静态路由
const constRoutes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index'),
        hidden: true
    },
    {
        path: '/forgetPwd',
        name: 'ForgetPwd',
        component: () => import('@/views/login/ForgetPwd.vue'),
        hidden: true
    },
    {
        path: '/401',
        component: () => import('@/views/error-page/401'),
        hidden: true
    },
    {
        path: '/404',
        component: () => import('@/views/error-page/404'),
        hidden: true
    },
    {
        path: '/redirect',
        component: Layout,
        hidden: true,
        children: [
            {
                path: '/redirect/:path(.*)',
                component: () => import('@/views/redirect/index')
            }
        ]
    },
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard',
        children: [
            {
                path: '/dashboard',
                component: () => import('@/views/dashboard/index'),
                name: 'dashboard',
                meta: {title: '首页', icon: 'dashboard', affix: true}
            }
        ]
    }
]

//定义一个const方法，方便方法复用
const createRouter = () => new VueRouter({
    scrollBehavior: () => ({y: 0}),
    routes: constRoutes
})

const router = createRouter()

export function resetRouter() {
    const newRouter = createRouter()
    router.matcher = newRouter.matcher //reset router
}

const whiteList = ['/login','/forgetPwd']
NProgress.configure({showSpinner: false})
router.beforeEach((to, from, next) => {
    // start progress bar
    NProgress.start()
    // set page title
    document.title = getPageTitle(to.meta.title)

    const isInfoReady = store.getters.signedInInfo.isInfoReady;
    const hasToken = !! getToken();

    if(hasToken && isInfoReady) { //已经登录并且有用户信息的，则直接放行
        next();
    } else if (hasToken && to.path === '/login') { //已经登录还请求登录页面，则跳转到主页
        next({path: '/'})
    } else if (hasToken && !isInfoReady) { //已经登录但还没有用户信息的，则获取用户信息
        loadSignedInInfo().then(isSuccess => {
            if(isSuccess){
                next({ ...to, replace: true })
                return
            }else{ //获取用户失败，重新跳转到登录页面
                removeToken()
                next(`login?redirect=${to.path}`)
            }
        })
    } else if (whiteList.indexOf(to.path) !== -1) {
        next()
    } else {
        next(`/login?redirect=${to.path}`)
    }
    NProgress.done()
});

router.afterEach(() => {
    // finish progress bar
    NProgress.done()
})

import {getSignedInInfo} from '@/api/system/user';
function loadSignedInInfo() {
    return getSignedInInfo().then((resp) => {
        if(resp.code !== 200){
            return new Promise(resolve => resolve(false));
        }

        const {userInfo, dictionary, authList} = resp.data;
        const accessedRoutes = routeBuilder(authList, menuRoutes)
        const accessedMenus = menuBuilder(authList)
        const accessPermits = permitBuilder(authList)

        resetRouter() //重置路由
        router.addRoutes(accessedRoutes)//追加后端返回的路由

        const allRoutes = constRoutes.concat(accessedRoutes)//全部路由
        const routeInfo = {accessedRoutes: allRoutes, accessedMenus: accessedMenus, accessPermits: accessPermits}
        const signedInInfo = {userInfo: userInfo, dictionary: dictionary, routeInfo: routeInfo}
        return store.dispatch('signedInInfo/saveSignedInInfo', signedInInfo);
    })
}

export default router
