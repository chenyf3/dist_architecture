package com.xpay.demo.dubbo.provider.facade;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.demo.dubbo.facade.entity.Order;
import com.xpay.demo.dubbo.facade.service.OrderFacade;
import com.xpay.demo.dubbo.provider.biz.OrderBiz;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@DubboService
public class OrderFacadeImpl implements OrderFacade {
    @Autowired
    OrderBiz orderBiz;

    @Override
    public boolean create(Order order) {
        return orderBiz.create(order);
    }

    @Override
    public boolean update(Order order) {
        return orderBiz.update(order);
    }

    @Override
    public Order getById(Long id) {
        return orderBiz.getById(id);
    }

    @Override
    public PageResult<List<Order>> listPage(Map<String, Object> paramMap, PageQuery pageQuery) {
        return orderBiz.listPage(paramMap, pageQuery);
    }
}
