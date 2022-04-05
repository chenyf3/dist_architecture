/**
 * This mixins is a function that can download file with blod.
 * When you need download function, use it with after code.
 *
 * Created by Jaykey 2020/0509
 *
 * import downloadMethod from '@/mixins/download';
 * mixins: [downloadMethod],
 */

export default {
  methods: {
    downloadWithBlob(blob, fileName) {
      const href = window.URL.createObjectURL(blob);
      // 兼容IE的方法
      if (window.navigator.msSaveBlob) {
        try {
          window.navigator.msSaveBlob(blob, fileName);
        } catch (e) {
          throw Error(e);
        }
      } else {
        let a = document.createElement('a');
        a.href = href;
        a.download = fileName;
        a.dispatchEvent(new MouseEvent('click'));
      }
    }
  }
};
