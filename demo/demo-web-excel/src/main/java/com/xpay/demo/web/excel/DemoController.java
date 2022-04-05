package com.xpay.demo.web.excel;

import com.xpay.common.utils.JsonUtil;
import com.xpay.common.utils.RandomUtil;
import com.xpay.demo.web.excel.utils.ExcelUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/demo")
public class DemoController {

    /**
     * 文件下载
     * @param response
     */
    @GetMapping("download")
    public void download(HttpServletResponse response){
        int max = 1000;
        List<DataModel> dataList = new ArrayList<>();
        for(int i=0; i<1000; i++){
            DataModel data = new DataModel();
            data.setAddress("广州市天河区" +i);
            data.setAge(RandomUtil.getInt(1, 110));
            data.setName("名字" + RandomUtil.getInt(10, 1000));
            data.setEmail("123xxx_" + i + "@123.com");
            data.setHeight(RandomUtil.getInt(160, 200));
            data.setSex(RandomUtil.getInt(1, 2));
            data.setSalary(RandomUtil.getInt(max, max*5)/(i+1D));
            dataList.add(data);
        }
        ExcelUtil.write(response, "Sheet1", dataList,  "excelData");
    }

    /**
     * 文件下载
     * @param response
     */
    @GetMapping("download2")
    public void download2(HttpServletResponse response){
        List<String> head = new ArrayList<>();
        head.add("地址");
        head.add("姓名");
        head.add("年龄");
        head.add("邮箱");
        head.add("身高");
        head.add("性别");
        head.add("薪资");

        int max = 1000;
        List<List<String>> dataList = new ArrayList<>();
        for(int i=0; i<1000; i++){
            List<String> row = new ArrayList<>();
            row.add("广州市天河区" +i);
            row.add("名字" + RandomUtil.getInt(10, 1000));
            row.add("" + RandomUtil.getInt(1, 110));
            row.add("123energy_" + i + "@123.com");
            row.add("" + RandomUtil.getInt(160, 200));
            row.add("" + RandomUtil.getInt(1, 2));
            row.add("" + RandomUtil.getInt(max, max*5)/(i+1D));
            dataList.add(row);
        }
        ExcelUtil.write(response, "Sheet1", head, dataList,  "excelData_2");
    }

    @ResponseBody
    @GetMapping("upload")
    public ModelAndView upload(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("upload");
        return mv;
    }

    @ResponseBody
    @PostMapping("doUpload")
    public String doUpload(@RequestParam("uploadFile") MultipartFile file){
        List<DataModel> dataList = ExcelUtil.read(file, DataModel.class);
        System.out.println("dataList.size = " + dataList.size() + ",dataList = " + JsonUtil.toJson(dataList));
        return "ok";
    }

    @ResponseBody
    @GetMapping("mutUpload")
    public ModelAndView mutUpload(){
        ModelAndView mv = new ModelAndView();
        mv.setViewName("mutUpload");
        return mv;
    }

    @ResponseBody
    @PostMapping("doMutUpload")
    public String doMutUpload(@RequestParam("file_1") MultipartFile file_1,
                              @RequestParam("file_2") MultipartFile file_2){
        List<DataModel> dataList_1 = ExcelUtil.read(file_1, DataModel.class);
        System.out.println("dataList_1.size = " + dataList_1.size() + ",dataList_1 = " + JsonUtil.toJson(dataList_1));

        List<DataModel> dataList_2 = ExcelUtil.read(file_2, DataModel.class);
        System.out.println("dataList_2.size = " + dataList_1.size() + ",dataList_2 = " + JsonUtil.toJson(dataList_2));
        return "ok";
    }
}
