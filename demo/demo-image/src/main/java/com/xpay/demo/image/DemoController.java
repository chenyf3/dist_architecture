package com.xpay.demo.image;

import com.xpay.common.utils.ImageUtil;
import com.xpay.common.utils.StringUtil;
import com.xpay.starter.plugin.client.FastdfsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Controller
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    FastdfsClient fastdfsClient;

    /**
     * 去展示图片压缩的页面
     * @return
     */
    @ResponseBody
    @GetMapping("toCompress")
    public ModelAndView toCompress(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("toCompress");
        return mv;
    }

    /**
     * 图片上传后压缩
     * @param file
     * @param scale
     * @param quality
     * @return
     */
    @ResponseBody
    @PostMapping("doCompress")
    public ModelAndView doCompress(@RequestParam("uploadFile") MultipartFile file,
                                   @RequestParam("scale") Double scale,
                                   @RequestParam("quality") Double quality){
        try{
            InputStream inputStream = file.getInputStream();
            int oldFileBytes = inputStream.available();

            byte[] newFileBytes = ImageUtil.compress(inputStream, scale, quality);

            System.out.println("oldFileBytes="+oldFileBytes+", newFileBytes.length = " + newFileBytes.length);
//            saveToLocalDisk(newFileBytes, "NEW_" + file.getOriginalFilename());
            String picName = saveToImageServer(newFileBytes, file.getOriginalFilename());
            return viewPic(picName);
        }catch(Exception e){
            e.printStackTrace();
            return new ModelAndView();
        }
    }

    @ResponseBody
    @GetMapping("toCompress2")
    public ModelAndView toCompress2(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("toCompress2");
        return mv;
    }

    @ResponseBody
    @PostMapping("doCompress2")
    public ModelAndView doCompress2(@RequestParam("uploadFile") MultipartFile file,
                                   @RequestParam("scale") Double scale,
                                   @RequestParam("quality") Double quality){
        try{
            InputStream inputStream = file.getInputStream();
            byte[] sourceBytes = new byte[inputStream.available()];
            inputStream.read(sourceBytes);

            //压缩一次
            ByteArrayInputStream sourceInS = new ByteArrayInputStream(sourceBytes);
            BufferedImage image1 = ImageIO.read(sourceInS);
            byte[] newFileBytes = ImageUtil.compress(image1, scale, quality);
            String picName1 = saveToImageServer(newFileBytes, file.getOriginalFilename());

            //进一步压缩，可用在缩略图之类的
            ByteArrayInputStream bis = new ByteArrayInputStream(sourceBytes);
            BufferedImage image2 = ImageIO.read(bis);
            byte[] miniFileBytes = ImageUtil.compress(image2, scale * 0.6, quality);
            String picName2 = saveToImageServer(miniFileBytes, file.getOriginalFilename());
            return viewPic(picName2);
        }catch(Exception e){
            e.printStackTrace();
            return new ModelAndView();
        }
    }

    /**
     * 去重新调整图片尺寸的页面(缩略图页面)
     * @return
     */
    @ResponseBody
    @GetMapping("toResize")
    public ModelAndView toResize(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("toResize");
        return mv;
    }

    /**
     * 图片上传后重新调整尺寸大小
     * @param file
     * @param width
     * @param height
     * @param keepRatio
     * @return
     */
    @ResponseBody
    @PostMapping("doResize")
    public ModelAndView doResize(@RequestParam("uploadFile") MultipartFile file,
                           @RequestParam("width")Integer width,
                           @RequestParam("height")Integer height,
                           @RequestParam("keepRatio")Integer keepRatio){
        try{
            boolean isKeepRatio = keepRatio != null && keepRatio == 1 ? true : false;
            FileInputStream inputStream = (FileInputStream) file.getInputStream();
            int oldFileBytes = inputStream.available();

            byte[] newFileBytes = ImageUtil.resize(inputStream, width, height, isKeepRatio, 0.5);

            System.out.println("oldFileBytes="+oldFileBytes+", newFileBytes.length = " + newFileBytes.length );
//            saveToLocalDisk(newFileBytes, "NEW_" + file.getOriginalFilename());
            String picName = saveToImageServer(newFileBytes, file.getOriginalFilename());
            return viewPic(picName);
        }catch(Exception e){
            e.printStackTrace();
            return new ModelAndView();
        }
    }

    /**
     * 去图片旋转的页面
     * @return
     */
    @ResponseBody
    @GetMapping("toRotate")
    public ModelAndView toRotate(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("toRotate");
        return mv;
    }

    /**
     * 上传后执行图片旋转
     * @param file
     * @param angle
     * @param width
     * @param height
     * @param quality
     * @return
     */
    @ResponseBody
    @PostMapping("doRotate")
    public ModelAndView doRotate(@RequestParam("uploadFile") MultipartFile file,
                                 @RequestParam("angle") Integer angle,
                                 @RequestParam("width")Integer width,
                                 @RequestParam("height")Integer height,
                                 @RequestParam("quality") Double quality){
        try{
            FileInputStream inputStream = (FileInputStream) file.getInputStream();
            int oldFileBytes = inputStream.available();

            byte[] newFileBytes = ImageUtil.rotate(inputStream, width, height, true, angle, quality);

            System.out.println("oldFileBytes="+oldFileBytes+", newFileBytes.length = " + newFileBytes.length);
//            saveToLocalDisk(newFileBytes, "NEW_" + file.getOriginalFilename());
            String picName = saveToImageServer(newFileBytes, file.getOriginalFilename());
            return viewPic(picName);
        }catch(Exception e){
            e.printStackTrace();
            return new ModelAndView();
        }
    }

    @ResponseBody
    @GetMapping("toOriRotate")
    public ModelAndView toOriRotate(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("toOriRotate");
        return mv;
    }

    @ResponseBody
    @PostMapping("doOriRotate")
    public ModelAndView doOriRotate(@RequestParam("uploadFile") MultipartFile file,
                                 @RequestParam("angle") Integer angle,
                                 @RequestParam("quality") Double quality){
        try{
            FileInputStream inputStream = (FileInputStream) file.getInputStream();
            int oldFileBytes = inputStream.available();

            byte[] newFileBytes = ImageUtil.rotate(inputStream, 0.8, angle, quality);

            System.out.println("oldFileBytes="+oldFileBytes+", newFileBytes.length = " + newFileBytes.length);
//            saveToLocalDisk(newFileBytes, "NEW_" + file.getOriginalFilename());
            String picName = saveToImageServer(newFileBytes, file.getOriginalFilename());
            return viewPic(picName);
        }catch(Exception e){
            e.printStackTrace();
            return new ModelAndView();
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

    private String saveToImageServer(byte[] imageBytes, String oriFileName){
        return fastdfsClient.uploadFile(imageBytes, oriFileName);
    }

    private void saveToLocalDisk(byte[] imageBytes, String fileName){
        FileImageOutputStream outputStream = null;
        try{
            outputStream = new FileImageOutputStream(new File(fileName));
            outputStream.write(imageBytes);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try{
                if(outputStream != null){
                    outputStream.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
