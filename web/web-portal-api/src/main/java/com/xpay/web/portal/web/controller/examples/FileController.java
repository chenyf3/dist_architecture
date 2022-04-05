package com.xpay.web.portal.web.controller.examples;

import com.xpay.common.statics.annotations.Permission;
import com.xpay.common.statics.exception.BizException;
import com.xpay.common.statics.result.RestResult;
import com.xpay.common.utils.*;
import com.xpay.starter.plugin.client.ObjectStorage;
import com.xpay.web.api.common.annotations.CurrentUser;
import com.xpay.web.api.common.ddo.vo.ImageVo;
import com.xpay.web.api.common.model.UserModel;
import com.xpay.web.api.common.service.CryptService;
import com.xpay.web.portal.web.controller.BaseController;
import com.xpay.web.portal.web.vo.examples.ImageRecordVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/examples/file")
public class FileController extends BaseController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    CryptService cryptService;
    @Autowired(required = false)
    ObjectStorage objectStorage;

    private String bucketName = "my-test-bucket3";//需要先再oss上创建bucket存储空间
    //保存上传的文件信息，用以模拟保存到数据库中的记录
    private AtomicLong idGen = new AtomicLong(0);
    private Map<String, ImageRecordVo> recordVoMap = new HashMap<>();

    @Permission("examples:file:operate")
    @RequestMapping("getImageRecord")
    public RestResult<List<ImageRecordVo>> getImageRecord() {
        //此处模拟从后端服务拿取到图片记录
        List<ImageRecordVo> recordVoList = BeanUtil.newAndCopy(Arrays.asList(recordVoMap.values().toArray()), ImageRecordVo.class);
        recordVoList.forEach(record -> {
            ImageVo imageVo = new ImageVo(record.getImageName(), DateUtil.addMinute(new Date(), 1).getTime());
            String imageName = JsonUtil.toJson(imageVo);
            imageName = cryptService.encryptForWeb(imageName, null);
            imageName = CodeUtil.urlEncode(imageName);//使用url编码，以方便前端使用get请求
            record.setImageName(imageName);
        });
        return RestResult.success(recordVoList);
    }

    /**
     * 文件上传
     * @return
     */
    @Permission("examples:file:upload")
    @PostMapping("uploadImage")
    public RestResult<String> uploadImage(@RequestParam("files") List<MultipartFile> files,
                                                @CurrentUser UserModel userModel){
        for (MultipartFile file : files) {
            try {
                String fileName = objectStorage.putObject(bucketName, file.getOriginalFilename(), file.getBytes(), file.getContentType());
                if (StringUtil.isEmpty(fileName)) {
                    return RestResult.error("文件上传失败," + file.getOriginalFilename());
                }

                //模拟保存到数据库中
                ImageRecordVo record = new ImageRecordVo();
                record.setId(idGen.incrementAndGet());
                record.setCreateTime(new Date());
                record.setCreator(userModel.getLoginName());
                record.setImageName(fileName);
                record.setOriName(file.getOriginalFilename());
                recordVoMap.put(fileName, record);
            } catch (Exception e) {
                logger.error("文件上传异常 {}", file.getOriginalFilename(), e);
                return RestResult.error("文件上传异常," + file.getOriginalFilename());
            }
        }
        String msg = this.recordVoMap.size() == files.size() ? "上传成功" : "部分失败";
        return RestResult.success(msg);
    }

    @Permission("examples:file:delete")
    @GetMapping("deleteImage")
    public RestResult<String> deleteImage(@RequestParam("fileNameEnc") String fileNameEnc){
        if(StringUtil.isEmpty(fileNameEnc)){
            throw new BizException(BizException.PARAM_INVALID, "文件名不能为空");
        }

        String fileNameDec = cryptService.decryptForWeb(fileNameEnc, null);
        ImageVo imageVo = JsonUtil.toBean(fileNameDec, ImageVo.class);
        Long expire = imageVo.getExpire();
        if (expire < System.currentTimeMillis()) {
            throw new BizException(BizException.PARAM_INVALID, "文件地址已过期");
        }

        boolean isSuccess = objectStorage.removeObject(imageVo.getName());
        if(isSuccess) {
            recordVoMap.remove(imageVo.getName());
        }
        return isSuccess ? RestResult.success("删除成功") : RestResult.error("删除失败");
    }
}
