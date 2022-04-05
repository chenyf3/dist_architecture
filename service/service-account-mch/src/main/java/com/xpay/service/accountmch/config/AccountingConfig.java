package com.xpay.service.accountmch.config;

import com.xpay.common.statics.enums.account.AccountProcessTypeEnum;
import com.xpay.service.accountmch.accounting.AccountingStrategy;
import com.xpay.service.accountmch.accounting.processors.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

@SpringBootConfiguration
public class AccountingConfig {
    public final static String DEFAULT_TOTAL_ORDER_COUNT = "0,0,0";

    @Bean
    public AccountingStrategy accountingStrategy(BeanFactory beanFactory){
        Map<Integer, AccountingProcessor> processorMap = new HashMap<>();
        processorMap.put(AccountProcessTypeEnum.CREDIT.getValue(), beanFactory.getBean(CreditProcessor.class));
        processorMap.put(AccountProcessTypeEnum.DEBIT_OUT.getValue(), beanFactory.getBean(DebitProcessor.class));
        processorMap.put(AccountProcessTypeEnum.DEBIT_RETURN.getValue(), beanFactory.getBean(ReturnProcessor.class));
        processorMap.put(AccountProcessTypeEnum.SELF_CIRCULATION.getValue(), beanFactory.getBean(CirculationProcessor.class));

        AdjustProcessor adjustProcessor = beanFactory.getBean(AdjustProcessor.class);
        processorMap.put(AccountProcessTypeEnum.ADJUST_ADD.getValue(), adjustProcessor);
        processorMap.put(AccountProcessTypeEnum.ADJUST_SUB.getValue(), adjustProcessor);
        processorMap.put(AccountProcessTypeEnum.ADJUST_AMOUNT_RATIO.getValue(), adjustProcessor);
        return new AccountingStrategy(processorMap);
    }
}
