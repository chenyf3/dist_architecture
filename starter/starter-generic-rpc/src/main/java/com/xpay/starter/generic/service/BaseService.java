package com.xpay.starter.generic.service;

import com.xpay.starter.generic.invoker.Parameters;
import com.xpay.starter.generic.invoker.DubboServiceInvoker;

/**
 * @author chenyf
 * 基础服务的方法聚合类
 */
public class BaseService {
    private DubboServiceInvoker dubboServiceInvoker;

    public BaseService(DubboServiceInvoker dubboServiceInvoker){
        this.dubboServiceInvoker = dubboServiceInvoker;
    }

    /**
     * 异步发送邮件，调用了 com.xpay.facade.message.service.EmailFacade#sendAsync(String,String,String)
     * @param groupKey
     * @param subject
     * @param content
     * @return
     */
    public boolean sendMailAsync(String groupKey, String subject, String content){
        String methodName = "sendAsync";

        Parameters parameters = Parameters.newInstance()
                .addParameter(String.class.getName(), groupKey)
                .addParameter(String.class.getName(), subject)
                .addParameter(String.class.getName(), content);

        Object result = dubboServiceInvoker.invoke(InterfaceConst.EMAIL_FACADE, null, methodName, parameters);
        if(result != null){
            return (boolean) result;
        }else{
            return false;
        }
    }

    /**
     * 发送邮件，调用了 com.xpay.facade.message.service.EmailFacade#sendAsync(EmailFromEnum,String,String[],String,String)
     * @param from
     * @param to
     * @param cc
     * @param subject
     * @param content
     * @return
     */
    public boolean sendMail(String from, String to, String[] cc, String subject, String content){
        String methodName = "send";

        Parameters parameters = Parameters.newInstance()
                .addParameter(String.class.getName(), from)
                .addParameter(String.class.getName(), to)
                .addParameter(String[].class.getName(), cc)
                .addParameter(String.class.getName(), subject)
                .addParameter(String.class.getName(), content);

        Object result = dubboServiceInvoker.invoke(InterfaceConst.EMAIL_FACADE, null, methodName, parameters);
        if(result != null){
            return (boolean) result;
        }else{
            return false;
        }
    }

    /**
     * 加锁，调用了 com.xpay.facade.lock.service.GlobalLockFacade#tryLock(String,int,String)
     * @param resourceId
     * @param expireSecond
     * @param clientFlag
     * @return
     */
    public String tryLock(String resourceId, int expireSecond, String clientFlag){
        String methodName = "tryLock";

        Parameters parameters = Parameters.newInstance()
                .addParameter(String.class.getName(), resourceId)
                .addParameter(int.class.getName(), expireSecond)
                .addParameter(String.class.getName(), clientFlag);

        Object result = dubboServiceInvoker.invoke(InterfaceConst.DIST_LOCK_FACADE, null, methodName, parameters);
        if(result != null){
            return (String) result;
        }else{
            return null;
        }
    }

    /**
     * 释放锁，调用了 com.xpay.facade.lock.service.GlobalLockFacade#unlock(String,String)
     * @param resourceId
     * @param clientId
     * @return
     */
    public boolean unlock(String resourceId, String clientId){
        String methodName = "unlock";

        Parameters parameters = Parameters.newInstance()
                .addParameter(String.class.getName(), resourceId)
                .addParameter(String.class.getName(), clientId);

        Object result = dubboServiceInvoker.invoke(InterfaceConst.DIST_LOCK_FACADE, null, methodName, parameters);
        if(result != null){
            return (boolean) result;
        }else{
            return false;
        }
    }
}
