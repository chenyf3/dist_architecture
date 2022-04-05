package com.xpay.demo.skywalking;

import com.xpay.demo.skywalking.vo.UserInfo;
import org.springframework.stereotype.Component;

@Component
public class UserBiz {

    public UserInfo getUserInfoById(int id) {
        return new UserInfo(id, "zhangsan", 20);
    }

}
