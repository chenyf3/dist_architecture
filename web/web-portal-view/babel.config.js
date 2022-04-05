/* eslint-disable prettier/prettier */
module.exports = {
  presets: ['@vue/cli-plugin-babel/preset', '@babel/preset-env'],
  plugins: [
    'equire',
    '@babel/plugin-transform-runtime',
    [
      'component',
      {
        'libraryName': 'element-ui',
        // 'style': false,
        'styleLibraryName': '~src/assets/style/element-ui'
      }
    ]
  ]
};
