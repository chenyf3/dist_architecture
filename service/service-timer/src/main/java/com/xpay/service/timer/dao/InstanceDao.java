package com.xpay.service.timer.dao;

import com.xpay.common.service.dao.MyBatisDao;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.StringUtil;
import com.xpay.service.timer.entity.Instance;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenyf on 2017/8/29.
 */
@Repository
public class InstanceDao extends MyBatisDao<Instance, Long> {

    public Instance getByInstanceId(String instanceId) {
        if (StringUtil.isEmpty(instanceId)) {
            throw new BizException(BizException.PARAM_INVALID, "instanceId不能为空");
        }
        Map<String, Object> param = new HashMap<>();
        param.put("instanceId", instanceId);
        return super.getOne(param);
    }

    public List<Instance> listOverTimeInstance(int overMinutes){
        Map<String, Object> param = new HashMap<>();
        param.put("minutes", overMinutes);
        return super.listBy("listOverTimeInstance", param);
    }

    public boolean updateScheduleStatus(String instanceId, Integer newStatus) {
        Map<String, Object> param = new HashMap<>();
        param.put("updateTime", new Date());
        param.put("newStatus", newStatus);
        param.put("instanceId", instanceId);
        return update("updateScheduleStatus", param) > 0;
    }

    public boolean updateCheckInTime(String instanceId) {
        Map<String, Object> param = new HashMap<>();
        param.put("instanceId", instanceId);
        param.put("updateTime", new Date());
        return update("updateCheckInTime", param) > 0;
    }
}
