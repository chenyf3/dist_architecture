const path = require('path');
const merge = require('webpack-merge');
const baseWebpackConfig = require('./webpack.base');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const FriendlyErrorsWebpackPlugin = require('friendly-errors-webpack-plugin');
const PreloadWebpackPlugin = require('preload-webpack-plugin');

module.exports = merge(baseWebpackConfig, {
  mode: 'development',
  devtool: 'cheap-module-source-map	',
  output: {
    path: path.resolve(__dirname, '../dist'),
    filename: path.posix.join('static', 'js/[name]-[hash:8].js'),
    chunkFilename: path.posix.join('static', 'js/[name]-[hash:8].js'),
    publicPath: './'
  },
  devServer: {
    port: 2020, // 端口
    compress: true, // 是否压缩
    inline: true,
    open: false,
    hot: true,
    progress: false,
    historyApiFallback: true,
    publicPath: '/',
    quiet: true,
    proxy: {
      '/': 'http://10.10.10.38:8101' // development environment
    }
  },
  plugins: [
    new webpack.HotModuleReplacementPlugin(),
    new webpack.DefinePlugin({
      'process.env': {
        BASE_URL: './',
        NODE_ENV: '"development"',
        VUE_APP_RSA_PUBLIC_KEY: '"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC8mQ/aJ+ryA1Sqx9sodSCgVYFg9hBvunJ9Kq1j2bE0Q0Wk/4SaBQ0/Z9nMLOx6WrdQHwQMgRmmgcDBE79+Z5L9ld5BmXFOrMNVeDXTyfUAb7eWnl5olO1NsXFS2+COH1kjB3/dMLyyeMhNd4XPVoKanaXhfqxAWj1YSmhJXQgwewIDAQAB"',
        VUE_APP_BASE_API: '"http://127.0.0.1:8101"' // development environment
      }
    }),
    new HtmlWebpackPlugin({
      title: 'XPAY商户系统',
      filename: 'login.html',
      template: './src/pages/login/login.html',
      inject: true,
      basePath: './'
      // chunks: ['vue-vendors', 'element-ui', 'jsencrypt', 'axios', 'login']
    }),
    new HtmlWebpackPlugin({
      title: 'XPAY商户系统',
      filename: 'index.html',
      template: './src/pages/app/index.html',
      inject: true,
      basePath: './'
      // chunks: ['vue-vendors', 'element-ui', 'echarts', 'jsencrypt', 'axios', 'index']
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
    new FriendlyErrorsWebpackPlugin({
      compilationSuccessInfo: {
        messages: ['XPAY商户系统 http://localhost:2020']
        // notes: ['Some additionnal notes to be displayed unpon successful compilation']
      }
    })
  ]
});
