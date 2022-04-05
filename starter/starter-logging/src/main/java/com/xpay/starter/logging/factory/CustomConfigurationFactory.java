package com.xpay.starter.logging.factory;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.json.JsonConfiguration;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 自定义的 ConfigurationFactory
 * @author chenyf
 */
public class CustomConfigurationFactory extends ConfigurationFactory {
    private volatile String name;
    private volatile String content;
    private final static CustomConfigurationFactory INSTANCE = new CustomConfigurationFactory();

    public static synchronized void setConfig(String name, String content){
        INSTANCE.name = name;
        INSTANCE.content = content;
    }

    public static CustomConfigurationFactory getSingleton() {
        return INSTANCE;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[] {".yml", ".yaml", ".json", ".jsn", ".xml", "*"};
    }

    @Override
    public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
        String type = null;
        String name = this.name;
        String content = this.content;
        if(content != null){
            //如果有配置内容，则使用配置的内容替换掉默认的ConfigurationSource
            InputStream inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
            try {
                source = new ConfigurationSource(inputStream);//替换参数传进来的source
            } catch (IOException e) {
                throw new RuntimeException("构建ConfigurationSource时出现异常", e);
            }
            type = name.substring(name.lastIndexOf("."));
        }else{
            String location = source.getLocation();
            if(location != null && location.lastIndexOf(".") > 0){
                type = location.substring(location.lastIndexOf("."));
            }
        }

        if(".xml".equalsIgnoreCase(type)){
            return new XmlConfiguration(loggerContext, source);
        }else if(".json".equalsIgnoreCase(type) || ".jsn".equalsIgnoreCase(type)){
            return new JsonConfiguration(loggerContext, source);
        }else if(".yaml".equalsIgnoreCase(type) || ".yml".equalsIgnoreCase(type)){
            return new YamlConfiguration(loggerContext, source);
        }else{
            throw new RuntimeException("未支持的配置文件类型！ type: " + type);
        }
    }
}
