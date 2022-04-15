package com.xpay.service.message.biz.email;

import com.xpay.common.utils.ConvertUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.facade.message.enums.EmailSendStatusEnum;
import com.xpay.service.message.dao.MailGroupDao;
import com.xpay.service.message.dao.MailDelayRecordDao;
import com.xpay.service.message.entity.MailDelayRecord;
import com.xpay.starter.plugin.plugins.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

/**
 * 邮件合并发送、邮件发送超时回退、过期邮件删除
 */
@Component
public class EmailMergeSendTask {
    private final static String TEMPLATE_FILE = "mergeDelay.ftl";
    private final static int EXECUTE_INTERVAL = 5;//任务执行间隔
    private final static TimeUnit EXECUTE_INTERVAL_UNIT = TimeUnit.SECONDS;//任务执行间隔的单位

    private final static int REVERT_INTERVAL = 30;//状态回退执行间隔
    private final static TimeUnit REVERT_INTERVAL_UNIT = TimeUnit.MINUTES;//状态回退执行间隔的单位

    private final static int DELETE_INTERVAL = 12;//删除执行间隔(小时)
    private final static TimeUnit DELETE_INTERVAL_UNIT = TimeUnit.HOURS;//删除执行间隔的单位

    private final static String GROUP_SEPARATOR = "|_|";//分组分割符号
    private final static int RECORDS_PER_PAGE = 300;//每次分页拉取多少条记录
    private final static int RECORDS_PER_EMAIL = 100;//每封邮件包含多少条记录

    private final static int SENDING_KEEP_MINUTES = 5;//处于'发送中'状态多久的记录需要回退
    private final static int FINISH_KEEP_DAYS = 2;//发送完毕的记录保留多少天

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ScheduledExecutorService sendExecutor = Executors.newScheduledThreadPool(1);
    private ScheduledExecutorService shareExecutor = Executors.newScheduledThreadPool(2);

    @Autowired
    EmailBiz emailBiz;
    @Autowired
    DistributedLock distributedLock;
    @Autowired
    MailGroupDao mailGroupDao;
    @Autowired
    MailDelayRecordDao mailDelayRecordDao;

    @PostConstruct
    public void initTask() {
        sendExecutor.scheduleAtFixedRate(new SendTask(), 0, EXECUTE_INTERVAL, EXECUTE_INTERVAL_UNIT);
        shareExecutor.scheduleAtFixedRate(new RevertTask(), 0, REVERT_INTERVAL, REVERT_INTERVAL_UNIT);
        shareExecutor.scheduleAtFixedRate(new DeleteTask(), 0, DELETE_INTERVAL, DELETE_INTERVAL_UNIT);
    }

    /**
     * 把'待发送'的记录合并发送出去
     */
    private class SendTask implements Runnable {
        @Override
        public void run() {
            String lockName = "emailDelayMergeSend:task";
            Object lock = distributedLock.tryLock(lockName, 3000, -1);
            if (lock == null) {
                return;
            }

            try {
                Date endTime = DateUtil.addMinute(new Date(), -1);
                boolean isContinue = true;
                Integer offset = 0;
                Integer currTimes = 1;
                do {
                    List<MailDelayRecord> mailGroupMergeList = this.listPendingRecord(endTime, offset, RECORDS_PER_PAGE);
                    if(mailGroupMergeList == null || mailGroupMergeList.isEmpty()){
                        break;
                    }else if(mailGroupMergeList.size() < RECORDS_PER_PAGE){
                        isContinue = false;
                    }

                    Map<String, List<MailDelayRecord>> recordGroupMap = this.splitGroup(mailGroupMergeList);

                    this.doSend(recordGroupMap);

                    currTimes ++;
                } while (isContinue && currTimes < 100);
            } finally {
                distributedLock.unlock(lock);
            }
        }

        private List<MailDelayRecord> listPendingRecord(Date endTime, Integer offset, Integer limit) {
            //为了可以使用到索引
            List<String> createDateList = new ArrayList<>();
            createDateList.add(DateUtil.formatDate(DateUtil.addDay(new Date(), -2)));
            createDateList.add(DateUtil.formatDate(DateUtil.addDay(new Date(), -1)));
            createDateList.add(DateUtil.formatDate(new Date()));
            return mailDelayRecordDao.listPendingRecord(createDateList, endTime, offset, limit);
        }

