package com.xpay.web.api.common.controller;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.enums.user.RoleTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.web.api.common.ddo.dto.RoleQueryDto;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.ddo.vo.AuthVo;
import com.xpay.web.api.common.ddo.vo.RoleVo;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.OperateLogService;
import com.xpay.web.api.common.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("role")
public class RoleController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    RoleService roleService;
    @Autowired
    OperateLogService operateLogService;

    /**
     * 获取角色列表
     */
    @Permission("sys:role:view")
    @PostMapping("listRole")
    public RestResult<PageResult<List<RoleVo>>> listRole(@RequestBody RoleQueryDto queryDto, @CurrentUser UserModel userModel) {
        Map<String, Object> paramMap = BeanUtil.toMapNotNull(queryDto);
        PageQuery pageQuery = PageQuery.newInstance(queryDto.getCurrentPage(), queryDto.getPageSize());
        PageResult<List<RoleVo>> pageResult = roleService.listRolePage(paramMap, pageQuery, userModel.getMchNo());
        return RestResult.success(pageResult);
    }

    /**
     * 获取所有角色
     */
    @Permission("sys:role:view")
    @GetMapping("listAllRoles")
    public RestResult<List<RoleVo>> listAllRoles(@Nullable @RequestBody HashMap<String, String> paramMap, @CurrentUser UserModel userModel) {
        List<RoleVo> allRoleList = roleService.listAllRoles(paramMap, userModel.getMchNo());
        return RestResult.success(allRoleList);
    }

    /**
     * 保存新添加的一个角色
     */
    @Permission("sys:role:add")
    @PostMapping("addRole")
    public RestResult<String> addRole(@RequestBody @Validated RoleVo roleVo, @CurrentUser UserModel userModel) {
        roleVo.setCreateTime(new Date());
        roleVo.setMchNo(userModel.getMchNo());
        roleVo.setMchType(userModel.getMchType());
        roleVo.setRoleType(RoleTypeEnum.USER.getValue());
        if(StringUtil.isEmpty(roleVo.getRemark())){
            roleVo.setRemark("");
        }
        try{
            roleService.addRole(roleVo, userModel.getLoginName());
            // 记录用户操作日志
            operateLogService.logAdd("添加角色，角色名称[" + roleVo.getRoleName() + "]", userModel);
            return RestResult.success("添加角色成功");
        } catch (BizException e) {
            return RestResult.error("添加角色失败," + e.getMsg());
        } catch (Exception e){
            logger.error("添加角色异常", e);
            return RestResult.error("添加角色失败,系统异常");
        }
    }

    /**
     * 更新角色
     */
    @Permission("sys:role:edit")
    @PostMapping("editRole")
    public RestResult<String> editRole(@RequestBody @Validated RoleVo roleVo, @CurrentUser UserModel userModel) {
        RoleVo roleOld = roleService.getRoleById(roleVo.getId(), userModel.getMchNo());
        if(roleOld == null){
            return RestResult.error("当前角色不存在");
        }

        roleOld.setRoleName(roleVo.getRoleName());
        roleOld.setRemark(roleVo.getRemark());
        try {
            roleService.editRole(roleOld, userModel.getLoginName(), userModel.getMchNo());
            // 记录用户操作日志
            operateLogService.logEdit("修改角色，[名称: " + roleOld.getRoleName() + ",id: " + roleOld.getId() + "]", userModel);
            return RestResult.success("修改角色成功");
        } catch (BizException e) {
            return RestResult.error("修改角色失败," + e.getMsg());
        } catch (Exception e){
            logger.error("修改角色异常", e);
            return RestResult.error("修改角色失败,系统异常");
        }
    }

    /**
     * 删除一个角色
     */
    @Permission("sys:role:delete")
    @PostMapping("deleteRole")
    public RestResult<String> deleteRole(@RequestParam Long roleId, @CurrentUser UserModel userModel) {
        RoleVo role = roleService.getRoleById(roleId, userModel.getMchNo());
        if (role == null) {
            return RestResult.error("无法获取要删除的角色");
        }

        try{
            roleService.deleteRoleAndRelatedById(roleId, userModel.getMchNo());
            operateLogService.logDelete("删除角色，名称:" + role.getRoleName(), userModel);
            return RestResult.success("删除角色成功");
        }catch (BizException e) {
            return RestResult.error("删除角色失败," + e.getMsg());
        } catch (Exception e){
            logger.error("删除角色异常", e);
            return RestResult.error("删除角色失败,系统异常");
        }
    }

    /**
     * 列出角色关联的菜单
     *
     * @return PmsMenuList .
     */
    @Permission("sys:role:assignAuth")
    @GetMapping("listRoleAuth")
    public RestResult<List<AuthVo>> listRoleAuth(@RequestParam Long roleId, @CurrentUser UserModel userModel) {
        List<AuthVo> functions = roleService.listAuthByRoleId(roleId, userModel.getMchNo());
        return RestResult.success(functions);
    }

    /**
     * 为角色分配权限
     */
    @Permission("sys:role:assignAuth")
    @PostMapping("assignRoleAuth")
    public RestResult<String> assignRoleAuth(@RequestParam Long roleId, @RequestBody List<Long> authIds, @CurrentUser UserModel userModel) {
        RoleVo role = roleService.getRoleById(roleId, userModel.getMchNo());
        if (role == null) {
            return RestResult.error("无法获取角色信息");
        } else if(role.getRoleType() != null && role.getRoleType() == RoleTypeEnum.ADMIN.getValue()) {
            return RestResult.error("不允许修改当前角色的权限！");
        }

        try {
            // 分配权限
            roleService.assignPermission(roleId, authIds, userModel.getMchNo());
            operateLogService.logEdit("修改角色权限[名称: " + role.getRoleName() + ", id: " + roleId + ", authIds: " + JsonUtil.toJson(authIds) + "]", userModel);
            return RestResult.success("分配权限成功");
        } catch (BizException e) {
            return RestResult.error("分配权限失败," + e.getMsg());
        } catch (Exception e){
            logger.error("给角色分配权限异常", e);
            return RestResult.error("分配权限失败,系统异常");
        }
    }
}
