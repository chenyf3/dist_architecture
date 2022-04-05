package com.xpay.service.extend.biz.devops;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.extend.dto.PublishRecordDto;
import com.xpay.facade.extend.enums.BuildResultEnum;
import com.xpay.facade.extend.enums.BuildStatusEnum;
import com.xpay.facade.extend.vo.IdcVo;
import com.xpay.facade.extend.vo.PublishInfoVo;
import com.xpay.service.extend.conifg.DevopsProperties;
import com.xpay.service.extend.dao.PublishRecordDao;
import com.xpay.service.extend.entity.PublishRecord;
import com.xpay.starter.plugin.plugins.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 项目发布逻辑层
 * @author chenyf
 */
@Component
public class DevopsBiz {
	public static final String PROJECT_PUBLISH_LOCK = "publishTaskLock";//分布式锁的key
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	PublishRecordDao publishRecordDao;
	@Autowired
	IdcFlowBiz idcFlowBiz;
	@Autowired
	JenkinsBiz jenkinsBiz;
	@Autowired
	DistributedLock redisLock;
	@Autowired
	DevopsProperties devopsProperties;
	PublishScheduleTask scheduleTask;

	@PostConstruct
	public void init(){
		long scanInterval = devopsProperties.getPublish().getScanInterval() * 1000L;
		int publishTimeout = devopsProperties.getPublish().getTimeoutSec();
		int jobQuietSec = devopsProperties.getJenkins().getJobQuietPeriod();
		scheduleTask = new PublishScheduleTask(scanInterval, publishTimeout, jobQuietSec);
		scheduleTask.start();
	}

	/**
	 * @param toIdcList         切入机房列表
	 * @param checkPublishing   是否检查还有正在执行中的发布记录
	 * @param operator          操作人
	 * @return
	 */
	public boolean flowSwitch(List<String> toIdcList, Boolean checkPublishing, String operator){
		if(checkPublishing != null && checkPublishing){
			PublishRecordDto record = getNotFinishPublishRecord();
			if(record != null){
				throw new BizException(BizException.BIZ_INVALID, "当前还有发布记录正在执行中，不能执行流量切换！");
			}
		}
		return idcFlowBiz.flowSwitch(toIdcList, operator);
	}

	public IdcVo getCurrIdcFlow(){
		return idcFlowBiz.getCurrIdcFlow();
	}

	public boolean isInSameIdc(String idcCode, String address){
		return idcFlowBiz.isInSameIdc(idcCode, address);
	}

