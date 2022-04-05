package com.xpay.demo.rocketmq.bizVo;

import com.xpay.common.statics.dto.mq.MsgDto;

import java.math.BigDecimal;
import java.util.List;

public class OrderVo extends MsgDto {
    private BigDecimal amount;
    private boolean isFinish;
    List<ItemVo> itemVoList;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean getIsFinish() {
        return isFinish;
    }

    public void setIsFinish(boolean finish) {
        isFinish = finish;
    }

    public List<ItemVo> getItemVoList() {
        return itemVoList;
    }

    public void setItemVoList(List<ItemVo> itemVoList) {
        this.itemVoList = itemVoList;
    }
}
