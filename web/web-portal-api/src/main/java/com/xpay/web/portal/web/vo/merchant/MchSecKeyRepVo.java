package com.xpay.web.portal.web.vo.merchant;

public class MchSecKeyRepVo {
	private Integer signType;
	private String mchPublicKey;
	private String platPublicKey;

	public Integer getSignType() {
		return signType;
	}

	public void setSignType(Integer signType) {
		this.signType = signType;
	}

	public String getMchPublicKey() {
		return mchPublicKey;
	}

	public void setMchPublicKey(String mchPublicKey) {
		this.mchPublicKey = mchPublicKey;
	}

	public String getPlatPublicKey() {
		return platPublicKey;
	}

	public void setPlatPublicKey(String platPublicKey) {
		this.platPublicKey = platPublicKey;
	}
}
