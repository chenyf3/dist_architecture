import store from '@/store';

export default {
  methods: {
    CHECK_AUTH(auth) {
      return auth && store.getters.checkAuthorization(auth);
    }
  }
};
