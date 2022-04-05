package com.xpay.common.statics.enums.merchant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 商户状态枚举
 */
public enum MerchantStatusEnum {
	CREATED(1, "已创建"),
	ACTIVE(2, "激活"),
	INACTIVE(3, "冻结"),
	;

	/** 枚举值 */
	private int value;

	/** 描述 */
	private String desc;

	private MerchantStatusEnum(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public int getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}

	public static MerchantStatusEnum getEnum(int value) {
		MerchantStatusEnum resultEnum = null;
		MerchantStatusEnum[] enumAry = MerchantStatusEnum.values();
		for (int i = 0; i < enumAry.length; i++) {
			if (enumAry[i].getValue() == value) {
				resultEnum = enumAry[i];
				break;
			}
		}
		return resultEnum;
	}

	public static List toList() {
		MerchantStatusEnum[] ary = MerchantStatusEnum.values();
		List list = new ArrayList();
		for (int i = 0; i < ary.length; i++) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("value", String.valueOf(ary[i].getValue()));
			map.put("desc", ary[i].getDesc());
			list.add(map);
		}
		return list;
	}

	public static Map<String, Map<String, Object>> toMap() {
		MerchantStatusEnum[] ary = MerchantStatusEnum.values();
		Map<String, Map<String, Object>> enumMap = new HashMap<String, Map<String, Object>>();
		for (int num = 0; num < ary.length; num++) {
			Map<String, Object> map = new HashMap<String, Object>();
			String key = ary[num].name();
			map.put("value", String.valueOf(ary[num].getValue()));
			map.put("desc", ary[num].getDesc());
			enumMap.put(key, map);
		}
		return enumMap;
	}
}
