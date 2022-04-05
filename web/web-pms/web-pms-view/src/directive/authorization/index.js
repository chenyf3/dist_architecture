import authorization from './authorization'

const install = function(Vue) {
  Vue.directive('authorize', authorization)
}

if (window.Vue) {
  window['authorize'] = authorization
  Vue.use(install); // eslint-disable-line
}

authorization.install = install
export default authorization
