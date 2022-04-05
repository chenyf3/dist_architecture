package com.xpay.starter.logging.listener;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.xpay.starter.logging.factory.CustomConfigurationFactory;
import com.xpay.starter.logging.config.Const;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationPreparedEvent;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Properties;

/**
 * 自定义的ApplicationListener，用以从nacos配置中心获取日志配置文件(log4j2.xml)
 * @author chenyf
 */
public class CustomLoggingApplicationListener implements GenericApplicationListener {
    private static final Class<?>[] EVENT_TYPES = { ApplicationStartingEvent.class,
            ApplicationEnvironmentPreparedEvent.class, ApplicationPreparedEvent.class, ContextClosedEvent.class,
            ApplicationFailedEvent.class };
    private static final Class<?>[] SOURCE_TYPES = { SpringApplication.class, ApplicationContext.class };
    public static final int DEFAULT_ORDER = LoggingApplicationListener.DEFAULT_ORDER - 1;

    /**
     * 要在 {@link org.springframework.boot.context.logging.LoggingApplicationListener} 前面执行
     */
    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Override
    public boolean supportsEventType(ResolvableType resolvableType) {
        return isAssignableFrom(resolvableType.getRawClass(), EVENT_TYPES);
    }

    @Override
    public boolean supportsSourceType(Class<?> sourceType) {
        return isAssignableFrom(sourceType, SOURCE_TYPES);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if(event instanceof ApplicationEnvironmentPreparedEvent) {
            onApplicationEnvironmentPreparedEvent((ApplicationEnvironmentPreparedEvent) event);
        }
    }

    private void onApplicationEnvironmentPreparedEvent(ApplicationEnvironmentPreparedEvent event) {
        //告知 log4j2 使用 CustomConfigurationFactory 作为配置工厂，参考：https://logging.apache.org/log4j/2.x/manual/customconfig.html
        ConfigurationFactory.setConfigurationFactory(CustomConfigurationFactory.getSingleton());
        try {
            String content = getContentFromConfigServer(event.getEnvironment());
            //把配置文件的内容放到 CustomConfigurationFactory 里面去，后续将用此配置内容来生成log4j2的Configuration对象
            String dataId = event.getEnvironment().getProperty(Const.LOG_CONFIG_NAME, Const.DEFAULT_LOG_CONFIG);
            CustomConfigurationFactory.setConfig(dataId, content);
        } catch(Exception e) {
            throw new RuntimeException("从nacos配置中心获取配置异常", e);
        }
    }

    private String getContentFromConfigServer(ConfigurableEnvironment environment) throws Exception {
        String serverAddr = environment.getProperty(Const.CONFIG_CENTER_SERVER_ADDR, "");
        String namespace = environment.getProperty(Const.CONFIG_CENTER_NAMESPACE, "");
        String username = environment.getProperty(Const.CONFIG_CENTER_USER, "");
        String password = environment.getProperty(Const.CONFIG_CENTER_PASSWORD, "");
        String group = environment.getProperty(Const.CONFIG_CENTER_GROUP, Const.DEFAULT_CONFIG_GROUP);
        String dataId = environment.getProperty(Const.LOG_CONFIG_NAME, Const.DEFAULT_LOG_CONFIG);
        if(serverAddr.trim().length() == 0){
            throw new RuntimeException("请设置nacos配置中心地址！");
        }

        Properties properties = new Properties();
        properties.put("serverAddr", serverAddr);
        properties.put("namespace", namespace);
        properties.put("username", username);
        properties.put("password", password);
        ConfigService configService = NacosFactory.createConfigService(properties);

        long timeout = 5000;
        return configService.getConfig(dataId, group, timeout);
    }

    private boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
        if (type != null) {
            for (Class<?> supportedType : supportedTypes) {
                if (supportedType.isAssignableFrom(type)) {
                    return true;
                }
            }
        }
        return false;
    }
}
