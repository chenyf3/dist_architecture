package com.xpay.service.timer.biz;

import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.timer.dto.OpLogContentDto;
import com.xpay.facade.timer.dto.OpLogDto;
import com.xpay.facade.timer.enums.OpTypeEnum;
import com.xpay.service.timer.config.TimerProperties;
import com.xpay.service.timer.dao.OpLogDao;
import com.xpay.service.timer.entity.OpLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 操作日志表的逻辑层，记录对Quartz实例、任务等的操作日志，方便业务管理和追踪
 * @author chenyf
 */
@Component
public class ExtOpLogBiz {
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	TimerProperties timerProperties;
	@Autowired
	private OpLogDao opLogDao;

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void addAsync(String objKey, String remark, OpTypeEnum opType, String logInfo){
		try{
			OpLogContentDto contentDto = new OpLogContentDto();
			contentDto.setOpType(opType.getValue());
			contentDto.setLogInfo(logInfo);
			addAsync(objKey, remark, contentDto);
		}catch(Throwable e){
			logger.error("保存日志异常 objKey={} remark={} msg={}", objKey, remark, e.getMessage());
		}
	}

	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public void addAsync(String objKey, String remark, OpLogContentDto contentDto){
		if(timerProperties.getOpLogEnable() == false){
			return;
		}

		CompletableFuture.runAsync(() -> {
			try{
				OpLog log = new OpLog();
				log.setCreateTime(new Date());
				log.setRemark(remark);
				log.setObjKey(objKey);
				log.setContent(JsonUtil.toJson(contentDto));
				opLogDao.insert(log);
			}catch(Throwable e){
				logger.error("保存日志异常 objKey={} remark={} msg={}", objKey, remark, e.getMessage());
			}
		});
	}

	public PageResult<List<OpLogDto>> listOpLogPage(Map<String, Object> paramMap, PageQuery pageQuery){
		PageResult<List<OpLog>> result = opLogDao.listPage(paramMap, pageQuery);
		List<OpLogDto> dtoList = BeanUtil.newAndCopy(result.getData(), OpLogDto.class);
		return PageResult.newInstance(dtoList, result);
	}

	private String getObjKeyForJob(String jobGroup, String jobName){
		return jobGroup + "-" + jobName;
	}
}
