package com.xpay.starter.plugin.pluginImpl;

import com.xpay.common.statics.constants.common.LogMarker;
import com.xpay.common.statics.constants.common.RemoteLogger;
import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.starter.plugin.consts.DeadLetter;
import com.xpay.starter.plugin.plugins.MQSender;
import com.xpay.starter.plugin.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * RabbitMQ消息发送器
 * @author chenyf
 */
public class RAQSender implements MQSender {
    public final static String DELAY_TYPE = "delay";
    private final static int SEND_TOO_LONG = 2500;//发送消息耗时太长的临界值
    private final Object lock = new Object();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Logger remoteLogger = LoggerFactory.getLogger(RemoteLogger.FAILOVER);
    private Map<String, String> exchangeNameMap = new HashMap<>();
    private Map<String, String> queueNameMap = new HashMap<>();
    private Map<String, String> bindingNameMap = new HashMap<>();
    private RabbitTemplate rabbitTemplate;
    private RabbitAdmin rabbitAdmin;

    public RAQSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitAdmin = new RabbitAdmin(rabbitTemplate);
    }

    @Override
    public boolean sendOne(MsgDto msg) {
        RAMQDest dest = getRAMQDest(msg.getTopic(), msg.getTags());
        declareAndBindIfNeed(dest);
        Message message = buildMessage(msg);
        long start = System.currentTimeMillis();
        try {
            if(true){
                throw new RuntimeException("测试RemoteLogger");
            }
            rabbitTemplate.send(dest.getExchange(), dest.getRoutingKey(), message);
        } catch (Exception e) {
            remoteLogger.error(MarkerFactory.getMarker(LogMarker.RABBITMQ_SEND_FAIL), Utils.toJson(msg), e);
        }
        printSendLongTime(start);
        return true;
    }

    @Override
    public void sendOne(MsgDto msg, Consumer<MsgDto> onFail) {
        boolean isOk = false;
        try{
            isOk = sendOne(msg);
        }catch(Throwable e){
            msg.setCause(e);
        }
        if(! isOk && onFail != null){
            onFail.accept(msg);
        }
    }

    /**
     * 发送延时消息
     *  1、rabbitmq的延时队列只能定义一个专用的Exchange，如果一个Exchange已经被定义成延时的Exchange，请不要再往这个Exchange发送非延时消息
     *
     * @param msg
     * @param delaySec
     * @return
     */
    @Override
    public boolean sendOneDelay(MsgDto msg, int delaySec) {
        if(msg.getHeader() == null){
            msg.setHeader(new HashMap<>());
        }
        msg.getHeader().put(MessageProperties.X_DELAY, String.valueOf(delaySec * 1000));

        //强制设置Exchange类型为delay
        String topic = msg.getTopic();
        if(topic.indexOf(MsgDto.SEPARATOR) > 0){
            String[] exchangeArr = topic.split(MsgDto.SEPARATOR);
            msg.setTopic(exchangeArr[0] + MsgDto.SEPARATOR + DELAY_TYPE);
        }else{
            msg.setTopic(topic + MsgDto.SEPARATOR + DELAY_TYPE);
        }
        return sendOne(msg);
    }

    @Override
    public boolean sendOrderly(MsgDto msg, String hashKey, long timeout){
        throw new RuntimeException("Not Support!");
    }

    @Override
    public boolean sendOne(String destination, String body, Map<String, String> header) {
        String[] destArr = destination.split(MsgDto.SEPARATOR);
        RAMQDest dest = getRAMQDest(destArr[0], destArr[1]);
        declareAndBindIfNeed(dest);

        byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
        MessageBuilder builder = MessageBuilder.withBody(bodyBytes);
        if(header != null && header.size() > 0){
            for(Map.Entry<String, String> entry : header.entrySet()){
                builder.setHeader(entry.getKey(), entry.getValue());
            }
        }

        Message message = builder.build();
        try {
            rabbitTemplate.send(dest.getExchange(), dest.getRoutingKey(), message);
        } catch(Exception e) {
            remoteLogger.error(MarkerFactory.getMarker(LogMarker.RABBITMQ_SEND_FAIL), body, e);
        }
        return true;
    }

    @Override
    public void sendOneWay(MsgDto msg) {
        sendOneAsync(msg, null);
    }

    @Override
    public void sendOneAsync(MsgDto msg, Consumer<MsgDto> callback) {
        CompletableFuture.runAsync(() -> {
            try {
                sendOne(msg);
            } catch(Throwable e) {
                msg.setCause(e);
            }
            if(callback != null){
                callback.accept(msg);
            }
        });
    }

    @Override
    public void sendOneDelayAsync(MsgDto msg, int delaySec) {
        CompletableFuture.runAsync(() -> sendOneDelay(msg, delaySec));
    }

    @Override
    public boolean sendBatch(String destination, List<? extends MsgDto> msgList) {
        throw new RuntimeException("Not Support!");
    }

    @Override
    public boolean sendBatch(List<? extends MsgDto> msgList) {
        throw new RuntimeException("Not Support!");
    }

    @Override
    public boolean sendTrans(MsgDto msg) {
        throw new RuntimeException("Not Support!");
    }

    /**
     * 发送消息并同步等待响应结果，只适合点对点模式
     * @param msg
     * @return
     */
    @Override
    public MsgDto sendAndReceive(MsgDto msg) {
        RAMQDest dest = getRAMQDest(msg.getTopic(), msg.getTags());
        declareAndBindIfNeed(dest);
        Message message = buildMessage(msg);
        long start = System.currentTimeMillis();
        Message respMsg = rabbitTemplate.sendAndReceive(dest.getExchange(), dest.getRoutingKey(), message);
        printSendLongTime(start);
        try{
            byte[] body = respMsg.getBody();
            return Utils.jsonToBean(body, MsgDto.class);
        }catch (Exception e){
            throw new RuntimeException("对响应数据进行转换的时候出现异常", e);
        }
    }

    @Override
    public void sendAndReceive(MsgDto msg, Consumer<MsgDto> onMessage) {
        CompletableFuture.runAsync(() -> {
            MsgDto respDto = null;
            try {
                respDto = sendAndReceive(msg);
            } catch(Exception e) {
                MsgDto failDto = new MsgDto();
                failDto.setCause(e);
            }
            onMessage.accept(respDto);
        });
    }

    @Override
    public <T> T getTemplate() {
        return (T) rabbitTemplate;
    }

    @Override
    public void destroy(){
        if(rabbitTemplate != null){
            rabbitTemplate.destroy();
        }
    }

    private RAMQDest getRAMQDest(String topic, String tags) {
        String[] exchangeArr = topic.split(MsgDto.SEPARATOR);
        String type = (exchangeArr.length > 1 && exchangeArr[1].length() > 0) ? exchangeArr[1] : ExchangeTypes.DIRECT; //默认使用DIRECT类型的Exchange
        String[] queueArr = tags.split(",");

        RAMQDest dest = new RAMQDest();
        dest.setExchange(exchangeArr[0]);
        dest.setType(type);
        dest.setQueue(queueArr[0]);
        dest.setRoutingKey(dest.getQueue());//直接使用队列名作为RoutingKey
        dest.getBindQueues().addAll(Arrays.asList(queueArr));
        return dest;
    }

    /**
     * 判断 Exchange、Queue、Binding 是否再本地的Map缓存中存在，如果不存在，则会向Broker端发起创建请求，创建完毕之后再放到本地的Map中缓存起来，
     * 这样做的好处是可以确保发送到Exchange的消息不会存在没法路由到Queue而被丢弃的情况，不过，如果有人在Broker端手动删除了Exchange或Queue或者Binding，
     * 客户端是无法感知到的，就不会再去重新创建这些。
     * @param dest
     */
    private void declareAndBindIfNeed(RAMQDest dest) {
        if (! exchangeNameMap.containsKey(dest.getExchange())) {
            synchronized(lock){
                if (! exchangeNameMap.containsKey(dest.getExchange())) {
                    Exchange exchange = buildExchange(dest.getExchange(), dest.getType());
                    rabbitAdmin.declareExchange(exchange);
                    exchangeNameMap.putIfAbsent(dest.getExchange(), dest.getType());
                }
            }
        }

        for(String queueName : dest.getBindQueues()){
            String routingKey = queueName;//直接使用队列名作为RoutingKey

            if(! queueNameMap.containsKey(queueName)){
                synchronized(lock){
                    if(! queueNameMap.containsKey(queueName)){
                        //配置死信队列，这样当消息消费失败的时候会转发到死信队列，以供人工处理
                        Map<String, Object> args = Collections.singletonMap(DeadLetter.DLX_ARGS, DeadLetter.DLX);
                        Queue queue = QueueBuilder.durable(queueName).withArguments(args).build();
                        rabbitAdmin.declareQueue(queue);
                        queueNameMap.putIfAbsent(queueName, routingKey);
                    }
                }
            }

            String bindingKey = dest.getExchange() + MsgDto.SEPARATOR + queueName;
            if(! bindingNameMap.containsKey(bindingKey)){
                synchronized(lock){
                    if(! bindingNameMap.containsKey(bindingKey)){
                        Exchange exchange = buildExchange(dest.getExchange(), dest.getType());
                        Queue queue = QueueBuilder.durable(queueName).build();
                        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey).noargs();

                        rabbitAdmin.declareBinding(binding);
                        bindingNameMap.putIfAbsent(bindingKey, routingKey);
                    }
                }
            }
        }
    }

    private Exchange buildExchange(String name, String type){
        Exchange exchange;
        if(ExchangeTypes.DIRECT.equals(type)){ //发送到此Exchange的消息会被转发到RoutingKey中指定的Queue，如果为每一个Queue都指定唯一一个RoutingKey，等同于传统JMS中Queue类型的消息
            exchange = ExchangeBuilder.directExchange(name).build();
        }else if(ExchangeTypes.FANOUT.equals(type)){ //发送到此Exchange的消息会被转发到与该Exchange绑定(Binding)的所有Queue上，无需RoutingKey，等同于传统JMS中Topic类型的消息
            exchange = ExchangeBuilder.fanoutExchange(name).build();
        }else if(DELAY_TYPE.equals(type)){ //延时消息模式
            //延时消息需要在Broker端安装 rabbitmq_delayed_message_exchange 插件，参考：https://github.com/rabbitmq/rabbitmq-delayed-message-exchange
            Map<String, Object> args = new HashMap<>();
            args.put("x-delayed-type", ExchangeTypes.DIRECT);
            exchange = new CustomExchange(name, "x-delayed-message", true, false, args);
        }else{
            //RabbitMQ还支持Topic和Header两种类型的交换器，Topic类型的交换器为FANOUT模式的变种，只不过FANOUT是全部都转发，
            // 而TOPIC模式则只转发给RoutingKey相匹配的Queue，相当于在FANOUT上多了一层过滤能力，所以，如果有需要使用到Topic模式的情况，
            // 完全可以通过多增加几个FANOUT来解决，而Header模式基本上也不存在使用的情况，所以这里直接没必要支持这两种类型的交换器了
            throw new RuntimeException("未支持的Exchange类型: " + type);
        }
        return exchange;
    }

    private Message buildMessage(MsgDto msg){
        Map<String, String> header = msg.getHeader();
        msg.setHeader(null);//置空，避免冗余浪费空间

        byte[] body = Utils.toJson(msg).getBytes(StandardCharsets.UTF_8);
        MessageBuilder builder = MessageBuilder.withBody(body);
        if(header != null && header.size() > 0){
            for(Map.Entry<String, String> entry : header.entrySet()){
                builder.setHeader(entry.getKey(), entry.getValue());
            }
        }

        Message message = builder.build();
        return message;
    }

    private void printSendLongTime(long start){
        long cost = System.currentTimeMillis() - start;
        if(cost > SEND_TOO_LONG){
            logger.warn("消息发送时间过长，耗时{}ms", cost);
        }
    }

    /**
     * rabbitmq的目的地
     */
    static class RAMQDest {
        private String exchange;//Exchange名称
        private String type;//Exchange类型
        private String queue;//本条消息要发往的队列名
        private String routingKey;//本条消息的RoutingKey
        private List<String> bindQueues = new ArrayList<>();//要和当前Exchange绑定的Queue列表

        public String getExchange() {
            return exchange;
        }

        public void setExchange(String exchange) {
            this.exchange = exchange;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getQueue() {
            return queue;
        }

        public void setQueue(String queue) {
            this.queue = queue;
        }

        public String getRoutingKey() {
            return routingKey;
        }

        public void setRoutingKey(String routingKey) {
            this.routingKey = routingKey;
        }

        public List<String> getBindQueues() {
            return bindQueues;
        }

        public void setBindQueues(List<String> bindQueues) {
            this.bindQueues = bindQueues;
        }
    }
}
