/**
 * 编码风格规范文件
 */
module.exports = {
    //此项是用来告诉eslint找当前配置文件不能往父级查找
    root: true,
    //此项指定环境的全局变量
    env: {
        node: true
    },
    extends: ['plugin:vue/essential', 'eslint:recommended'],
    parserOptions: {
        parser: 'babel-eslint'
    },
    rules: {
        'no-console': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'no-debugger': process.env.NODE_ENV === 'production' ? 'warn' : 'off',
        'quotes': [1, 'single'],
        'no-undef': 'warn'
    }
};
