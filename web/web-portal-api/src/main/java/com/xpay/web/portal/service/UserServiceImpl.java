package com.xpay.web.portal.service;

import com.xpay.common.statics.enums.user.UserStatusEnum;
import com.xpay.common.statics.enums.user.UserTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.facade.user.dto.PortalUserDto;
import com.xpay.facade.user.service.PortalUserFacade;
import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.web.api.common.config.WebApiProperties;
import com.xpay.web.api.common.ddo.vo.UserVo;
import com.xpay.web.api.common.ddo.dto.LoginDto;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.UserService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    WebApiProperties properties;
    @Autowired
    RedisClient redisClient;

    @DubboReference
    PortalUserFacade portalUserFacade;

    @Override
    public PageResult<List<UserVo>> listUserPage(Map<String, Object> paramMap, PageQuery pageQuery, String mchNo){
        this.validateMchNo(mchNo);

        if(paramMap == null){
            paramMap = new HashMap<>();
        }
        paramMap.put("mchNo", mchNo);
        PageResult<List<PortalUserDto>> pageResult = portalUserFacade.listUserPage(paramMap, pageQuery);

        if(pageResult == null || pageResult.getData().isEmpty()) {
            return PageResult.newInstance(new ArrayList<>(), pageResult);
        }

        List<UserVo> userList = new ArrayList<>();
        for(PortalUserDto userDto : pageResult.getData()){
            UserVo vo = BeanUtil.newAndCopy(userDto, UserVo.class);
            userList.add(vo);
        }
        return PageResult.newInstance(userList, pageResult);
    }

    @Override
    public boolean addUser(UserVo userVo, String creator, String mchNo){
        this.validateMchNo(mchNo);

        UserVo.validateUser(userVo);

        PortalUserDto portalUser = BeanUtil.newAndCopy(userVo, PortalUserDto.class);
        portalUser.setCreateTime(new Date());
        portalUser.setType(UserTypeEnum.USER.getValue());
        portalUser.setMchNo(mchNo);
        portalUser.setCreator(creator);
        portalUser.setModifier(creator);
        portalUserFacade.createUser(portalUser);
        return true;
    }

    /**
     * 获取UserDto
     * @param userId
     * @param mchNo
     * @return
     */
    @Override
    public UserVo getUserById(Long userId, String mchNo){
        validateMchNo(mchNo);
        PortalUserDto userDto = portalUserFacade.getUserById(userId);
        if(userDto == null){
            throw new BizException("当前用户不存在");
        }else if(! mchNo.equals(userDto.getMchNo())){
            throw new BizException("当前用户不属于当前商户！");
        }
        return BeanUtil.newAndCopy(userDto, UserVo.class);
    }

    /**
     * 获取UserModel，实现类中可以考虑使用缓存
     * @param loginName
     * @param mchNo
     * @return
     */
    @Override
    public UserModel getUserModelByLoginName(String loginName, String mchNo){
        PortalUserDto userDto = portalUserFacade.getUserByLoginName(loginName);
        if(StringUtil.isNotEmpty(mchNo) && (userDto == null || ! mchNo.equals(userDto.getMchNo()))){
            throw new BizException("非法操作，当前用户不属于当前商户！");
        }
        return BeanUtil.newAndCopy(userDto, UserModel.class);
    }

    @Cacheable(value = "redisCache", key = "targetClass + '.' + methodName + '.' + #loginName")
    @Override
    public UserModel getUserModelByLoginNameCache(String loginName, String mchNo){
        PortalUserDto userDto = portalUserFacade.getUserByLoginName(loginName);
        return BeanUtil.newAndCopy(userDto, UserModel.class);
    }

    @Override
    public UserVo getAdminUser(String mchNo) {
        this.validateMchNo(mchNo);

        PortalUserDto userDto = portalUserFacade.getAdminUser(mchNo);
        if(userDto == null){
            throw new BizException("当前商户还不存在管理员用户");
        }else if(! mchNo.equals(userDto.getMchNo())){
            throw new BizException("当前用户不属于当前商户！");
        }
        return BeanUtil.newAndCopy(userDto, UserVo.class);
    }

    @Override
    public boolean updateUser(UserVo user, String modifier, String mchNo) {
        this.validateMchNo(mchNo);

        UserVo userVo = getUserById(user.getId(), mchNo);
        userVo.setRealName(user.getRealName());
        userVo.setEmail(user.getEmail());
        userVo.setRemark(user.getRemark());
        userVo.setModifier(modifier);// 修改者

        UserVo.validateUser(userVo);

        PortalUserDto portalUser = BeanUtil.newAndCopy(userVo, PortalUserDto.class);
        portalUserFacade.updateUser(portalUser);
        return true;
    }

    @Override
    public boolean assignRoles(Long userId, List<Long> roleIds, String modifier, String mchNo) {
        this.validateMchNo(mchNo);
        PortalUserDto portalUser = portalUserFacade.getUserById(userId);
        if(portalUser == null || ! mchNo.equals(portalUser.getMchNo())){
            throw new BizException("当前用户不属于当前商户！");
        }else if(portalUser.getType() == UserTypeEnum.ADMIN.getValue()){
            throw new BizException("当前用户不允许分配角色！");
        }
        portalUserFacade.assignRoles(userId, roleIds);
        return true;
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer newStatus, String modifier, String mchNo){
        this.validateMchNo(mchNo);
        PortalUserDto userDto = portalUserFacade.getUserById(userId);
        if(userDto == null || ! mchNo.equals(userDto.getMchNo())){
            throw new BizException("当前用户不属于当前商户！");
        }
        userDto.setStatus(newStatus);
        userDto.setModifier(modifier);
        portalUserFacade.updateUser(userDto);
        return true;
    }

    @Override
    public boolean updatePassword(Long userId, String newPwd, String modifier, String mchNo){
        PortalUserDto userDto = portalUserFacade.getUserById(userId);
        if(userDto == null){
            throw new BizException("非法操作，用户信息错误！");
        }else if(! mchNo.equals(userDto.getMchNo())){
            throw new BizException("非法操作，商户信息匹配错误！");
        }

        portalUserFacade.updateUserPwd(userId, newPwd, modifier);
        return true;
    }

    @Override
    public boolean validLoginPwd(String loginName, String password){
        return portalUserFacade.validLoginPwd(loginName, password);
    }

    @Override
    public boolean deleteUser(Long userId, String modifier, String mchNo){
        this.validateMchNo(mchNo);
        PortalUserDto userDto = portalUserFacade.getUserById(userId);
        if(userDto == null || ! mchNo.equals(userDto.getMchNo())){
            throw new BizException("非法操作，当前用户不属于当前商户！");
        }
        portalUserFacade.deleteUserById(userId);
        return true;
    }

    @Override
    public void afterLoginSuccess(UserModel userModel){
        String key = getPwdErrorCountStoreKey(userModel.getLoginName());
        redisClient.del(key);
    }

    @Override
    public void afterLoginPwdError(LoginDto loginDto){
        String key = getPwdErrorCountStoreKey(loginDto.getLoginName());
        String countStr = redisClient.get(key);
        if(countStr == null){
            redisClient.set(key, "1", 1 * 60 * 60);//一段时间内不允许重复输入错误次数过多
            return;
        }

        int maxTimes = 6;
        logger.warn("帐号：【{}】 密码错误次数：【{}】", loginDto.getLoginName(), countStr);
        int pwdErrCount = Integer.valueOf(countStr).intValue();
        if(pwdErrCount < maxTimes){
            redisClient.incr(key);
            if(pwdErrCount++ < maxTimes){
                return;
            }
        }

        //超过最大错误次数，把账户状态置为'冻结'
        logger.warn("帐号：{} 密码错误次数过多，账号被冻结！", loginDto.getLoginName(), countStr);
        PortalUserDto userDto = portalUserFacade.getUserByLoginName(loginDto.getLoginName());
        if(userDto.getStatus() == UserStatusEnum.ACTIVE.getValue()){
            userDto.setStatus(UserStatusEnum.INACTIVE.getValue());
            portalUserFacade.updateUser(userDto);
        }

        redisClient.del(key);//账户已冻结，删除计数器
    }

    @Override
    public  void afterLogout(UserModel userModel){

    }

    @Override
    public void afterPwdReset(String loginName){
        String key = getPwdErrorCountStoreKey(loginName);
        redisClient.del(key);
    }

    private void validateMchNo(String mchNo){
        if(StringUtil.isEmpty(mchNo)){
            throw new BizException("mchNo不能为空");
        }
    }

    private String getPwdErrorCountStoreKey(String loginName){
        return "PWD_ERROR_COUNT:" + loginName;
    }
}
