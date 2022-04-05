package com.xpay.service.config.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.service.config.entity.DistLock;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyf
 */
@Repository
public class DistLockDao extends MyBatisDao<DistLock, Long> {

    public DistLock getByResourceId(String resourceId) {
        Map<String, Object> param = new HashMap<>(3);
        param.put("resourceId", resourceId);
        return getOne("getByResourceId", param);
    }


}
