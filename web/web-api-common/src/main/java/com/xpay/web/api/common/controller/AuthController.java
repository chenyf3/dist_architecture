package com.xpay.web.api.common.controller;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.enums.user.AuthTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.BeanUtil;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.ddo.vo.AuthVo;
import com.xpay.web.api.common.ddo.vo.AuthNodeVo;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.AuthService;
import com.xpay.web.api.common.service.OperateLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("auth")
public class AuthController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    AuthService authService;
    @Autowired
    OperateLogService operateLogService;

    /**
     * 列出要管理的功能.
     *
     * @return AuthVo
     */
    @Permission("sys:auth:view | sys:role:assignAuth")
    @RequestMapping("listAllAuth")
    public RestResult<List<AuthVo>> listAllAuth(@CurrentUser UserModel userModel, @Nullable @RequestBody HashMap<String, String> paramMap) {
        List<AuthVo> allAuths = authService.listAllAuth(paramMap, userModel.getMchNo());
        return RestResult.success(allAuths);
    }

    /**
     * 返回权限功能的树形结构
     */
    @Permission("sys:auth:view | sys:role:assignAuth")
    @RequestMapping("listAuthTree")
    public RestResult<List<AuthNodeVo>> listAuthTree(@CurrentUser UserModel userModel, @Nullable @RequestBody HashMap<String, String> paramMap) {
        List<AuthVo> allAuths = authService.listAllAuth(paramMap, userModel.getMchNo());
        Map<Long, List<AuthVo>> mapByPid = allAuths.stream().collect(Collectors.groupingBy(AuthVo::getParentId));

        List<AuthNodeVo> nodeList;
        if (mapByPid.get(0L) != null) {
            nodeList = mapByPid.get(0L).stream()
                    .map(authDto -> buildAuthNode(authDto, mapByPid))
                    .collect(Collectors.toList());
        } else {
            nodeList = new ArrayList<>();
        }
        return RestResult.success(nodeList);
    }

    /**
     * 保存新增功能.
     *
     * @return operateSuccess or operateError .
     */
    @Permission("sys:auth:add")
    @RequestMapping("addAuth")
    public RestResult<String> addAuth(@RequestBody AuthVo authVo, @CurrentUser UserModel userModel) {
        try {
            if (null == authVo.getParentId() || authVo.getParentId() == 0) {
                //表示添加的是一级菜单
                authVo.setAuthType(AuthTypeEnum.MENU_TYPE.getValue());
            } else {
                AuthVo parentAuth = authService.getAuthById(authVo.getParentId());
                if (parentAuth.getAuthType() != AuthTypeEnum.MENU_TYPE.getValue()) {
                    throw new BizException("父节点必须是菜单");
                }
            }

            authService.addAuth(authVo);
            operateLogService.logAdd("添加权限[" + authVo.getName() + "]", userModel);
            return RestResult.success("添加权限成功");
        } catch (BizException e) {
            return RestResult.error(e.getMsg());
        } catch (Exception e) {
            logger.error("添加权限时出现异常", e);
            return RestResult.error("添加权限失败，系统异常");
        }
    }

    /**
     * 保存要修改的功能.
     *
     * @return .
     */
    @Permission("sys:auth:edit")
    @RequestMapping("editAuth")
    public RestResult<String> editAuth(@RequestBody AuthVo authDto, @CurrentUser UserModel userModel) {
        try {
            AuthVo current = authService.getAuthById(authDto.getId());
            if(current == null){
                return RestResult.error("权限不存在！");
            }

            authService.editAuth(authDto);
            // 记录系统操作日志
            operateLogService.logEdit("修改权限，权限名称[" + authDto.getName() + "]", userModel);
            return RestResult.success("修改成功");
        } catch (BizException e) {
            return RestResult.error("修改失败,"+e.getMsg());
        } catch (Exception e) {
            // 记录系统操作日志
            logger.error("修改权限出现异常", e);
            operateLogService.logEdit("修改权限出现异常，权限名称[" + authDto.getName() + "]", userModel);
            return RestResult.error("修改失败，系统异常");
        }
    }

    /**
     * 删除功能.
     * @return .
     */
    @Permission("sys:auth:delete")
    @RequestMapping("deleteAuth")
    public RestResult<String> deleteAuth(@RequestParam("authId") Long authId, @CurrentUser UserModel userModel) {
        String authName = null;
        try {
            AuthVo authVo = null;
            if (authId == null || authId == 0 || (authVo = authService.getAuthById(authId)) == null) {
                return RestResult.error("无法获取要删除的数据");
            }

            authName = authVo.getName();

            // 先判断此菜单下是否有子菜单
            List<AuthVo> childMenuList = authService.listAuthByParentId(authId, userModel.getMchNo());
            if(! childMenuList.isEmpty()){
                childMenuList.stream()
                        .filter(p -> p.getAuthType() == AuthTypeEnum.MENU_TYPE.getValue())
                        .collect(Collectors.toList());
                if (! childMenuList.isEmpty()) {
                    return RestResult.error("此权限下关联有【" + childMenuList.size() + "】个子菜单，不能支接删除!");
                }
            }

            // 删除掉权限以及其子权限（该方法会同时删除与该权限关联的roleAuth）
            authService.deleteAuthAndRelated(authId);

            // 记录系统操作日志
            operateLogService.logDelete("删除权限，权限名称[" + authVo.getName() + "]", userModel);
            return RestResult.success("删除成功");
        } catch (BizException e) {
            return RestResult.error("删除失败," + e.getMsg());
        } catch (Exception e) {
            // 记录系统操作日志
            logger.error("删除权限出现异常", e);
            operateLogService.logDelete("删除权限出现异常，功能名称[" + authName + "]", userModel);
            return RestResult.error("删除权限出错，系统异常");
        }
    }

    /**
     * 从根节点开始构建权限功能树
     * @param root      根节点
     * @param mapByPid  根据父节点聚集map
     */
    private AuthNodeVo buildAuthNode(AuthVo root, Map<Long, List<AuthVo>> mapByPid) {
        AuthNodeVo authNode = copyProperties(root);

        if (AuthTypeEnum.MENU_TYPE.getValue() == authNode.getAuthType() && mapByPid.get(root.getId()) != null) {
            mapByPid.get(root.getId()).forEach(pmsAuth -> {
                AuthNodeVo children = buildAuthNode(pmsAuth, mapByPid);
                if (AuthTypeEnum.ACTION_TYPE.getValue() == pmsAuth.getAuthType()) {
                    authNode.getActionChildren().add(children);
                } else {
                    authNode.getChildren().add(children);
                }
            });
        }
        return authNode;
    }

    private AuthNodeVo copyProperties(AuthVo authDto) {
        AuthNodeVo authNode = BeanUtil.newAndCopy(authDto, AuthNodeVo.class);
        authNode.setChildren(new ArrayList<>());
        authNode.setActionChildren(new ArrayList<>());
        return authNode;
    }
}
