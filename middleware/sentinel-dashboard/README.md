# Sentinel 控制台

## 1. 改动说明
本项目基于官方1.8.2版本进行修改，主要有两类功能变动：  
1. 把默认的内存存储规则改为nacos存储规则，在sentinel-dashboard上修改规则后会push到nacos上做数据同步，在nacos上修改配置后，也可以在dashboard上主动拉取做到数据同步，
在实际使用时建议在dashboard上发布、修改配置，一是因为在dashboard页面上配置清晰简单，二是避免出现一会在dashboard上修改，一会又在nacos上修改的混乱情况，避免出现两边数据不一致的风险  
2. 增加nacos的相关配置参数，比如：serverAddr、namespace、groupId、username、password 等 

主要改动地方如下：  
1. 在pom.xml文件中引用 com.alibaba.csp:sentinel-datasource-nacos 这个依赖的地方把 <scope>test</scope> 给去掉了  
2. webapp/resources/app/scripts/directives/sidebar/sidebar.html 的第57行从 dashboard.flowV1({app: entry.app}) 改成了 dashboard.flow({app: entry.app})  
3. webapp/resources/app/views/flow_v2.html 页面新增了刷新按钮  
4. 在 com.alibaba.csp.sentinel.dashboard.config 下新增了 NacosConfig.java 和 NacosProperties.java 两个类  
5. 在 com.alibaba.csp.sentinel.dashboard.rule 下 新增 naocs 这个 package，这个包下面新增对 flow、authority、degrade、gateway、paramFlow、system 这六种规则的nacos持久化支持  
6. com.alibaba.csp.sentinel.dashboard.controller.v2.FlowControllerV2 这个类里面的 ruleProvider、rulePublisher 两个属性的@Qualifier注解中名称分别改为flowRuleNacosProvider、flowRuleNacosPublisher，同时，增删改之前先从配置中心同步一次配置  
7. com.alibaba.csp.sentinel.dashboard.controller 包下的 AuthorityRuleController、DegradeController、ParamFlowRuleController、SystemController 修改了获取规则和发布规则的地方，以支持使用nacos做持久化，同时，增删改之前先从配置中心同步一次配置  
8. 修改 com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter 类及其子类，增加重置id的方法，避免重启之后内存中的自增id重新从0开始，此时如果新增规则，其自增id会覆盖掉配置中心的某些规则
8. com.alibaba.csp.sentinel.dashboard.controller.gateway 包下的 GatewayFlowRuleController 修改了获取规则和发布规则的地方，以支持使用nacos做持久化  
10. 修改 com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher 下 publish 方法的返回值为 boolean，这个接口的各个实现类中也修改了此方法的返回值为boolean  
11. 在 resources/application.properties 中新增了连接 nacos 的相关配置  
12. 新增 resources/sentinel.properties 配置文件，让当前项目也注册到dashboard，方便查看此应用是否健康  

## 2. 生产环境部署方案
对于dashboard的部署性质主要考虑两方面的内容，一是流控规则的一致性，二是监控数据和心跳数据的准确性，对于第一个问题，由于使用了nacos作为持久化存储，
所以如果部署多个dashboard节点，这几个节点之间的规则数据是可以达到最终一致的，需要考虑的就是心跳数据和监控数据，对于心跳数据来说，一般几秒才发送一次，
其实dashboard的负载压力是很小的，关键在与监控数据，是由dashboard主动向各个客户端去拉取的，这个时候如果客户端数量很多，那dashboard可能就会处理
不过来了，这个时候其实可以对客户端进行分片处理，假设有100个应用，每个应用部署3个实例，那整个集群一共有300个实例，那可以这样分片，部署3个dashboard
实例，第1个dashboard实例负责35个应用的实例，第2个dashboard实例负责另外35个应用的实例，第3个dashboard实例负责剩下的30个应用的实例，然后，在
3个dashboard实例的前面放一个代理(比如nginx)，然后代理根据应用名称来进行路由转发即可，相信到这一步，已经可以满足绝大部分公司的需求了，如果集群规
模继续扩大，代理机器都扛不住心跳请求了，那可以增加代理节点来处理，客户端启动的时候可以随机选择一个代理节点，这样，就可以无限的水平拓容了。综上，
如果是小规模集群，使用VIP方式部署，保证到dashboard的高可用就可以了，如果是中等规模集群，加一个代理来进行分片即可，如果是非常大规模的集群，可以通过
增加代理节点来拓容即可，示例图如下：  
![arch_pic](/docs/images/sentinel-dashboard-deploy-prod.png)

## 3. 更多说明
更多相关使用说明请参考官方文档：https://github.com/alibaba/Sentinel/wiki