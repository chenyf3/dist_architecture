package com.xpay.service.message.config;

import com.xpay.service.message.config.properties.AMQMonitorProperties;
import com.xpay.service.message.task.ActiveMQMonitorTask;
import com.xpay.service.message.utils.ActiveMQUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

@EnableConfigurationProperties(AMQMonitorProperties.class)
@SpringBootConfiguration
public class ActiveMQMonitorConfig {
    @Autowired
    AMQMonitorProperties amqMonitorProperties;
    @Autowired
    ActiveMQProperties activeMQProperties;

    @ConditionalOnProperty(value = "spring.activemq.monitor.enabled", havingValue = "true")
    @Bean
    public ActiveMQMonitorTask activeMQMonitorTask(ApplicationEventPublisher publisher) {
        List<AMQMonitorProperties.Node> nodes = amqMonitorProperties.getNodes();
        if(nodes == null || nodes.size() <= 0){ //没有需要监控的节点，直接返回
            return null;
        }

        String[] brokerHostPort = ActiveMQUtil.splitHostPort(activeMQProperties.getBrokerUrl());
        List<String> hostPortList = diffRepeat(brokerHostPort);
        List<AMQMonitorProperties.Node> newNodes = new ArrayList<>();
        int len = Math.min(hostPortList.size(), nodes.size());
        for(int i=0; i<len; i++){
            AMQMonitorProperties.Node node = nodes.get(i);

            String hostPort = hostPortList.get(i);
            String host = hostPort.substring(0, hostPort.lastIndexOf(":"));
            node.setHost(host);
            if(node.getPort() == null){
                node.setPort(amqMonitorProperties.getPort());
            }
            if(node.getUsername() == null){
                node.setUsername(amqMonitorProperties.getUsername());
            }
            if(node.getPassword() == null){
                node.setPassword(amqMonitorProperties.getPassword());
            }
            newNodes.add(node);
        }
        amqMonitorProperties.setNodes(newNodes);

        return new ActiveMQMonitorTask(amqMonitorProperties, publisher);
    }

    private List<String> diffRepeat(String[] brokerHostPort){
        List<String> hostList = new ArrayList<>();
        for(int i=0; i<brokerHostPort.length; i++){
            String hostPort = brokerHostPort[i];
            if(! hostList.contains(hostPort)){
                hostList.add(hostPort);
            }
        }
        return hostList;
    }
}
