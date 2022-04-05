package com.xpay.web.portal.web.vo.common;

import com.xpay.common.validator.validators.Phone;
import jakarta.validation.constraints.NotBlank;

/**
 * 发送短信接口
 *
 * @author pengyz
 */
public class SmsVo {
	@Phone(message = "手机号码有误")
	private String phone;
	@NotBlank(message = "登录名不能为空")
	private String loginName;
	private String smsType;

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public String getSmsType() {
		return smsType;
	}

	public void setSmsType(String smsType) {
		this.smsType = smsType;
	}


}
