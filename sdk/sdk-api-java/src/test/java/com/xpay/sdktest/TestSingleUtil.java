package com.xpay.sdktest;

import com.xpay.sdk.api.entity.Request;
import com.xpay.sdk.api.entity.Response;
import com.xpay.sdk.api.entity.SecretKey;
import com.xpay.sdk.api.enums.OrderStatus;
import com.xpay.sdk.api.enums.RespCode;
import com.xpay.sdk.api.enums.SignType;
import com.xpay.sdk.api.utils.*;

import java.math.BigDecimal;
import java.util.*;

public class TestSingleUtil {
    public static String PATH = "/backend-flux";


    public static void main(String[] args){
        final SecretKey key = new SecretKey();
        key.setMchPriKey(Keys.mchPrivateKey);//商户私钥
        key.setPlatPubKey(Keys.platPublicKey);//平台公钥

        int rand = RandomUtil.getInt(1, 10);
        if(rand % 2 == 0){
            jsonRequest(key);
        }else{
            formRequest(key);
        }
    }

    private static void jsonRequest(SecretKey key){
        String secKey = RandomUtil.get16LenStr();
        String iv = RandomUtil.get16LenStr();

        SingleVo vo = new SingleVo();
        vo.setProductAmount(BigDecimal.valueOf(20.01));
        vo.setCount(String.valueOf(RandomUtil.getInt(3, 20)));
        vo.setProductName(AESUtil.encryptCBC("xx商品123", secKey, iv));//加密

        Request request = new Request();
        request.setMchNo(Keys.mchNo);
        request.setMethod("demo.single");
        request.setVersion("1.0");
        request.setSignType(SignType.RSA.getValue());
        request.setTimestamp(String.valueOf(System.currentTimeMillis()));
        request.setData(vo);
        request.joinSecKey(secKey, iv);

        final String url = "127.0.0.1:8099" + PATH;
        try{
            Response response = RequestUtil.doJsonRequest(url, request, key);
            if(response.getData() == null){
                System.out.println("响应数据的data为空 Response = " + JsonUtil.toJson(response));
                return;
            }

            Map<String, Object> respData = JsonUtil.toBean(response.getData().toString(), HashMap.class);
            if(RespCode.SUCCESS.getCode().equals(response.getRespCode())
                    && OrderStatus.SUCCESS.getStatus().equals(respData.get("orderStatus"))){
                //交易成功
                SingleVo respVo = JsonUtil.toBean(response.getData().toString(), SingleVo.class);
                if(StringUtil.isNotEmpty(response.getSecKey())){
                    String[] secKeyArr = response.getSecKey().split(":");
                    secKey = secKeyArr[0];
                    iv = secKeyArr[1];
                    respVo.setProductName(AESUtil.decryptCBC(respVo.getProductName(), secKey, iv));
                }
                System.out.println("交易成功 Response.SingleVo = " + JsonUtil.toJson(respVo) + " Response = " + JsonUtil.toJson(response));
            }else{
                System.out.println("交易未完成 Response = " + JsonUtil.toJson(response));
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }

    private static void formRequest(SecretKey key){
        String secKey = RandomUtil.get16LenStr();
        String iv = RandomUtil.get16LenStr();

        SingleVo vo = new SingleVo();
        vo.setProductAmount(BigDecimal.valueOf(20.01));
        vo.setCount("13");
        vo.setProductName(AESUtil.encryptCBC("xx商品123", secKey, iv));

        Map<String, String> param = new HashMap<>();
        param.put("mchNo", Keys.mchNo);
        param.put("method", "demo.singleForm");
        param.put("version", "1.0");
        param.put("signType", SignType.RSA.getValue());
        param.put("data", JsonUtil.toJson(vo));
        param.put("secKey", secKey + ":" + iv);
        param.put("timestamp", String.valueOf(System.currentTimeMillis()));

        final String url = "127.0.0.1:8099/backend-flux";
        try{
            Response response = RequestUtil.doFormRequest(url, param, key);
            if(response.getData() == null){
                System.out.println("响应数据的data为空 Response = " + JsonUtil.toJson(response));
                return;
            }

            Map<String, Object> respData = JsonUtil.toBean(response.getData().toString(), HashMap.class);
            if(RespCode.SUCCESS.getCode().equals(response.getRespCode())
                    && OrderStatus.SUCCESS.getStatus().equals(respData.get("orderStatus"))){
                //交易成功
                SingleVo respVo = JsonUtil.toBean(response.getData().toString(), SingleVo.class);
                if(StringUtil.isNotEmpty(response.getSecKey())){
                    String[] secKeyArr = response.getSecKey().split(":");
                    secKey = secKeyArr[0];
                    iv = secKeyArr[1];
                    respVo.setProductName(AESUtil.decryptCBC(respVo.getProductName(), secKey, iv));
                }
                System.out.println("交易成功 SingleVo = " + JsonUtil.toJson(respVo) + " Response = " + JsonUtil.toJson(response));
            }else{
                System.out.println("交易未完成 Response = " + JsonUtil.toJson(response));
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }
}
