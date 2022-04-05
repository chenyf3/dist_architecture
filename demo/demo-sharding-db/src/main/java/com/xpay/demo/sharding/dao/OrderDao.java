package com.xpay.demo.sharding.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.demo.sharding.entity.Order;
import org.springframework.stereotype.Repository;

@Repository
public class OrderDao extends MyBatisDao<Order, Long> {


}
