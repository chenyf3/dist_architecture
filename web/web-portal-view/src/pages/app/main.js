import Vue from 'vue';
import App from './App.vue';
import router from '@/router/app/index';
import store from '@/store';

// CSS部分 样式加载顺序
import '@/assets/style/common.styl';
import '@/assets/style/seach-table.styl';
import '@/assets/style/flex.styl';
import '@/assets/style/element-ui/index.css';

import VueQrcode from '@chenfengyuan/vue-qrcode';

// 过滤器安装
import filters from '@/tools/filters';
Object.entries(filters).forEach(filter => {
  Vue.filter(filter[0], filter[1]);
});
// 自定义指令安装
import directives from '@/tools/directives';
Object.entries(directives).forEach(directive => {
  Vue.directive(directive[0], directive[1]);
});

/* 按需引入 Element UI -----START----- */
import {
  Pagination,
  Dialog,
  Dropdown,
  DropdownMenu,
  DropdownItem,
  Menu,
  Submenu,
  MenuItem,
  Input,
  Radio,
  RadioGroup,
  RadioButton,
  Checkbox,
  CheckboxGroup,
  Switch,
  Select,
  Option,
  Button,
  Table,
  TableColumn,
  DatePicker,
  Popover,
  Form,
  FormItem,
  Tree,
  Col,
  Card,
  Container,
  Header,
  Aside,
  Main,
  Loading,
  Message,
  Icon,
  Image,
  Upload,
  TimeSelect,
  MessageBox,
  Notification,
  Tooltip,
  Tabs,
  TabPane,
  Carousel,
  CarouselItem,
  Divider,
  Footer,
  // Autocomplete, MenuItemGroup, InputNumber,
  // CheckboxButton, OptionGroup, ButtonGroup, TimeSelect,
  // TimePicker, Tooltip, Breadcrumb, BreadcrumbItem,
  // Tag, Alert, Slider, Row,
  // Progress, Spinner, Badge, Rate, Steps, Step,
  // Carousel, CarouselItem, Collapse, CollapseItem,
  // Cascader, ColorPicker, Transfer, Footer,
  // Timeline, TimelineItem, Link, Divider, , Calendar,
  // Backtop, PageHeader, CascaderPanel, Notification
} from 'element-ui';

Vue.use(Pagination);
Vue.use(Dialog);
Vue.use(Dropdown);
Vue.use(DropdownMenu);
Vue.use(DropdownItem);
Vue.use(Menu);
Vue.use(Submenu);
Vue.use(MenuItem);
Vue.use(Input);
Vue.use(Radio);
Vue.use(RadioGroup);
Vue.use(RadioButton);
Vue.use(Card);
Vue.use(Checkbox);
Vue.use(CheckboxGroup);
Vue.use(Switch);
Vue.use(Select);
Vue.use(Option);
Vue.use(Button);
Vue.use(Table);
Vue.use(TableColumn);
Vue.use(DatePicker);
Vue.use(Form);
Vue.use(FormItem);
Vue.use(Tree);
Vue.use(Container);
Vue.use(Header);
Vue.use(Aside);
Vue.use(Main);
Vue.use(Popover);
Vue.use(Col);
Vue.use(Icon);
Vue.use(Image);
Vue.use(Upload);
Vue.use(TimeSelect);
Vue.use(Tooltip);
Vue.use(Tabs);
Vue.use(TabPane);
Vue.use(Loading.directive);
Vue.use(Carousel);
Vue.use(CarouselItem);
Vue.use(Divider);
Vue.use(Footer);

Vue.prototype.$confirm = MessageBox.confirm;
Vue.prototype.$notify = Notification;
Vue.prototype.$message = Message;
/* 按需引入 Element UI -----END----- */

Vue.component(VueQrcode.name, VueQrcode);

Vue.config.productionTip = false;

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app');
