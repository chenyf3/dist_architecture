package com.xpay.starter.sentinel.dubbo;

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;
import com.alibaba.csp.sentinel.slots.system.SystemBlockException;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

/**
 * 当发生sentinel流控时，dubbo提供者端的全局fallback处理器
 * @author chenyf
 */
public class DubboProviderFallback implements DubboFallback {

    @Override
    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex) {
        //TODO 根据自身的业务需求来实现处理逻辑
        return AsyncRpcResult.newDefaultAsyncResult(ex.toRuntimeException(), invocation);
    }
}
