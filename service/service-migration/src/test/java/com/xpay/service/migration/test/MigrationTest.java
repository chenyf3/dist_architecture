package com.xpay.service.migration.test;

import com.xpay.common.statics.constants.migrate.MigratorName;
import com.xpay.common.statics.constants.mqdest.TopicDest;
import com.xpay.common.statics.constants.mqdest.TopicGroup;
import com.xpay.common.statics.dto.migrate.MigrateParamDto;
import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.statics.enums.migrate.MigrateItemEnum;
import com.xpay.common.utils.JsonUtil;
import com.xpay.starter.plugin.plugins.MQSender;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class MigrationTest extends BaseTestCase {
    @Autowired
    MQSender mqSender;

    @Test
    public void testMigration(){
        MsgDto msgDto = new MsgDto();
        msgDto.setTopic(TopicDest.DATA_MIGRATION);
        msgDto.setTags(TopicGroup.COMMON_GROUP);
        msgDto.setMchNo("455455544656556");
        msgDto.setTrxNo("455555555555555");

        MigrateParamDto migrateDto = new MigrateParamDto();
        migrateDto.setMigratorName(MigratorName.ACCOUNT_MCH_MIGRATION);
        migrateDto.setMigrateItems(new String[]{MigrateItemEnum.ACCOUNT_MCH_PROCESS_DETAIL.name(),
                MigrateItemEnum.ACCOUNT_MCH_PROCESS_PENDING.name()});
        msgDto.setJsonParam(JsonUtil.toJson(migrateDto));
        mqSender.sendOne(msgDto);
    }
}
