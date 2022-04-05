package com.xpay.demo.dubbo.consumer;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.RandomUtil;
import com.xpay.demo.dubbo.facade.entity.Order;
import com.xpay.demo.dubbo.facade.service.OrderFacade;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/demo")
public class DemoController {
    private AtomicLong idGenerator = new AtomicLong(0);
    @DubboReference
    OrderFacade orderFacade;

    @RequestMapping("create")
    public Mono<Order> create() {
        Order order = new Order();
        order.setId(idGenerator.incrementAndGet());
        order.setTrxNo(RandomUtil.get16LenStr());
        order.setAmount(BigDecimal.valueOf(RandomUtil.getInt(1, 200)));
        order.setCount(RandomUtil.getInt(1, 5));
        order.setName("商品名称" + RandomUtil.getInt(5, 80));
        boolean isSuccess = orderFacade.create(order);
        if (isSuccess) {
            order = orderFacade.getById(order.getId());
            return Mono.just(order);
        } else {
            return Mono.empty();
        }
    }

    @RequestMapping("update")
    public Mono<Order> update(Long id, String name){
        Order order = orderFacade.getById(id);
        if (order != null) {
            order.setName(name);
            orderFacade.update(order);
            order = orderFacade.getById(id);
        }
        return order == null ? Mono.empty() : Mono.just(order);
    }

    @RequestMapping("getById")
    public Mono<Order> getById(Long id){
        Order order = orderFacade.getById(id);
        return order == null ? Mono.empty() : Mono.just(order);
    }

    @RequestMapping("listPage")
    public Mono<PageResult<List<Order>>> listPage(){
        Map<String, Object> paramMap = new HashMap<>();
        PageQuery pageQuery = PageQuery.newInstance(1, 20);
        PageResult<List<Order>> pageResult = orderFacade.listPage(paramMap, pageQuery);
        return Mono.just(pageResult);
    }
}
