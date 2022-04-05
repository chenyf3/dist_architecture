package com.xpay.service.migration.listener;

import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.statics.dto.migrate.MigrateParamDto;
import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.utils.JsonUtil;
import com.xpay.service.migration.migrators.DataMigrateHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
public class MigrateMsgListener {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    DataMigrateHelper dataMigrateHelper;

    public MigrateMsgListener(DataMigrateHelper dataMigrateHelper){
        this.dataMigrateHelper = dataMigrateHelper;
    }

    /**
     * 消费数据迁移的消息
     * @param msg
     */
    @JmsListener(destination = TopicDest.DATA_MIGRATION, subscription = "dataMigrateConsume", concurrency = "1-2")
    public void dataMigrateConsume(String msg) {
        try {
            MsgDto msgDto = JsonUtil.toBean(msg, MsgDto.class);
            MigrateParamDto migrateDto = JsonUtil.toBean(msgDto.getJsonParam(), MigrateParamDto.class);
            dataMigrateHelper.submitMigrationTask(migrateDto);
        } catch (Exception e) {
            logger.error("添加数据迁移任务时出现异常 MsgDto = {} ", msg, e);
        }
    }
}
