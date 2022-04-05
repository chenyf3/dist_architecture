package com.xpay.demo.sharding.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.demo.sharding.entity.OrderItem;
import org.springframework.stereotype.Repository;

@Repository
public class OrderItemDao extends MyBatisDao<OrderItem, Long> {

}
