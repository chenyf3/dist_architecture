import Vue from 'vue'
import App from './App'
import router from './router'
import store from './store'
import Cookies from 'js-cookie'

//CSS部分，样式加载顺序
import 'normalize.css/normalize.css' // a modern alternative to CSS resets
import './styles/element-variables.scss'
import '@/styles/index.scss' // global css

//其他组件引入
import './icons' //图标库
import './tools/error-log' //错误日志搜集

// ElementUI 引入和安装
import Element from 'element-ui'
Vue.use(Element, {
    size: Cookies.get('size') || 'medium', // set element-ui default size
})

//e-icon-picker引入和安装，使用文档：https://e-icon-picker.cnovel.club/
import iconPicker from 'e-icon-picker';
import 'e-icon-picker/lib/index.css'; // 基本样式，包含基本图标
Vue.use(iconPicker, {ElementUI: true, eIcon: false, eIconSymbol: false, FontAwesome: false});

// 自定义全局过滤器安装
import filters from '@/tools/filters';
Object.entries(filters).forEach(filter => {
    Vue.filter(filter[0], filter[1]);
});

// 全局方法安装
import methods from '@/tools/method'
Vue.use(methods)

// 自定义全局指令安装
import VAuthorize from '@/directive/authorization';
VAuthorize.install(Vue)//install是自定义的方法，里面实际上还是调用了 Vue.directive() 方法来安装全局指令

Vue.config.productionTip = false

new Vue({
    el: '#app',
    router,
    store,
    render: h => h(App)
})
