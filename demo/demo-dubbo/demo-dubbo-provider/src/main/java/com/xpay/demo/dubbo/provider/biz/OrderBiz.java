package com.xpay.demo.dubbo.provider.biz;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.demo.dubbo.facade.entity.Order;
import com.xpay.demo.dubbo.provider.dao.OrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class OrderBiz {
    @Autowired
    OrderDao orderDao;

    public boolean create(Order order) {
        return orderDao.create(order);
    }

    public boolean update(Order order) {
        return orderDao.update(order);
    }

    public Order getById(Long id) {
        return orderDao.getById(id);
    }

    public PageResult<List<Order>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return orderDao.listPage(paramMap, pageQuery);
    }
}
