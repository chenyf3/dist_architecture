# xpay—商户后台

### 基本信息

使用npm启动项目，可以更改npm源为淘宝镜像

```shell
# package.json
npm run dev # 启动本地开发环境
npm run test # 启动本地直连测试环境

npm run build:dev # 打包开发环境
npm run build:test # 打包测试环境
npm run build # 打包生产环境
```

本项目中保留原有vue-cli提供的构建方式，所以一下指令也是可以用于启动项目和打包的，但是**不建议使用**，目前已经定制化Webpack的工作流程。

```shell
# package.json
npm run dev:vue # 启动本地开发环境
npm run test:vue # 启动本地直连测试环境

npm run build:vue:dev # 打包开发环境
npm run build:vue:test # 打包测试环境
npm run build:vue # 打包生产环境
```

##### DLL提取公共资源

`DllPlugin`与`DllReferencePlugin`目前仅使用在测试环境，后面会解释不使用在生产的原因。这两个Webpack的插件主要作用是提取公共部分的代码，一次性打包，之后只需要读取对应的打包文件就可以使用。具体可以参考官方文档[1]。

在启用DLL之后，需要先在本地执行以下指令完成基础库的打包：

```sh
npm run dll
```

DLL指令会在`public/library`中生成对应的文件，然后在执行`npm run build:test`即可。

---



### 开发事项

#### 项目结构

##### webpack配置单独在一个文件夹中：

```sh
webpack
├── webpack.base.js
├── webpack.dev.js
├── webpack.dll.js
├── webpack.prod.js
└── webpack.test.js
```

##### 主要代码：

```sh
src
├── assets
├── components
├── mixins
├── pages
├── router
├── store
├── tools
└── views
    ├── home
    ├── login
    ├── product
    ├── terminal
    ├── trade
    └── userCenter
```

这是目前主要业务代码部分的结构。项目采用的双页面的方式，一个页面主要是对外的“公共页面”也是登录页面，另一个是APP的主页。

对应的基本配置和入口文件都放在`src/pages`下面，有对应的入口JS文件和对应的HTML模版。

**页面代码位置**

页面根据产品需求，按照顶级菜单做切分，在代码中根据具体的页面顺序做了排序。

`src/views`下的每一个文件夹对应一个菜单中心，其中的文件夹和对应的页面文件也和实际菜单形成一一对应的关系。

`src/api`和`src/router`的结构与`views`的类似，对应的模块都对应到具体的页面来整理代码，防止出现冗余的代码。`src/menuRoutes`中的路由配置需要注意一点：

```js
const menuRoutes = [
  //...其他路由
  {
    // 用户信息查询
    path: '/product/oilCard/userInfo',
    name: 'UserInfo',
    component: () => import(/* webpackChunkName: 'oilCard' */ '@/views/product/2-oilCard/2-UserInfo.vue')
  },
  // ... 其他路由
];

export default menuRoutes;

```

在动态引入路由文件的时候需要添加对应的`webpackChunkName`，这个标示将在后面打包分包的时候起重要的作用。

`src/asstes`中存放了静态资源，图片和对应的CSS文件，该项目使用的CSS预编译器是Stylus，与原来的Sass的效果是一样的，对原生的CSS也是同样支持的。

`src/components`中存放公用的组件和对应的`layout`，这里在登录页面和主页采用了两个不同的`layout`模板。

`src/mixins`中存放可以混入使用的Vue模块，目前已经有的模块是：权限确认模块【authorize】、数据字典【dict】、下载或导出文件【download】。使用方式是在对应的页面代码中引入，并声明mixins即可在页面访问到模块中定义的数据和方法。

`src/pages`中存放两个页面的入口文件，目前都对第三方库`element-ui`和`echarts`采用按需引入的方式，如有新引入的组件和模块，需要在这个文件夹中的入口文件声明。

在入口文件中：

```js
// 过滤器安装
import filters from '@/tools/filters';
Object.entries(filters).forEach(filter => {
  Vue.filter(filter[0], filter[1]);
});
// 自定义指令安装
import directives from '@/tools/directives';
Object.entries(directives).forEach(directive => {
  Vue.directive(directive[0], directive[1]);
});
```

引入了对应的自定义过滤器和自定义指令，目前已有的

**自定义过滤器**

1. 数据字典过滤器`DictFilter`，可以在展示数据中直接使用，后面需要添加对应的枚举值；
2. 金额过滤器`money`；
3. 时间过滤器`timeFilter`精度到秒；
4. 日期过滤器`dayFilter`精度到天。

**自定义指令：**

1. 权限控制：可以控制页面元素的展示，根据实际配置的权限来控制。`v-authorize`需要在后面添加对应的权限字符串。
2. 防抖操作：`v-debounce`，默认配置是点击事件，防抖时间是300ms。

---

`src/store`中存放Vuex的代码，目前已有三个模块的Vuex数据，分别是用户信息、产品开通信息、权限认证三个部分。权限认证部分主要是配合自定义指令和mixins使用，产品开通主要在部分页面实现特殊的业务逻辑，用户信息是在商户中心使用的比较多。

---



#### Webpack定制细节

