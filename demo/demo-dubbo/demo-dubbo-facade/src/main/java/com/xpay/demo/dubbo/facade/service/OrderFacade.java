package com.xpay.demo.dubbo.facade.service;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.demo.dubbo.facade.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderFacade {

    boolean create(Order order);

    boolean update(Order order);

    Order getById(Long id);

    PageResult<List<Order>> listPage(Map<String, Object> paramMap, PageQuery pageQuery);
}
