/*
 * Powered By [xpay.com]
 */
package com.xpay.service.message.dao;

import com.xpay.common.service.dao.MyBatisDao;

import com.xpay.service.message.entity.MailReceiver;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class MailReceiverDao extends MyBatisDao<MailReceiver, Long> {

    public MailReceiver getByGroupKey(String groupKey){
        Map<String, Object> map = new HashMap<>();
        map.put("groupKey", groupKey);
        return getOne(map);
    }
}
