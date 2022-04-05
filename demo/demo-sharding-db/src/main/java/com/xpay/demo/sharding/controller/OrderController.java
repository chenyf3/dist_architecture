package com.xpay.demo.sharding.controller;

import com.xpay.common.utils.RandomUtil;
import com.xpay.demo.sharding.biz.OrderBiz;
import com.xpay.demo.sharding.entity.Order;
import com.xpay.demo.sharding.entity.Product;
import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.starter.sharding.properties.GroupRule;
import com.xpay.starter.sharding.utils.ShardingRuleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    OrderBiz orderBiz;
    @Autowired
    RedisClient redisClient;

    //这里的group名称需要跟shardingRule.json中的名称保持一致，此处为了测试方便，所以给每个组分配不同的key，以便可以直接测试某个分组
    private String tableName = "t_order";
    private String group01_name = "group01";
    private String group01_id_key = "group01_orderId_key";

    private String group02_name = "group02";
    private String group02_id_key = "group02_orderId_key";

    private String group03_name = "group03";
    private String group03_id_key = "group03_orderId_key";

    private Long userId = 1001L;

    /**
     * 重置订单各个组id
     *

     TRUNCATE TABLE `g01-orderds-01`.`t_order_1`;
     TRUNCATE TABLE `g02-orderds-01`.`t_order_1`;
     TRUNCATE TABLE `g02-orderds-01`.`t_order_2`;
     TRUNCATE TABLE `g02-orderds-02`.`t_order_1`;
     TRUNCATE TABLE `g02-orderds-02`.`t_order_2`;
     TRUNCATE TABLE `g03-orderds-01`.`t_order_1`;
     TRUNCATE TABLE `g03-orderds-02`.`t_order_1`;
     TRUNCATE TABLE `g03-orderds-03`.`t_order_1`;
     TRUNCATE TABLE `g01-orderds-01`.`t_order_item_1`;
     TRUNCATE TABLE `g02-orderds-01`.`t_order_item_1`;
     TRUNCATE TABLE `g02-orderds-01`.`t_order_item_2`;
     TRUNCATE TABLE `g02-orderds-02`.`t_order_item_1`;
     TRUNCATE TABLE `g02-orderds-02`.`t_order_item_2`;
     TRUNCATE TABLE `g03-orderds-01`.`t_order_item_1`;
     TRUNCATE TABLE `g03-orderds-02`.`t_order_item_1`;
     TRUNCATE TABLE `g03-orderds-03`.`t_order_item_1`;

     * @return
     */
    @RequestMapping(value = "/resetOrderId")
    public Mono<String> resetOrderId(){
        GroupRule groupRule01 = ShardingRuleUtil.getGroupRule(tableName, group01_name);
        GroupRule groupRule02 = ShardingRuleUtil.getGroupRule(tableName, group02_name);
        GroupRule groupRule03 = ShardingRuleUtil.getGroupRule(tableName, group03_name);

        redisClient.set(group01_id_key, String.valueOf(groupRule01.getStartId()));
        redisClient.set(group02_id_key, String.valueOf(groupRule02.getStartId()));
        redisClient.set(group03_id_key, String.valueOf(groupRule03.getStartId()));
        return Mono.justOrEmpty("success");
    }

    @RequestMapping(value = "/setGroup2OrderId")
    public Mono<String> setGroup2OrderId(){
        //直接设置第二组的id，用以测试第二张表的写入情况
        GroupRule groupRule02 = ShardingRuleUtil.getGroupRule(tableName, group02_name);
        Long orderId = groupRule02.getDbRules().get(0).getTableRules().get(1).getStartId();
        redisClient.set(group02_id_key, String.valueOf(orderId));
        return Mono.justOrEmpty("success");
    }

    /**
     * 添加订单
     * @param groupNum
     * @return
     */
    @RequestMapping(value = "/addOrder")
    public Mono<String> addOrder(@NonNull @RequestParam Integer groupNum) {
        String key = null;
        GroupRule groupRule = null;
        if(groupNum == 1){
            groupRule = ShardingRuleUtil.getGroupRule(tableName, group01_name);
            key = group01_id_key;
        }else if(groupNum == 2){
            groupRule = ShardingRuleUtil.getGroupRule(tableName, group02_name);
            key = group02_id_key;
        }else if(groupNum == 3){
            groupRule = ShardingRuleUtil.getGroupRule(tableName, group03_name);
            key = group03_id_key;
        }else{
            return Mono.justOrEmpty("fail, 未支持的groupNum：" + groupNum);
        }

        Long initVal = groupRule.getStartId();
        Long maxVal = groupRule.getEndId() == -1 ? Long.MAX_VALUE : groupRule.getEndId();

        if(! redisClient.exists(key)){
            redisClient.set(key, String.valueOf(initVal));
        }
        long orderId = redisClient.loopIncrId(key, 1, maxVal);

        Order order = new Order();
        order.setOrderId(orderId);
        order.setAmount(BigDecimal.valueOf(RandomUtil.getInt(100, 500)));
        order.setRemark("addOrder_group_" + groupNum);
        order.setUserId(userId);
        orderBiz.addOrder(order);
        return Mono.justOrEmpty("success");
    }

    @RequestMapping(value = "/addOrderAndItem")
    public Mono<String> addOrderAndItem(@NonNull @RequestParam Integer groupNum, Long productId) {
        String key = null;
        GroupRule groupRule = null;
        if(groupNum == 1){
            groupRule = ShardingRuleUtil.getGroupRule(tableName, group01_name);
            key = group01_id_key;
        }else if(groupNum == 2){
            groupRule = ShardingRuleUtil.getGroupRule(tableName, group02_name);
            key = group02_id_key;
        }else if(groupNum == 3){
            groupRule = ShardingRuleUtil.getGroupRule(tableName, group03_name);
            key = group03_id_key;
        }else{
            return Mono.justOrEmpty("fail, 未支持的groupNum：" + groupNum);
        }

        Long initVal = groupRule.getStartId();
        Long maxVal = groupRule.getEndId() == -1 ? Long.MAX_VALUE : groupRule.getEndId();

        if(! redisClient.exists(key)){
            redisClient.set(key, String.valueOf(initVal));
        }
        long orderId = redisClient.loopIncrId(key, 1, maxVal);

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userId);
        order.setRemark("addOrderAndItem_group_" + groupNum);

        orderBiz.addOrderAndItem(order, productId);
        return Mono.justOrEmpty("success");
    }

    /**
     * 同一个库的批量插入
     *
     * 经测试：
     *  1、同一个库批量写入有事务保障，能做到同时写入或同时失败
     *
     * @return
     */
    @RequestMapping(value = "/batchAddOrderSameDB")
    public Mono<String> batchAddOrderSameDB() {
        //只有第1分组是单库单表的，所以不存在跨库的情况
        int groupNum = 1;
        String key = group01_id_key;

        GroupRule groupRule = ShardingRuleUtil.getGroupRule(tableName, group01_name);
        Long initVal = groupRule.getStartId();
        Long maxVal = groupRule.getEndId();

        if(! redisClient.exists(key)){
            redisClient.set(key, String.valueOf(initVal));
        }

        boolean isThrowException = RandomUtil.getInt(1, 10) % 2 == 0;
        int max = 100;
        List<Order> orderList = new ArrayList<>();
        while (--max >= 0){
            long orderId = redisClient.loopIncrId(key, 1, maxVal);

            String remark = "batchAddOrderSameDB_" + groupNum;
            if(isThrowException && max == 10){
                remark = remark + RandomUtil.getDigitStr(100);//超过数据库长度，会抛出异常，用以验证单库时的批量写入事务性
            }

            Order order = new Order();
            order.setOrderId(orderId);
            order.setRemark(remark);
            order.setAmount(BigDecimal.valueOf(RandomUtil.getInt(100, 500)));
            order.setUserId(userId);
            orderList.add(order);
        }

        orderBiz.addOrder(orderList);
        return Mono.justOrEmpty("success");
    }

    /**
     * 跨库的批量插入
     *
     * 经测试：
     *  1、批量insert语句，会根据id写入到不同的库
     *  2、某个库抛出异常时，写入到这个库中的数据全部失败，其他库的数据不受抛异常库的影响，即：同一个库内有事务保障，不同库之间没有事务保障
     *
     * @return
     */
    @RequestMapping(value = "/batchAddOrderMultiDB")
    public Mono<String> batchAddOrderMultiDB() {
        int max = 100;
        int group01Count = 0, group02Count = 0, group03Count = 0;
        List<Order> orderList = new ArrayList<>();
        boolean isThrowException = RandomUtil.getInt(1, 10) % 2 == 0;

        while (--max >= 0){
            int groupNum =  RandomUtil.getInt(1, 3);
            String key;
            GroupRule groupRule;
            if(groupNum == 1){
                groupRule = ShardingRuleUtil.getGroupRule(tableName, group01_name);
                key = group01_id_key;
                group01Count++;
            }else if(groupNum == 2){
                groupRule = ShardingRuleUtil.getGroupRule(tableName, group02_name);
                key = group02_id_key;
                group02Count++;
            }else if(groupNum == 3){
                groupRule = ShardingRuleUtil.getGroupRule(tableName, group03_name);
                key = group03_id_key;
                group03Count++;
            }else{
                return Mono.justOrEmpty("fail, 未支持的groupNum：" + groupNum);
            }

            Long initVal = groupRule.getStartId();
            Long maxVal = groupRule.getEndId() == -1 ? Long.MAX_VALUE : groupRule.getEndId();

            if(! redisClient.exists(key)){
                redisClient.set(key, String.valueOf(initVal));
            }

            long orderId = redisClient.loopIncrId(key, 1, maxVal);

            String remark = "batchAddOrderMultiDB_" + groupNum;
            if(isThrowException && max == 10){
                remark = remark + RandomUtil.getDigitStr(100);//超过数据库长度，会抛出异常，用以验证跨库时的批量写入事务性
            }

            Order order = new Order();
            order.setOrderId(orderId);
            order.setRemark(remark);
            order.setAmount(BigDecimal.valueOf(RandomUtil.getInt(100, 500)));
            order.setUserId(userId);
            orderList.add(order);
        }
        System.out.println("group01Count = "+group01Count+", group02Count = "+group02Count+", group03Count = " + group03Count);
        orderBiz.addOrder(orderList);
        return Mono.justOrEmpty("success");
    }

    /**
     * 更新
     * @param orderId
     * @param desc
     * @return
     */
    @RequestMapping(value = "/updateOrder")
    public Mono<String> updateOrder(@NonNull @RequestParam Long orderId, @NonNull @RequestParam String desc) {
        Order order = orderBiz.getOrder(orderId);
        order.setRemark(order.getRemark() + "_" + desc);
        orderBiz.updateOrder(order);
        return Mono.justOrEmpty("success");
    }

    /**
     * 删除
     * @param orderId
     * @return
     */
    @RequestMapping(value = "/deleteOrder")
    public Mono<String> deleteOrder(@NonNull @RequestParam Long orderId) {
        orderBiz.deleteOrder(orderId);
        return Mono.justOrEmpty("success");
    }

    /**
     * 往产品表添加记录
     * 经测试：
     *  1、没有配置分片策略的表，会选择默认数据源进行写入
     *
     * @param name
     * @return
     */
    @RequestMapping(value = "/addProduct")
    public Mono<String> addProduct(@NonNull @RequestParam String name) {
        Product product = new Product();
        product.setInStock(RandomUtil.getInt(500, 1000));
        product.setPrice(BigDecimal.valueOf(RandomUtil.getInt(1000, 3000)));
        product.setName(name + "_" + RandomUtil.getInt(1, 600));
        orderBiz.addProduct(product);
        return Mono.justOrEmpty("success");
    }

    /**
     * 下单
     * 经测试：
     *  1、没有配置分片策略的表，会选择默认数据源进行写入
     *  2、同一个库中的多张表操作有事务保障
     *
     * @param productId
     * @return
     */
    @RequestMapping(value = "/buy")
    public Mono<String> buy(@NonNull @RequestParam Long productId) {
        String key = group01_id_key;
        long orderId = redisClient.loopIncrId(key, 1, 2000 * 10000L);
        boolean isThrowEx = RandomUtil.getInt(1, 10) % 2 == 0;
        orderBiz.buy(orderId, productId, 1001L, isThrowEx);
        return Mono.justOrEmpty("success");
    }
}
