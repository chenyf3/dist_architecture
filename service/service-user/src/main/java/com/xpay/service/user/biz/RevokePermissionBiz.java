package com.xpay.service.user.biz;

import com.xpay.common.statics.constants.common.PublicStatus;
import com.xpay.common.statics.enums.user.OperateLogTypeEnum;
import com.xpay.common.statics.enums.user.RevokeAuthStatusEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.user.dto.PortalAuthDto;
import com.xpay.facade.user.dto.PortalOperateLogDto;
import com.xpay.facade.user.dto.PortalRevokeAuthDto;
import com.xpay.service.user.dao.*;
import com.xpay.service.user.entity.PortalRevokeAuth;
import com.xpay.starter.plugin.plugins.DistributedLock;
import org.redisson.api.RLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 商户权限回收
 */
@Component
public class RevokePermissionBiz {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    PortalRoleUserDao portalRoleUserDao;
    @Autowired
    PortalUserDao portalUserDao;
    @Autowired
    PortalRoleAuthDao portalRoleAuthDao;
    @Autowired
    PortalRoleDao portalRoleDao;
    @Autowired
    PortalRevokeAuthDao portalRevokeAuthDao;
    @Autowired
    PortalOperateLogBiz portalOperateLogBiz;
    @Autowired
    PortalPermissionBiz portalPermissionBiz;
    @Autowired
    DistributedLock<RLock> redisLock;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * 收回商户的管理员角色，即取消商户管理员操作员和管理员角色的关联
     */
    @Transactional(rollbackFor = Exception.class)
    public void revokeMerchantPermission(Integer revokeType, String objectKey, List<String> mchNos, String modifier, String remark){
        if(revokeType == null){
            throw new BizException(BizException.BIZ_INVALID, "revokeType不能为空");
        }else if(StringUtil.isEmpty(objectKey)){
            throw new BizException(BizException.BIZ_INVALID, "objectKey不能为空");
        }else if(mchNos == null || mchNos.isEmpty()){
            logger.info("本次操作没有商户需要回收功能，本次请求将忽略 revokeType={} objectKey={} modifier={} remark={}", revokeType, objectKey, modifier, remark);
            return;
        }

        PortalRevokeAuth revokeAuth = buildRevokeAuth(revokeType, objectKey, mchNos, modifier, remark);
        portalRevokeAuthDao.insert(revokeAuth);
        this.addTask(revokeAuth.getId());
    }

    public PageResult<List<PortalRevokeAuthDto>> listRevokeAuthPage(Map<String, Object> paramMap, PageQuery pageQuery){
        PageResult<List<PortalRevokeAuth>> result = portalRevokeAuthDao.listPage(paramMap, pageQuery);
        return PageResult.newInstance(BeanUtil.newAndCopy(result.getData(), PortalRevokeAuthDto.class), result);
    }

    public PortalRevokeAuth getOnePendingRecord(){
        return portalRevokeAuthDao.getOnePendingRecord();
    }

    /**
     * 执行功能权限回收
     * @param id
     * @return
     */
    public boolean doAuthRevoke(Long id){
        if(id == null){
            throw new BizException(BizException.BIZ_INVALID, "请传入记录id");
        }
        PortalRevokeAuth revokeAuth = portalRevokeAuthDao.getById(id);
        if(revokeAuth == null){
            throw new BizException(BizException.BIZ_INVALID, "当前记录不存在！");
        }else if(revokeAuth.getStatus() == RevokeAuthStatusEnum.FINISH.getValue()){
            throw new BizException(BizException.BIZ_INVALID, "当前记录已完成！");
        }

        addTask(revokeAuth.getId());
        return true;
    }

    private PortalRevokeAuth buildRevokeAuth(Integer revokeType, String objectKey, List<String> mchNos, String modifier, String remark){
        PortalRevokeAuth revokeAuth = new PortalRevokeAuth();
        revokeAuth.setCreateTime(new Date());
        revokeAuth.setVersion(0);
        revokeAuth.setCreator(modifier);
        revokeAuth.setRevokeType(revokeType);
        revokeAuth.setStatus(RevokeAuthStatusEnum.PENDING.getValue());
        revokeAuth.setMchNos(JsonUtil.toJson(mchNos));
        revokeAuth.setCurrMchNo("");
        revokeAuth.setObjectKey(objectKey);
        revokeAuth.setRemark(remark);
        return revokeAuth;
    }

    private void addTask(Long recordId){
        executor.submit(new RevokeTask(recordId));
    }

