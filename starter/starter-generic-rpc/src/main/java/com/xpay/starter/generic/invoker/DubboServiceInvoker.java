package com.xpay.starter.generic.invoker;

import com.xpay.starter.generic.utils.Util;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenyf
 * 作用：用以实现dubbo接口的泛化调用，类似REST调用，只不过REST模式下是使用http协议发起调用，而在这里是通过RMI使用dubbo协议发起调用
 */
public class DubboServiceInvoker {
    private final static String REFERENCE_CONFIG_CACHE_NAME = "_GENERIC_INVOKE_";
    private final static String DIRECT_REFERENCE_CONFIG_CACHE_NAME = "_DIRECT_GENERIC_INVOKE_";

    /**
     * @param applicationName   当前应用名称
     * @param registryAddress   注册中心地址
     * @param username          注册中心用户名
     * @param password          注册中心密码
     */
    public DubboServiceInvoker(String applicationName, String registryAddress, Integer port, String username, String password) {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(applicationName);

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);
        registryConfig.setPort(port);
        registryConfig.setUsername(username);
        registryConfig.setPassword(password);

        SingletonHolder.INSTANCE.application = applicationConfig;
        SingletonHolder.INSTANCE.registry = registryConfig;
    }

    public Object invoke(String interfaceClass, String methodName, Parameters parameters){
        return SingletonHolder.INSTANCE.invoke(null, interfaceClass, methodName, parameters);
    }

    public Object invoke(String url, String interfaceClass, String methodName, Parameters parameters){
        return SingletonHolder.INSTANCE.invoke(url, interfaceClass, methodName, parameters);
    }

    public void destroy(){
        try{
            ReferenceConfigCache.getCache(REFERENCE_CONFIG_CACHE_NAME).destroyAll();
        }catch(Exception e){
        }
        try{
            ReferenceConfigCache.getCache(DIRECT_REFERENCE_CONFIG_CACHE_NAME).destroyAll();
        }catch(Exception e){
        }
    }

    private static class SingletonHolder {
        private static Invoker INSTANCE = new Invoker();
    }

    private static class Invoker {
        private ApplicationConfig application;
        private RegistryConfig registry;
        private ReferenceConfigCache.KeyGenerator directInvokeKeyGenerator = referenceConfig -> {
            String iName = referenceConfig.getInterface();
            if (StringUtils.isBlank(iName)) {
                Class<?> clazz = referenceConfig.getInterfaceClass();
                iName = clazz.getName();
            }
            if (StringUtils.isBlank(iName)) {
                throw new IllegalArgumentException("No interface info in ReferenceConfig" + referenceConfig);
            }

            StringBuilder ret = new StringBuilder();
            if (!StringUtils.isBlank(referenceConfig.getGroup())) {
                ret.append(referenceConfig.getGroup()).append("/");
            }
            ret.append(iName);
            if (!StringUtils.isBlank(referenceConfig.getVersion())) {
                ret.append(":").append(referenceConfig.getVersion());
            }
            ret.append("->").append(referenceConfig.getUrl());
            return ret.toString();
        };

        Object invoke(String url, String interfaceClass, String methodName, Parameters parameters){
            Map<String, String> param = new HashMap<>(8);
            url = splitParamAndUrl(url, param);

            boolean isDirectCall = url != null && url.trim().length() > 0;
            ReferenceConfig<GenericService> reference = new ReferenceConfig<>();
            reference.setApplication(application);
            if(isDirectCall){
                reference.setUrl(url);
            }else{
                reference.setRegistry(registry);
            }
            reference.setInterface(interfaceClass); //接口名
            reference.setGeneric("true"); //声明为泛化接口
            reference.setCheck(false);
            setReferenceParam(reference, param);

            //ReferenceConfig实例很重，封装了与注册中心的连接以及与提供者的连接，需要缓存，否则重复生成ReferenceConfig可能造成性能问题和内存和连接泄漏
            GenericService genericService;
            if(isDirectCall){
                ReferenceConfigCache cache = ReferenceConfigCache.getCache(DIRECT_REFERENCE_CONFIG_CACHE_NAME, directInvokeKeyGenerator);
                genericService = cache.get(reference); //用GenericService替代所有接口引用
            }else{
                ReferenceConfigCache cache = ReferenceConfigCache.getCache(REFERENCE_CONFIG_CACHE_NAME);
                genericService = cache.get(reference);
            }

            List<Parameters.Parameter> parameterList = parameters.getParameterList();
            int len = parameterList.size();
            String[] invokeParamTypes = new String[len];
            Object[] invokeParams = new Object[len];
            for(int i = 0; i < len; i++){
                invokeParamTypes[i] = parameterList.get(i).getType();
                invokeParams[i] = parameterList.get(i).getValue();
            }
            return genericService.$invoke(methodName, invokeParamTypes, invokeParams);
        }

        private String splitParamAndUrl(String url, Map<String, String> param){
            String[] urlArr = url == null ? null : url.split("\\?");
            if(urlArr == null || urlArr.length <= 1){
                return urlArr.length == 1 ? urlArr[0] : url;
            }

            String newUrl = urlArr[0];
            String[] paramArr = urlArr[1].split("&");
            for(int i=0; i<paramArr.length; i++){
                String[] pair = paramArr[i].split("=");
                if(pair.length == 1){
                    param.put(pair[0], "");
                }else if(pair.length == 2){
                    param.put(pair[0], pair[1]);
                }
            }
            return newUrl;
        }

        private void setReferenceParam(ReferenceConfig<GenericService> reference, Map<String, String> param){
            if(param == null){
                return;
            }
            if(Util.isNotEmpty(param.get("group"))){
                reference.setGroup(param.get("group"));
            }
            if(Util.isNotEmpty(param.get("version"))){
                reference.setVersion(param.get("version"));
            }
            if(Util.isNotEmpty(param.get("timeout"))){
                reference.setTimeout(Integer.valueOf(param.get("timeout")));
            }
        }
    }
}
