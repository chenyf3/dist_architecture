/* eslint-disable no-constant-condition */
const path = require('path');
const webpack = require('webpack');
const VueLoaderPlugin = require('vue-loader/lib/plugin-webpack4');
const ProgressBarPlugin = require('progress-bar-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const { CleanWebpackPlugin } = require('clean-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CompressionPlugin = require('compression-webpack-plugin');

module.exports = {
  context: path.resolve(__dirname, '../'),
  entry: {
    index: './src/pages/app/main.js',
    login: './src/pages/login/login.js'
  },
  output: {
    path: path.resolve(__dirname, '../dist'),
    filename: path.posix.join(
        'static',
        // process.env.NODE_ENV !== 'production' ? 'js/[name]-[contenthash:8].js' : 'js/[name]-[hash:8].js'
        'js/[name]-[hash:8].js'
    ),
    chunkFilename: path.posix.join(
        'static',
        'js/[name]-[hash:8].js'
        // process.env.NODE_ENV !== 'production' ? 'js/[name]-[contenthash:8].js' : 'js/[name]-[hash:8].js'
    ),
    publicPath: './'
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, '../src')
    },
    extensions: ['.js', '.vue', '.styl', '.css']
  },
  module: {
    rules: [
      {
        test: /\.js$/,
        use: [
          {
            loader: 'thread-loader',
            options: {
              workers: 3
            }
          },
          'babel-loader'
        ],
        exclude: /node_modules/
      },
      {
        test: /\.vue$/,
        use: 'vue-loader'
      },
      {
        test: /\.css$/,
        use: [
          {
            loader: process.env.NODE_ENV !== 'production' ? 'vue-style-loader' : MiniCssExtractPlugin.loader,
            options: {
              publicPath: '../../'
            }
          },
          'css-loader'
        ]
      },
      {
        test: /\.styl(us)?$/,
        use: [
          {
            loader: process.env.NODE_ENV !== 'production' ? 'vue-style-loader' : MiniCssExtractPlugin.loader,
            options: {
              publicPath: '../../'
            }
          },
          'css-loader',
          'stylus-loader'
        ]
      },
      {
        test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
        loader: 'file-loader',
        options: {
          name: '[name]-[hash:8].[ext]',
          outputPath: 'static/fonts'
        }
      },
      {
        test: /\.(png|svg|jpg|gif)$/,
        use: [
          {
            loader: 'file-loader',
            options: {
              name: '[name]-[hash:8].[ext]',
              outputPath: 'static/images'
            }
          }
        ]
      }
    ]
  },
  plugins: [
    new CopyWebpackPlugin({
      patterns: [
        {
          from: path.resolve(__dirname, '../public'),
          to: 'static',
          globOptions: {
            ignore: [
              // Ignore all `js` files
              // '**/*.js',
              // Ignore all files in all library
              '**/library/**'
            ]
          }
        }
      ]
    }),
    new webpack.ProvidePlugin({
      R: 'ramda',
      ramda: 'ramda'
    }),
    new MiniCssExtractPlugin({
      filename: path.posix.join('static', 'css/[name]-[contenthash:8].css')
    }),
    new VueLoaderPlugin(),
    new ProgressBarPlugin(),
    // 生成 gzip 文件
    new CompressionPlugin({
      test: /\.(js)|(css)$/,
      threshold: 2048,
      minRatio: 0.8,
      cache: false
    }),
    new CleanWebpackPlugin()
  ]
};