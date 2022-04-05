import Vue from 'vue';
import App from './Login.vue';
import router from '@/router/login/index.js';
import store from '@/store';

// 自定义指令安装
import directives from '@/tools/directives';
Object.entries(directives).forEach(directive => {
  if (directive[0] === 'debounce') {
    Vue.directive(directive[0], directive[1]);
  }
});

Vue.config.productionTip = false;

// Element UI JS
import { Form, FormItem, Input, Button, Container, Main, Message, Radio, RadioGroup, Loading, Icon } from 'element-ui';

Vue.use(Form);
Vue.use(FormItem);
Vue.use(Input);
Vue.use(Button);
Vue.use(Container);
Vue.use(Main);
Vue.use(Radio);
Vue.use(RadioGroup);
Vue.use(Icon);
Vue.use(Loading.directive);

Vue.prototype.$message = Message;

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#login');
