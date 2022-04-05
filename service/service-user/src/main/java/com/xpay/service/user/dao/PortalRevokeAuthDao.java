/*
 * Powered By [xpay.com]
 */
package com.xpay.service.user.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.statics.enums.user.RevokeAuthStatusEnum;
import com.xpay.service.user.entity.PortalRevokeAuth;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class PortalRevokeAuthDao extends MyBatisDao<PortalRevokeAuth, Long> {

    /**
     * 获取一条‘待处理’的记录
     * @return
     */
    public PortalRevokeAuth getOnePendingRecord(){
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("status", RevokeAuthStatusEnum.PENDING.getValue());
        paramMap.put(SORT_COLUMNS, "ID ASC");
        return getOne(paramMap);
    }
}
