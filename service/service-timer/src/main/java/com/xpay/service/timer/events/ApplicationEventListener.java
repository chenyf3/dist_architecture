package com.xpay.service.timer.events;

import com.xpay.service.timer.biz.ExtInstanceBiz;
import com.xpay.service.timer.config.ExtProperties;
import com.xpay.service.timer.config.TimerProperties;
import com.xpay.service.timer.quartz.base.SchedulerManager;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.config.spring.context.event.ServiceBeanExportedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationEventListener implements org.springframework.context.ApplicationListener<ApplicationEvent> {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ExtProperties properties = new ExtProperties();

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if(event instanceof ApplicationReadyEvent){
            onApplicationReadyEvent((ApplicationReadyEvent) event);
        }else if(event instanceof ServiceBeanExportedEvent){
            onServiceBeanExportedEvent((ServiceBeanExportedEvent) event);
        }
    }

    /**
     * SpringBoot应用准备就绪的事件(可理解为SpringBoot应用启动完成)
     * @param event
     */
    private void onApplicationReadyEvent(ApplicationReadyEvent event){
        SchedulerManager schedulerManager = applicationContext.getBean(SchedulerManager.class);
        TimerProperties timerProperties = applicationContext.getBean(TimerProperties.class);

        properties.setInstanceId(schedulerManager.getSchedulerInstanceId());
        properties.setCheckInInterval(Integer.valueOf(timerProperties.getQuartz().getOrDefault("org.quartz.jobStore.clusterCheckinInterval", "5000")));
        startSchedulerIfAllReady();
    }

    /**
     * 此为dubbo的任一服务暴露完成之后发布的事件，
     * @param event
     */
    private void onServiceBeanExportedEvent(ServiceBeanExportedEvent event) {
        if(properties.getRpcAddress() != null){ //理论上来说所有接口的前缀地址都是相同的，只要有取到一个即可
            return;
        }

        StringBuilder strBuild = new StringBuilder();
        List<URL> urls = event.getServiceBean().getExportedUrls();
        for(URL url : urls){
            if(! "dubbo".equalsIgnoreCase(url.getProtocol())){
                continue;
            }
            strBuild.append(url.getProtocol())
                    .append("://")
                    .append(url.getAddress())
                    .append("?group=")
                    .append(url.getParameter("group", ""))
                    .append("&version=")
                    .append(url.getParameter("version", ""))
                    .append("&timeout=")
                    .append(url.getParameter("timeout", ""));
            break;
        }
        if(properties.getRpcAddress() == null){
            properties.setRpcAddress(strBuild.toString());
        }
        startSchedulerIfAllReady();
    }

    private void startSchedulerIfAllReady(){
        //1.参数检查，如果参数还未准备好，就不执行实例启动
        if(properties.getInstanceId() == null || properties.getRpcAddress() == null){
            return;
        }

        //2.初始化拓展实例
        ExtInstanceBiz extInstanceBiz = applicationContext.getBean(ExtInstanceBiz.class);
        extInstanceBiz.initExtInstance(properties);
        boolean isStandBy = extInstanceBiz.isExtInstanceStandBy();

        //3.启动Quartz实例（要在拓展实例初始化完成之后再启动）
        if(isStandBy){ //被挂起的实例，在重启之后依然处于挂起状态
            logger.info("因拓展实例为挂起状态，故Quartz实例将不启动，请人工启动！instanceId={}", properties.getInstanceId());
        }else{
            //需要启动，因为在SchedulerFactoryBean中配置了不自动启动
            SchedulerManager schedulerManager = applicationContext.getBean(SchedulerManager.class);
            schedulerManager.startScheduler();
            logger.info("Quartz实例已启动 instanceId={}", properties.getInstanceId());
        }
    }
}
