package com.xpay.web.api.common.controller;

import com.xpay.common.statics.enums.product.ProductCodeEnum;
import com.xpay.common.statics.enums.product.ProductTypeEnum;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.CodeUtil;
import com.xpay.common.utils.DateUtil;
import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.starter.plugin.client.ObjectStorage;
import com.xpay.starter.plugin.ddo.ObjectInfo;
import com.xpay.web.api.common.ddo.vo.ImageVo;
import com.xpay.web.api.common.service.CryptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 已注册用户的公用控制器，即需要用户登录后才能使用的，未登录的用户不可使用
 */
@RestController
@RequestMapping("signInPublic")
public class SignInPublicController {
    @Autowired
    CryptService cryptService;
    //使用minio、阿里云oss作为文件服务器时使用
    @Autowired(required = false)
    @Qualifier(value = "ossStorage")
    ObjectStorage objectStorage;

    /**
     * 获取产品相关信息
     * @return
     */
    @RequestMapping("getProductInfo")
    public RestResult<Map<String, Object>> getProductInfo(){
        Map<String, Object> productInfoMap = new HashMap<>();
        productInfoMap.put("productTypeMap", ProductTypeEnum.toMap());
        productInfoMap.put("productCodeMap", ProductCodeEnum.toTypeMap());
        return RestResult.success(productInfoMap);
    }

    /**
     * 下载文件
     * @param fileNameEnc   经过加密的文件名，前端发送请求时需要经过urlencode
     * @param response
     */
    @GetMapping("download")
    public void download(@RequestParam String fileNameEnc, HttpServletResponse response) {
        ImageVo imageVo = checkAndTransferImageVo(fileNameEnc);
        ObjectInfo objectInfo = objectStorage.getObjectInfo(imageVo.getName(), imageVo.getVersion());
        if(objectInfo == null) {
            throw new BizException(BizException.BIZ_INVALID, "文件内容不存在");
        }

        try {
            byte[] buffer = objectStorage.getObject(imageVo.getName());
            String fileName = DateUtil.formatCompactDateTime(new Date()) + objectInfo.getSuffix();
            response.setContentType(objectInfo.getContentType());
            response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));//文件名能显示中文
            response.getOutputStream().write(buffer);
        } catch(Exception e) {
            throw new BizException(BizException.BIZ_INVALID, "获取文件内容异常", e);
        }
    }

    /**
     * 在浏览器上通过 <img></img> 标签访问加密过的图片
     * @param picName   经过加密的文件名，前端发送请求时需要经过urlencode
     * @param response
     */
    @GetMapping("viewPicEnc")
    public void viewPicEnc(@RequestParam String picName, HttpServletResponse response){
        ImageVo imageVo = checkAndTransferImageVo(picName);
        ObjectInfo objectInfo = objectStorage.getObjectInfo(imageVo.getName(), imageVo.getVersion());
        if(objectInfo == null) {
            throw new BizException(BizException.BIZ_INVALID, "文件内容不存在");
        }

        try {
            byte[] buffer = objectStorage.getObject(imageVo.getName());
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType(objectInfo.getContentType());
            response.getOutputStream().write(buffer);
        } catch(Exception e) {
            throw new BizException(BizException.BIZ_INVALID, "获取图片内容异常", e);
        }
    }

    /**
     * 在浏览器上通过 <img></img> 标签访问图片
     * @param picName   文件名
     * @param response
     */
    @GetMapping("viewPic")
    public void viewPic(@RequestParam String picName, HttpServletResponse response){
        if (StringUtil.isEmpty(picName)) {
            throw new BizException(BizException.BIZ_INVALID, "文件名不能为空");
        }

        ObjectInfo objectInfo = objectStorage.getObjectInfo(picName, null);
        if(objectInfo == null) {
            throw new BizException(BizException.BIZ_INVALID, "文件内容不存在");
        }
        try {
            byte[] buffer = objectStorage.getObject(picName);
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType(objectInfo.getContentType());
            response.getOutputStream().write(buffer);
        } catch(Exception e) {
            throw new BizException(BizException.BIZ_INVALID, "获取图片内容异常", e);
        }
    }

    private ImageVo checkAndTransferImageVo(String fileNameEnc){
        if(objectStorage == null) {
            throw new BizException(BizException.BIZ_INVALID, "当前系统配置不支持图片获取");
        }else if(StringUtil.isEmpty(fileNameEnc)){
            throw new BizException(BizException.PARAM_INVALID, "文件名不能为空");
        }

        String fileNameDec = cryptService.decryptForWeb(fileNameEnc, null);
        ImageVo imageVo = JsonUtil.toBean(fileNameDec, ImageVo.class);
        Long expire = imageVo.getExpire();
        if (expire < System.currentTimeMillis()) {
            throw new BizException(BizException.PARAM_INVALID, "文件地址已过期");
        }
        return imageVo;
    }
}
