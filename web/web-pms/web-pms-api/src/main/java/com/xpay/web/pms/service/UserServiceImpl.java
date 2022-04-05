package com.xpay.web.pms.service;

import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.enums.user.UserStatusEnum;
import com.xpay.common.statics.enums.user.UserTypeEnum;
import com.xpay.common.utils.BeanUtil;
import com.xpay.facade.user.dto.PmsUserDto;
import com.xpay.facade.user.service.PmsUserFacade;
import com.xpay.starter.plugin.client.RedisClient;
import com.xpay.web.api.common.ddo.vo.UserVo;
import com.xpay.web.api.common.ddo.dto.LoginDto;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.CryptService;
import com.xpay.web.api.common.service.UserService;
import com.xpay.web.pms.config.AppConstant;
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
    RedisClient redisClient;
    @Autowired
    CryptService cryptService;
    @DubboReference
    PmsUserFacade pmsUserFacade;

    @Override
    public UserModel getUserModelByLoginName(String loginName, String mchNo) {
        PmsUserDto userDto = pmsUserFacade.getUserByLoginName(loginName);
        return BeanUtil.newAndCopy(userDto, UserModel.class);
    }

    @Cacheable(value = "redisCache", key = "targetClass + '.' + methodName + '.' + #loginName")
    @Override
    public UserModel getUserModelByLoginNameCache(String loginName, String mchNo){
        PmsUserDto userDto = pmsUserFacade.getUserByLoginName(loginName);
        return BeanUtil.newAndCopy(userDto, UserModel.class);
    }

    @Override
    public UserVo getAdminUser(String mchNo) {
        throw new BizException(BizException.BIZ_INVALID, "不支持此功能");
    }

    @Override
    public UserVo getUserById(Long userId, String mchNo){
        if(userId == null){
            return null;
        }
        PmsUserDto userDto = pmsUserFacade.getUserById(userId);
        return BeanUtil.newAndCopy(userDto, UserVo.class);
    }

    @Override
    public boolean addUser(UserVo userDto, String creator, String mchNo){
        UserVo.validateUser(userDto);

        PmsUserDto newUser = BeanUtil.newAndCopy(userDto, PmsUserDto.class);
        newUser.setCreateTime(new Date());
        newUser.setStatus(UserStatusEnum.UNAUDITED.getValue());
        newUser.setType(UserTypeEnum.USER.getValue());
        newUser.setCreator(creator);
        newUser.setModifier(creator);

        pmsUserFacade.createUser(newUser);
        return true;
    }

    @Override
    public boolean updateUserStatus(Long userId, Integer newStatus, String modifier, String mchNo) {
        PmsUserDto userDto = pmsUserFacade.getUserById(userId);
        userDto.setModifier(modifier);
        userDto.setStatus(newStatus);
        pmsUserFacade.updateUser(userDto);
        return true;
    }

    @Override
    public boolean updatePassword(Long userId, String newPwd, String modifier, String mchNo){
        pmsUserFacade.updateUserPwd(userId, newPwd);
        return true;
    }

    @Override
    public boolean validLoginPwd(String loginName, String password){
        return pmsUserFacade.validLoginPwd(loginName, password);
    }

    @Override
    public boolean updateUser(UserVo userNew, String modifier, String mchNo){
        userNew.setModifier(modifier);
        UserVo.validateUser(userNew);

        PmsUserDto pmsUser = BeanUtil.newAndCopy(userNew, PmsUserDto.class);
        pmsUserFacade.updateUser(pmsUser);
        return true;
    }

    @Override
    public boolean assignRoles(Long userId, List<Long> roleIds, String modifier, String mchNo){
        return pmsUserFacade.assignRoles(userId, roleIds, modifier);
    }

    @Override
    public boolean deleteUser(Long userId, String modifier, String mchNo) {
        pmsUserFacade.deleteUserById(userId);
        return true;
    }

    @Override
    public PageResult<List<UserVo>> listUserPage(Map<String, Object> paramMap, PageQuery pageQuery, String mchNo){
        PageResult<List<PmsUserDto>> pageResult = pmsUserFacade.listUserPage(paramMap, pageQuery);
        if(pageResult.getData() == null || pageResult.getData().isEmpty()) {
            return PageResult.newInstance(new ArrayList<>(), pageResult);
        }

        List<UserVo> userVoList = new ArrayList<>();
        for(PmsUserDto userDto : pageResult.getData()){
            UserVo vo = BeanUtil.newAndCopy(userDto, UserVo.class);
            vo.setLoginPwd("******");
            userVoList.add(vo);
        }
        return PageResult.newInstance(userVoList, pageResult);
    }

    @Override
    public void afterLoginSuccess(UserModel userModel) {
        String key = getPwdErrorCountStoreKey(userModel.getLoginName());
        redisClient.del(key);
    }

    @Override
    public void afterLoginPwdError(LoginDto loginDto) {
        String key = getPwdErrorCountStoreKey(loginDto.getLoginName());
        String countStr = redisClient.get(key);
        if(countStr == null){
            redisClient.set(key, "0", 24 * 60 * 60);//一段时间内不允许重复输入错误次数过多
            return;
        }

        logger.warn("帐号：【{}】 密码错误次数：【{}】", loginDto.getLoginName(), countStr);
        int pwdErrCount = Integer.valueOf(countStr).intValue();
        if(pwdErrCount < AppConstant.WEB_PWD_INPUT_ERROR_LIMIT){
            redisClient.incr(key);
        }else{
            //超过最大错误次数，把账户状态置为'冻结'
            PmsUserDto userDto = pmsUserFacade.getUserByLoginName(loginDto.getLoginName());
            userDto.setStatus(UserStatusEnum.INACTIVE.getValue());
            pmsUserFacade.updateUser(userDto);

            redisClient.del(key);//账户已冻结，删除计数器
        }
    }

    @Override
    public void afterLogout(UserModel userModel) {

    }

    @Override
    public void afterPwdReset(String loginName){
        String key = getPwdErrorCountStoreKey(loginName);
        redisClient.del(key);
    }

    private String getPwdErrorCountStoreKey(String loginName){
        return "PWD_ERROR_COUNT:" + loginName;
    }
}
