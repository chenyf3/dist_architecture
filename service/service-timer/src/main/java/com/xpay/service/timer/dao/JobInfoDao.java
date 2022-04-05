package com.xpay.service.timer.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.service.timer.entity.JobInfo;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyf on 2017/8/29.
 */
@Repository
public class JobInfoDao extends MyBatisDao<JobInfo, Long> {

    public JobInfo getByName(String jobGroup, String jobName) {
        Map<String, Object> param = new HashMap<>();
        param.put("jobGroup", jobGroup);
        param.put("jobName", jobName);
        return super.getOne(param);
    }
}
