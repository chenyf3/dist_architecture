package com.xpay.demo.sharding.biz;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.AmountUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.demo.sharding.dao.OrderDao;
import com.xpay.demo.sharding.dao.OrderItemDao;
import com.xpay.demo.sharding.dao.ProductDao;
import com.xpay.demo.sharding.entity.Order;
import com.xpay.demo.sharding.entity.OrderItem;
import com.xpay.demo.sharding.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
public class OrderBiz {
    @Autowired
    OrderDao orderDao;
    @Autowired
    OrderItemDao orderItemDao;
    @Autowired
    ProductDao productDao;

    public void addOrder(Order order){
        orderDao.insert(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void addOrderAndItem(Order order, Long productId){
        Product product = getProduct(productId);

        OrderItem item = new OrderItem();
        item.setOrderId(order.getOrderId());
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setPrice(product.getPrice());
        item.setCount(RandomUtil.getInt(1, 3));

        order.setAmount(AmountUtil.mul(BigDecimal.valueOf(item.getCount()), item.getPrice()));

        product.setInStock(product.getInStock() - item.getCount());
        if(product.getInStock() < 0){//此处测试用而已，不用考虑并发情况
            throw new BizException("库存已不足");
        }

        orderDao.insert(order);
        orderItemDao.insert(item);
        productDao.update(product);
    }

    public void addOrder(List<Order> orderList){
        orderDao.insert(orderList);
    }

    public void updateOrder(Order order){
        orderDao.update(order);
    }

    public void deleteOrder(Long orderId){
        orderDao.deleteById(orderId);
    }

    public Order getOrder(Long orderId){
        return orderDao.getById(orderId);
    }

    public void addProduct(Product product){
        productDao.insert(product);
    }

    public Product getProduct(Long productId){
        return productDao.getById(productId);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean buy(Long orderId, Long productId, Long userId, boolean isThrowEx){
        Product product = productDao.getById(productId);

        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setPrice(product.getPrice());
        item.setCount(RandomUtil.getInt(1, 3));

        int count = item.getCount();
        Order order = new Order();
        order.setUserId(userId);
        order.setAmount(AmountUtil.mul(BigDecimal.valueOf(count), product.getPrice()));
        order.setOrderId(orderId);
        order.setRemark("buy_test_" + RandomUtil.getInt(1, 100));

        product.setInStock(product.getInStock() - count);
        if(product.getInStock() < 0){//此处测试用而已，不用考虑并发情况
            throw new BizException("库存已不足");
        }

        orderDao.insert(order);
        orderItemDao.insert(item);

        productDao.update(product);

        if(isThrowEx){
            throw new BizException("测试单库事务特性");
        }
        return true;
    }


}
