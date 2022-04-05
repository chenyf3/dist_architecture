package com.xpay.starter.logging.config;

public class Const {
    /**--------------------- 配置中心相关的配置名 -------------------*/
    public final static String CONFIG_CENTER_SERVER_ADDR = "spring.cloud.nacos.config.server-addr";
    public final static String CONFIG_CENTER_NAMESPACE = "spring.cloud.nacos.config.namespace";
    public final static String CONFIG_CENTER_GROUP = "spring.cloud.nacos.config.group";
    public final static String CONFIG_CENTER_USER = "spring.cloud.nacos.config.username";
    public final static String CONFIG_CENTER_PASSWORD = "spring.cloud.nacos.config.password";

    /**--------------------- 日志相关的配置名 -------------------*/
    public final static String LOG_CONFIG_NAME = "logging.config-name";
    public final static String LOG_HOME = "logging.log-home";
    public final static String LOG_NAME = "logging.log-name";

    /**--------------------- 一些默认值 -------------------*/
    public final static String DEFAULT_CONFIG_GROUP = "DEFAULT_GROUP";
    public final static String DEFAULT_LOG_CONFIG = "log4j2.xml";
    public final static String DEFAULT_LOG_HOME = "logs";
}
