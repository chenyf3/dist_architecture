package com.xpay.web.api.common.manager.impl;

import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.web.api.common.config.Constants;
import com.xpay.web.api.common.manager.FuncManager;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.AuthService;
import com.xpay.web.api.common.service.UserService;

import java.util.HashMap;
import java.util.Map;

public class FuncManagerImpl implements FuncManager {
    private String appName;
    private RedisClient redisClient;
    private AuthService authService;
    private UserService userService;

    public FuncManagerImpl(String appName, RedisClient redisClient, AuthService authService, UserService userService){
        this.appName = appName;
        this.redisClient = redisClient;
        this.authService = authService;
        this.userService = userService;
    }

    @Override
    public Map<String, String> getAuthMapByLoginName(String loginName, String mchNo) {
        String key = getAuthStoreKey(loginName);
        Map<String, String> functionMap = redisClient.get(key, HashMap.class);
        //如果没有，再取多一次
        if(functionMap == null || functionMap.isEmpty()) {
            UserModel model = userService.getUserModelByLoginName(loginName, mchNo);
            if(model != null){
                functionMap = authService.getAuthMapByUserId(model.getId(), mchNo);
            }
            if(functionMap != null && !functionMap.isEmpty()){
                redisClient.set(key, functionMap, 30 * 60);
            }else{
                functionMap = new HashMap<>();
            }
        }
        return functionMap;
    }

    @Override
    public void afterLogout(UserModel userModel) {
        String key = getAuthStoreKey(userModel.getLoginName());
        redisClient.del(key);
    }

    private String getAuthStoreKey(String loginName){
        return Constants.CACHE_PERMISSION_FLAG + appName + ":" + loginName;
    }
}
