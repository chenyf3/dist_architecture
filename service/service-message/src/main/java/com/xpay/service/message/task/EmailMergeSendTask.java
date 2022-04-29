package com.xpay.service.message.task;

import com.xpay.common.statics.dto.common.TaskWatcher;
import com.xpay.common.utils.ConvertUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.facade.message.enums.EmailSendStatusEnum;
import com.xpay.service.message.biz.email.EmailBiz;
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
    private final static int MERGE_SEND_INTERVAL = 5;//发送任务执行间隔（分）
    private final static TimeUnit MERGE_SEND_INTERVAL_UNIT = TimeUnit.MINUTES;//任务执行间隔的单位

    private final static int DELETE_INTERVAL = 12;//删除执行间隔(小时)
    private final static TimeUnit DELETE_INTERVAL_UNIT = TimeUnit.HOURS;//删除执行间隔的单位

    private final static String GROUP_SEPARATOR = "|_|";//分组分割符号
    private final static int RECORDS_PER_PAGE = 500;//每次分页拉取多少条记录
    private final static int RECORDS_PER_EMAIL = 100;//每封邮件包含多少条记录

    private final static int SENDING_KEEP_MINUTES = 5;//处于'发送中'状态的记录，多久后需要回退
    private final static int RECORD_KEEP_DAYS = 3;//对于已发送或超过最大重试次数的记录，保留多少天
    private final static int MAX_RESEND_TIMES = 10;//最大重试发送次数

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ScheduledExecutorService sendExecutor = Executors.newScheduledThreadPool(1);
    private final ScheduledExecutorService shareExecutor = Executors.newScheduledThreadPool(2);

    @Autowired
    EmailBiz emailBiz;
    @Autowired
    DistributedLock distributedLock;

    @PostConstruct
    public void initTask() {
        sendExecutor.scheduleAtFixedRate(new RevertAndSendTask(), 0, MERGE_SEND_INTERVAL, MERGE_SEND_INTERVAL_UNIT);
        shareExecutor.scheduleAtFixedRate(new DeleteTask(), 0, DELETE_INTERVAL, DELETE_INTERVAL_UNIT);
    }

    /**
     * 把'待发送'的记录合并发送出去
     */
    private class RevertAndSendTask implements Runnable {
        @Override
        public void run() {
            String lockName = "emailDelayMergeSend:task";
            Object lock = distributedLock.tryLock(lockName, 3000, -1);
            if (lock == null) {
                return;
            }

            TaskWatcher watcher = new TaskWatcher("状态回退任务");
            try {
                watcher.begin();
                doRevert(watcher);
                watcher.end();
                logger.info("任务执行结果: {}", watcher.toString());

                watcher.reset("邮件合并发送任务");
                watcher.begin();
                doMergeSend(watcher);
                watcher.end();
                logger.info("任务执行结果: {}", watcher.toString());
            } catch (Exception e) {
                logger.error("执行回退或合并发送任务时出现异常", e);
            } finally {
                distributedLock.unlock(lock);
            }
        }

        /**
         * 执行回退处理，即把处于 '发送中' 状态超过一定时间的记录回退到 '待发送'
         */
        public void doRevert(TaskWatcher watcher) {
            Date endTime = DateUtil.addMinute(new Date(), (-1 * SENDING_KEEP_MINUTES));
            boolean isContinue = true;
            int offset = 0;
            int limit = RECORDS_PER_PAGE;
            int maxSendTimes = MAX_RESEND_TIMES;
            int currTimes = 1;

            do {
                logger.info("currTimes={} offset={} 将要查询发送超时的记录", currTimes, offset);
                List<MailDelayRecord> recordList = listSendingOvertimeRecord(endTime, offset, limit, maxSendTimes);
                logger.info("currTimes={} offset={} 查询到需要回退的{}条记录", currTimes, offset, recordList==null?0:recordList.size());
                if(recordList == null || recordList.isEmpty()){
                    break;
                }else if(recordList.size() <  RECORDS_PER_PAGE){
                    isContinue = false;
                }

                int failCount = revertSendingToPending(recordList);
                offset += failCount;
                currTimes++;

                watcher.addFail(failCount).addSuccess(recordList.size()-failCount);
            } while (isContinue && currTimes < 100);
        }

        /**
         * 找出发送超时的记录（即处于 '发送中' 状态超过一定时间的记录）
         * @param endTime
         * @param offset
         * @param limit
         * @return
         */
        private List<MailDelayRecord> listSendingOvertimeRecord(Date endTime, Integer offset, Integer limit, Integer maxSendTimes) {
            List<String> createDateList = getNeedScanDateList();//为了可以使用到索引
            return emailBiz.listSendingOvertimeDelayRecord(createDateList, endTime, offset, limit, maxSendTimes);
        }

        /**
         * 执行邮件的合并发送
         */
        private void doMergeSend(TaskWatcher watcher) {
            Date endTime = DateUtil.addMinute(new Date(), -1);
            boolean isContinue = true;
            int offset = 0;
            int limit = RECORDS_PER_PAGE;
            int maxSendTimes = MAX_RESEND_TIMES;
            int currTimes = 1;
            do {
                //1.分页找出'待发送'的记录
                logger.info("currTimes={} offset={} endDate={} 将查询需要合并发送的记录", currTimes, offset, DateUtil.formatDate(endTime));
                List<MailDelayRecord> recordList = this.listPendingRecord(endTime, offset, limit, maxSendTimes);
                if(recordList == null || recordList.isEmpty()){
                    break;
                }else if(recordList.size() < RECORDS_PER_PAGE){
                    isContinue = false;
                }

                //2.分组，把 相同groupKey、相同邮件主题 的记录分到同一组
                Map<String, List<MailDelayRecord>> recordGroupMap = this.splitGroup(recordList);
                logger.info("currTimes={} offset={} 查询到需要合并发送的{}条记录，将分成{}组发送", currTimes, offset, recordList.size(), recordGroupMap.size());

                //3.分组发送邮件
                int groupNum = 0;
                for (Map.Entry<String, List<MailDelayRecord>> entry : recordGroupMap.entrySet()) {
                    groupNum ++;
                    String key = entry.getKey();//groupKey_邮件主题
                    String groupKey = key.substring(0, key.indexOf(GROUP_SEPARATOR));
                    String subject = key.substring(key.indexOf(GROUP_SEPARATOR) + GROUP_SEPARATOR.length());

                    List<MailDelayRecord> delayRecordList = entry.getValue();
                    logger.info("currTimes={} groupNum={} 将为当前分组进行合并发送", currTimes, groupNum);
                    int failCount = this.sendEmail(groupKey, subject, delayRecordList, currTimes, groupNum);
                    offset += failCount;

                    watcher.addFail(failCount).addSuccess(recordList.size()-failCount);
                }

                currTimes ++;
            } while (isContinue && currTimes < 100);
        }

        private List<MailDelayRecord> listPendingRecord(Date endTime, Integer offset, Integer limit, Integer maxSendTimes) {
            //为了可以使用到索引
            List<String> createDateList = getNeedScanDateList();
            return emailBiz.listPendingDelayRecord(createDateList, endTime, offset, limit, maxSendTimes);
        }

        private Map<String, List<MailDelayRecord>> splitGroup(List<MailDelayRecord> mailGroupMergeList) {
            //相同groupKey、相同主题的分到同一组
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
         * 发送邮件
         * @param delayRecordList
         * @return  返回发送失败的记录个数
         */
        private int sendEmail(String groupKey, String subject, List<MailDelayRecord> delayRecordList, int currTimes, int groupNum) {
            int failCount = 0;
            //1.分组，即每封邮件中应该包含多少条记录数
            Map<Integer, List<MailDelayRecord>> groupMap = ConvertUtil.splitGroup(delayRecordList, RECORDS_PER_EMAIL);
            logger.info("currTimes={} groupNum={} 将分成{}封邮件发送", currTimes, groupNum, groupMap.size());
            int mailNum = 0;
            for (Map.Entry<Integer, List<MailDelayRecord>> entry : groupMap.entrySet()) {
                mailNum ++;
                List<MailDelayRecord> recordList = entry.getValue();
                String content;
                try {
                    //2.解析邮件模板，转换成要发送的邮件内容
                    Map<String, Object> tplParam = new HashMap<>();
                    tplParam.put("emailList", recordList);
                    content = emailBiz.resolveTplContent(TEMPLATE_FILE, tplParam);
                } catch (Exception e) {
                    logger.error("currTimes={} groupNum={} mailNum={} 邮件模板解析异常，将跳过本批次的{}条记录数", currTimes, groupNum, mailNum, recordList.size(), e);
                    failCount += recordList.size();
                    continue;
                }

                //3.先把记录更新为'发送中'，并更新发送起始时间
                boolean isSuccess = updatePendingToSending(recordList);
                if (isSuccess) {
                    logger.info("currTimes={} groupNum={} mailNum={} recordList.size={} 已更新为'发送中'", currTimes, groupNum, mailNum, recordList.size());
                } else {
                    logger.error("currTimes={} groupNum={} mailNum={} 更新为'发送中'失败，将跳过本批次的{}条记录数", currTimes, groupNum, mailNum, recordList.size());
                    failCount += recordList.size();
                    continue;
                }

                //4.执行邮件发送
                try {
                    isSuccess = emailBiz.sendHtml(groupKey, subject, content);
                } catch (Exception e) {
                    isSuccess = false;
                    logger.error("currTimes={} groupNum={} mailNum={} recordList.size={} 邮件发送异常", currTimes, groupNum, mailNum, e);
                }

                //5.根据邮件发送结果进行状态扭转
                if (isSuccess) {
                    logger.info("currTimes={} groupNum={} mailNum={} recordList.size={} 邮件发送成功，将更新为'已发送'", currTimes, groupNum, mailNum, recordList.size());
                    //5.1 发送成功则更新为'已发送'，如果此处更新失败，超过一定时间后会被回退到'待发送'，这个可能会造成邮件的重复发送，但不会漏发
                    updateSendingToFinish(recordList);
                } else {
                    //5.2 发送失败则回退到'待发送'，如果此处更新失败，超过一定时间后也会被回退到'待发送'
                    logger.error("currTimes={} groupNum={} mailNum={} recordList.size={} 邮件发送失败，将回退到'待发送'", currTimes, groupNum, mailNum, recordList.size());
                    revertSendingToPending(recordList);
                    failCount += recordList.size();
                }
            }
            return failCount;
        }

        /**
         * 发送前更新为'发送中'
         * @param recordList
         * @return  返回更新失败的记录个数
         */
        private boolean updatePendingToSending(List<MailDelayRecord> recordList){
            try {
                List<Long> idList = new ArrayList<>();
                recordList.forEach(e -> idList.add(e.getId()));
                emailBiz.updatePendingDelayRecordToSending(idList, new Date());
                return true;
            } catch (Exception e) {
                logger.error("把'待发送'状态更新为'发送中'时出现失败", e);
                return false;
            }
        }

        /**
         * 发送后更新为'已发送'
         * @param recordList
         * @return  返回更新失败的记录个数
         */
        private void updateSendingToFinish(List<MailDelayRecord> recordList) {
            try {
                List<Long> idList = new ArrayList<>();
                recordList.forEach(e -> idList.add(e.getId()));
                emailBiz.updateSendingDelayRecordToFinish(idList, new Date());
            } catch (Exception e) {
                logger.error("把'发送中'状态更新为'已发送'状态时出现异常", e);
            }
        }

        /**
         * 把处于 '发送中' 状态超过5分钟的记录回退到 '待发送' 状态，避免发送成功/失败，但是更新为 '已发送' 状态的时候失败了
         * @param recordList
         * @return  返回更新失败的记录个数
         */
        private int revertSendingToPending(List<MailDelayRecord> recordList) {
            List<Long> idList = new ArrayList<>();
            recordList.forEach(e -> idList.add(e.getId()));
            try {
                int successCount = emailBiz.revertSendingDelayRecordToPending(idList);
                return recordList.size() - successCount;
            } catch (Exception e) {
                logger.error("把'发送中'状态回退到'待发送'状态时出现异常", e);
                return recordList.size();
            }
        }

        private List<String> getNeedScanDateList() {
            List<String> createDateList = new ArrayList<>();
            createDateList.add(DateUtil.formatDate(DateUtil.addDay(new Date(), -2)));
            createDateList.add(DateUtil.formatDate(DateUtil.addDay(new Date(), -1)));
            createDateList.add(DateUtil.formatDate(new Date()));
            return createDateList;
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

            TaskWatcher watcher = new TaskWatcher("过期记录删除任务");
            watcher.begin();
            try {
                Date endTime = DateUtil.addDay(new Date(), (-1 * RECORD_KEEP_DAYS));
                boolean isContinue = true;
                int offset = 0;
                int maxSendTimes = MAX_RESEND_TIMES;
                int currTimes = 1;

                do {
                    logger.info("currTimes={} offset={} endDate={} 将要查询需要删除的记录", currTimes, offset, DateUtil.formatDate(endTime));
                    List<MailDelayRecord> recordList = listFinishOrOvertimesRecord(endTime, offset, RECORDS_PER_PAGE, maxSendTimes);
                    logger.info("currTimes={} offset={} endDate={} 查询到需要删除的{}条记录", currTimes, offset, DateUtil.formatDate(endTime), recordList==null?0:recordList.size());
                    if(recordList == null || recordList.isEmpty()){
                        break;
                    }else if(recordList.size() < RECORDS_PER_PAGE){
                        isContinue = false;
                    }

                    int failCount = deleteFinishRecord(recordList);
                    offset += failCount;
                    currTimes++;

                    watcher.addFail(failCount).addSuccess(recordList.size()-failCount);
                } while (isContinue && currTimes < 100);
            } catch (Exception e) {
                logger.error("执行删除任务时出现异常", e);
            } finally {
                distributedLock.unlock(lock);
            }

            watcher.end();
            logger.info("任务执行结果: {}", watcher.toString());
        }

        /**
         * 拉取'已发送'或者超过最大重试次数的记录
         * @param endTime
         * @param offset
         * @param limit
         * @return
         */
        private List<MailDelayRecord> listFinishOrOvertimesRecord(Date endTime, Integer offset, Integer limit, Integer maxSendTimes) {
            return emailBiz.listFinishOrOvertimesDelayRecord(endTime, offset, limit, maxSendTimes);
        }

        /**
         * 删除发送完毕的记录
         * @param recordList
         * @return  返回删除失败的记录数
         */
        private int deleteFinishRecord(List<MailDelayRecord> recordList) {
            List<Long> idList = new ArrayList<>();
            for (MailDelayRecord record : recordList) {
                if (record.getStatus() == EmailSendStatusEnum.FINISH.getValue() || record.getSendTimes() >= MAX_RESEND_TIMES) {
                    idList.add(record.getId());
                }
            }
            if (idList.isEmpty()) {
                return recordList.size();
            }

            try {
                emailBiz.deleteDelayRecord(idList);
                return recordList.size() - idList.size();
            } catch (Exception e) {
                logger.error("本批次删除失败", e);
                return recordList.size();
            }
        }
    }
}
