package com.xpay.sdktest;

import com.xpay.sdk.api.entity.Request;
import com.xpay.sdk.api.entity.Response;
import com.xpay.sdk.api.entity.SecretKey;
import com.xpay.sdk.api.enums.SignType;
import com.xpay.sdk.api.utils.*;

import java.math.BigDecimal;

public class TestFlowLimit {
//    public static String PATH = "/backend-mvc";
//    public static String PATH = "/backend-flux";
    public static String PATH = "/backend-nosen";

    public static void main(String[] args){
        final SecretKey key = new SecretKey();
        key.setMchPriKey(Keys.mchPrivateKey);//商户私钥
        key.setPlatPubKey(Keys.platPublicKey);//平台公钥

        int max = 10000000;
        for(int i =0; i<max; i++){
            jsonRequest(key);
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
        request.setMethod("demo.single");//demo.single、demo.flowLimit
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

            System.out.println("接收到响应数据 Response = " + JsonUtil.toJson(response));
        }catch(Throwable e){
            e.printStackTrace();
        }
    }
}