    class RevokeTask implements Runnable {
        private Long recordId;

        public RevokeTask(Long recordId){
            this.recordId = recordId;
        }

        @Override
        public void run() {
            //加分布式锁，确保全局串行化处理
            String lockName = "portalRevokePermission";
            RLock lock = redisLock.tryLock(lockName, 2000, -1);
            if (lock == null) {
                logger.error("recordId={} lockName={} 获取锁失败，不执行本次任务", recordId, lockName);
                return;
            }

            try {
                while (true) {
                    doRevoke();

                    //当前的记录处理完毕了，则继续处理下一条记录
                    PortalRevokeAuth revokeAuth = getOnePendingRecord();
                    if (revokeAuth == null || recordId >= revokeAuth.getId()) { //避免进入死循环
                        break;
                    }
                    this.recordId = revokeAuth.getId();
                }
            } catch (Throwable e) {
                logger.error("recordId={} 回收权限时出现异常", recordId, e);
            } finally {
                redisLock.unlock(lock);
            }
        }

        private void doRevoke() throws Exception {
            PortalRevokeAuth revokeAuth = portalRevokeAuthDao.getById(recordId);
            if(revokeAuth == null){
                Thread.sleep(3000);//休眠3秒，等待数据库事务提交
                revokeAuth = portalRevokeAuthDao.getById(recordId);
                if(revokeAuth == null){
                    logger.error("recordId={} 在休眠3秒后还是没有获取到数据库记录", recordId);
                    return;
                }
            }

            if(revokeAuth.getStatus() == RevokeAuthStatusEnum.FINISH.getValue()){
                logger.info("recordId={} 已完成，将不再继续处理", recordId);
                return;
            }else if(revokeAuth.getStatus() == RevokeAuthStatusEnum.PENDING.getValue()){
                revokeAuth.setStatus(RevokeAuthStatusEnum.PROCESSION.getValue());
                portalRevokeAuthDao.update(revokeAuth);
                revokeAuth.setVersion(revokeAuth.getVersion() + 1);
            }

            List<String> mchNos = JsonUtil.toList(revokeAuth.getMchNos(), String.class);
            int index = 0;
            if(StringUtil.isNotEmpty(revokeAuth.getCurrMchNo())){
                index = mchNos.indexOf(revokeAuth.getCurrMchNo());
            }

            for(int i=index; i<mchNos.size(); i++){
                String mchNo = mchNos.get(i);

                try {
                    //1.找出当前商户现在拥有的所有权限
                    List<Long> allAuthIds = new ArrayList<>();
                    List<PortalAuthDto> allPermissions = portalPermissionBiz.listAllAuthByMchNo(mchNo);
                    allPermissions.forEach(f -> allAuthIds.add(f.getId()));

                    //2.找出当前商户已关联的所有权限id
                    List<Long> needDelAuthIds = new ArrayList<>();
                    List<Long> authIds = portalRoleAuthDao.listAuthIdByMchNo(mchNo);

                    //3.找出已关联但是不在所有功能集合里面的功能id
                    for(Long authId : authIds){
                        if(! allAuthIds.contains(authId)){
                            needDelAuthIds.add(authId);
                        }
                    }

                    //4.删除不应该跟当前商户的角色进行关联的记录
                    if(! needDelAuthIds.isEmpty()){
                        portalRoleAuthDao.deleteByMchNoAndAuthIds(mchNo, needDelAuthIds);

                        PortalOperateLogDto operateLog = new PortalOperateLogDto();
                        operateLog.setMchNo(mchNo);
                        operateLog.setStatus(PublicStatus.ACTIVE);
                        operateLog.setIp("");
                        operateLog.setContent(revokeAuth.getRemark() + ":" + JsonUtil.toJson(needDelAuthIds));
                        operateLog.setCreateTime(new Date());
                        operateLog.setOperateType(OperateLogTypeEnum.DELETE.getValue());
                        operateLog.setLoginName("system");
                        portalOperateLogBiz.createOperateLog(operateLog);
                    }

                    revokeAuth.setCurrMchNo(mchNo);
                    portalRevokeAuthDao.update(revokeAuth);
                    revokeAuth.setVersion(revokeAuth.getVersion() + 1);
                } catch(Exception e) {
                    logger.error("recordId={} mchNo={} 回收权限时出现异常 ", recordId, mchNo, e);
                }
            }

            revokeAuth.setStatus(RevokeAuthStatusEnum.FINISH.getValue());
            portalRevokeAuthDao.update(revokeAuth);
        }
    }
}