	/**
	 * 新增发布
	 * @param record
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public boolean addPublish(PublishRecordDto record, Integer buildType) {
		if(StringUtil.isEmpty(record.getBuildMsg())){
			throw new BizException(BizException.BIZ_INVALID, "构建信息不能为空！");
		}else if(StringUtil.isEmpty(record.getApps())){
			throw new BizException(BizException.BIZ_INVALID, "发布项目不能为空");
		}

		String jobName = devopsProperties.getJenkins().getBuildJobName();
		if(StringUtil.isEmpty(jobName)){
			throw new BizException(BizException.BIZ_INVALID, "buildJobName参数没有配置触发构建的Jenkins任务名");
		}

		if(idcFlowBiz.isIdcHasFlow(record.getIdc())){
			throw new BizException(BizException.BIZ_INVALID, "要发布的机房有流量,不能上线到此机房!");
		}

		PublishRecord publishRecord = BeanUtil.newAndCopy(record, PublishRecord.class);
		String apps = publishRecord.getApps().trim().replace("\n", ",");
		String notifyEmail = publishRecord.getNotifyEmail();
		if(StringUtil.isNotEmpty(notifyEmail)){
			notifyEmail = notifyEmail.trim().replace("\n", ",");
		}

		publishRecord.setApps(apps);
		publishRecord.setCreateTime(new Date());
		publishRecord.setVersion(0);
		publishRecord.setJobName(jobName);
		publishRecord.setBuildNo(String.valueOf(0));//默认初始话为0，如果不需要在镜像仓中查看镜像的历史版本，可以直接置为：latest
		publishRecord.setStatus(BuildStatusEnum.PENDING.getValue());
		publishRecord.setProcessTimes(0);
		publishRecord.setPublishTimes(1);
		publishRecord.setIdc(publishRecord.getIdc() == null ? "" : publishRecord.getIdc());
		publishRecord.setIdc(publishRecord.getIdc() == null ? "" : publishRecord.getIdc());
		publishRecord.setRelayApp(publishRecord.getRelayApp() == null ? "" : publishRecord.getRelayApp());
		publishRecord.setNotifyEmail(notifyEmail == null ? "" : notifyEmail);
		publishRecord.setNotifyUrl(publishRecord.getNotifyUrl() == null ? "" : publishRecord.getNotifyUrl());
		publishRecord.setModifier(publishRecord.getModifier());
		publishRecord.setRemark(publishRecord.getRemark() == null ? "" : StringUtil.subLeft(publishRecord.getRemark(), 80));

		publishRecordDao.insert(publishRecord);

		if(!"latest".equals(publishRecord.getBuildNo())){
			String buildNoStr = DateUtil.formatShortDate(new Date()) + String.format("%1$07d", publishRecord.getId());
			publishRecord.setBuildNo(buildNoStr);
			publishRecordDao.update(publishRecord);
		}

		scheduleTask.wakeup();//唤醒线程去处理任务，以加快处理速度
		return true;
	}

	/**
	 * 发布结果回调
	 * @param id
	 * @param result
	 * @return
	 */
	public void publishResultCallback(Long id, BuildResultEnum result){
		logger.info("接收到上线发布回调 publishId={} result={}", id, result);
		if(id == null){
			throw new BizException(BizException.BIZ_INVALID, "发布记录id不能为空！");
		}else if(result == null){
			throw new BizException(BizException.BIZ_INVALID, "构建结果不能为空！");
		}

		PublishRecord publishRecord = publishRecordDao.getById(id);
		if(publishRecord == null){
			throw new BizException(BizException.BIZ_INVALID, "发布记录不存在！");
		}else if(isFinalStatus(publishRecord.getStatus())){
			throw new BizException(BizException.BIZ_INVALID, "发布记录已为终态！");
		}

		Integer status = convertStatusFromBuildResult(result);
		if(status == null || ! isFinalStatus(status)){
			throw new BizException(BizException.BIZ_INVALID, "回调的构建结果只能是终态！");
		}

		publishRecord.setStatus(status);
		publishRecordDao.update(publishRecord);

		scheduleTask.wakeup();//唤醒线程去处理任务，以加快处理速度
	}

	/**
	 * 重新发布任务，当因为某些原因导致项目部署失败(如编译失败等)，可以使用此方法重新部署
	 * @param id
	 * @param relayApp
	 * @param remark
	 * @param notifyUrl
	 * @param modifier
	 * @return
	 */
	public boolean republish(long id, String relayApp, String remark, String notifyUrl, String modifier){
		PublishRecord publishRecord = publishRecordDao.getById(id);

		if(publishRecord.getStatus() != BuildStatusEnum.FAILURE.getValue()
				&& publishRecord.getStatus() != BuildStatusEnum.UNSTABLE.getValue()
				&& publishRecord.getStatus() != BuildStatusEnum.ABORT.getValue()){
			throw new BizException(BizException.BIZ_INVALID, "处于 '失败' | '不稳定' | '已取消' 状态的记录才允许重新发布！");
		}

		if(idcFlowBiz.isIdcHasFlow(publishRecord.getIdc())){
			throw new BizException(BizException.BIZ_INVALID, "要发布的机房有流量,不能上线到此机房！");
		}

		publishRecord.setQueueId(null);
		publishRecord.setBuildId(null);
		publishRecord.setStatus(BuildStatusEnum.PENDING.getValue());
		publishRecord.setRelayApp(relayApp == null ? "" : relayApp);
		publishRecord.setModifier(modifier);
		publishRecord.setPublishTimes(publishRecord.getPublishTimes() + 1);//发布次数加1
		publishRecord.setProcessTimes(0);//处理次数清零
		publishRecord.setRemark(remark != null ? StringUtil.subLeft(remark, 80) : publishRecord.getRemark());
		publishRecord.setNotifyUrl(notifyUrl == null ? "" : notifyUrl);
		publishRecordDao.update(publishRecord);

		scheduleTask.wakeup();//唤醒线程去处理任务
		return true;
	}

