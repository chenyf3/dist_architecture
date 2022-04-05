package com.xpay.demo.fasfdfs;

import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.libs.fdfs.fasfdfs.FileInfo;
import com.xpay.starter.plugin.client.FastdfsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    FastdfsClient fastdfsClient;

    @GetMapping("upload")
    public ModelAndView upload(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("upload");
        return mv;
    }

    @PostMapping("doUpload")
    public String doUpload(@RequestParam("uploadFile") MultipartFile file) {
        if(file == null){
            throw new RuntimeException("上传的文件为空");
        }

        try{
            String fileName = fastdfsClient.uploadFile(file.getBytes(), file.getOriginalFilename());
            return fileName;
        }catch(Exception e){
            e.printStackTrace();
            return "error";
        }
    }

    @RequestMapping("download")
    public void download(String fileName, HttpServletRequest request, HttpServletResponse response) {
        if(StringUtil.isEmpty(fileName)){
            throw new RuntimeException("文件名称不能为空");
        }

        try{
            byte[] buffer = fastdfsClient.downloadAsByte(fileName);
            response.setContentType(request.getContentType());
            response.getOutputStream().write(buffer);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping("viewPic")
    public ModelAndView viewPic(String picName) {
        if(StringUtil.isEmpty(picName)){
            throw new RuntimeException("图片名称不能为空");
        }

        ModelAndView mv = new ModelAndView();
        mv.setViewName("viewPic");
        mv.addObject("picName", picName);
        return mv;
    }

    /**
     * 测试页面上<img></img>方式访问图片
     * @return
     */
    @GetMapping("getPic")
    public void getPic(String picName, HttpServletResponse response) {
        if(StringUtil.isEmpty(picName)){
            throw new RuntimeException("图片名称不能为空");
        }else if(picName.lastIndexOf(".") < 0){
            throw new RuntimeException("图片格式不正确");
        }

        try{
            byte[] buffer = fastdfsClient.downloadAsByte(picName);
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("image/jpeg/jpg/png/gif/bmp/tiff/svg");
            response.getOutputStream().write(buffer);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping("getFileInfo")
    public String getFileInfo(String fileName){
        if(StringUtil.isEmpty(fileName)){
            throw new RuntimeException("文件名称不能为空");
        }

        try{
            FileInfo fileInfo = fastdfsClient.queryFile(fileName);
            return JsonUtil.toJson(fileInfo);
        }catch(Exception e){
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
            boolean isSuccess = fastdfsClient.deleteFile(fileName);
            return isSuccess ? "ok" : "fail";
        }catch(Exception e){
            e.printStackTrace();
            return "error";
        }
    }
}
