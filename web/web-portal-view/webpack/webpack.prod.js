const baseWebpackConfig = require('./webpack.base');

// const path = require('path');
const webpack = require('webpack');
const merge = require('webpack-merge');
const HtmlWebpackPlugin = require('html-webpack-plugin');
// const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const cssnano = require('cssnano');
const OptimizeCSSAssetsPlugin = require('optimize-css-assets-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const PreloadWebpackPlugin = require('preload-webpack-plugin');
// const BundleAnalyzerPlugin = require('webpack-bundle-analyzer').BundleAnalyzerPlugin;

module.exports = merge(baseWebpackConfig, {
  mode: 'production',
  optimization: {
    moduleIds: 'hashed', // 使用 contenthash 来实现增量更新
    splitChunks: {
      cacheGroups: {
        vue: {
          test: /(vue|vue-router|vuex)/,
          name: 'vue-vendors',
          chunks: 'all',
          minChunks: 2,
          minSize: 1024,
          maxSize: 0,
          priority: 1
        },
        elementUI: {
          test: /(element-ui)/,
          name: 'element-ui',
          chunks: 'all',
          minChunks: 1,
          minSize: 1024,
          maxSize: 0,
          priority: 0,
          enforce: true
        },
        echarts: {
          test: /(echarts)/,
          name: 'echarts',
          chunks: 'all',
          minChunks: 1,
          minSize: 1024,
          maxSize: 0,
          priority: 0
        },
        jsencrypt: {
          test: /(jsencrypt)/,
          name: 'jsencrypt',
          chunks: 'all',
          minChunks: 1,
          minSize: 1024,
          maxSize: 0,
          priority: 0
        },
        axios: {
          test: /(axios)/,
          name: 'axios',
          chunks: 'all',
          minChunks: 1,
          minSize: 1024,
          maxSize: 0,
          priority: 0
        }
      }
    },
    minimizer: [
      // 多线程压缩
      new TerserPlugin({
        parallel: true
      })
    ]
  },
  plugins: [
    // new BundleAnalyzerPlugin(),
    new CleanWebpackPlugin(),
    // 定义环境变量
    new webpack.DefinePlugin({
      'process.env': {
        BASE_URL: './',
        NODE_ENV: '"production"',
        VUE_APP_RSA_PUBLIC_KEY: '"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCay77pZmzmGw+zctAG6Gr+nzybuNGxswh9RJd7P8owoXFGCEM8G/XEJ5RfixAuTRMqPflr+4dD2+q0NmOULCcbSs0v8wxz3+Vw5aJffjIAhwvdaXohvwMuoPOCv7W7tRpzHVnVJATp+xgIFiQ7c1MxlhnznNMO5Swo5kmlCu56IwIDAQAB"',
        VUE_APP_BASE_API: '"https://portalapi.xpay.com"'
      }
    }),
    // 压缩 CSS
    new OptimizeCSSAssetsPlugin({
      assetNameRegExp: /\.(css|styl(us))$/g,
      cssProcessor: cssnano
    }),
    new HtmlWebpackPlugin({
      title: '欢迎使用XPAY商户系统',
      filename: 'login.html',
      template: './src/pages/login/login.html',
      inject: true,
      basePath: './',
      chunks: ['vue-vendors', 'element-ui', 'jsencrypt', 'axios', 'login'],
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
      title: 'XPAY-商户系统',
      filename: 'index.html',
      template: './src/pages/app/index.html',
      inject: true,
      basePath: './',
      chunks: ['vue-vendors', 'element-ui', 'echarts', 'jsencrypt', 'axios', 'index'],
      minify: {
        html5: true,
        collapseWhitespace: true,
        preserveLineBreaks: false,
        minifyCSS: true,
        minifyJS: true,
        removeComments: false
      }
    }),
    // 在HTML模版中添加 preload 和 prefetch
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
    })
  ]
});
