const merge = require('webpack-merge');
const baseWebpackConfig = require('./webpack.base');
const webpack = require('webpack');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const path = require('path');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const cssnano = require('cssnano');
const AddAssetHtmlWebpackPlugin = require('add-asset-html-webpack-plugin');
const PreloadWebpackPlugin = require('preload-webpack-plugin');
// const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

module.exports = merge(baseWebpackConfig, {
  mode: 'production',
  devtool: 'cheap-module-source-map',
  optimization: {
    moduleIds: 'hashed', // 使用 contenthash 来实现增量更新
    splitChunks: {
      cacheGroups: {
        // vue: {
        //   test: /(vue|vue-router|vuex)/,
        //   name: 'vue-vendors',
        //   chunks: 'all',
        //   minChunks: 2,
        //   minSize: 1024,
        //   maxSize: 0,
        //   priority: 1
        // },
        elementUI: {
          test: /(element-ui)/,
          name: 'element-ui',
          chunks: 'all',
          minChunks: 1,
          minSize: 1024,
          maxSize: 0,
          priority: 0
        },
        echarts: {
          test: /(echarts)/,
          name: 'echarts',
          chunks: 'all',
          minChunks: 1,
          minSize: 1024,
          maxSize: 0,
          priority: 0
        }
        // jsencrypt: {
        //   test: /(jsencrypt)/,
        //   name: 'jsencrypt',
        //   chunks: 'all',
        //   minChunks: 1,
        //   minSize: 1024,
        //   maxSize: 0,
        //   priority: 0
        // },
        // axios: {
        //   test: /(axios)/,
        //   name: 'axios',
        //   chunks: 'all',
        //   minChunks: 1,
        //   minSize: 1024,
        //   maxSize: 0,
        //   priority: 0
        // }
      }
    }
  },
  plugins: [
    // new BundleAnalyzerPlugin(),
    new CleanWebpackPlugin(),
    new webpack.DefinePlugin({
      'process.env': {
        BASE_URL: './',
        NODE_ENV: '"production"',
        VUE_APP_RSA_PUBLIC_KEY: '"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCay77pZmzmGw+zctAG6Gr+nzybuNGxswh9RJd7P8owoXFGCEM8G/XEJ5RfixAuTRMqPflr+4dD2+q0NmOULCcbSs0v8wxz3+Vw5aJffjIAhwvdaXohvwMuoPOCv7W7tRpzHVnVJATp+xgIFiQ7c1MxlhnznNMO5Swo5kmlCu56IwIDAQAB"',
        VUE_APP_BASE_API: '"http://10.10.10.39:8301"'
      }
    }),
    new OptimizeCSSAssetsPlugin({
      assetNameRegExp: /\.(css|styl(us))$/g,
      cssProcessor: cssnano
    }),
    new HtmlWebpackPlugin({
      title: 'XPAY商户系统',
      filename: 'login.html',
      template: './src/pages/login/login.html',
      inject: true,
      basePath: './',
      chunks: ['element-ui', 'login'],
      minify: {
        html5: true,
        collapseWhitespace: true,
        preserveLineBreaks: false,
        minifyCSS: true,
        minifyJS: true,
        removeComments: false
      }
    }),
    new HtmlWebpackPlugin({
      title: 'XPAY商户系统',
      filename: 'index.html',
      template: './src/pages/app/index.html',
      inject: true,
      basePath: './',
      chunks: ['element-ui', 'echarts', 'index'],
      minify: {
        html5: true,
        collapseWhitespace: true,
        preserveLineBreaks: false,
        minifyCSS: true,
        minifyJS: true,
        removeComments: false
      }
    }),
    new PreloadWebpackPlugin({
      rel: 'prefetch',
      include: 'asyncChunks',
      exclude: ['login', 'index']
    }),
    new PreloadWebpackPlugin({
      rel: 'preload',
      include: ['login', 'index', 'login-index'],
      as(entry) {
        if (/\.css$/.test(entry)) return 'style';
        if (/\.woff$/.test(entry)) return 'font';
        if (/\.png$/.test(entry)) return 'image';
        return 'script';
      }
    }),
    // DllPlugin
    new webpack.DllReferencePlugin({
      context: __dirname,
      manifest: require('../public/library/axios.json')
    }),
    new webpack.DllReferencePlugin({
      context: __dirname,
      manifest: require('../public/library/jsencrypt.json')
    }),
    new webpack.DllReferencePlugin({
      context: __dirname,
      manifest: require('../public/library/ramda.json')
    }),
    new webpack.DllReferencePlugin({
      context: __dirname,
      manifest: require('../public/library/vue.json')
    }),
    // 引入 DllPlugin
    new AddAssetHtmlWebpackPlugin({
      filepath: path.resolve(__dirname, '../public/library/*.js'),
      publicPath: './static/js/dll',
      outputPath: './static/js/dll'
    }),
    new webpack.NamedChunksPlugin()
  ]
});