	/**
	 * 审核发布记录（只能审核为终态），如果因为某些原因导致程序无法从jenkins中同步构建结果时，可以通过该方法人工处理
	 * @param id
	 * @param status
	 * @return
	 */
	public boolean auditPublishRecord(long id, Integer status, String remark, String modifier){
		if(status != BuildStatusEnum.SUCCESS.getValue()
				&& status != BuildStatusEnum.FAILURE.getValue()
				&& status != BuildStatusEnum.ABORT.getValue()){
			throw new BizException(BizException.BIZ_INVALID, "只能审核为终态");
		}
		PublishRecord publishRecord = publishRecordDao.getById(id);
		if(publishRecord == null){
			throw new BizException(BizException.BIZ_INVALID, "记录不存在");
		}else if(publishRecord.getStatus() != BuildStatusEnum.TIMEOUT.getValue()){
			throw new BizException(BizException.BIZ_INVALID, "只能审核处于超时状态的记录");
		}
		publishRecord.setStatus(status);
		publishRecord.setModifier(modifier);
		publishRecord.setRemark(remark == null ? publishRecord.getRemark() : StringUtil.subLeft(remark, 80));
		publishRecordDao.update(publishRecord);
		return true;
	}

	/**
	 * 取消任务发布
	 * @param id
	 * @param modifier
	 * @return
	 */
	public boolean cancelPublish(Long id, String modifier){
		PublishRecord publishRecord = getById(id);
		if(publishRecord == null){
			throw new BizException(BizException.BIZ_INVALID, "项目发布记录不存在");
		}else if(isFinalStatus(publishRecord.getStatus())){
			throw new BizException(BizException.BIZ_INVALID, "当前记录已是终态，无法再取消！");
		}else if(BuildStatusEnum.TIMEOUT.getValue() == publishRecord.getStatus()){
			throw new BizException(BizException.BIZ_INVALID, "当前记录已超时，无法取消，请人工审核！");
		}

		//如果还在 '待处理' 状态，说明还未请求jenkins进行任务构建，则直接更改记录状态即可
		if(publishRecord.getStatus() == BuildStatusEnum.PENDING.getValue()){
			publishRecord.setStatus(BuildStatusEnum.ABORT.getValue());
			publishRecord.setRemark(StringUtil.subLeft(DateUtil.formatDateTime(new Date()) + " 人工取消任务("+modifier+")", 80));
			publishRecordDao.update(publishRecord);
			return true;
		}

		//如果已请求jenkins进行任务构建，则请求jenkins进行任务取消(包括取消排队和取消构建)
		boolean isSuccess = jenkinsBiz.cancelJob(publishRecord.getJobName(), publishRecord.getQueueId(), publishRecord.getBuildId());
		if(isSuccess){
			publishRecord.setStatus(BuildStatusEnum.ABORT.getValue());
			publishRecord.setRemark(StringUtil.subLeft(DateUtil.formatDateTime(new Date()) + " 人工取消任务("+modifier+")", 80));
			publishRecordDao.update(publishRecord);
			logger.info("任务取消成功 publishId={} buildMsg={} modifier={}", publishRecord.getId(), publishRecord.getBuildMsg(), modifier);
		}else{
			logger.info("任务取消失败 publishId={} buildMsg={} modifier={}", publishRecord.getId(), publishRecord.getBuildMsg(), modifier);
		}
		return isSuccess;
	}

