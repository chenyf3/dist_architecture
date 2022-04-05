package com.xpay.starter.sentinel.manager;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.nacos.NacosDataSource;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.xpay.starter.sentinel.config.NacosConst;
import com.xpay.starter.sentinel.config.ClientProperties;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 流控规则管理器
 * @author chenyf
 */
public class RuleManager {
    private final AtomicBoolean initFinish = new AtomicBoolean(false);
    private final String appName;
    private final ClientProperties.RuleServer ruleServer;

    public RuleManager(String appName, ClientProperties.RuleServer ruleServer){
        this.appName = appName;
        this.ruleServer = ruleServer;
    }

    public void init(){
        if(initFinish.compareAndSet(false, true)){
            initDynamicRuleProperty();
        }
    }

    public void destroy(){

    }

    /**
     * 初始化流控规则，从nacos配置中心动态获取流控规则
     * 如果不想使用这里的方式，也可以使用 spring cloud 来配置流控规则，详情请参考 spring-cloud-starter-alibaba-sentinel 官方文档中关于
     * 配置流控规则的：https://github.com/alibaba/spring-cloud-alibaba/wiki/Sentinel
     */
    private void initDynamicRuleProperty() {
        if (ruleServer.getServerAddr() == null || ruleServer.getServerAddr().trim().length() == 0) {
            return;
        }

        String groupId = ruleServer.getGroupId();
        Properties properties = new Properties();
        properties.put(NacosConst.SERVER_ADDR, ruleServer.getServerAddr());
        properties.put(NacosConst.NAMESPACE, ruleServer.getNamespace());
        properties.put(NacosConst.USERNAME, ruleServer.getUsername());
        properties.put(NacosConst.PASSWORD, ruleServer.getPassword());
        //配置FlowRule的限流规则从nacos配置中心获取，实现动态更新
        ReadableDataSource<String, List<FlowRule>> flowRuleSource = new NacosDataSource<>(properties, groupId,
                appName + NacosConst.FLOW_DATA_ID_POSTFIX, source -> JSON.parseObject(source, new TypeReference<List<FlowRule>>() {}));
        FlowRuleManager.register2Property(flowRuleSource.getProperty());

        //配置DegradeRule的熔断规则从nacos配置中心获取，实现动态更新
        ReadableDataSource<String, List<DegradeRule>> degradeRuleSource = new NacosDataSource<>(properties, groupId,
                appName + NacosConst.DEGRADE_DATA_ID_POSTFIX, source -> JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {}));
        DegradeRuleManager.register2Property(degradeRuleSource.getProperty());

        //配置SystemRule的熔断规则从nacos配置中心获取，实现动态更新
        ReadableDataSource<String, List<SystemRule>> systemRuleSource = new NacosDataSource<>(properties, groupId,
                appName + NacosConst.SYSTEM_DATA_ID_POSTFIX, source -> JSON.parseObject(source, new TypeReference<List<SystemRule>>() {}));
        SystemRuleManager.register2Property(systemRuleSource.getProperty());

        //配置ParamFlowRule的限流规则从nacos配置中心获取，实现动态更新
        ReadableDataSource<String, List<ParamFlowRule>> paramRuleSource = new NacosDataSource<>(properties, groupId,
                appName + NacosConst.PARAM_FLOW_DATA_ID_POSTFIX, source -> JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {}));
        ParamFlowRuleManager.register2Property(paramRuleSource.getProperty());

        //配置AuthorityRule的授权规则从nacos配置中心获取，实现动态更新
        ReadableDataSource<String, List<AuthorityRule>> authorityRuleSource = new NacosDataSource<>(properties, groupId,
                appName + NacosConst.AUTHORITY_DATA_ID_POSTFIX, source -> JSON.parseObject(source, new TypeReference<List<AuthorityRule>>() {}));
        AuthorityRuleManager.register2Property(authorityRuleSource.getProperty());
    }
}
