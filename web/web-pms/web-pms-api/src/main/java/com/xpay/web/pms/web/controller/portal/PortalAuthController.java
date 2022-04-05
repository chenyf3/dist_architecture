/**
 *
 */
package com.xpay.web.pms.web.controller.portal;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.enums.user.AuthTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.query.PageQuery;
import com.xpay.common.statics.result.PageResult;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.facade.user.dto.PortalAuthDto;
import com.xpay.facade.user.dto.PortalRevokeAuthDto;
import com.xpay.facade.user.service.PortalPermissionFacade;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.ddo.vo.AuthNodeVo;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.pms.web.controller.BaseController;
import com.xpay.web.pms.web.vo.portal.RevokeAuthQueryVo;
import org.apache.dubbo.config.annotation.DubboReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description: 商户后台菜单权限控制器
 */
@RestController
@RequestMapping("portalAuth")
public class PortalAuthController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @DubboReference
    private PortalPermissionFacade portalPermissionFacade;

    /**
     * 列出要管理的菜单.
     *
     * @return PortalMenuList .
     */
    @Permission("portal:auth:view")
    @RequestMapping("listAllAuth")
    public RestResult<List<PortalAuthDto>> listAllAuth() {
        List<PortalAuthDto> allAuths = portalPermissionFacade.listAllAuth();
        return RestResult.success(allAuths);
    }

    /**
     * 返回权限功能的树形结构
     */
    @Permission("portal:auth:view")
    @RequestMapping("listAuthTree")
    public RestResult<List<AuthNodeVo>> listAuthTree() {
        List<PortalAuthDto> allAuths = portalPermissionFacade.listAllAuth();
        Map<Long, List<PortalAuthDto>> mapByPid = allAuths.stream().collect(Collectors.groupingBy(PortalAuthDto::getParentId));

        List<AuthNodeVo> nodeVOList;
        if (mapByPid.get(0L) != null) {
            nodeVOList = mapByPid.get(0L).stream()
                    .map(portalAuth -> buildAuthNodeDto(portalAuth, mapByPid))
                    .collect(Collectors.toList());
        } else {
            nodeVOList = new ArrayList<>();
        }
        return RestResult.success(nodeVOList);
    }

    /**
     * 保存新增菜单.
     *
     * @return
     */
    @Permission("portal:auth:add")
    @RequestMapping("addAuth")
    public RestResult<String> addAuth(@RequestBody PortalAuthDto portalAuth, @CurrentUser UserModel userModel) {
        try {
            if (portalAuth.getParentId() == null || portalAuth.getParentId() == 0) {
                //表示添加的是一级菜单
                portalAuth.setAuthType(AuthTypeEnum.MENU_TYPE.getValue());
            } else {
                PortalAuthDto parentAuth = portalPermissionFacade.getAuthById(portalAuth.getParentId());
                if (parentAuth.getAuthType() != AuthTypeEnum.MENU_TYPE.getValue()) {
                    return RestResult.error("父节点必须是菜单");
                }
            }
            portalPermissionFacade.createAuth(portalAuth);
            super.logAdd("添加商户后台权限[" + portalAuth.getName() + "]", userModel);
            return RestResult.success("添加成功");
        } catch (BizException e) {
            return RestResult.error("添加失败,"+e.getMsg());
        } catch (Exception e) {
            // 记录系统操作日志
            logger.error("== portalAuthAdd exception:", e);
            return RestResult.error("添加失败");
        }
    }

    /**
     * 保存要修改的菜单.
     *
     * @return
     */
    @Permission("portal:auth:edit")
    @RequestMapping("editAuth")
    public RestResult<String> editAuth(@RequestBody PortalAuthDto portalAuth, @CurrentUser UserModel userModel) {
        try {
            PortalAuthDto current = portalPermissionFacade.getAuthById(portalAuth.getId());
            current.setName(portalAuth.getName());
            current.setNumber(portalAuth.getNumber());
            current.setPermissionFlag(portalAuth.getPermissionFlag());
            current.setUrl(portalAuth.getUrl());
            current.setIcon(portalAuth.getIcon());
            portalPermissionFacade.updateAuth(current);
            // 记录系统操作日志
            super.logEdit("修改商户后台权限，权限名称[" + portalAuth.getName() + "]", userModel);
            return RestResult.success("修改成功");
        } catch (BizException e) {
            return RestResult.error("修改失败,"+e.getMsg());
        } catch (Exception e) {
            // 记录系统操作日志
            logger.error("== portalAuthEdit exception:", e);
            return RestResult.error("修改失败,系统异常");
        }
    }

    /**
     * 删除菜单.
     *
     * @return
     */
    @Permission("portal:auth:delete")
    @RequestMapping("deleteAuth")
    public RestResult<String> deleteAuth(@RequestParam("authId") Long authId, @CurrentUser UserModel userModel) {
        String authName = null;
        try {
            PortalAuthDto auth;
            if (authId == null || authId == 0 || (auth = portalPermissionFacade.getAuthById(authId)) == null) {
                return RestResult.error("无法获取要删除的数据");
            }
            authName = auth.getName();

            // 先判断此菜单下是否有子菜单
            List<PortalAuthDto> childMenuList = portalPermissionFacade.listAuthByParentId(authId).stream()
                    .filter(p -> p.getAuthType() == AuthTypeEnum.MENU_TYPE.getValue())
                    .collect(Collectors.toList());
            if (!childMenuList.isEmpty()) {
                return RestResult.error("此权限下关联有【" + childMenuList.size() + "】个子菜单，不能支接删除!");
            }
            // 删除掉权限以及其子权限（该方法会同时删除与这些权限关联的roleAuth）
            portalPermissionFacade.deleteAuthById(authId);

            // 记录系统操作日志
            super.logDelete("删除权限，[名称：" + auth.getName() + "，id：" + auth.getId() + "]", userModel);
            return RestResult.success("删除成功");
        } catch (BizException e) {
            return RestResult.error("删除失败,"+e.getMsg());
        } catch (Exception e) {
            // 记录系统操作日志
            logger.error("== authDelete exception:", e);
            super.logDelete("删除权限，[名称：" + authName + "，id：" + authId + "]", userModel);
            return RestResult.error("删除权限出错");
        }
    }

    @Permission("portal:auth:revoke")
    @RequestMapping("listRevokeAuth")
    public RestResult<PageResult<List<PortalRevokeAuthDto>>> listRevokeAuth(@RequestBody RevokeAuthQueryVo queryVo) {
        PageQuery pageQuery = PageQuery.newInstance(queryVo.getCurrentPage(), queryVo.getPageSize());
        Map<String, Object> paramMap = BeanUtil.toMapNotNull(queryVo);
        PageResult<List<PortalRevokeAuthDto>> pageResult = portalPermissionFacade.listRevokeAuthPage(paramMap, pageQuery);
        return RestResult.success(pageResult);
    }

    @Permission("portal:auth:doRevoke")
    @RequestMapping("doRevokeAuth")
    public RestResult<String> doRevokeAuth(@RequestParam Long revokeId) {
        try{
            boolean isSuccess = portalPermissionFacade.doAuthRevoke(revokeId);
            return isSuccess ? RestResult.success("任务提交成功，请等待处理结果") : RestResult.error("操作失败");
        }catch(BizException e){
            return RestResult.error("操作失败，" + e.getMsg());
        }catch(Exception e){
            logger.error("revokeId={} 执行权限回收时异常", revokeId, e);
            return RestResult.error("操作失败，系统异常");
        }
    }

    /**
     * 从根节点开始构建权限功能树
     * @param root      根节点
     * @param mapByPId  根据父节点聚集map
     */
    private AuthNodeVo buildAuthNodeDto(PortalAuthDto root, Map<Long, List<PortalAuthDto>> mapByPId) {
        AuthNodeVo authNodeDto = copyProperties(root);

        if (AuthTypeEnum.MENU_TYPE.getValue() == authNodeDto.getAuthType() && mapByPId.get(root.getId()) != null) {
            mapByPId.get(root.getId()).forEach(portalAuth -> {
                AuthNodeVo children = buildAuthNodeDto(portalAuth, mapByPId);
                if (AuthTypeEnum.ACTION_TYPE.getValue() == portalAuth.getAuthType()) {
                    authNodeDto.getActionChildren().add(children);
                } else {
                    authNodeDto.getChildren().add(children);
                }
            });
        }
        return authNodeDto;
    }

    private AuthNodeVo copyProperties(PortalAuthDto portalAuth) {
        AuthNodeVo authNodeDto = new AuthNodeVo();
        authNodeDto.setId(portalAuth.getId());
        authNodeDto.setParentId(portalAuth.getParentId());
        authNodeDto.setName(portalAuth.getName());
        authNodeDto.setNumber(portalAuth.getNumber());
        authNodeDto.setPermissionFlag(portalAuth.getPermissionFlag());
        authNodeDto.setAuthType(portalAuth.getAuthType());
        authNodeDto.setUrl(portalAuth.getUrl());
        authNodeDto.setChildren(new ArrayList<>());
        authNodeDto.setActionChildren(new ArrayList<>());
        return authNodeDto;
    }
}
