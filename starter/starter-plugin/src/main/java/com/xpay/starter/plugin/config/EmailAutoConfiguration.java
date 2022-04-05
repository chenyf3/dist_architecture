package com.xpay.starter.plugin.config;

import com.xpay.starter.plugin.client.EmailClient;
import com.xpay.starter.plugin.properties.MailProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.*;

@ConditionalOnProperty(name = "email.enable", havingValue = "true")
@ConditionalOnClass(JavaMailSenderImpl.class)
@EnableConfigurationProperties(MailProperties.class)
@Configuration
public class EmailAutoConfiguration {

    @ConditionalOnMissingBean
    @Bean("emailClient")
    public EmailClient emailClient(MailProperties properties){
        LinkedHashMap<String, JavaMailSender> map = new LinkedHashMap<>();
        LinkedHashMap<String, String> senderInfo = new LinkedHashMap<>();
        for(MailProperties.Sender sendConfig : properties.getSenders()){
            JavaMailSender mailSender = buildJavaMailSender(sendConfig);
            map.put(sendConfig.getUsername(), mailSender);
            senderInfo.put(sendConfig.getUsername(), sendConfig.getDesc());
        }
        return new EmailClient(map, senderInfo);
    }

    private JavaMailSender buildJavaMailSender(MailProperties.Sender sendConfig) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(sendConfig.getHost());
        if (sendConfig.getPort() != null) {
            sender.setPort(sendConfig.getPort());
        }
        sender.setUsername(sendConfig.getUsername());
        sender.setPassword(sendConfig.getPassword());
        sender.setProtocol(sendConfig.getProtocol());
        if (sendConfig.getDefaultEncoding() != null) {
            sender.setDefaultEncoding(sendConfig.getDefaultEncoding().name());
        }
        if (!sendConfig.getProperties().isEmpty()) {
            Properties properties = new Properties();
            properties.putAll(sendConfig.getProperties());
            sender.setJavaMailProperties(properties);
        }
        return sender;
    }
}
