package com.xpay.web.api.common.ddo.vo;

import java.util.List;
import java.util.Map;

public class SignedInInfoVo {
    private UserInfoVo userInfo;
    private Map<String, List<DictionaryVo.Item>> dictionary;
    private List<AuthVo> authList;

    public UserInfoVo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfoVo userInfo) {
        this.userInfo = userInfo;
    }

    public Map<String, List<DictionaryVo.Item>> getDictionary() {
        return dictionary;
    }

    public void setDictionary(Map<String, List<DictionaryVo.Item>> dictionary) {
        this.dictionary = dictionary;
    }

    public List<AuthVo> getAuthList() {
        return authList;
    }

    public void setAuthList(List<AuthVo> authList) {
        this.authList = authList;
    }


}
