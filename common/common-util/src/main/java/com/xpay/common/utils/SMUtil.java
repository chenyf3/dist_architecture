package com.xpay.common.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.CharEncoding;
import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.gm.GMObjectIdentifiers;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * 关于国密SM2的公共方法
 */
public class SMUtil {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    private static final String ALGORITHM = "EC";
    private final static String SM4_ALGORITHM = "SM4";
    private final static String SM4_ECB_PADDING = "SM4/ECB/PKCS5Padding";

    public static final String PUBLIC_KEY = "publicKey";
    public static final String PRIVATE_KEY = "privateKey";


    /**
     * 生成公私钥对
     * @return
     */
    public static Map<String, String> genKeyPair() {
        // 获取SM2 椭圆曲线推荐参数
        X9ECParameters ecParameters = GMNamedCurves.getByName("sm2p256v1");
        // 构造EC 算法参数
        ECNamedCurveParameterSpec sm2Spec = new ECNamedCurveParameterSpec(
                // 设置SM2 算法的 OID
                GMObjectIdentifiers.sm2p256v1.toString()
                // 设置曲线方程
                , ecParameters.getCurve()
                // 椭圆曲线G点
                , ecParameters.getG()
                // 大整数N
                , ecParameters.getN());

        Map<String, String> sm2KeyPair = new HashMap<>();
        try {
            // 创建 密钥对生成器
            KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM);
            // 使用SM2的算法区域初始化密钥生成器
            gen.initialize(sm2Spec, new SecureRandom());
            // 获取密钥对
            KeyPair keyPair = gen.generateKeyPair();
            sm2KeyPair.put(PUBLIC_KEY, CodeUtil.base64Encode(keyPair.getPublic().getEncoded()));
            sm2KeyPair.put(PRIVATE_KEY, CodeUtil.base64Encode(keyPair.getPrivate().getEncoded()));
        } catch (Exception e) {
            throw new RuntimeException("SM2密钥对生成异常" , e);
        }
        return sm2KeyPair;
    }

    /**
     * 签名
     *
     * @param priKeyBase64 签名私钥
     * @param data  明文
     * @return
     */
    public static String sign(String priKeyBase64, String data) {
        if (StringUtil.isEmpty(priKeyBase64)){
            throw new IllegalArgumentException("privateKey 不能为空");
        }

        try {
            byte[] keyBytes = CodeUtil.base64Decode(priKeyBase64);
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            PrivateKey privateK = keyFactory.generatePrivate(pkcs8KeySpec);
            Signature signature = Signature.getInstance(GMObjectIdentifiers.sm2sign_with_sm3.toString());
            signature.initSign(privateK);
            signature.update(data.getBytes(CharEncoding.ISO_8859_1));
            return CodeUtil.base64Encode(signature.sign());
        } catch (Exception e) {
            throw new RuntimeException("sign has error:" , e);
        }
    }

    public static boolean validPublicKey(String publicKeyBase64){
        //TODO
        return false;
    }


    /**
     * 验证签名
     *
     * @param pubKeyBase64  签名公钥
     * @return
     */
    public static boolean verify(String data, String pubKeyBase64, String sign) {
        if (StringUtil.isEmpty(pubKeyBase64)){
            throw new IllegalArgumentException("privateKey 不能为空");
        }

        byte[] keyBytes = CodeUtil.base64Decode(pubKeyBase64);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM,new BouncyCastleProvider());
            PublicKey publicK = keyFactory.generatePublic(keySpec);
            Signature signature = Signature.getInstance(GMObjectIdentifiers.sm2sign_with_sm3.toString(),new BouncyCastleProvider());
            signature.initVerify(publicK);
            signature.update(data.getBytes(CharEncoding.ISO_8859_1));
            return signature.verify(CodeUtil.base64Decode(sign));
        }catch (Exception e){
            throw new RuntimeException("sverify has error:" , e);
        }
    }

    /**
     * 国密SM4软加密
     *
     * @param data 需要加密的数据
     * @param key  16位的秘钥
     * @return 加密后的Base64字符串
     * @throws Exception
     */
    public static String encryptSM4Ecb(String data, String key) throws Exception {
        try {

            Cipher cipher = Cipher.getInstance(SM4_ECB_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), SM4_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            byte[] bytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(bytes);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 国密SM4软解密
     *
     * @param encryptData
     * @param key
     * @return
     * @throws Exception
     */
    public static String decryptSM4Ecb(String encryptData, String key) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(SM4_ECB_PADDING, BouncyCastleProvider.PROVIDER_NAME);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), SM4_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] bytes = cipher.doFinal(Base64.decodeBase64(encryptData));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw e;
        }
    }

    public static void main(String[] args) throws Exception {
//        SM2KeyPair sm2KeyPair = createKeyPair();
//        String text = "wocaole";
//        String publicKey = sm2KeyPair.getPublicKey();
//        String privateKey = sm2KeyPair.getPrivateKey();
//        String sign = sign("",text);
//        System.out.println(sign);
//        boolean verify = verify(text,publicKey,sign);
//        System.out.println(verify);
    }
}
