const path = require('path');
function resolve(dir) {
  return path.join(__dirname, dir);
}

module.exports = {
  publicPath: './',
  outputDir: 'dist',
  assetsDir: 'static',
  // 多页配置
  pages: {
    login: {
      entry: 'src/pages/login/login.js',
      template: 'src/pages/login/login.html',
      filename: 'login.html',
      title: '欢迎使用xpay',
      chunks: ['chunk-vendors', 'chunk-common', 'login']
    },
    index: {
      entry: 'src/pages/app/main.js',
      template: 'src/pages/app/index.html',
      filename: 'index.html',
      title: 'xpay-商户后台',
      chunks: ['chunk-vendors', 'chunk-common', 'index']
    }
  },
  lintOnSave: process.env.NODE_ENV === 'development',
  productionSourceMap: process.env.NODE_ENV === 'development',
  devServer: {
    port: 8020,
    overlay: {
      warnings: false,
      errors: true
    },
    proxy: {
      '^/': {
        target: 'http://127.0.0.1.63:8101',
        ws: true,
        changeOrigin: true
      }
    }
  },
  configureWebpack: {
    resolve: {
      alias: {
        '@': resolve('src')
      }
    }
  }
};
