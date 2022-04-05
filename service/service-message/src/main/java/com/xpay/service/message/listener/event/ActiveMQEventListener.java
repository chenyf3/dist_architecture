package com.xpay.service.message.listener.event;

import com.xpay.common.statics.enums.message.EmailGroupKeyEnum;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.HttpUtil;
import com.xpay.service.message.biz.email.EmailBiz;
import com.xpay.service.message.config.properties.AMQMonitorProperties;
import com.xpay.service.message.listener.model.ExceptionMsg;
import com.xpay.service.message.listener.model.MetaDataDto;
import com.xpay.service.message.listener.model.QueueProperty;
import com.xpay.service.message.utils.ActiveMQUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @description ActiveMQ监控事件监听
 * @author chenyf
 * @date 2019/02/27
 */
@Component
public class ActiveMQEventListener {
    private final static Logger logger = LoggerFactory.getLogger(ActiveMQEventListener.class);
    /**
     * 告警通知任务线程
     */
    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SyncBrokerMetaData syncBrokerMetaData = new SyncBrokerMetaData();

    @Autowired
    private ActiveMQProperties activeMQProperties;
    @Autowired
    private AMQMonitorProperties monitorProperties;
    @Autowired
    EmailBiz emailBiz;

    /**
     * 接收消息积压事件并发送预警邮件
     * @param event
     */
    @EventListener
    public void onMessageBacklog(MessageBacklogEvent event) {
        try {
            String topic = MessageBacklogEvent.TOPIC;
            List<QueueProperty> queueList = event.getQueueList();
            String brokerName = queueList.get(0).getBrokerName();
            StringBuilder content = new StringBuilder();
            content.append("<div>BrokerName: ").append(brokerName).append("</div>").append("<br>")
                    .append("<div>")
                    .append("<table border='1'>")
                    .append("<thead>")
                    .append("<tr><th colspan='4' align='center'>ActiveMQ消息积压</th></tr>")
                    .append("<tr>")
                    .append("<th align='center'>队列名</th>")
                    .append("<th align='center'>消息积压数</th>")
                    .append("<th align='center'>消费者数</th>")
                    .append("<th align='center'>生产者数</th>")
                    .append("</tr>")
                    .append("</thead>")
                    .append("<tbody>");
            for(QueueProperty queueProperty : queueList){
                content.append("<tr>")
                        .append("<td align='left'>").append(queueProperty.getQueueName()).append("</td>")
                        .append("<td align='center'>").append(queueProperty.getQueueSize()).append("</td>")
                        .append("<td align='center'>").append(queueProperty.getConsumerCount()).append("</td>")
                        .append("<td align='center'>").append(queueProperty.getProducerCount()).append("</td>")
                        .append("</tr>");
            }
            content.append("</tbody>").append("</table>").append("</div>");
            executorService.submit(new AlertNotifyTask(topic, content.toString()));
        } catch (Exception e) {
            logger.error("MessageBacklogEvent 发送预警通知时出现异常", e);
        }
    }

    /**
     * 接收无消费者事件并发送预警邮件
     * @param event
     */
    @EventListener
    public void onMessageNoConsumer(MessageNoConsumerEvent event) {
        try{
            String topic = MessageNoConsumerEvent.TOPIC;
            List<QueueProperty> queueList = event.getQueueList();
            String brokerName = queueList.get(0).getBrokerName();

            StringBuilder content = new StringBuilder();
            content.append("<div>BrokerName: ").append(brokerName).append("</div>").append("<br>")
                    .append("<div>")
                    .append("<table border='1'>")
                    .append("<thead>")
                    .append("<tr><th colspan='3' align='center'>ActiveMQ队列无消费者</th></tr>")
                    .append("<tr>")
                    .append("<th align='center'>队列名</th>")
                    .append("<th align='center'>消息积压数</th>")
                    .append("<th align='center'>生产者数</th>")
                    .append("</tr>")
                    .append("</thead>")
                    .append("<tbody>");
            for(QueueProperty queueProperty : queueList){
                content.append("<tr>")
                        .append("<td align='left'>").append(queueProperty.getQueueName()).append("</td>")
                        .append("<td align='center'>").append(queueProperty.getQueueSize()).append("</td>")
                        .append("<td align='center'>").append(queueProperty.getProducerCount()).append("</td>")
                        .append("</tr>");
            }
            content.append("</tbody>").append("</table>").append("</div>");

            executorService.submit(new AlertNotifyTask(topic, content.toString()));
        } catch (Exception e) {
            logger.error("MessageNoConsumerEvent 发送预警通知时出现异常", e);
        }
    }

