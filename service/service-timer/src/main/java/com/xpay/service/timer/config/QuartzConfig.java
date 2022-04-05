package com.xpay.service.timer.config;

import com.xpay.service.timer.quartz.base.JobManager;
import com.xpay.service.timer.quartz.base.SchedulerManager;
import com.xpay.service.timer.quartz.processor.EventProcessor;
import com.xpay.service.timer.quartz.listener.SchedulerListener;
import com.xpay.service.timer.quartz.listener.JobListener;
import com.xpay.service.timer.quartz.listener.TriggerListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author chenyf on 2017/3/9.
 */
@SpringBootConfiguration
public class QuartzConfig {
    @Autowired
    TimerProperties timerProperties;

    /**
     * spring用来和Quartz交互的对象
     * https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/quartz/SchedulerFactoryBean.html
     * @return
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(ApplicationContext applicationContext,
                                                     DataSource dataSource,
                                                     PlatformTransactionManager transactionManager,
                                                     TriggerListener triggerListener,
                                                     JobListener jobListener,
                                                     SchedulerListener schedulerListener){
        //SpringBeanJobFactory在Quartz框架实例化org.quartz.Job的实现类时，能够实现依赖注入
        SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);

        SchedulerFactoryBean schedulerFactoryBean = new SchedulerFactoryBean();
        schedulerFactoryBean.setJobFactory(jobFactory);
        //设置数据源
        schedulerFactoryBean.setDataSource(dataSource);
        //设置事务管理器
        schedulerFactoryBean.setTransactionManager(transactionManager);
        schedulerFactoryBean.setOverwriteExistingJobs(false);
        schedulerFactoryBean.setWaitForJobsToCompleteOnShutdown(true);
        //设置全局trigger监听器
        schedulerFactoryBean.setGlobalTriggerListeners(triggerListener);
        //设置全局job监听器
        schedulerFactoryBean.setGlobalJobListeners(jobListener);
        schedulerFactoryBean.setSchedulerListeners(schedulerListener);
        //设置成手工启动，因为启动之前可以做一些判断是否可以启动
        schedulerFactoryBean.setAutoStartup(false);

        Properties properties = new Properties();
        timerProperties.getQuartz().forEach((key, value) -> {
            properties.setProperty(key, value);
        });
        schedulerFactoryBean.setQuartzProperties(properties);

        return schedulerFactoryBean;
    }

    @Bean
    public SchedulerManager schedulerManager(SchedulerFactoryBean schedulerFactoryBean){
        return new SchedulerManager(schedulerFactoryBean);
    }

    @Bean
    public JobManager jobManager(SchedulerFactoryBean schedulerFactoryBean){
        return new JobManager(schedulerFactoryBean);
    }

    /**
     * 定义一个任务监听器
     * @return
     */
    @Bean
    public JobListener jobListener(EventProcessor eventProcessor){
        return new JobListener(eventProcessor);
    }

    /**
     * 定义一个触发器监听器
     * @return
     */
    @Bean
    public TriggerListener triggerListener(EventProcessor eventProcessor){
        return new TriggerListener(eventProcessor);
    }

    @Bean
    public SchedulerListener schedulerListener(EventProcessor eventProcessor){
        return new SchedulerListener(eventProcessor);
    }

    @Bean
    public EventProcessor eventProcessor(){
        return new EventProcessor();
    }
}
