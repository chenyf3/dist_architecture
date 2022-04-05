package demo.xpay.demo.timer;

import com.xpay.common.statics.enums.common.TimeUnitEnum;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.facade.timer.dto.InstanceDto;
import com.xpay.facade.timer.dto.JobInfoDto;
import com.xpay.facade.timer.dto.OpLogDto;
import com.xpay.facade.timer.service.TimerAdminFacade;
import com.xpay.facade.timer.service.TimerFacade;
import com.xpay.facade.timer.util.CronExpressionUtil;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("demo")
public class DemoController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @DubboReference
    TimerFacade timerFacade;
    @DubboReference
    TimerAdminFacade timerAdminFacade;

    @RequestMapping(value = "/pauseInstance")
    public boolean pauseInstance(String instanceId) {
        try{
            return timerAdminFacade.pauseInstance(instanceId, "testOperator");
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @RequestMapping(value = "/resumeInstance")
    public boolean resumeInstance(String instanceId) {
        try{
            return timerAdminFacade.resumeInstance(instanceId, "testOperator");
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @RequestMapping(value = "/pauseJob")
    public boolean pauseJob(String jobGroup, String jobName) {
        try{
            timerFacade.pauseJob(jobGroup, jobName, "testOperator");
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @RequestMapping(value = "/resumeJob")
    public boolean resumeJob(String jobGroup, String jobName) {
        try{
            timerFacade.resumeJob(jobGroup, jobName, "testOperator");
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @RequestMapping(value = "/triggerJob")
    public boolean triggerJob(String jobGroup, String jobName) {
        try{
            timerFacade.triggerJob(jobGroup, jobName, "testOperator");
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    //添加定时任务的请求参数样例
    //{
    //    "jobType":1,
    //    "intervals":20,
    //    "intervalUnit":2,
    //    "cronExpression":"0 */2 * * * ?",
    //    "jobDescription":"测试任务fdfd4545到",
    //    "jobGroup":"testGroup",
    //    "jobName":"simpleJob_02",
    //    "startTime":"2020-10-25 12:17:14",
    //    "destination":"amq://timer.test.desc",
    //    "paramJson":"{\"key_01\": \"value_01\", \"key_02\": 30, \"key_03\": 30.06, \"key_04\": true, \"key_05\": [\"1\", \"2\", \"3\"], \"key_06\": [{\"key_01\": \"val_01\", \"key_02\": \"val_02\"}, {\"key_03\": \"val_03\", \"key_04\": \"val_04\"}]}"
    //}
    /**
     * @param jobInfoMap
     * @return
     */
    @RequestMapping(value = "/scheduleJob")
    public boolean scheduleJob(@RequestBody Map<String, Object> jobInfoMap) {
        try{
            JobInfoDto jobInfo = JsonUtil.toBean(JsonUtil.toJson(jobInfoMap), JobInfoDto.class);
            timerFacade.scheduleJob(jobInfo, "testOperator");
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @RequestMapping(value = "/rescheduleJob")
    public boolean rescheduleJob(String jobGroup, String jobName){
        try{
            JobInfoDto jobInfo = timerFacade.getJobInfoByName(jobGroup, jobName);

            int interval = RandomUtil.getInt(10, 20);
            if(jobInfo.getJobType() == JobInfoDto.INTERVAL_JOB){
                int type = RandomUtil.getInt(0, 1);
                jobInfo.setJobType(JobInfoDto.CRON_JOB);
                if(type == 0){
                    jobInfo.setCronExpression(CronExpressionUtil.getSecondlyExpression(interval));//秒级别
                    System.out.println("间隔任务 -> cron任务，interval = " + interval + "(秒)");
                }else{
                    interval = RandomUtil.getInt(1, 3);
                    jobInfo.setCronExpression(CronExpressionUtil.getMinutelyExpression(interval));//分钟级别
                    System.out.println("间隔任务 -> cron任务，interval = " + interval + "(分)");
                }
            }else if(jobInfo.getJobType() == JobInfoDto.CRON_JOB){
                jobInfo.setJobType(JobInfoDto.INTERVAL_JOB);
                jobInfo.setIntervals(interval);
                jobInfo.setIntervalUnit(TimeUnitEnum.SECOND.getValue());
                System.out.println("cron任务 -> 间隔任务 -> ，interval = " + interval + "(秒)");
            }
            timerFacade.rescheduleJob(jobInfo, "testOperator");
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @RequestMapping(value = "/deleteJob")
    public boolean deleteJob(String jobGroup, String jobName){
        try{
            timerFacade.deleteJob(jobGroup, jobName, "testOperator");
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }

    @RequestMapping(value = "/listJobInfo")
    public RestResult<List<JobInfoDto>> listJobInfo() {
        try{
            PageResult<List<JobInfoDto>> pageResult = timerFacade.listPage(new HashMap<>(), PageQuery.newInstance(1, 20));
            return RestResult.success(pageResult.getData());
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/listOpLog")
    public RestResult<List<OpLogDto>> listOpLog() {
        try{
            PageResult<List<OpLogDto>> pageResult = timerAdminFacade.listOpLogPage(new HashMap<>(), PageQuery.newInstance(1, 100));
            return RestResult.success(pageResult.getData());
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/listInstance")
    public RestResult<List<InstanceDto>> listInstance() {
        try{
            PageResult<List<InstanceDto>> pageResult = timerAdminFacade.listInstancePage(new HashMap<>(), PageQuery.newInstance(1, 100));
            return RestResult.success(pageResult.getData());
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
