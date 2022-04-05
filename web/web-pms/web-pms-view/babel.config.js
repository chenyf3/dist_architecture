/**
 * babel是一个Javascript编译器，是目前前端开发最常用的工具之一，主要用于将 ECMAScript 2015+ 版本的代码转换为向后兼容的 JavaScript 语法，
 * 以便能够运行在当前和旧版本的浏览器或其他环境
 *
 */
module.exports = {
    presets: [
        // https://github.com/vuejs/vue-cli/tree/master/packages/@vue/babel-preset-app
        '@vue/cli-plugin-babel/preset'
    ],
    'env': {
        'development': {
            // babel-plugin-dynamic-import-node plugin only does one thing by converting all import() to require().
            // This plugin can significantly increase the speed of hot updates, when you have a large number of pages.
            // https://panjiachen.github.io/vue-element-admin-site/guide/advanced/lazy-loading.html
            'plugins': ['dynamic-import-node']
        }
    }
}
