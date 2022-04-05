package com.xpay.demo.dubbo.provider.dao;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.demo.dubbo.facade.entity.Order;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 模拟dao层操作数据库
 */
@Component
public class OrderDao {
    private Map<Long, Order> database = new TreeMap<>();

    public boolean create(Order order) {
        database.put(order.getId(), order);
        return true;
    }

    public boolean update(Order order) {
        database.put(order.getId(), order);
        return false;
    }

    public Order getById(Long id) {
        return database.get(id);
    }

    public PageResult<List<Order>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        List<Order> orders = new ArrayList<>();
        database.forEach((k,v) -> orders.add(v));
        return PageResult.newInstance(orders, pageQuery, (long)database.size());
    }
}
