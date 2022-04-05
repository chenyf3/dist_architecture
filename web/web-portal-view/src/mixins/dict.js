/**
 * This mixins is a function that can use Dictionary data.
 *
 * Created by Jaykey 2020/0509
 *
 * import downloadMethod from '@/mixins/dict';
 * mixins: [dict],
 */

export default {
  data() {
    return {
      DICT: {}
    };
  },
  mounted() {
    this.DICT = JSON.parse(localStorage.getItem('dictionary'));
  }
};