##### 基础配置

双页面配置，通过`HtmlWebpackPlugin`实现。对应的代码使用压缩、分包优化。生产环境CSS通过`MiniCssExtractPlugin`、`cssnano`和`OptimizeCSSAssetsPlugin`进行提取和压缩处理。

公共部分的资源通过`splitChunks`来提取，业务代码的部分也在路由文件中配置了对应`webpackChunkName`做到了一个模块的代码形成一个对应的js，这样可以减少用户在不使用对应模块的时候，无需加载该部分的业务代码。

```js
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
	]
}
```

项目中使用`thread-loader`开启使用缓存来打包，提升二次构建的速度。

开发环境和测试环境都使用了source map来实现代码定位，开发环境的配置更轻便，主要是提供开发使用，测试环境的打包代码的原则是更接近生产环境，在这个基础上加上`cheap-module-source-map`来实现代码定位。

###### 开启gzip压缩

```js
plugins: [
	// 生成 gzip 文件
  new CompressionPlugin({
    test: /\.(js)|(css)$/,
    threshold: 2048,
    minRatio: 0.8,
    cache: false  // 开发环境为false, 测试和生产true 开启缓存构建
  }),
]
```

gzip 的使用需要配合Nginx的配置，具体见下面部分。



测试环境使用DllPlugin来优化打包体验，这里阐述一下具体原因，为什么在生产环境没有使用，DllPlugin主要是对公共资源的打包，在生产环境其实不存在一次上线多次打包的情况，这个时候对应的公共资源的引入其实是很少的，而在测试环境经常需要打包，但是公共资源的部分并不需要再次打包分析来处理。

---

### Nginx 配置

通过Nginx开启对应的gzip的使用和对客户端缓存的控制：

1. 开发和测试环境的配置如下：

```nginx
server {
    listen       3003;
    server_name  10.10.10.63;

    #charset koi8-r;
    #access_log  /var/log/nginx/host.access.log  main;

    location / {
        root   /usr/share/nginx/html;
        index  index.html index.htm;
    }
    location /dev {
        alias  /home/xpay/view/web-portal-view;
        index  login.html index.html index.htm;
        # 开启 gzip 压缩
        gzip  on;
        gzip_buffers 32 4k;
        gzip_comp_level  6;
        gzip_min_length  100;
        gzip_types application/javascript text/css text/xml;
        gzip_disable  "MSIE [1-6]\.";
        gzip_vary  on;
        # 开启缓存控制
        location ~ .*\.(?:css|js)$ {
                expires 600s;
        }
        location ~ .*\.(?:gif|jpg|jpeg|png|bmp|swf)$ {
                expires 15d;
        }
        location ~ .*\.(?:html)$ {
                add_header Cache-Control "public, no-cache, must-revalidate,  proxy-revalidate";
        }
    }
    location /test {
        alias  /home/xpay/test/view/web-portal-view;
        index  login.html index.html index.htm;
        # 开启 gzip 压缩
        gzip  on;
        gzip_buffers 32 4k;
        gzip_comp_level  6;
        gzip_min_length  100;
        gzip_types application/javascript text/css text/xml;
        gzip_disable  "MSIE [1-6]\.";
        gzip_vary  on;
        # 开启缓存控制
        location ~ .*\.(?:css|js)$ {
                expires 600s;
        }
        location ~ .*\.(?:gif|jpg|jpeg|png|bmp|swf)$ {
                expires 15d;
        }
        location ~ .*\.(?:html)$ {
                add_header Cache-Control "public, no-cache, must-revalidate, proxy-revalidate";
        }
    }

    #error_page  404              /404.html;
    error_page 404 /404.html;
    location = /40x.html {
    }

    # redirect server error pages to the static page /50x.html
    #
    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
    }
}
```

2.生产的Nginx配置

```nginx
server {
    listen	443 ssl;
    server_name portal.xpay.com;

    gzip  on;
    gzip_buffers 32 4k;
    gzip_comp_level  6;
    gzip_min_length  100;
    gzip_types application/javascript text/css text/xml;
    gzip_disable  "MSIE [1-6]\.";
    gzip_vary  on;

    location ~ .*\.(?:css|js)$ {
        proxy_pass      http://web-portal-view;
        proxy_cookie_path       /portal-view/  /;
        expires 1d;
    }
    location ~ .*\.(?:gif|jpg|jpeg|png|bmp|swf)$ {
        proxy_pass      http://web-portal-view;
        proxy_cookie_path       /portal-view/  /;
        expires 15d;
    }
    location ~ .*\.(?:html)$ {
        proxy_pass      http://web-portal-view;
        proxy_cookie_path       /portal-view/  /;
        add_header Cache-Control "public, no-cache, must-revalidate, proxy-revalidate";
    }

    location / {
        proxy_pass      http://web-portal-view;
        proxy_cookie_path       /portal-view/  /;
        expires 1d;
    }
}
```


*PS：已经在此项目中引入ramda*

---

###### Reference 参考链接

[DllPlugin & DllReferencePlugin](https://webpack.js.org/plugins/dll-plugin/)

[Stylus](https://stylus-lang.com/)