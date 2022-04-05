package com.xpay.starter.sentinel.dubbo;

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

/**
 * 当发生sentinel流控时，dubbo消费者端的全局fallback处理器
 * @author chenyf
 */
public class DubboConsumerFallback implements DubboFallback {

    @Override
    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex) {
        //TODO 根据自身的业务需求来实现处理逻辑
        return AsyncRpcResult.newDefaultAsyncResult(ex.toRuntimeException(), invocation);
    }
}
