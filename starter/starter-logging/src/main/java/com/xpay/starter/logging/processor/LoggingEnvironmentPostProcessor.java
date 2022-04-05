package com.xpay.starter.logging.processor;

import com.xpay.starter.logging.config.Const;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 环境变量处理器，在这里设置变量到System系统属性中，在log4j2.xml里面就可以通过 ${sys:xxx} 来获取变量值
 * @author chenyf
 */
public class LoggingEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {
    public static final int DEFAULT_ORDER = ConfigFileApplicationListener.DEFAULT_ORDER + 1;

    /**
     * 顺序要比 {@link org.springframework.boot.context.config.ConfigFileApplicationListener} 低，这样才能保证Spring的配置文件已被读取到Environment里面
     * @return
     */
    @Override
    public int getOrder() {
        return DEFAULT_ORDER;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String logHome = environment.getProperty(Const.LOG_HOME, Const.DEFAULT_LOG_HOME);
        String logName = environment.getProperty(Const.LOG_NAME, "");
        if("".equals(logName.trim())){
            logName = environment.getProperty("spring.application.name", "");
        }

        System.setProperty("logHome", logHome);
        System.setProperty("logName", logName);
    }
}
