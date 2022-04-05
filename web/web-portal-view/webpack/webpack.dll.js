const path = require('path');
const webpack = require('webpack');

module.exports = {
  context: process.cwd(),
  resolve: {
    extensions: ['.js', '.json', '.styl', '.css'],
    modules: [__dirname, 'node_modules']
  },
  entry: {
    vue: ['vue', 'vue-router', 'vuex', 'vuex-persistedstate'],
    axios: ['axios'],
    jsencrypt: ['jsencrypt'],
    ramda: ['ramda']
  },
  output: {
    filename: '[name].dll.js',
    path: path.resolve(__dirname, '../public/library/'),
    library: '[name]'
  },
  plugins: [
    new webpack.DllPlugin({
      name: '[name]',
      path: path.join(__dirname, '../public/library/[name].json')
    })
  ]
};
