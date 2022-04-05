package com.xpay.common.api.utils;

import java.lang.reflect.UndeclaredThrowableException;

/**
 * alibaba 开源流控框架 sentinel 的工具类
 */
public class SentinelBlockUtil {
    public static Boolean isBlockExceptionExist = false;
    static {
        try {
            Class.forName("com.alibaba.csp.sentinel.slots.block.BlockException");
            isBlockExceptionExist = true;
        } catch (ClassNotFoundException e) {
        }
    }

    public static boolean isBlockException(Throwable ex){
        if(ex == null) return false;
        if(!isBlockExceptionExist) return false;

        return com.alibaba.csp.sentinel.slots.block.BlockException.isBlockException(ex);
    }

    public static String getBlockTypeMsg(Throwable ex){
        if(!isBlockException(ex)){
            return "";
        }

        if (ex instanceof com.alibaba.csp.sentinel.slots.block.flow.FlowException) {
            return "Too Many Request";
        } else if(ex instanceof com.alibaba.csp.sentinel.slots.block.degrade.DegradeException) {
            return "Service Unavailable";
        } else if(ex instanceof com.alibaba.csp.sentinel.slots.system.SystemBlockException) {
            return "Too Many Request";
        } else {
            return "Service Limiting";
        }
    }

    public static String getBlockMsg(Throwable ex){
        if(!isBlockException(ex)){
            return ex.getMessage();
        }

        String msg = null;
        if(ex instanceof UndeclaredThrowableException){
            msg = ex.getCause().getMessage();
            if(msg == null){
                msg = ex.getCause().getClass().getName();
            }
        }else if(ex instanceof com.alibaba.csp.sentinel.slots.block.BlockException){
            String resource = ((com.alibaba.csp.sentinel.slots.block.BlockException) ex).getRule().getResource();
            String limitApp = ((com.alibaba.csp.sentinel.slots.block.BlockException) ex).getRule().getLimitApp();
            String ruleClass = ((com.alibaba.csp.sentinel.slots.block.BlockException) ex).getRule().getClass().getSimpleName();
            msg = "{resource=" + resource + ", limitApp=" + limitApp +  ", ruleType=" + ruleClass + "}";
        }
        if(msg == null){
            msg = ex.getClass().getName();
        }
        return msg;
    }
}
