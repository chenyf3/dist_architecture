package com.xpay.service.message.test;

import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.message.service.EmailFacade;
import com.xpay.service.message.dao.MailDelayRecordDao;
import com.xpay.service.message.entity.MailDelayRecord;
import org.apache.dubbo.config.annotation.DubboReference;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailMergeTest extends BaseTestCase {

    @DubboReference
    EmailFacade emailFacade;
    @Autowired
    MailDelayRecordDao mailDelayRecordDao;


    @Ignore
    @Test
    public void testMergeDelay(){
        String groupKey = "SYS_MONITOR_ALERT_GROUP";
        String subject = "测试合并发送";
        String content = "合并发送主题邮件";
        for(int i=0; i<1000; i++){
            String trxNo = "PL0000000000" + (i+1);
            emailFacade.sendHtmlMerge(groupKey, subject, content, trxNo);
        }
    }

    @Ignore
    @Test
    public void testList(){
        Map<String, Object> paramMap = new HashMap<>();
        String sortColumns = "id desc";
        Integer offset = 10;
        Integer limit = 30;
        for(int i=0; i<100; i++){
            mailDelayRecordDao.listBy(paramMap, sortColumns, offset, limit);
        }
    }

    @Ignore
    @Test
    public void testMap(){
//        SELECT DISTINCT TRX_NO FROM tbl_mail_delay_record
        Map<String, MailDelayRecord> delayRecordMap = mailDelayRecordDao.mapBy(new HashMap<>(), "trxNo");
        System.out.println(JsonUtil.toJson(delayRecordMap));
    }

    @Ignore
    @Test
    public void testBatchUpdate(){
        Map<String, Object> paramMap = new HashMap<>();
        String sortColumns = "id ASC";
        Integer offset = 0;
        Integer limit = 300;
        List<MailDelayRecord> recordList = mailDelayRecordDao.listBy(paramMap, sortColumns, offset, limit);
        for (int i=0; i<recordList.size(); i++) {
            MailDelayRecord record = recordList.get(i);
            record.setContent(record.getContent() + "_" + (i+1));
//            record.setStatus(i+1);
        }
        long start = System.currentTimeMillis();
        mailDelayRecordDao.update(recordList);
        System.out.println("耗时：" + (System.currentTimeMillis()-start) + "(ms),记录数：" + recordList.size());
    }
}
