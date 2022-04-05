package com.xpay.gateway.callback.enums;

/**
 * 有回调的公司枚举
 */
public enum CompanyEnum {
    ALIPAY("/alipay"),
    WECHAT_PAY("/wechatPay")
    ;
    /**
     * 一级访问路径，将会根据此路径来确定是哪家公司的回调，进而会使用这家公司的签名、验签规则，以及响应数据的生成
     */
    private String firstPath;

    /** ------------------------ 各个公司的 Spring Bean Name ------------------------ */
    public final static String ALIPAY_BEAN_NAME = "ALIPAY";
    public final static String WECHAT_PAY_BEAN_NAME = "WECHAT_PAY";
    /** ------------------------ 各个公司的 Spring Bean Name ------------------------ */

    private CompanyEnum(String firstPath){
        this.firstPath = firstPath;
    }

    public String getFirstPath() {
        return firstPath;
    }

    public void setFirstPath(String firstPath) {
        this.firstPath = firstPath;
    }

    public static CompanyEnum getEnum(String name){
        return CompanyEnum.valueOf(name);
    }

    public static CompanyEnum getEnumByFirstPath(String firstPath){
        CompanyEnum[] companies = CompanyEnum.values();
        for(int i=0; i<companies.length; i++){
            if(companies[i].getFirstPath().equals(firstPath)){
                return companies[i];
            }
        }
        return null;
    }

    public static String getCompanyBeanName(CompanyEnum company){
        String beanName;
        switch (company) {
            case ALIPAY:
                beanName = CompanyEnum.ALIPAY_BEAN_NAME;
                break;
            case WECHAT_PAY:
                beanName = CompanyEnum.WECHAT_PAY_BEAN_NAME;
                break;
            default:
                beanName = null;
                break;
        }
        return beanName;
    }
}