        private Map<String, List<MailDelayRecord>> splitGroup(List<MailDelayRecord> mailGroupMergeList) {
            //相同groupKey、相同主题的合并为一组
            Map<String, List<MailDelayRecord>> groupMap = new HashMap<>();
            for(MailDelayRecord delayRecord : mailGroupMergeList){
                String key = delayRecord.getGroupKey() + GROUP_SEPARATOR + delayRecord.getSubject();
                if (!groupMap.containsKey(key)) {
                    groupMap.put(key, new ArrayList<>());
                }
                groupMap.get(key).add(delayRecord);
            }
            return groupMap;
        }

        /**
         *
         * @param recordGroupMap
         * @return  返回发送失败的记录个数
         */
        private int doSend(Map<String, List<MailDelayRecord>> recordGroupMap) {
            int failCount = 0;
            for(Map.Entry<String, List<MailDelayRecord>> entry : recordGroupMap.entrySet()) {
                String key = entry.getKey();//groupKey_邮件主题
                List<MailDelayRecord> delayRecordList = entry.getValue();
                Map<Integer, List<MailDelayRecord>> smallGroupMap  = ConvertUtil.splitGroup(delayRecordList, RECORDS_PER_EMAIL);

                String groupKey = key.substring(0, key.indexOf(GROUP_SEPARATOR));
                String subject = key.substring(key.indexOf(GROUP_SEPARATOR) + GROUP_SEPARATOR.length());
                for(Map.Entry<Integer, List<MailDelayRecord>> innerEntry : smallGroupMap.entrySet()){
                    List<MailDelayRecord> recordList = innerEntry.getValue();

                    Map<String, Object> tplParam = new HashMap<>();
                    tplParam.put("emailList", recordList);

                    updatePendingToSending(recordList);

                    String content = emailBiz.resolveTplContent(TEMPLATE_FILE, tplParam);
                    boolean isSuccess = emailBiz.sendHtml(groupKey, subject, content);

                    if(isSuccess){
                        updateSendingToFinish(recordList);
                    }else{
                        failCount += recordList.size();
                        revertSendingToPending(recordList);//回退到待发送状态
                    }
                }
            }
            return failCount;
        }

        /**
         * 发送前更新为'发送中'
         * @param recordList
         * @return  返回更新失败的记录个数
         */
        private int updatePendingToSending(List<MailDelayRecord> recordList){
            List<Long> idList = new ArrayList<>();
            recordList.forEach(e -> idList.add(e.getId()));
            int successCount = mailDelayRecordDao.updatePendingToSending(idList, new Date());
            return recordList.size() - successCount;
        }

        /**
         * 发送后更新为'已发送'
         * @param recordList
         * @return  返回更新失败的记录个数
         */
        private int updateSendingToFinish(List<MailDelayRecord> recordList){
            List<Long> idList = new ArrayList<>();
            recordList.forEach(e -> idList.add(e.getId()));
            int successCount = mailDelayRecordDao.updateSendingToFinish(idList, new Date());
            return recordList.size() - successCount;
        }

        private int revertSendingToPending(List<MailDelayRecord> recordList){
            List<Long> idList = new ArrayList<>();
            for(MailDelayRecord delayRecord : recordList){
                if(delayRecord.getStatus() != EmailSendStatusEnum.SENDING.getValue()){
                    continue;
                }
                idList.add(delayRecord.getId());
            }
            if(idList.isEmpty()){
                return 0;
            }
            int successCount = mailDelayRecordDao.revertSendingToPending(idList);
            return idList.size() - successCount;
        }
    }

