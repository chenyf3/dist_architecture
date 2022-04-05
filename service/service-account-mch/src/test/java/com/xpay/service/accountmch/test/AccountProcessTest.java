package com.xpay.service.accountmch.test;

import com.xpay.common.statics.dto.account.AccountProcessDto;
import com.xpay.common.statics.dto.account.AccountRequestDto;
import com.xpay.common.statics.enums.account.AccountMchAmountTypeEnum;
import com.xpay.common.statics.enums.account.AccountProcessTypeEnum;
import com.xpay.common.statics.enums.account.AccountStatusEnum;
import com.xpay.common.statics.enums.product.ProductCodeEnum;
import com.xpay.common.statics.enums.product.ProductTypeEnum;
import com.xpay.facade.accountmch.service.AccountManageFacade;
import com.xpay.facade.accountmch.service.AccountProcessMchFacade;
import com.xpay.facade.sequence.service.SequenceFacade;
import com.xpay.service.accountmch.biz.AccountScheduleBiz;
import org.apache.dubbo.config.annotation.Reference;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AccountProcessTest extends BaseTestCase {
    private String accountNo = "1000000000001";
    private String accountNo2 = "1000000000002";
    private String accountNo3 = "1000000000003";

    @Autowired
    AccountScheduleBiz accountScheduleBiz;
    @Reference
    AccountManageFacade accountManageFacade;
    @Reference
    AccountProcessMchFacade accountProcessMchFacade;
    @Reference
    SequenceFacade sequenceFacade;


    @Ignore
    @Test
    public void testCreateAccount(){
        accountManageFacade.createAccount(accountNo, BigDecimal.valueOf(10000000L), BigDecimal.ONE, "chenyf");
        accountManageFacade.createAccount(accountNo2, BigDecimal.valueOf(10000000L), BigDecimal.valueOf(0.8), "chenyf");
        accountManageFacade.createAccount(accountNo3, BigDecimal.valueOf(10000000L), BigDecimal.valueOf(0.9), "chenyf");
    }

    @Ignore
    @Test
    public void testChangeAccountStatus(){
        accountManageFacade.changeAccountStatus(accountNo, AccountStatusEnum.FREEZE_CREDIT,"chenyf", "unit test");
    }

//    @Ignore
    @Test
    public void testExecuteAsync(){
        AccountRequestDto requestDto = new AccountRequestDto();
        requestDto.setCallbackQueue("account-mch:callback");

        long start = System.currentTimeMillis();
        int i = 0, max = 5000;
        while(i < max){
            i++;

            AccountProcessDto processDto = new AccountProcessDto();
            processDto.setAccountNo(accountNo);
            processDto.setTrxNo(sequenceFacade.nextSnowId("APD", true));
            processDto.setMchTrxNo("" + sequenceFacade.nextSnowId());
            processDto.setProcessType(AccountProcessTypeEnum.CREDIT.getValue());
            processDto.setAmountType(AccountMchAmountTypeEnum.TOTAL_ADVANCE_AMOUNT.getValue());
            processDto.setAmount(BigDecimal.valueOf(100));
            processDto.setFee(BigDecimal.ONE);
            processDto.setBussType(ProductTypeEnum.PAY_RECEIVE.getValue());
            processDto.setBussCode(ProductCodeEnum.RECEIVE_WECHAT_H5.getValue());
            processDto.setDesc("test receive 2");

            List<AccountProcessDto> processDtoList = new ArrayList<>();
            processDtoList.add(processDto);
            accountProcessMchFacade.executeAsync(requestDto, processDtoList);
        }

        System.out.println("testExecuteAsync max=" + max + " 耗时" + ((System.currentTimeMillis()-start)/1000) + "秒");
        try{
            Thread.sleep(10000);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void testExecuteSyncForAsync(){
        Long[] ids = new Long[]{1L,2L,3L};
        for(int i=0; i<ids.length; i++){
            accountProcessMchFacade.executeSync(ids[i]);
        }
    }

    @Ignore
    @Test
    public void testExecuteCredit(){
        int i = 0, max = 1;
        while(i < max) {
            i++;

            AccountRequestDto requestDto = new AccountRequestDto();
            requestDto.setCallbackQueue("account-mch:callback");

            AccountProcessDto processDto = new AccountProcessDto();
            processDto.setAccountNo(accountNo2);
            processDto.setTrxNo(sequenceFacade.nextSnowId("APD", true));
            processDto.setMchTrxNo("" + sequenceFacade.nextSnowId());
            processDto.setProcessType(AccountProcessTypeEnum.CREDIT.getValue());
            processDto.setAmountType(AccountMchAmountTypeEnum.TOTAL_ADVANCE_AMOUNT.getValue());
            processDto.setAmount(BigDecimal.valueOf(100));
            processDto.setFee(BigDecimal.ONE);
            processDto.setBussType(ProductTypeEnum.PAY_RECEIVE.getValue());
            processDto.setBussCode(ProductCodeEnum.RECEIVE_WECHAT_H5.getValue());
            processDto.setDesc("test direct receive 2");

            long start = System.currentTimeMillis();
            accountProcessMchFacade.executeSync(requestDto, Collections.singletonList(processDto));
            System.out.println("index="+i+" costMills="+(System.currentTimeMillis()-start));
        }
    }

//    @Ignore
    @Test
    public void testAdjustAdvanceRatio(){
        accountManageFacade.adjustAdvanceRatio(accountNo, BigDecimal.valueOf(10000000L), BigDecimal.valueOf(0.9), "chenyf" , "test adjust ratio");
    }

    @Ignore
    @Test
    public void testExecuteDebit(){
        long start = System.currentTimeMillis();
        int i = 0, max = 3000;
        while(i < max){
            i++;

            AccountRequestDto requestDto = new AccountRequestDto();
            requestDto.setCallbackQueue("account-mch:callback");

            AccountProcessDto processDto = new AccountProcessDto();
            processDto.setAccountNo(accountNo);
            processDto.setTrxNo(sequenceFacade.nextSnowId("APD", true));
            processDto.setMchTrxNo("" + sequenceFacade.nextSnowId());
            processDto.setProcessType(AccountProcessTypeEnum.DEBIT_OUT.getValue());
            processDto.setAmountType(AccountMchAmountTypeEnum.AVAIL_ADVANCE_AMOUNT.getValue());
            processDto.setAmount(BigDecimal.valueOf(100));
            processDto.setFee(BigDecimal.ONE);
            processDto.setBussType(ProductTypeEnum.PAYMENT.getValue());
            processDto.setBussCode(ProductCodeEnum.PAYMENT_ADVANCE.getValue());
            processDto.setDesc("test direct debit");

            accountProcessMchFacade.executeSync(requestDto, Collections.singletonList(processDto));
        }
        System.out.println("testExecuteDebit max=" + max + " 耗时" + ((System.currentTimeMillis()-start)/1000) + "秒");
    }

//    @Ignore
    @Test
    public void testExecuteReturn(){
        String origTrxNo = "APD21010625314639499759707";

        AccountRequestDto requestDto = new AccountRequestDto();
        requestDto.setCallbackQueue("account-mch:callback");

        AccountProcessDto processDto = new AccountProcessDto();
        processDto.setAccountNo(accountNo);
        processDto.setTrxNo(origTrxNo);
        processDto.setMchTrxNo("" + sequenceFacade.nextSnowId());
        processDto.setProcessType(AccountProcessTypeEnum.DEBIT_RETURN.getValue());
        processDto.setAmountType(AccountMchAmountTypeEnum.SOURCE_DEBIT_AMOUNT.getValue());
        processDto.setAmount(BigDecimal.valueOf(100));
        processDto.setFee(BigDecimal.ZERO);
        processDto.setBussType(ProductTypeEnum.PAYMENT.getValue());
        processDto.setBussCode(ProductCodeEnum.PAYMENT_ADVANCE.getValue());
        processDto.setDesc("test direct return");

        accountProcessMchFacade.executeSync(requestDto, Collections.singletonList(processDto));
    }

    @Ignore
    @Test
    public void testRetainAmountDebit(){
        long start = System.currentTimeMillis();
        int i = 0, max = 753;
        while(i < max){
            i++;

            AccountRequestDto requestDto = new AccountRequestDto();
            requestDto.setCallbackQueue("account-mch:callback");

            AccountProcessDto processDto = new AccountProcessDto();
            processDto.setAccountNo(accountNo);
            processDto.setTrxNo(sequenceFacade.nextSnowId("APD", true));
            processDto.setMchTrxNo("" + sequenceFacade.nextSnowId());
            processDto.setProcessType(AccountProcessTypeEnum.DEBIT_OUT.getValue());
            processDto.setAmountType(AccountMchAmountTypeEnum.AVAIL_BALANCE_AMOUNT.getValue());
            processDto.setAmount(BigDecimal.valueOf(100));
            processDto.setFee(BigDecimal.ONE);
            processDto.setBussType(ProductTypeEnum.REFUND.getValue());
            processDto.setBussCode(ProductCodeEnum.PAYMENT_ADVANCE.getValue());
            processDto.setDesc("test retain amount debit");

            accountProcessMchFacade.executeSync(requestDto, Collections.singletonList(processDto));
        }
        System.out.println("testRetainAmountDebit max=" + max + " 耗时" + ((System.currentTimeMillis()-start)/1000) + "秒");
    }

    @Ignore
    @Test
    public void testRetainAmountReturn(){
        String origTrxNo = "APD20123123146265885806656";

        AccountRequestDto requestDto = new AccountRequestDto();
        requestDto.setCallbackQueue("account-mch:callback");

        AccountProcessDto processDto = new AccountProcessDto();
        processDto.setAccountNo(accountNo2);
        processDto.setTrxNo(origTrxNo);
        processDto.setMchTrxNo("" + sequenceFacade.nextSnowId());
        processDto.setProcessType(AccountProcessTypeEnum.DEBIT_RETURN.getValue());
        processDto.setAmountType(AccountMchAmountTypeEnum.SOURCE_DEBIT_AMOUNT.getValue());
        processDto.setAmount(BigDecimal.valueOf(90));
        processDto.setFee(BigDecimal.ONE);
        processDto.setBussType(ProductTypeEnum.REFUND.getValue());
        processDto.setBussCode(ProductCodeEnum.PAYMENT_ADVANCE.getValue());
        processDto.setDesc("test retain amount return");

        accountProcessMchFacade.executeSync(requestDto, Collections.singletonList(processDto));
    }

    @Ignore
    @Test
    public void testRetainAmountDebit3(){
        AccountRequestDto requestDto = new AccountRequestDto();
        requestDto.setCallbackQueue("account-mch:callback");

        AccountProcessDto processDto = new AccountProcessDto();
        processDto.setAccountNo(accountNo2);
        processDto.setTrxNo(sequenceFacade.nextSnowId("APD", true));
        processDto.setMchTrxNo("" + sequenceFacade.nextSnowId());
        processDto.setProcessType(AccountProcessTypeEnum.DEBIT_OUT.getValue());
        processDto.setAmountType(AccountMchAmountTypeEnum.AVAIL_BALANCE_AMOUNT.getValue());
        processDto.setAmount(BigDecimal.valueOf(320));
        processDto.setFee(BigDecimal.ONE);
        processDto.setBussType(ProductTypeEnum.REFUND.getValue());
        processDto.setBussCode(ProductCodeEnum.PAYMENT_ADVANCE.getValue());
        processDto.setDesc("testRetainAmountDebit3");

        accountProcessMchFacade.executeSync(requestDto, Collections.singletonList(processDto));
    }

    @Ignore
    @Test
    public void testRetainAmountReturn3(){
        String origTrxNo = "APD1909090001433876660490240";

        AccountRequestDto requestDto = new AccountRequestDto();
        requestDto.setCallbackQueue("account-mch:callback");

        AccountProcessDto processDto = new AccountProcessDto();
        processDto.setAccountNo(accountNo2);
        processDto.setTrxNo(origTrxNo);
        processDto.setMchTrxNo("" + sequenceFacade.nextSnowId());
        processDto.setProcessType(AccountProcessTypeEnum.DEBIT_RETURN.getValue());
        processDto.setAmountType(AccountMchAmountTypeEnum.SOURCE_DEBIT_AMOUNT.getValue());
        processDto.setAmount(BigDecimal.valueOf(30));
        processDto.setFee(BigDecimal.ONE);
        processDto.setBussType(ProductTypeEnum.REFUND.getValue());
        processDto.setBussCode(ProductCodeEnum.PAYMENT_ADVANCE.getValue());
        processDto.setDesc("testRetainAmountReturn3");

        accountProcessMchFacade.executeSync(requestDto, Collections.singletonList(processDto));
    }

//    @Ignore
    @Test
    public void testMergeProcess(){
        accountScheduleBiz.scanPendingAndDoMergeProcess();
    }
}
