package com.xpay.web.api.common.manager;

import com.xpay.web.api.common.model.UserModel;

import java.util.Map;

public interface FuncManager {
    Map<String, String> getAuthMapByLoginName(String loginName, String mchNo);

    public void afterLogout(UserModel userModel);
}
