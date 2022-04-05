package com.xpay.web.api.common.controller;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.constants.message.EmailSend;
import com.xpay.common.statics.enums.user.UserStatusEnum;
import com.xpay.common.statics.enums.user.UserTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.*;
import com.xpay.web.api.common.config.WebApiProperties;
import com.xpay.web.api.common.ddo.dto.*;
import com.xpay.web.api.common.ddo.vo.*;
import com.xpay.web.api.common.enums.SmsType;
import com.xpay.web.api.common.manager.CodeManager;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.manager.FuncManager;
import com.xpay.web.api.common.manager.TokenManager;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.util.VerifyCodeUtil;
import com.xpay.web.api.common.util.WebUtil;
import com.xpay.web.api.common.service.*;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Description: 公用的用户管理控制器
 */
@RestController
@RequestMapping("user")
public class UserController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private TokenManager tokenManager;
    @Autowired
    private FuncManager funcManager;
    @Autowired
    private CodeManager codeManager;
    @Autowired
    CryptService cryptService;
    @Autowired
    MessageService messageService;
    @Autowired
    UserService userService;
    @Autowired
    AuthService functionService;
    @Autowired
    OperateLogService operateLogService;
    @Autowired
    RoleService roleService;
    @Autowired
    DictionaryService dictionaryService;
    @Autowired
    WebApiProperties properties;

    /**
     * 发送找回登录密码的验证码
     * @param type
     * @param loginName
     * @param request
     * @return
     */
    @GetMapping("forgetLoginPwdCode")
    public RestResult<String> forgetLoginPwdCode(@RequestParam Integer type, @RequestParam String loginName, HttpServletRequest request){
        int phoneType = 1;
        int mailType = 2;
        if(type == null || StringUtil.isEmpty(loginName)){
            return RestResult.error("参数错误");
        }else if(type != phoneType && type != mailType){
            return RestResult.error("对不起，未支持的密码找回方式！");
        }

        String clientIp = WebUtil.getIpAddr(request);
        String limitKey = getForgetPwdLimitKey(clientIp);
        if(codeManager.getLimitVal(limitKey) != null){
            return RestResult.error("对不起，请勿频繁获取验证码");
        }

        String mailOrPhone = null;
        UserModel userModel = userService.getUserModelByLoginName(loginName, null);
        if(userModel == null){
            return RestResult.error("对不起，用户信息错误！");
        }else if(type == phoneType){
            mailOrPhone = userModel.getMobileNo();
        }else if(type == mailType){
            mailOrPhone = userModel.getEmail();
        }

        if(StringUtil.isEmpty(mailOrPhone)){
            return RestResult.error("对不起，用户信息不支持！");
        }

        int expireMinute = 5;
        String code = RandomUtil.getDigitStr(6);
        boolean isSuccess = false;
        try{
            if(type == phoneType){
                isSuccess = messageService.sendSmsCode(mailOrPhone, code, SmsType.RETRIEVE_LOGIN_PWD.getValue());
            }else if(type == mailType){
                String msg = "[" + properties.getPlatformName() + "] 尊敬的用户，您正在进行密码找回操作，验证码为：" + code
                        + "，" + expireMinute + "分钟内有效，请勿泄漏！";
                isSuccess = messageService.sendEmailHtml(EmailSend.MCH_NOTIFY, mailOrPhone, null, "密码找回",  msg);
            }
            if(! isSuccess){
                return RestResult.error("验证码发送失败!");
            }
        }catch (BizException e){
            return RestResult.error("验证码发送失败，" + e.getMsg());
        }catch (Exception e){
            logger.error("忘记密码处发送验证码异常 loginName = {}", loginName, e);
            return RestResult.error("验证码发送失败，系统异常");
        }

        String cacheKey = getForgetPwdCacheKey(loginName);
        codeManager.cacheCode(cacheKey, code, expireMinute * 60, limitKey);
        return RestResult.success("验证码发送成功");
    }

    /**
     * 确认找回登录密码
     * @param pwdVo
     * @return
     */
    @PostMapping("retrieveLoginPwd")
    public RestResult<String> retrieveLoginPwd(@RequestBody @Validated RetrievePwdDto pwdVo, HttpServletRequest request) {
        String newPwd = cryptService.decryptForWeb(pwdVo.getNewPwd(), properties.getRsaPrivateKey());
        String confirmPwd = cryptService.decryptForWeb(pwdVo.getConfirmPwd(), properties.getRsaPrivateKey());

        if (!ValidateUtil.validPassword(newPwd, 8, 20, true, true, true)) {
            return RestResult.error("登录密码必须由字母、数字、特殊符号组成,8--20位");
        } else if (!newPwd.equals(confirmPwd)) {
            return RestResult.error("新密码和确认密码不一致");
        }

        //校验手机验证码是否正确
        String clientIp = WebUtil.getIpAddr(request);
        String limitKey = getForgetPwdLimitKey(clientIp);
        String cacheKey = getForgetPwdCacheKey(pwdVo.getLoginName());
        String code = codeManager.getCode(cacheKey);
        if(StringUtil.isEmpty(code)){
            return RestResult.error("验证码已失效！");
        }else if(! code.equals(pwdVo.getCode())){
            codeManager.deleteCode(cacheKey, limitKey);
            return RestResult.error("验证码不正确，请重新获取！");
        }

        newPwd = cryptService.encryptSha1(newPwd);
        UserModel userModel = userService.getUserModelByLoginName(pwdVo.getLoginName(), null);
        boolean isSuccess = userService.updatePassword(userModel.getId(), newPwd, userModel.getLoginName(), userModel.getMchNo());
        if(isSuccess){
            codeManager.deleteCode(cacheKey, limitKey);
            operateLogService.logEdit("密码找回成功, IP=" + clientIp, userModel);
        }
        return isSuccess ? RestResult.success("密码找回成功") : RestResult.success("密码重置失败！");
    }

    /**
     * 图形验证码
     * @return
     */
    @RequestMapping("imgVerifyCode")
    public RestResult<Map<String, String>> imgVerifyCode(@Nullable String oldCodeKey){
        VerifyCodeUtil.CodeInfo codeInfo = VerifyCodeUtil.createCode();

        codeManager.cacheCode(codeInfo.getCodeKey(), codeInfo.getCode(), 60, null);

        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("imgBase64", codeInfo.getImgBase64());
        resultMap.put("codeKey", codeInfo.getCodeKey());

        if(StringUtil.isNotEmpty(oldCodeKey)){ //如果有传入旧验证码的key，则及时删除，避免占用内存
            CompletableFuture.runAsync(() -> codeManager.deleteCode(oldCodeKey, null));
        }
        return RestResult.success(resultMap);
    }

    /**
     * 发送登录验证码(短信接收)
     * @param loginDto
     * @return
     */
    @RequestMapping("loginSmsCode")
    public RestResult<Map<String, String>> loginSmsCode(@RequestBody LoginDto loginDto){
        if (loginDto == null || StringUtil.isEmpty(loginDto.getLoginName())) {
            return RestResult.error("请输入登录名！");
        }else if(StringUtil.isEmpty(loginDto.getPassword())){
            return RestResult.error("请输入登录密码！");
        }else{
            loginDto.setLoginName(loginDto.getLoginName().trim());
        }

        //2.判断账号是否存在
        UserModel userModel = userService.getUserModelByLoginName(loginDto.getLoginName(), null);
        if (userModel == null) {
            return RestResult.error("用户名或密码不正确");
        } else if (userModel.getStatus() == UserStatusEnum.INACTIVE.getValue()) {
            return RestResult.error("该帐号已冻结！");
        } else if(StringUtil.isEmpty(userModel.getMobileNo())) {
            return RestResult.error("当前账户信息不完整，无法使用该验证码方式！");
        }

        //3.判断密码是否正确、账户状态是否正常
        String pwdStr = cryptService.decryptForWeb(loginDto.getPassword(), properties.getRsaPrivateKey());
        pwdStr = cryptService.encryptSha1(pwdStr);
        boolean isPass = userService.validLoginPwd(loginDto.getLoginName(), pwdStr);
        if (! isPass) {
            userService.afterLoginPwdError(loginDto);
            return RestResult.error("用户名或密码不正确");
        } else if (UserStatusEnum.UNAUDITED.getValue() == userModel.getStatus()) {
            return RestResult.error("账号未审核");
        }

        String limitKey = getSmsLoginCodeLimitKey(userModel.getLoginName());
        String codeKey = getSmsLoginCodeCacheKey(loginDto.getLoginName());
        String phone = userModel.getMobileNo();
        String code = RandomUtil.getDigitStr(6);
        boolean isSuccess = messageService.sendSmsCode(phone, code, SmsType.LOGIN_CODE.getValue());
        if(isSuccess){
            codeManager.cacheCode(codeKey, code, 5*60, limitKey);
            String tail = StringUtil.subRight(phone, 4);
            Map<String, String> data = new HashMap<>();
            data.put("msg", "短信验证码已发送至尾号为" + tail + "的手机上，请查收！");
            return RestResult.success(data);
        }else{
            return RestResult.error("短信验证码发送失败！");
        }
    }

    /**
     * 登录
     * @param loginDto
     * @param request
     * @return
     */
    @RequestMapping("login")
    public RestResult<Map<String, Object>> login(@RequestBody LoginDto loginDto, HttpServletRequest request) {
        if (loginDto == null || StringUtil.isEmpty(loginDto.getLoginName())) {
            return RestResult.error("请输入用登录名！");
        }else if(StringUtil.isEmpty(loginDto.getPassword())){
            return RestResult.error("请输入登录密码！");
        }else{
            loginDto.setLoginName(loginDto.getLoginName().trim());
        }

        //1.校验验证码是否正确
        String codeKey = null, verifyCode = null, limitKey = null;
        if(loginDto.getCodeType() == null){
            return RestResult.error("请指定验证码校验方式！");
        }else if(loginDto.getCodeType() == 1){
            if(StringUtil.isEmpty(loginDto.getCodeKey())){
                return RestResult.error("验证码key不能为空");
            }
            codeKey = loginDto.getCodeKey();
            verifyCode = codeManager.getCode(codeKey);
        }else if(loginDto.getCodeType() == 2){
            limitKey = getSmsLoginCodeLimitKey(loginDto.getLoginName());
            codeKey = getSmsLoginCodeCacheKey(loginDto.getLoginName());
            verifyCode = codeManager.getCode(codeKey);
        }else{
            return RestResult.error("未支持的验证码方式！");
        }

        if(StringUtil.isEmpty(verifyCode)){
            return RestResult.error("验证码已失效或不存在");
        }else if(! verifyCode.equalsIgnoreCase(loginDto.getVerifyCode())){
            return RestResult.error("验证码不正确");
        }else{
            codeManager.deleteCode(codeKey, limitKey);
        }

        //2.判断账号是否存在
        UserModel userModel = userService.getUserModelByLoginName(loginDto.getLoginName(), null);
        if (userModel == null) {
            return RestResult.error("用户名或密码不正确");
        }else if (userModel.getStatus() == UserStatusEnum.INACTIVE.getValue()) {
            return RestResult.error("该帐号已冻结！");
        }

        //3.判断密码是否正确、账户状态是否正常
        String pwdStr = cryptService.decryptForWeb(loginDto.getPassword(), properties.getRsaPrivateKey());
        pwdStr = cryptService.encryptSha1(pwdStr);
        boolean isPass = userService.validLoginPwd(userModel.getLoginName(), pwdStr);
        if (! isPass) {
            userService.afterLoginPwdError(loginDto);
            return RestResult.error("用户名或密码不正确");
        } else if (UserStatusEnum.UNAUDITED.getValue() == userModel.getStatus()) {
            return RestResult.error("账号未审核");
        }

        //4.上面的校验都通过，说明登录成功，则生成token等信息返回给客户端
        String requestIp = WebUtil.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");
        userModel.setRequestIp(requestIp);

        String token = tokenManager.createAndStoreToken(userModel.getLoginName(), requestIp, userAgent, userModel.getMchNo());
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("token", token);

        userService.afterLoginSuccess(userModel);
        operateLogService.logLoginSuccess("登录成功，IP="+requestIp, userModel);

        return RestResult.success(resultMap);
    }

    /**
     * 获取用户已登录后的信息
     * @return
     */
    @RequestMapping("getSignedInInfo")
    public RestResult<SignedInInfoVo> getSignedInInfo(@CurrentUser UserModel userModel){
        UserInfoVo userInfo = BeanUtil.newAndCopy(userModel, UserInfoVo.class);
        userInfo.setUserId(userModel.getId());

        Map<String, List<DictionaryVo.Item>> dictionaryMap = dictionaryService.listAllDictionary();

        List<AuthVo> authList = functionService.listAuthByUserId(userModel.getId(), userModel.getMchNo());

        SignedInInfoVo signedInInfo = new SignedInInfoVo();
        signedInInfo.setUserInfo(userInfo);
        signedInInfo.setDictionary(dictionaryMap);
        signedInInfo.setAuthList(authList);
        return RestResult.success(signedInInfo);
    }

    /**
     * 获取当前登录用户的信息
     * @param userModel
     * @return
     */
    @RequestMapping("self")
    public RestResult<UserVo> self(@CurrentUser UserModel userModel){
        UserVo userDto = userService.getUserById(userModel.getId(), userModel.getMchNo());
        userDto.setLoginPwd("******");
        return RestResult.success(userDto);
    }

    /**
     * 退出登录
     * @return logout.
     */
    @RequestMapping("logout")
    public RestResult<String> logout(@CurrentUser UserModel userModel) {
        if (userModel != null) {
            tokenManager.deleteTokenFromServer(userModel.getLoginName());
            funcManager.afterLogout(userModel);
            userService.afterLogout(userModel);
            operateLogService.logLogout("退出成功，IP="+userModel.getRequestIp(), userModel);
        }
        return RestResult.success("退出成功");
    }

    /**
     * 新增用户
     */
    @Permission("sys:user:add")
    @RequestMapping("addUser")
    public RestResult<String> addUser(@CurrentUser UserModel userModel, @RequestBody UserVo userDto) {
        try{
            String newPwd = cryptService.decryptForWeb(userDto.getLoginPwd(), properties.getRsaPrivateKey());
            if (!ValidateUtil.validPassword(newPwd, 6, 20, true, true, true)) {
                return RestResult.error("登录密码须由字母、数字、特殊符号组成，6~20位");
            }

            newPwd = cryptService.encryptSha1(newPwd);
            userDto.setLoginPwd(newPwd);
            userDto.setType(UserTypeEnum.USER.getValue());//只能添加普通用户
            userDto.setMchNo(userModel.getMchNo());
            userDto.setMchType(userModel.getMchType());
            userDto.setCreator(userModel.getLoginName());
            userDto.setModifier(userModel.getLoginName());

            boolean isSuccess = userService.addUser(userDto, userModel.getLoginName(), userModel.getMchNo());
            if(isSuccess){
                operateLogService.logEdit("新增用户[" + userDto.getLoginName() + "]", userModel);
                return RestResult.success("新增成功");
            }else{
                return RestResult.error("新增失败");
            }
        } catch(BizException e) {
            return RestResult.error("新增失败," + e.getMsg());
        } catch(Exception e) {
            logger.error("编辑用户异常", e);
            return RestResult.error("新增失败," + e.getMessage());
        }
    }

    /**
     * 修改用户信息
     * @return
     */
    @Permission("sys:user:edit")
    @RequestMapping("editUser")
    public RestResult<String> editUser(@CurrentUser UserModel userModel, @RequestBody UserVo updateInfo) {
        UserVo userDto = userService.getUserById(updateInfo.getId(), userModel.getMchNo());
        if (userDto == null) {
            return RestResult.error("用户不存在");
        }else if (UserTypeEnum.ADMIN.getValue() == userDto.getType() && userDto.getId().longValue() != userModel.getId().longValue()) {
            //管理员只能自己修改自己，不能被其他人修改
            return RestResult.error("当前用户不允许修改");
        }

        try{
            userService.updateUser(updateInfo, userModel.getLoginName(), userModel.getMchNo());
            operateLogService.logEdit("修改用户：" + JsonUtil.toJson(updateInfo), userModel);
            return RestResult.success("修改成功");
        } catch(BizException e) {
            return RestResult.error("修改失败，"+e.getMsg());
        } catch(Exception e) {
            logger.error("编辑用户异常", e);
            return RestResult.error("修改失败，系统异常");
        }
    }

    /**
     * 为用户分配角色
     * @param assignDto
     * @param userModel
     * @return
     */
    @Permission("sys:user:assignRoles")
    @RequestMapping("assignRoles")
    public RestResult<String> assignRoles(@NotNull @RequestBody AssignRoleDto assignDto, @CurrentUser UserModel userModel){
        Long userId = assignDto.getUserId();
        List<Long> roleIds = assignDto.getRoleIds();
        if(userId == null){
            return RestResult.error("操作失败，请指定要分配的用户！");
        }

        UserVo userDtoNow = userService.getUserById(userId, userModel.getMchNo());
        if (userDtoNow == null) {
            return RestResult.error("用户不存在");
        }else if (UserTypeEnum.ADMIN.getValue() == userDtoNow.getType()) {
            return RestResult.error("当前用户不允许修改关联的角色！");
        }

        try {
            userService.assignRoles(userId, roleIds, userModel.getLoginName(), userModel.getMchNo());
            operateLogService.logEdit("修改用户角色[" + userDtoNow.getLoginName() + "]，更改后角色[" + roleIds + "]", userModel);
            return RestResult.success("操作成功");
        } catch(BizException e) {
            return RestResult.error("操作失败，"+e.getMsg());
        } catch(Exception e) {
            logger.error("修改用户角色异常", e);
            return RestResult.error("操作失败，系统异常");
        }
    }

    /**
     * 修改用户状态
     * @retur
     */
    @Permission("sys:user:changeStatus")
    @RequestMapping("changeUserStatus")
    public RestResult<String> changeUserStatus(@CurrentUser UserModel userModel, @RequestParam Long userId) {
        if (Objects.equals(userModel.getId(), userId)) {
            return RestResult.error("不能修改自己账户的状态");
        }
        UserVo userDto = userService.getUserById(userId, userModel.getMchNo());
        if (userDto == null) {
            return RestResult.error("用户不存在");
        }else if (UserTypeEnum.ADMIN.getValue() == userDto.getType()) {
            return RestResult.error("当前用户状态不允许修改");
        }

        if (userDto.getStatus() == UserStatusEnum.UNAUDITED.getValue() && Objects.equals(userModel.getLoginName(), userDto.getLoginName())) {
            return RestResult.error("不能审核自己！");//如果是未审核
        }
        Integer newStatus;
        Integer oldStatus = userDto.getStatus();
        if (oldStatus == UserStatusEnum.UNAUDITED.getValue()) {
            newStatus = UserStatusEnum.ACTIVE.getValue();
        } else if (oldStatus == UserStatusEnum.ACTIVE.getValue()) {
            newStatus = UserStatusEnum.INACTIVE.getValue();
        } else if (oldStatus == UserStatusEnum.INACTIVE.getValue()) {
            newStatus = UserStatusEnum.ACTIVE.getValue();
        }else{
            return RestResult.error("未预期的用户状态！");
        }

        try{
            userService.updateUserStatus(userDto.getId(), newStatus, userModel.getLoginName(), userModel.getMchNo());
            operateLogService.logEdit("修改用户状态成功[" + userDto.getLoginName() + "],oldStatus:" + oldStatus + ",newStatus:" + newStatus, userModel);
            return RestResult.success("操作成功");
        } catch(BizException e) {
            return RestResult.error("修改失败，"+e.getMsg());
        } catch(Exception e) {
            logger.error("修改用户状态异常", e);
            return RestResult.error("修改失败，系统异常");
        }
    }

    /**
     * 删除用户
     * @return
     */
    @Permission("sys:user:delete")
    @RequestMapping("deleteUser")
    public RestResult<String> deleteUser(@RequestParam Long userId, @CurrentUser UserModel userModel) {
        UserVo userDto = userService.getUserById(userId, userModel.getMchNo()); // 查询用户信息
        if (userDto == null) {
            return RestResult.error("用户不存在");
        }else if (UserTypeEnum.ADMIN.getValue() == userDto.getType()){
            return RestResult.error("当前用户不允许删除");
        }

        try{
            userService.deleteUser(userId, userModel.getLoginName(), userModel.getMchNo());
            operateLogService.logDelete("删除用户，登录名:" + userDto.getLoginName(), userModel);
            return RestResult.success("删除用户成功");
        } catch(BizException e) {
            return RestResult.error("删除失败，"+e.getMsg());
        } catch(Exception e) {
            logger.error("删除用户异常", e);
            return RestResult.error("删除失败，系统异常");
        }
    }

    /**
     * 修改密码
     * @param userModel
     * @param pwdChangeDto
     * @return
     */
    @RequestMapping("changePwd")
    public RestResult<String> changePwd(@CurrentUser UserModel userModel, @RequestBody @Validated PwdChangeDto pwdChangeDto){
        String oldPwd = cryptService.decryptForWeb(pwdChangeDto.getOldPwd(), properties.getRsaPrivateKey());
        String newPwd = cryptService.decryptForWeb(pwdChangeDto.getNewPwd(), properties.getRsaPrivateKey());
        String confirmPwd = cryptService.decryptForWeb(pwdChangeDto.getConfirmPwd(), properties.getRsaPrivateKey());
        if (!ValidateUtil.validPassword(newPwd, 6, 20, true, true, true)) {
            return RestResult.error("登录密码须由字母、数字、特殊符号组成，6~20位");
        }else if(newPwd.equals(oldPwd)){
            return RestResult.error("新旧密码不能相同");
        }else if(! newPwd.equals(confirmPwd)){
            return RestResult.error("新密码和确认密码不一致");
        }

        oldPwd = cryptService.encryptSha1(oldPwd);
        boolean isPass = userService.validLoginPwd(userModel.getLoginName(), oldPwd);
        if(! isPass){
            return RestResult.error("密码不正确，无权修改");
        }

        newPwd = cryptService.encryptSha1(newPwd);
        boolean isOk = userService.updatePassword(userModel.getId(), newPwd, userModel.getLoginName(), userModel.getMchNo());
        if(isOk){
            try{
                tokenManager.deleteTokenFromServer(userModel.getLoginName());//密码修改成功，删除token强制用户重新登录
            }catch (Exception e){
                logger.error("loginName={} 密码修改成功后删除token异常", userModel.getLoginName(), e);
            }
            return RestResult.success("密码修改成功！");
        }else{
            return RestResult.error("修改失败！");
        }
    }

    /**
     * 重置用户密码.
     * @return
     */
    @Permission("sys:user:resetPwd")
    @PostMapping("resetUserPwd")
    public RestResult<String> resetUserPwd(@CurrentUser UserModel userModel, @Validated @RequestBody PwdResetDto resetDto) {
        UserVo userDto = userService.getUserById(resetDto.getUserId(), userModel.getMchNo());
        if (userDto == null) {
            return RestResult.error("用户不存在");
        }else if(UserTypeEnum.ADMIN.getValue() == userDto.getType().intValue()) {
            return RestResult.error("当前用户的密码不允许重置");
        }

        try{
            String newPwd = cryptService.decryptForWeb(resetDto.getNewPwd(), properties.getRsaPrivateKey());
            if (!ValidateUtil.validPassword(newPwd, 6, 20, true, true, true)) {
                return RestResult.error("登录密码须由字母、数字、特殊符号组成，6~20位");
            }

            newPwd = cryptService.encryptSha1(newPwd);
            userService.updatePassword(userDto.getId(), newPwd, userModel.getLoginName(), userModel.getMchNo());
            operateLogService.logEdit("重置用户[" + userDto.getLoginName() + "]的密码", userModel);
            userService.afterPwdReset(userDto.getLoginName());
            return RestResult.success("密码重置成功");
        } catch(BizException e) {
            return RestResult.error("密码重置失败，"+e.getMsg());
        } catch(Exception e) {
            logger.error("重置用户密码异常", e);
            return RestResult.error("密码重置失败，系统异常");
        }
    }

    /**
     * 分页列出用户信息，并可按登录名、姓名、状态进行查询.
     */
    @Permission("sys:user:view")
    @RequestMapping("listUser")
    public RestResult<PageResult<List<UserVo>>> listUser(@RequestBody UserQueryDto queryDto, @CurrentUser UserModel userModel) {
        Map<String, Object> paramMap = BeanUtil.toMapNotNull(queryDto);
        PageQuery pageQuery = PageQuery.newInstance(queryDto.getCurrentPage(), queryDto.getPageSize());
        PageResult<List<UserVo>> pageResult = userService.listUserPage(paramMap, pageQuery, userModel.getMchNo());
        return RestResult.success(pageResult);
    }

    /**
     * 根据用户id查询用户信息
     * @param userId
     * @param userModel
     * @return
     */
    @Permission("sys:user:view")
    @RequestMapping("getUserById")
    public RestResult<UserVo> getUserById(@RequestParam Long userId, @CurrentUser UserModel userModel) {
        UserVo dto = userService.getUserById(userId, userModel.getMchNo());
        if(dto != null){
            dto.setLoginPwd("******");
            List<RoleVo> pmsRoles = roleService.listRoleByUserId(userId, userModel.getMchNo());
            dto.setRoleIds(pmsRoles.stream().map(RoleVo::getId).collect(Collectors.toList()));
        }
        return RestResult.success(dto);
    }

    /**
     * 分页列出用户操作日志，并可按登录名、姓名、状态进行查询.
     */
    @Permission("sys:operateLog:view")
    @RequestMapping("listOperateLog")
    public RestResult<PageResult<List<OperateLogVo>>> listOperateLog(@RequestBody OperateLogQueryDto queryDto,
                                                                     @CurrentUser UserModel userModel) {
        Map<String, Object> paramMap = BeanUtil.toMapNotNull(queryDto);
        PageQuery pageQuery = PageQuery.newInstance(queryDto.getCurrentPage(), queryDto.getPageSize());
        PageResult<List<OperateLogVo>> result = operateLogService.listOperateLogPage(paramMap, pageQuery, userModel.getMchNo());
        return RestResult.success(result);
    }

    private String getSmsLoginCodeCacheKey(String loginName){
        return properties.getAppName() + ":smsLoginCodeCache:" + loginName;
    }
    private String getSmsLoginCodeLimitKey(String loginName){
        return properties.getAppName() + ":smsLoginCodeLimit:" + loginName;
    }
    private String getForgetPwdLimitKey(String address){
        return properties.getAppName() + ":forgetPwdLimit:" + address;
    }
    private String getForgetPwdCacheKey(String loginName){
        return properties.getAppName() + "::forgetPwdCache:" + loginName;
    }
}