	/**
	 * 同步机房部署
	 * @param toIdc
	 * @param publishIdList
	 * @param syncMsg
	 * @param operator
	 * @return
	 */
	public boolean syncIdcPublish(String toIdc, List<Long> publishIdList, String syncMsg, String notifyUrl, String operator) {
		if(StringUtil.isEmpty(toIdc)){
			throw new BizException(BizException.BIZ_INVALID, "同步机房的编号不能为空");
		}else if(publishIdList == null || publishIdList.size() <= 0){
			throw new BizException(BizException.BIZ_INVALID, "发布记录id不能为空");
		}if(StringUtil.isEmpty(syncMsg)){
			throw new BizException(BizException.BIZ_INVALID, "同步描述不能为空");
		}else if(idcFlowBiz.isFlowSwitching()){
			throw new BizException(BizException.BIZ_INVALID, "机房流量切换中,请等待切量完成后再试");
		}else if(idcFlowBiz.isIdcHasFlow(toIdc)){
			throw new BizException(BizException.BIZ_INVALID, "当前机房有流量,不能同步到此机房");
		}

		//对发布记录进行是否存在、发布状态、上线机房等校验
		String firstIdc = null;
		List<PublishRecord> publishRecords = publishRecordDao.listByIdList(publishIdList);
		if(publishRecords.isEmpty()){
			throw new BizException(BizException.BIZ_INVALID, "任务发布记录不存在");
		}else if(publishRecords.size() != publishIdList.size()){
			throw new BizException(BizException.BIZ_INVALID, "传入的publishId中有不存在发布记录的情况！");
		}else{
			for(PublishRecord task : publishRecords){
				if(task.getStatus() != BuildStatusEnum.SUCCESS.getValue()){
					throw new BizException(BizException.BIZ_INVALID, "批次号：" + task.getBuildNo() + " 没有发布成功，不能同步代码！");
				}else if(toIdc.equals(task.getIdc())){
					throw new BizException(BizException.BIZ_INVALID, "批次号：" + task.getBuildNo() + " 不能同步到上线时的机房！");
				}else if(StringUtil.isNotEmpty(firstIdc) && !firstIdc.equals(task.getIdc())){
					throw new BizException(BizException.BIZ_INVALID, "只能同步在同一个机房上线的发布记录！");
				}else if(StringUtil.isEmpty(firstIdc)){
					firstIdc = task.getIdc();
				}
			}
		}

		//每个项目只选择最大的那个构建编号
		Map<String, String> appNameMapVersion = new LinkedHashMap<>();
		for (PublishRecord task : publishRecords) {
			String[] appArr = task.getApps().split(",");
			for(String app : appArr){
				String buildNoStr = task.getBuildNo();
				if(! appNameMapVersion.containsKey(app)){
					appNameMapVersion.put(app, buildNoStr);
					continue;
				}else if("latest".equals(buildNoStr)){
					appNameMapVersion.replace(app, buildNoStr);
					continue;
				}else if("latest".equals(appNameMapVersion.get(app))){
					continue;
				}

				Long buildNo = Long.valueOf(buildNoStr);
				Long buildNoInMap = Long.valueOf(appNameMapVersion.get(app));
				if(buildNo > buildNoInMap){
					appNameMapVersion.replace(app, buildNo.toString());
				}
			}
		}

		List<String> packageNameList = new ArrayList<>();
		for (Map.Entry<String, String> entry : appNameMapVersion.entrySet()) {
			String packageName = entry.getKey() + ":" + entry.getValue();
			packageNameList.add(packageName);
		}

		logger.info("项目同步机房 toIdc={} syncMsg={} operator={}", toIdc, syncMsg, operator);
		return jenkinsBiz.publish(toIdc, syncMsg, packageNameList, notifyUrl);
	}

	/**
	 * 根据id查询项目发布记录
	 * @param id
	 * @return
	 */
	public PublishRecord getById(long id){
		PublishRecord record = publishRecordDao.getById(id);
		return record;
	}

	public PublishRecordDto getDtoById(long id){
		PublishRecord record = publishRecordDao.getById(id);
		return BeanUtil.newAndCopy(record, PublishRecordDto.class);
	}

