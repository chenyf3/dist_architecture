package com.xpay.sdktest;

import com.xpay.sdk.api.entity.Request;
import com.xpay.sdk.api.entity.Response;
import com.xpay.sdk.api.entity.SecretKey;
import com.xpay.sdk.api.enums.OrderStatus;
import com.xpay.sdk.api.enums.RespCode;
import com.xpay.sdk.api.enums.SignType;
import com.xpay.sdk.api.utils.AESUtil;
import com.xpay.sdk.api.utils.JsonUtil;
import com.xpay.sdk.api.utils.RandomUtil;
import com.xpay.sdk.api.utils.RequestUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TestBatchUtil {
    public static String PATH = "/backend-flux";


    public static void main(String[] args){
        final SecretKey key = new SecretKey();
        key.setMchPriKey(Keys.mchPrivateKey);//商户私钥
        key.setPlatPubKey(Keys.platPublicKey);//平台公钥

        String secKey = RandomUtil.get16LenStr();//生成随机的密钥
        String iv = RandomUtil.get16LenStr();

        int maxCount = 1000;
        List<SingleVo> detailList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for(int i=1; i<=maxCount; i++){
            //包含多种字符来加密，可测试加解密通用性
            String productName = "都是交流交流发就发465dff34DWS34PO发的发生的34343，。？<>{}@！#%￥%~,;'=》》‘；【】@发生的开发商的讲课费" + i;

            SingleVo singleVo = new SingleVo();
            singleVo.setCount("1");
            singleVo.setProductAmount(BigDecimal.valueOf(RandomUtil.getInt(2, 200)));
            singleVo.setProductName(AESUtil.encryptCBC(productName, secKey, iv));

            totalAmount = totalAmount.add(singleVo.getProductAmount());
            detailList.add(singleVo);
        }
        BatchVo batchVo = new BatchVo();
        batchVo.setTotalCount(String.valueOf(detailList.size()));
        batchVo.setTotalAmount(totalAmount);
        batchVo.setDetailList(detailList);

        final Request request = new Request();
        request.setMethod("demo.batch");
        request.setVersion("1.0");
        request.setMchNo(Keys.mchNo);
        request.setSignType(SignType.RSA.getValue());
        request.setData(batchVo);
        request.setTimestamp(String.valueOf(System.currentTimeMillis()));
        request.joinSecKey(secKey, iv);

        final String url = "127.0.0.1:8099" + PATH;
        try{
            Response response = RequestUtil.doJsonRequest(url, request, key);
            if(response.getData() == null){
                System.out.println("响应数据的data为空 Response = " + JsonUtil.toJson(response));
                return;
            }

            BatchRespVo batchVoResp = JsonUtil.toBean(response.getData().toString(), BatchRespVo.class);
            if(RespCode.SUCCESS.getCode().equals(response.getRespCode())
                    && OrderStatus.SUCCESS.getStatus().equals(batchVoResp.getOrderStatus())){
                //交易成功
                System.out.println("交易成功 Response = " + JsonUtil.toJson(response));

                for(SingleRespVo singleVo : batchVoResp.getSingleList()){
                    String[] secKeyArr = response.getSecKey().split(":");
                    secKey = secKeyArr[0];
                    iv = secKeyArr[1];
                    singleVo.setProductName(AESUtil.decryptCBC(singleVo.getProductName(), secKey, iv));
                }

                System.out.println("解密后的data数据为 = " + JsonUtil.toJson(batchVoResp));
            }else{
                System.out.println("交易未完成 Response = " + JsonUtil.toJson(response));
            }
        }catch(Throwable e){
            e.printStackTrace();
        }
    }
}
