package com.xpay.common.statics.dto.account;

import com.xpay.common.statics.dto.mq.MsgDto;

public class AdvanceClearMsgDto extends MsgDto {
    private Long accountClearId;


    public AdvanceClearMsgDto(){}

    public AdvanceClearMsgDto(String topic, String tags){
        super(topic, tags);
    }

    public Long getAccountClearId() {
        return accountClearId;
    }

    public void setAccountClearId(Long accountClearId) {
        this.accountClearId = accountClearId;
    }
}