    /**
     * 同步Master的元数据到Backup
     * @param event
     */
    @EventListener
    public void syncBrokerMetadata(SyncMetaDataEvent event){
        syncBrokerMetaData.doSync(event.getMetaDataDto());
    }

    /**
     * 接收监控异常事件并发送预警邮件
     * @param event
     */
    @EventListener
    public void onMonitorException(MonitorExceptionEvent event) {
        try{
            String topic = MonitorExceptionEvent.TOPIC;
            ExceptionMsg exceptionMsg = event.getExceptionMsg();
            Throwable throwable = exceptionMsg.getThrowable();

            StringBuilder content = new StringBuilder();
            content.append("<div>BrokerName: ").append(exceptionMsg.getBroker()).append("</div>").append("<br>")
                    .append("<div>")
                    .append("<table border='1'>")
                    .append("<thead>")
                    .append("<tr><th colspan='2' align='center'>ActiveMQ监控异常</th></tr>")
                    .append("</thead>")
                    .append("<tbody>");

            content.append("<tr>")
                    .append("<td align='center' style='font-weight:bold'>描述</td><td align='left'>").append(exceptionMsg.getRemark()).append("</td>")
                    .append("</tr><tr>")
                    .append("<td align='center' style='font-weight:bold'>异常信息</td><td align='left'>").append(throwable.getClass().getName()+": "+throwable.getMessage()).append("</td>")
                    .append("</tr>");
            content.append("</tbody>").append("</table>").append("</div>");
            executorService.submit(new AlertNotifyTask(topic, content.toString()));
        } catch (Exception e) {
            logger.error("MonitorExceptionEvent 发送预警通知时出现异常", e);
        }
    }

    private class AlertNotifyTask implements Runnable {
        private String topic;
        private String content;

        AlertNotifyTask(String topic, String content) {
            this.topic = topic;
            this.content = content;
        }

        @Override
        public void run() {
            try {
                emailBiz.sendHtml(EmailGroupKeyEnum.SYS_MONITOR_ALERT_GROUP.name(), topic, content);
            } catch (Exception e) {
                logger.error("alert notify exception", e);
            }
        }
    }

    private class SyncBrokerMetaData {
        public void doSync(MetaDataDto metaDataDto) {
            AMQMonitorProperties.Node node = metaDataDto.getNode();
            try{
                String url = node.getHost() + ":" + node.getPort() + "/api/jolokia/exec/org.apache.activemq:type=Broker,brokerName=" + node.getBrokerName();
                Map<String, String> header = ActiveMQUtil.buildHeader(node);
                for(String destName : metaDataDto.getDestinations()){
                    boolean isVTopic = ActiveMQUtil.isVirtualTopic(destName);
                    String realUrl = url + (isVTopic ? "/addTopic/" + destName : "/addQueue/" + destName);

                    HttpUtil.getSync(realUrl, header, null);
                }
                logger.info("完成元数据同步 host:port={} brokerName={} destNameSet={}",  node.getHost(), node.getBrokerName(), JsonUtil.toJson(metaDataDto.getDestinations()));
            }catch(Exception e){
                logger.error("同步元数据时出现异常 syncDestNameSet = {}", JsonUtil.toJson(metaDataDto.getDestinations()), e);
            }
        }
    }
}
