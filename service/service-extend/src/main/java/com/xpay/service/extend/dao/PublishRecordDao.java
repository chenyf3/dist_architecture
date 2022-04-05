package com.xpay.service.extend.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.service.extend.entity.PublishRecord;
import org.springframework.stereotype.Repository;

import java.util.HashMap;

@Repository
public class PublishRecordDao extends MyBatisDao<PublishRecord, Long>{

    public PublishRecord getNotFinishPublishRecord(){
        return getOne("getNotFinishPublishRecord", new HashMap<>());
    }

}
