package com.xpay.service.config.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.service.config.entity.DistLock;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyf
 */
@Repository
public class DistLockDao extends MyBatisDao<DistLock, Long> {

    public DistLock getByResourceId(String resourceId) {
        return getOne("getByResourceId", Collections.singletonMap("resourceId", resourceId));
    }


}
