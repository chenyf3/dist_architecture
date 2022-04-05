import Vue from 'vue';
import Vuex from 'vuex';
import vuexPersistance from 'vuex-persistedstate';

import userInfo from './modules/userInfo.js';
import authorization from './modules/authorization.js';
import products from './modules/products';

Vue.use(Vuex);

export default new Vuex.Store({
  state: {},
  mutations: {},
  actions: {},
  modules: {
    userInfo,
    authorization,
    products
  },
  plugins: [
    vuexPersistance({
      storage: window.sessionStorage
    })
  ]
});
