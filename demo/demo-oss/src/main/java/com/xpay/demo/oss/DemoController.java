package com.xpay.demo.oss;

import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.starter.plugin.client.OSSClient;
import com.xpay.starter.plugin.client.ObjectStorage;
import com.xpay.starter.plugin.ddo.BucketInfo;
import com.xpay.starter.plugin.ddo.ObjectInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/demo")
public class DemoController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    @Qualifier(value = "ossStorage")
    ObjectStorage objectStorage;

    @GetMapping("upload")
    public ModelAndView upload(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("upload");
        return mv;
    }

    /**
     * 文件上传，已测试通过得文件类型有：rar、zip、pdf、jpg、jpeg、png、xls、xlsx、txt
     * @param file
     * @return
     */
    @PostMapping("doUpload")
    public String doUpload(@RequestParam("bucketName") String bucketName, @RequestParam("uploadFile") MultipartFile file) {
        if(file == null){
            throw new RuntimeException("上传的文件为空");
        }

        String contentType = file.getContentType();
        String oriFileName = file.getOriginalFilename();
        String objectName;
        try {
            objectName = objectStorage.putObject(bucketName, oriFileName, file.getBytes(), contentType);
            logger.info("文件上传完毕 fileSize={} objectName={}", file.getBytes().length, objectName);
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }

        return objectName;
    }

    @GetMapping("upload2")
    public ModelAndView upload2(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("upload2");
        return mv;
    }

    /**
     * 上传一个小文件，测试一个bucket下能放多少个object
     * @param bucketName
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("doUpload2")
    public String doUpload2(@RequestParam("bucketName") String bucketName, @RequestParam("uploadFile") MultipartFile file) throws Exception {
        if(file == null){
            throw new RuntimeException("上传的文件为空");
        }

        long start = System.currentTimeMillis();
        int success = 0, fail = 0, current = 0, max = 100000;
        int len = String.valueOf(max).length();
        String format = "%1$0" + len + "d";
        String contentType = file.getContentType();
        String oriFileName = file.getOriginalFilename();
        byte[] data = file.getBytes();
        while (current++ <= max) {
            try {
                String objectName = String.format(format, current) + "_" + oriFileName;
                String objectFullName = objectStorage.getObjectFullName(objectName, objectName);
                objectStorage.putObject(bucketName, objectFullName, data, contentType, null, null);
                success ++;
            } catch(Exception e) {
                fail ++;
                e.printStackTrace();
            }
        }
        int cost = (int)((System.currentTimeMillis()-start) / 1000);
        return "success=" + success + ",fail=" + fail + ", cost=" + cost;
    }

    @GetMapping("viewPic")
    public ModelAndView viewPic() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("viewPic");
        return mv;
    }

    /**
     * 测试页面上<img></img>方式访问图片
     * @return
     */
    @GetMapping("getPic")
    public void getPic(String fileName, HttpServletResponse response) {
        if(StringUtil.isEmpty(fileName)){
            throw new RuntimeException("图片名称不能为空");
        }else if(fileName.lastIndexOf(".") < 0){
            throw new RuntimeException("图片格式不正确");
        }

        try{
            byte[] buffer = objectStorage.getObject(fileName);
            logger.info("文件获取完毕 fileSize={} objectName={}", buffer.length, fileName);

            ObjectInfo objectInfo = objectStorage.getObjectInfo(fileName, null);
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setDateHeader("Expires", 0);
            response.setContentType(objectInfo.getContentType());
            response.getOutputStream().write(buffer);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping("viewPic2")
    public ModelAndView viewPic2() {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("viewPic2");
        return mv;
    }

    /**
     * 分页获取图片，用以测试分段获取是否可行
     * @param fileName
     * @param response
     */
    @GetMapping("getPicPage")
    public void getPicPage(String fileName, HttpServletResponse response) {
        if(StringUtil.isEmpty(fileName)){
            throw new RuntimeException("图片名称不能为空");
        }else if(fileName.lastIndexOf(".") < 0){
            throw new RuntimeException("图片格式不正确");
        }

        int offset = 0;
        int pageSize = 10240;//每次分页获取的字节数，10KB
        byte[] allBuffer = null;
        boolean isGoOn = true;
        while (isGoOn) {
            byte[] buffer = objectStorage.getObject(fileName, offset, pageSize);
            System.out.println("buffer.length = " + buffer.length);
            offset += buffer.length;
            allBuffer = ArrayUtils.addAll(allBuffer, buffer);
            if(buffer.length < pageSize){
                isGoOn = false;
            }
        }

        ObjectInfo objectInfo = objectStorage.getObjectInfo(fileName, null);

        try{
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            response.setContentType(objectInfo.getContentType());
            response.setCharacterEncoding("utf-8");
            response.getOutputStream().write(allBuffer);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("getFileInfo")
    public String getFileInfo(String fileName){
        if(StringUtil.isEmpty(fileName)){
            throw new RuntimeException("文件名称不能为空");
        }

        try {
            ObjectInfo objectInfo = objectStorage.getObjectInfo(fileName, null);
            return JsonUtil.toJson(objectInfo);
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @RequestMapping("delete")
    public String delete(String fileName){
        if(StringUtil.isEmpty(fileName)){
            throw new RuntimeException("文件名称不能为空");
        }
        try{
            boolean isSuccess = objectStorage.removeObject(fileName);
            return isSuccess ? "ok" : "fail";
        }catch(Exception e){
            e.printStackTrace();
            return "error";
        }
    }

    @RequestMapping("deleteBatch")
    public String deleteBatch(String fileName){
        if(StringUtil.isEmpty(fileName)){
            throw new RuntimeException("文件名称不能为空");
        }

        List<String> fileNameList = Arrays.asList(fileName.split(","));
        try {
            boolean isSuccess = objectStorage.removeObject(fileNameList);
            return isSuccess ? "ok" : "fail";
        } catch(Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    @RequestMapping("getObjUrl")
    public String getObjUrl(String fileName){
        return objectStorage.getObjectURL(fileName, 2 * 60);
    }

    @RequestMapping("listBucketNames")
    public List<String> listBucketNames(){
        return objectStorage.listBucketNames();
    }

    @RequestMapping("listBuckets")
    public List<BucketInfo> listBuckets(){
        return objectStorage.listBuckets();
    }

    @RequestMapping("listObjectNames")
    public List<String> listObjectNames(String bucketName){
        return objectStorage.listObjectNames(bucketName);
    }

    @RequestMapping("hasObject")
    public boolean hasObject(String bucketName){
        return objectStorage.hasObject(bucketName);
    }

    @RequestMapping("removeBucket")
    public boolean removeBucket(String bucketName){
        return objectStorage.removeBucket(bucketName);
    }
}
