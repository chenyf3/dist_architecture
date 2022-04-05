/*
 * Powered By [xpay.com]
 */
package com.xpay.service.merchant.biz;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.MD5Util;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.merchant.dto.MerchantTradePwdDto;
import com.xpay.service.merchant.dao.MerchantTradePwdDao;
import com.xpay.service.merchant.entity.MerchantTradePwd;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Component
public class MerchantTradePwdBiz {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private MerchantTradePwdDao merchantTradePwdDao;

	public MerchantTradePwdDto getById(long id){
		MerchantTradePwd merchantTradePwd = merchantTradePwdDao.getById(id);
		return BeanUtil.newAndCopy(merchantTradePwd, MerchantTradePwdDto.class);
	}

	public MerchantTradePwdDto getByMchNo(String mchNo){
		MerchantTradePwd merchantTradePwd = merchantTradePwdDao.getByMerchantNo(mchNo);
		return BeanUtil.newAndCopy(merchantTradePwd, MerchantTradePwdDto.class);
	}

	/**
	 * 创建交易密码
	 * @param mchNo
	 * @param tradePwd
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean createTradePwd(String mchNo, String tradePwd) {
		logger.info("交易密码创建 mchNo={}", mchNo);
		if(StringUtil.isEmpty(mchNo)){
			throw new BizException(BizException.BIZ_INVALID, "商户编号不能为空");
		}else if(StringUtil.isEmpty(tradePwd)){
			throw new BizException(BizException.BIZ_INVALID, "交易密码不能为空");
		}

		MerchantTradePwd tradePwdOld = merchantTradePwdDao.getByMerchantNo(mchNo);
		if(tradePwdOld != null){
			throw new BizException(BizException.BIZ_INVALID, "当前商户交易密码已存在，不能重复添加！");
		}

		MerchantTradePwd merchantTradePwd = new MerchantTradePwd();
		merchantTradePwd.setCreateTime(new Date());
		merchantTradePwd.setVersion(0);
		merchantTradePwd.setInitial(PublicStatus.ACTIVE);
		merchantTradePwd.setMchNo(mchNo);
		merchantTradePwd.setErrorTimes(0);
		merchantTradePwd.setTradePwd(encryptPwd(tradePwd));
		merchantTradePwdDao.insert(merchantTradePwd);
		return true;
	}

	/**
	 * 更新交易密码
	 * @param mchNo
	 * @param newPwd
	 * @param modifier
	 * @param remark
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean updateTradePwd(String mchNo, String newPwd, String modifier, String remark) {
		logger.info("修改交易密码 mchNo={} modifier={} remark={}", mchNo, modifier, remark);

		if(StringUtil.isEmpty(mchNo)){
			throw new BizException(BizException.BIZ_INVALID, "商户编号不能为空");
		}else if(StringUtil.isEmpty(newPwd)){
			throw new BizException(BizException.BIZ_INVALID, "新交易密码不能为空");
		}

		MerchantTradePwd tradePwd = merchantTradePwdDao.getByMerchantNo(mchNo);
		if(tradePwd == null){
			throw new BizException(BizException.BIZ_INVALID, "交易密码不存在！");
		}
		tradePwd.setTradePwd(encryptPwd(newPwd));
		merchantTradePwdDao.update(tradePwd);
		return true;
	}

	/**
	 * 校验用户交易密码
	 * @param mchNo 商户编号
	 * @param pwd   交易密码
	 */
	public boolean validTradePwd(String mchNo, String pwd) {
		if(StringUtil.isEmpty(mchNo)){
			throw new BizException("商户编号不能为空");
		}else if(StringUtil.isEmpty(pwd)){
			throw new BizException("密码不能为空");
		}

		MerchantTradePwdDto userPwd = getByMchNo(mchNo);
		if (userPwd == null) {
			throw new BizException("交易密码不存在！");
		}

		pwd = encryptPwd(pwd);
		return userPwd.getTradePwd().equals(pwd);
	}

	private String encryptPwd(String pwd){
		return MD5Util.getMD5Hex(pwd);
	}
}
