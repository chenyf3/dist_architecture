package com.xpay.web.pms.web.controller.portal;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.enums.user.RoleTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.facade.user.dto.PortalAuthDto;
import com.xpay.facade.user.dto.PortalRoleDto;
import com.xpay.facade.user.dto.PortalUserDto;
import com.xpay.facade.user.service.PortalPermissionFacade;
import com.xpay.facade.user.service.PortalUserFacade;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.ddo.vo.RoleVo;
import com.xpay.web.api.common.ddo.dto.RoleQueryDto;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.pms.web.controller.BaseController;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * Description: 商户后台角色管理
 */
@RestController
@RequestMapping("portalRole")
public class PortalRoleController extends BaseController {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @DubboReference
    private PortalPermissionFacade portalPermissionFacade;
    @DubboReference
    private PortalUserFacade portalUserFacade;

    /**
     * 获取角色列表
     */
    @Permission("portal:role:view")
    @PostMapping("listRole")
    public RestResult<PageResult<List<PortalRoleDto>>> listRole(@RequestBody RoleQueryDto queryVo) {
        try {
            PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());
            PageResult<List<PortalRoleDto>> pageResult = portalPermissionFacade.listRolePage(BeanUtil.toMapNotNull(queryVo), pageQuery);
            return RestResult.success(pageResult);
        } catch (Exception e) {
            logger.error("== listPortalRole exception:", e);
            return RestResult.error("获取角色失败");
        }
    }

    /**
     * 新增管理员角色
     */
    @Permission("portal:role:addAdmin")
    @PostMapping("addAdminRole")
    public RestResult<String> addAdminRole(@RequestBody @Validated RoleVo roleVo, @CurrentUser UserModel userModel) {
        try {
            PortalRoleDto portalRole = BeanUtil.newAndCopy(roleVo, PortalRoleDto.class);
            portalRole.setMchNo("");
            portalRole.setCreateTime(new Date());
            portalRole.setRoleType(RoleTypeEnum.ADMIN.getValue());//只能添加管理员类型的角色
            portalPermissionFacade.createRole(portalRole);
            // 记录操作员操作日志
            super.logAdd("添加角色，[名称: " + portalRole.getRoleName() + "]", userModel);
            return RestResult.success("添加角色成功");
        } catch (BizException e) {
            return RestResult.error("添加角色失败，"+e.getMsg());
        } catch (Exception e) {
            logger.error("== addPortalRole exception:", e);
            return RestResult.error("添加角色失败");
        }
    }

    /**
     * 编辑管理员角色
     * @param roleVo
     * @param userModel
     * @return
     */
    @Permission("portal:role:editAdmin")
    @PostMapping("editAdminRole")
    public RestResult<String> editAdminRole(@RequestBody @Validated RoleVo roleVo, @CurrentUser UserModel userModel) {
        try {
            PortalRoleDto role = portalPermissionFacade.getRoleById(roleVo.getId());
            if (role == null) {
                return RestResult.error("无法获取要修改的角色！");
            }else if(role.getRoleType() != RoleTypeEnum.ADMIN.getValue()){ //只能编辑管理员类型的角色
                return RestResult.error("无法修改商户的自定义角色！");
            }

            role.setRemark(roleVo.getRemark());
            role.setRoleName(roleVo.getRoleName());
            role.setAutoAssign(roleVo.getAutoAssign());
            portalPermissionFacade.updateRole(role);
            // 记录操作员操作日志
            super.logAdd("编辑角色，[名称: " + role.getRoleName() + "]", userModel);
            return RestResult.success("编辑角色成功");
        } catch (BizException e) {
            return RestResult.error("编辑角色失败，"+e.getMsg());
        } catch (Exception e) {
            logger.error("编辑角色异常", e);
            return RestResult.error("编辑角色失败");
        }
    }

    /**
     * 删除管理员角色
     */
    @Permission("portal:role:deleteAdmin")
    @PostMapping("deleteAdminRole")
    public RestResult<String> deleteAdminRole(@RequestParam Long roleId, @CurrentUser UserModel userModel) {
        try {
            PortalRoleDto role = portalPermissionFacade.getRoleById(roleId);
            if (role == null) {
                return RestResult.error("无法获取要删除的角色");
            }else if(role.getRoleType() != RoleTypeEnum.ADMIN.getValue()){ //只能删除管理员类型的角色
                return RestResult.error("无法删除商户自定义的角色");
            }

            portalPermissionFacade.deleteAdminRoleById(roleId, userModel.getLoginName(), "删除角色");
            super.logDelete("删除角色，[名称: " + role.getRoleName() + "]", userModel);
            return RestResult.success("删除角色成功");
        } catch (BizException e) {
            return RestResult.error("删除角色失败，"+e.getMsg());
        } catch (Exception e) {
            logger.error("== deletePortalRole exception:", e);
            super.logDelete("删除角色出错:" + e.getMessage(), userModel);
            return RestResult.error("删除失败");
        }
    }

    /**
     * 列出所有的管理员角色
     * @param mchUserId
     * @return
     */
    @Permission("portal:role:assignAdminRoleAuth")
    @RequestMapping("listAllAdminRoles")
    public RestResult<List<PortalRoleDto>> listAllAdminRoles(@Nullable Long mchUserId) {
        try {
            Integer mchType = null;
            if(mchUserId != null){
                PortalUserDto portalUser = portalUserFacade.getUserById(mchUserId);
                if(portalUser == null) {
                    return RestResult.error("指定的商户用户不存在");
                }
                mchType = portalUser.getMchType();
            }
            List<PortalRoleDto> allRoleList = portalPermissionFacade.listAllAdminRoles(mchType);
            return RestResult.success(allRoleList);
        } catch (Exception e) {
            logger.error("获取管理员角色列表异常", e);
            return RestResult.error("获取管理员角色列表异常");
        }
    }

    /**
     * 列出角色关联的权限
     * @return
     */
    @Permission("portal:role:assignAdminRoleAuth")
    @GetMapping("listRoleAuth")
    public RestResult<List<PortalAuthDto>> listRoleAuth(@RequestParam Long roleId) {
        List<PortalAuthDto> auths = portalPermissionFacade.listAuthByRoleId(roleId);
        return RestResult.success(auths);
    }

    /**
     * 为管理员角色分配权限
     */
    @Permission("portal:role:assignAdminRoleAuth")
    @PostMapping("assignAdminRoleAuth")
    public RestResult<String> assignAdminRoleAuth(@RequestParam Long roleId,
                                                      @RequestBody List<Long> authIds,
                                                      @CurrentUser UserModel userModel) {
        try {
            PortalRoleDto portalRole = portalPermissionFacade.getRoleById(roleId);
            if (portalRole == null) {
                return RestResult.error("无法获取角色信息");
            }else if(portalRole.getRoleType() != RoleTypeEnum.ADMIN.getValue()){//只能分配管理员类型的角色
                return RestResult.error("无法给商户的自定义角色分配权限！");
            }

            // 分配菜单权限，功能权限
            portalPermissionFacade.assignAdminRolePermission(roleId, authIds, userModel.getLoginName(), "调整角色权限");
            super.logEdit("修改角色权限[名称：" + portalRole.getRoleName() + "]，权限Id[" + JsonUtil.toJson(authIds) + "]", userModel);
            return RestResult.success("分配权限成功");
        } catch (BizException e) {
            return RestResult.error("分配权限失败，"+e.getMsg());
        } catch (Exception e) {
            logger.error("分配权限出现错误!", e);
            return RestResult.error("分配权限出现错误");
        }
    }
}
