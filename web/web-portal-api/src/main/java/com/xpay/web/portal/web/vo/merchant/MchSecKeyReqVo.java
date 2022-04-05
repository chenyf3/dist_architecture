package com.xpay.web.portal.web.vo.merchant;

import com.xpay.common.validator.validators.Chinese;
import jakarta.validation.constraints.NotNull;

public class MchSecKeyReqVo {
	@NotNull(message="验证码不能为空")
	private String verifyCode;//短信验证码
	@NotNull(message="加解密类型不能为空")
	private Integer signType;
	@Chinese(message="商户公钥不能含有中文字符")
	@NotNull(message="商户公钥不能为空")
	private String mchPubKey;//商户公钥
	private Boolean updatePlatKey;//是否更新平台密钥对

	public String getVerifyCode() {
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}

	public Integer getSignType() {
		return signType;
	}

	public void setSignType(Integer signType) {
		this.signType = signType;
	}

	public String getMchPubKey() {
		return mchPubKey;
	}

	public void setMchPubKey(String mchPubKey) {
		this.mchPubKey = mchPubKey;
	}

	public Boolean getUpdatePlatKey() {
		return updatePlatKey;
	}

	public void setUpdatePlatKey(Boolean updatePlatKey) {
		this.updatePlatKey = updatePlatKey;
	}
}
