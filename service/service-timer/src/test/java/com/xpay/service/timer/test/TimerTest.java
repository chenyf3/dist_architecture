package com.xpay.service.timer.test;

import com.xpay.common.statics.enums.common.TimeUnitEnum;
import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.timer.dto.JobInfoDto;
import com.xpay.facade.timer.service.TimerAdminFacade;
import com.xpay.facade.timer.service.TimerFacade;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Test;

import java.util.*;

public class TimerTest extends BaseTestCase {
    @Reference
    TimerFacade timerFacade;
    @Reference
    TimerAdminFacade timerAdminFacade;

//    @Ignore
    @Test
    public void testTimer() throws Exception {
        String jobGroup = "testGroup";
        String jobName = "simpleJob_01";
        JobInfoDto job = JobInfoDto.newIntervalTask(jobGroup, jobName, "amq://timer.test.desc");
        job.setToRepeatForeverTask(new Date(), 10, TimeUnitEnum.SECOND.getValue());

        timerFacade.deleteJob(jobGroup, jobName, "tester");

        List<String> strArray = new ArrayList<>();
        strArray.add("1");
        strArray.add("2");
        strArray.add("3");

        List<Map<String, Object>> objArray = new ArrayList<>();
        Map<String, Object> map1 = new HashMap<>();
        map1.put("key_01", "val_01");
        map1.put("key_02", "val_02");
        objArray.add(map1);
        Map<String, Object> map2 = new HashMap<>();
        map2.put("key_03", "val_03");
        map2.put("key_04", "val_04");
        objArray.add(map2);

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("key_01", "value_01");
        paramMap.put("key_02", 30);
        paramMap.put("key_03", 30.06);
        paramMap.put("key_04", true);
        paramMap.put("key_05", strArray);
        paramMap.put("key_06", objArray);
        job.setParamJson(JsonUtil.toJson(paramMap));

        timerFacade.scheduleJob(job, "tester");

        Thread.sleep(50000);
    }
}
