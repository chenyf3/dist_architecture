package com.xpay.demo.lo4j2;

import com.xpay.common.statics.dto.mq.MsgDto;
import com.xpay.common.utils.JsonUtil;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/log")
public class HttpLogController {

    /**
     * 接收远程日志请求(JSON格式)
     * @return
     */
    @RequestMapping("receiveJson")
    public String receiveJson(HttpServletRequest request){
        String body = readBody(request);

        Map<String, String> map = JsonUtil.toBean(body, HashMap.class);
        String payload = map.getOrDefault("message", null);

        System.out.println("receiveJson loggerName: " + map.getOrDefault("loggerName", null) + ", body: " + body);
        return "success";
    }

    /**
     * 接收远程日志请求(Text文本)
     * @param request
     * @return
     */
    @RequestMapping("receiveText")
    public String receiveText(HttpServletRequest request){
        String body = readBody(request);
        System.out.println("receiveText body: " + body);
        return "success";
    }


    private String readBody(HttpServletRequest request){
        try {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }
}
