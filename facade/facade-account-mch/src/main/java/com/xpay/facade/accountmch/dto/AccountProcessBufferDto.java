package com.xpay.facade.accountmch.dto;

import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.dto.account.AccountRequestDto;
import com.xpay.common.statics.dto.mq.MsgDto;

import java.util.List;

public class AccountProcessBufferDto extends MsgDto {
    private AccountRequestDto requestDto;
    private List<AccountProcessDto> processDtoList;

    public AccountRequestDto getRequestDto() {
        return requestDto;
    }

    public void setRequestDto(AccountRequestDto requestDto) {
        this.requestDto = requestDto;
    }

    public List<AccountProcessDto> getProcessDtoList() {
        return processDtoList;
    }

    public void setProcessDtoList(List<AccountProcessDto> processDtoList) {
        this.processDtoList = processDtoList;
    }
}