	/**
	 * 分页查询项目发布记录
	 * @param paramMap
	 * @param pageQuery
	 * @return
	 */
	public PageResult<List<PublishRecordDto>> listPage(Map<String, Object> paramMap, PageQuery pageQuery){
		PageResult<List<PublishRecord>> result = publishRecordDao.listPage(paramMap, pageQuery);
		if(result.getData() != null){
			for(PublishRecord publishRecord : result.getData()){
				publishRecord.setApps(publishRecord.getApps().replace(",", "\n"));
				publishRecord.setNotifyEmail(publishRecord.getNotifyEmail().replace(",", "\n"));
			}
		}
		return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), PublishRecordDto.class), result);
	}

	/**
	 * 获取发布上线相关信息，如：机房列表、邮件通知人等
	 * @return
	 */
	public PublishInfoVo getPublishInfo(){
		List<IdcVo> idcVos = idcFlowBiz.listAllIdc();
		PublishInfoVo infoVo = new PublishInfoVo();
		infoVo.setIdcList(idcVos);
		infoVo.setEmailReceiver(devopsProperties.getPublish().getEmailReceiver());
		return infoVo;
	}

	/**
	 * 获取一个还未结束的发布记录
	 * @return
	 */
	public PublishRecordDto getNotFinishPublishRecord(){
		PublishRecord record = publishRecordDao.getNotFinishPublishRecord();
		return BeanUtil.newAndCopy(record, PublishRecordDto.class);
	}

	/**
	 * 判断是否已经到达终态
	 * @param status
	 * @return
	 */
	private boolean isFinalStatus(Integer status){
		return status == BuildStatusEnum.SUCCESS.getValue()
						|| status == BuildStatusEnum.FAILURE.getValue()
						|| status == BuildStatusEnum.UNSTABLE.getValue()
						|| status == BuildStatusEnum.ABORT.getValue();
	}

	private Integer convertStatusFromBuildResult(BuildResultEnum buildResult) {
		switch (buildResult) {
			case PROCESSING:
				return BuildStatusEnum.PROCESSING.getValue();
			case SUCCESS:
				return BuildStatusEnum.SUCCESS.getValue();
			case FAILURE:
				return BuildStatusEnum.FAILURE.getValue();
			case UNSTABLE:
				return BuildStatusEnum.UNSTABLE.getValue();
			case ABORTED:
				return BuildStatusEnum.ABORT.getValue();
			default:
				return null;
		}
	}

	/**
	 * 项目发布的定时任务，设计成了串行处理，只有当前任务到达终态，才会继续处理下一个任务
	 */
	private class PublishScheduleTask extends Thread {
		private long scanInterval;//任务扫描间隔（毫秒）
		private int publishTimeout;//发布超时时间(秒)
		private int jobQuietSec;//job的静默时间(秒)

		private PublishScheduleTask(long scanInterval, int publishTimeout, int jobQuietSec){
			this.scanInterval = scanInterval;
			this.publishTimeout = publishTimeout;
			this.jobQuietSec = jobQuietSec;
		}

		@Override
		public void run() {
			while(true){
				Object lock = null;
				try{
					lock = redisLock.tryLock(PROJECT_PUBLISH_LOCK, 3000, -1);
					if(lock == null){
						logger.info("获取锁失败，可能任务正在其他实例上执行中");
						waitAWhile(scanInterval);
						continue;
					}

					boolean isGoOn = true;//标识是否继续处理下一条记录
					while(isGoOn){
						boolean isFinish = executeNextRecord();
						isGoOn = isFinish ? true : false;//如果当前记录已完结，则继续处理下一条记录，否则就应该休眠一段时间之后重新扫描记录
					}
				} catch (InterruptedException e){
					logger.error("项目发布线程被中断，停止执行！");
					break;
				} catch(Throwable e) {
					logger.error("在扫描并处理发布记录时出现异常", e);
				} finally {
					if(lock != null) {
						try{
							redisLock.unlock(lock);
						}catch(Exception e){
							logger.error("释放分布式锁出现异常", e);
						}
					}
				}

				try {
					waitAWhile(scanInterval);
				} catch (InterruptedException e) {
					logger.error("项目发布线程被中断，停止执行！");
					break;
				}
			}
		}

		/**
		 * 处理下一条发布记录
		 * @return	true|false	如果当前任务到达终态或者超时则返回true，否则返回false
		 */
		private boolean executeNextRecord() throws InterruptedException {
			PublishRecord publishRecord = publishRecordDao.getNotFinishPublishRecord();
			if(publishRecord == null){
				return false;
			}

			try {
				if(DateUtil.isOverhead(new Date(), publishRecord.getCreateTime(), publishTimeout)){ //已超时，直接置为超时状态，后续人工介入审核
					publishRecord.setStatus(BuildStatusEnum.TIMEOUT.getValue());
					publishRecord.setProcessTimes(publishRecord.getProcessTimes() + 1);
					publishRecordDao.update(publishRecord);
					return true;
				}

				if(idcFlowBiz.isFlowSwitching()){
					logger.info("当前正在进行流量切换，将不进行项目发布 publishId={}", publishRecord.getId());
					return false;
				}else if(idcFlowBiz.isIdcHasFlow(publishRecord.getIdc())){
					logger.error("要上线的机房有流量,不可上线到此机房! publishId={}", publishRecord.getId());
					publishRecord.setRemark("要上线的机房有流量,不可上线到此机房");
					publishRecord.setProcessTimes(publishRecord.getProcessTimes() + 1);
					publishRecordDao.update(publishRecord);
					return false;
				}

				boolean isSubmit = executePending(publishRecord);
				if(isSubmit){ //一般来说提交之后很快就会进入处理阶段，所以可以等待几秒，然后就可立马进入处理阶段了，加快处理效率
					long waitMillSec = (jobQuietSec * 1000) * 2L;
					if(waitMillSec > scanInterval){
						waitMillSec = scanInterval;
					}
					waitAWhile(waitMillSec);
					publishRecord = getById(publishRecord.getId());
				}

				executeProcessing(publishRecord);

				return isFinalStatus(publishRecord.getStatus());
			} finally {
				sendNotifyIfNeed(publishRecord);
			}
		}

		/**
		 * 处理处于 '待处理' 状态的记录，即请求jenkins进行项目构建和发布
		 * @param publishRecord
		 * @return	如果已提交到jenkins则返回true，否则返回false
		 */
		private boolean executePending(PublishRecord publishRecord) {
			if(publishRecord.getStatus() != BuildStatusEnum.PENDING.getValue()){
				return false;
			}

			String jobName = publishRecord.getJobName();
			String buildMsg = publishRecord.getBuildMsg();
			String apps = publishRecord.getApps();
			String idc = publishRecord.getIdc();
			String version = String.valueOf(publishRecord.getBuildNo());
			String relayApp = publishRecord.getRelayApp();
			String notifyEmail = publishRecord.getNotifyEmail();
			String notifyUrl = publishRecord.getNotifyUrl();
			if(StringUtil.isNotEmpty(notifyUrl)){
				if(notifyUrl.lastIndexOf("?") == -1){
					notifyUrl += "?buildSeq=" + publishRecord.getId();
				}else{
					notifyUrl += "&buildSeq=" + publishRecord.getId();
				}
			}

			Integer queueId = jenkinsBiz.buildAndPublish(jobName, buildMsg, apps, idc, version, relayApp, notifyEmail, notifyUrl);
			if(queueId != null){
				logger.info("已向jenkins发送任务部署请求 jobName={} queueId={} buildMsg={}", publishRecord.getJobName(), queueId, publishRecord.getBuildMsg());
				publishRecord.setQueueId(queueId);
				publishRecord.setStatus(BuildStatusEnum.QUEUEING.getValue());
				publishRecord.setRelayApp("");//清空,在每次重新发布时根据需要来填写
			}
			publishRecord.setProcessTimes(publishRecord.getProcessTimes() + 1);
			publishRecordDao.update(publishRecord);
			return queueId != null && queueId > 0;
		}

		/**
		 * 处理处于 '排队中'、'处理中' 状态的记录，即：向jenkins查询处理结果
		 * @param publishRecord
		 */
		private void executeProcessing(PublishRecord publishRecord){
			if(publishRecord.getStatus() != BuildStatusEnum.QUEUEING.getValue()
					&& publishRecord.getStatus() != BuildStatusEnum.PROCESSING.getValue()){
				return;
			}

			Integer buildId = publishRecord.getBuildId();
			Integer status = publishRecord.getStatus();

			//通过队列Id来查询构建Id，因为构建任务可能正在jenkins的待构建队列中或者正在构建中
			if(buildId == null){
				Integer resultId = jenkinsBiz.getBuildIdByQueueId(publishRecord.getQueueId(), publishRecord.getJobName());
				if(resultId == -1){
					status = BuildStatusEnum.ABORT.getValue();
				}else if(resultId == 0){
					status = BuildStatusEnum.QUEUEING.getValue();
				}else if(resultId > 0){
					buildId = resultId;
				}
			}

			//通过构建Id查询构建结果
			if(buildId != null){
				BuildResultEnum buildResult = jenkinsBiz.queryBuildResult(publishRecord.getJobName(), buildId);
				status = convertStatusFromBuildResult(buildResult);
			}

			publishRecord.setBuildId(buildId);
			publishRecord.setStatus(status);
			publishRecord.setProcessTimes(publishRecord.getProcessTimes() + 1);
			publishRecordDao.update(publishRecord);
		}

		/**
		 * 休眠并等待
		 * @param millSec
		 */
		private synchronized void waitAWhile(long millSec) throws InterruptedException {
			long rand = RandomUtil.getInt(100, 300);
			wait(millSec + rand);//增加一个小的随机数，避免一直被同一个实例抢占到
		}

		 private synchronized void wakeup(){
			notify();
		 }

		/**
		 * 如果有需要则发送通知（邮件等通知）
		 * @param publishRecord
		 */
		private void sendNotifyIfNeed(PublishRecord publishRecord){
			//还未达到终态的记录不发送通知
			if(! isFinalStatus(publishRecord.getStatus())){
				return;
			}

			//TODO 发送通知
			CompletableFuture.runAsync(() -> {
				try {

				} catch(Exception e) {
					logger.error("发布记录发送通知时出现异常 publishId={}", publishRecord.getId(), e);
				}
			});
		}
	}
}