    /**
     * 把处于'发送中'状态超过一段时间(如5分钟)的记录回退到'待发送'状态，这样可能会造成邮件重复发送，但不会漏发送
     */
    private class RevertTask implements Runnable {
        @Override
        public void run() {
            String lockName = "emailDelayRevert:task";
            Object lock = distributedLock.tryLock(lockName, 3000, -1);
            if (lock == null) {
                return;
            }

            try {
                Date endTime = DateUtil.addMinute(new Date(), (-1 * SENDING_KEEP_MINUTES));
                boolean isContinue = true;
                Integer offset = 0;
                Integer currTimes = 1;

                do {
                    List<MailDelayRecord> recordList = listSendingOvertimeRecord(endTime, offset, RECORDS_PER_PAGE);
                    if(recordList == null || recordList.isEmpty()){
                        break;
                    }else if(recordList.size() <  RECORDS_PER_PAGE){
                        isContinue = false;
                    }

                    int failCount = revertSendingToPending(recordList);
                    offset += failCount;
                    currTimes++;
                } while (isContinue && currTimes < 100);
            } finally {
                distributedLock.unlock(lock);
            }
        }

        /**
         * 找出发送超时的记录（即处于 '发送中' 状态超过一定时间的记录）
         * @param endTime
         * @param offset
         * @param limit
         * @return
         */
        private List<MailDelayRecord> listSendingOvertimeRecord(Date endTime, Integer offset, Integer limit){
            //为了可以使用到索引
            List<String> createDateList = new ArrayList<>();
            createDateList.add(DateUtil.formatDate(DateUtil.addDay(new Date(), -2)));
            createDateList.add(DateUtil.formatDate(DateUtil.addDay(new Date(), -1)));
            createDateList.add(DateUtil.formatDate(new Date()));
            return mailDelayRecordDao.listSendingOvertimeRecord(createDateList, endTime, offset, limit);
        }

        /**
         * 把处于 '发送中' 状态超过5分钟的记录回退到 '待发送' 状态，避免发送成功/失败，但是更新为 '已发送' 状态的时候失败了
         */
        private int revertSendingToPending(List<MailDelayRecord> recordList){
            List<Long> idList = new ArrayList<>();
            for(MailDelayRecord delayRecord : recordList){
                if(delayRecord.getStatus() != EmailSendStatusEnum.SENDING.getValue()){
                    continue;
                }
                idList.add(delayRecord.getId());
            }
            if(idList.isEmpty()){
                return 0;
            }
            int successCount = mailDelayRecordDao.revertSendingToPending(idList);
            return idList.size() - successCount;
        }
    }

    /**
     * 把'已发送'且创建时间超过一段时间(如2天)的记录删除掉
     */
    private class DeleteTask implements Runnable {
        @Override
        public void run() {
            String lockName = "emailDelayDelete:task";
            Object lock = distributedLock.tryLock(lockName, 3000, -1);
            if (lock == null) {
                return;
            }

            try {
                Date endTime = DateUtil.addDay(new Date(), (-1 * FINISH_KEEP_DAYS));
                boolean isContinue = true;
                Integer offset = 0;
                Integer currTimes = 1;

                do {
                    List<MailDelayRecord> recordList = listFinishOvertimeRecord(endTime, offset, RECORDS_PER_PAGE);
                    if(recordList == null || recordList.isEmpty()){
                        break;
                    }else if(recordList.size() < RECORDS_PER_PAGE){
                        isContinue = false;
                    }

                    deleteFinishRecord(recordList);

                    currTimes++;
                } while (isContinue && currTimes < 100);
            } finally {
                distributedLock.unlock(lock);
            }
        }

        private List<MailDelayRecord> listFinishOvertimeRecord(Date endTime, Integer offset, Integer limit) {
            return mailDelayRecordDao.listFinishOvertimeRecord(endTime, offset, limit);
        }

        /**
         * 把发送完毕且时间超过 2 天的记录删除掉
         */
        private void deleteFinishRecord(List<MailDelayRecord> recordList) {
            List<Long> idList = new ArrayList<>();
            for(MailDelayRecord record : recordList){
                if(record.getStatus() != EmailSendStatusEnum.FINISH.getValue()){
                    continue;
                }
                idList.add(record.getId());
            }

            if(idList.size() > 0){
                mailDelayRecordDao.deleteByIdList(idList);
            }
        }
    }
}
