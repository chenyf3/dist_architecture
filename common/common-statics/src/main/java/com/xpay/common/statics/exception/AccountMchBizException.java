package com.xpay.common.statics.exception;

public class AccountMchBizException extends BizException {

    public final static int ACCOUNT_RECORD_NOT_EXIT = 200100001;//账户记录不存在
    public final static int ACCOUNT_STATUS_IS_INACTIVE = 200100001;//账户'禁用'状态
    public final static int ACCOUNT_PROCESS_REPEAT = 200100002;//重复账务处理
    public final static int TOTAL_BALANCE_NOT_ENOUGH = 200100003;//账户总余额不足
    public final static int SETTLED_AMOUNT_NOT_ENOUGH = 200100004;//已结算金额不足
    public final static int UNSETTLE_AMOUNT_NOT_ENOUGH = 200100005;//待清算金额不足
    public final static int AVAIL_ADVANCE_AMOUNT_NOT_ENOUGH = 200100006;//可用垫资金额不足
    public final static int AVAIL_BALANCE_NOT_ENOUGH = 200100007;//可用余额不足
    public final static int AVAIL_BALANCE_NOT_ENOUGH_FOR_RCMS = 200100008;//金额风控
    public final static int DEBIT_ABLE_AMOUNT_NOT_ENOUGH = 200100009;//可出款金额不足
    public final static int ACCOUNT_AMOUNT_CALC_ERROR = 200100009;//账户金额计算错误
    public final static int ADVANCE_CLEAR_FAIL = 200100009;//垫资清零失败
    public final static int ACCOUNT_SNAPSHOT_FAIL = 200100009;//账户快照失败
    public final static int ACQUIRE_LOCK_FAIL = 200100009;//账户锁失败



    public AccountMchBizException(){
        super();
    }
    public AccountMchBizException(int code, String msg) {
        super(code, msg, null);
    }
}
