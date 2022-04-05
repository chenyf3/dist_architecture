# 第三方公司回调网关
## 项目作用  
用以微信、支付宝等等第三方公司的统一回调入口，在本项目中实现签名、验签、响应等处理  

## 如何配置回调公司？
1. 在 com.xpay.gateway.callback.enums.CompanyEnum 中定义回调公司的枚举值和BeanName，并在 getCompanyBeanName(...) 方法中新增此公司的逻辑
2. 新建java类实现 com.xpay.gateway.callback.service.CompanyService 接口，并使用CompanyEnum 中定义的 BeanName 作为此实现类的Spring Bean Name  
3. 在配置文件中使用 spring.cloud.gateway.routes 定义好此公司回调的一级路径，注意此处的一级路径要与 CompanyEnum 中定义的一致
4. 在配置文件中使用 gateway.pathConf[一级路径名].allowMethods 定义好此一级路径下允许的子路径，此处的子路径要严格控制好，不能多也不能少，不要把后端服务中不能对外暴露的接口暴露出去
