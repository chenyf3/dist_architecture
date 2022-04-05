package com.xpay.common.utils;

import com.xpay.common.exception.UtilException;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * RSA加解密的工具类，可参考 https://segmentfault.com/a/1190000011263680
 */
public class RSAUtil {
    public static final int KEY_SIZE_RSA1 = 1024;//密钥长度
    public static final int KEY_SIZE_RSA2 = 2048;//rsa256的密钥长度
    public static final String SIGNATURE_ALGORITHM_SHA2 = "SHA256withRSA";
    public static final String KEY_ALGORITHM = "RSA";
    public static final String DEFAULT_ENCRYPT_ALGORITHM = "RSA/ECB/PKCS1Padding";
    public static final String ANDROID_ENCRYPT_ALGORITHM = "RSA/ECB/NoPadding";

    public static final String PUBLIC_KEY = "publicKey";
    public static final String PRIVATE_KEY = "privateKey";

    /**
     * 添加算法库
     */
    static {
        Provider[] providers = Security.getProviders();
        boolean isProviderAdded = false;
        if (providers != null && providers.length > 0) {
            for (int i = 0; i < providers.length; i++) {
                if (providers[i] instanceof org.bouncycastle.jce.provider.BouncyCastleProvider) {
                    isProviderAdded = true;
                    break;
                }
            }
        }
        if (!isProviderAdded) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    /**
     * 生成RSA签名串（返回经过 base64 编码之后的字符串）
     * @param cleartext    需要生成签名串的明文数据
     * @param priKeyBase64 经过base64编码过的RSA私钥
     * @return
     */
    public static String sign(String cleartext, String priKeyBase64) {
        try {
            byte[] dataBytes = cleartext.getBytes(StandardCharsets.UTF_8);
            return sign(dataBytes, priKeyBase64);
        } catch (Throwable e) {
            throw new UtilException("RSA签名失败", e);
        }
    }

    /**
     * 生成RSA签名串（返回经过 base64 编码之后的字符串）
     * @param data         需要生成签名串的明文数据
     * @param priKeyBase64 经过base64编码过的RSA密钥
     * @return
     */
    public static String sign(byte[] data, String priKeyBase64) {
        try {
            PrivateKey priKey = parsePrivateKey(priKeyBase64);
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_SHA2);
            signature.initSign(priKey);
            signature.update(data);
            return CodeUtil.base64Encode(signature.sign());
        } catch (Throwable e) {
            throw new UtilException("RSA签名失败", e);
        }
    }

    /**
     * 验证RSA签名串
     * @param cleartext     需要验签的明文数据
     * @param pubKeyBase64  base64编码过的公钥
     * @param signStrBase64 base64编码过的签名串
     * @return
     */
    public static boolean verify(String cleartext, String pubKeyBase64, String signStrBase64) {
        try {
            byte[] dataBytes = cleartext.getBytes(StandardCharsets.UTF_8);
            return verify(dataBytes, pubKeyBase64, signStrBase64);
        } catch (Throwable e) {
            throw new UtilException("RSA验签失败", e);
        }
    }

    /**
     * 验证RSA签名串
     * @param data          需要验签的明文数据
     * @param pubKeyBase64  base64编码过的公钥
     * @param signStrBase64 base64编码过的签名串
     * @return
     */
    public static boolean verify(byte[] data, String pubKeyBase64, String signStrBase64) {
        try {
            RSAPublicKey publicKey = parsePublicKey(pubKeyBase64);
            byte[] signBytes = CodeUtil.base64Decode(signStrBase64);

            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM_SHA2);
            signature.initVerify(publicKey);
            signature.update(data);
            return signature.verify(signBytes);
        } catch (Throwable e) {
            throw new UtilException("RSA验签失败", e);
        }
    }

    /**
     * 使用RSA公钥进行加密（返回经过 base64 编码之后的字符串）
     * @param cleartext       需要加密的明文内容
     * @param publicKeyBase64 base64编码过的公钥
     * @return 加密密文
     */
    public static String encryptByPublicKey(String cleartext, String publicKeyBase64) {
        try {
            RSAPublicKey publicKey = parsePublicKey(publicKeyBase64);
            Cipher cipher = Cipher.getInstance(DEFAULT_ENCRYPT_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] dataBytes = cleartext.getBytes(StandardCharsets.UTF_8);
            dataBytes = rsaSplitCodec(cipher, Cipher.ENCRYPT_MODE, dataBytes, publicKey.getModulus().bitLength());
            return CodeUtil.base64Encode(dataBytes);
        } catch (Throwable e) {
            throw new UtilException("RSA加密失败", e);
        }
    }

    /**
     * 使用RSA私钥进行解密（返回明文字符串）
     *
     * @param secretTextBase64 base64编码过的密文数据
     * @param privateKeyBase64 base64编码过的私钥
     * @return
     */
    public static String decryptByPrivateKey(String secretTextBase64, String privateKeyBase64) {
        try {
            RSAPrivateKey privateKey = parsePrivateKey(privateKeyBase64);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] dataBytes = CodeUtil.base64Decode(secretTextBase64);
            dataBytes = rsaSplitCodec(cipher, Cipher.DECRYPT_MODE, dataBytes, privateKey.getModulus().bitLength());
            return new String(dataBytes, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            throw new UtilException("RSA解密失败", e);
        }
    }

    /**
     * web端的rsa加解密使用 RSA1
     * @param content
     * @param publicKeyBase64
     * @return
     */
    public static String encryptForWeb(String content, String publicKeyBase64) {
        try {
            Key key = parsePublicKey(publicKeyBase64);
            Cipher cipher = Cipher.getInstance(DEFAULT_ENCRYPT_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] dataBytes = content.getBytes(StandardCharsets.UTF_8);
            return CodeUtil.base64Encode(cipher.doFinal(dataBytes));
        } catch (Throwable e) {
            throw new RuntimeException("RSA加密失败", e);
        }
    }

    /**
     * web端的rsa加解密使用 RSA1
     * @param contentBase64
     * @param privateKeyBase64
     * @return
     */
    public static String decryptForWeb(String contentBase64, String privateKeyBase64) {
        try {
            Key key = parsePrivateKey(privateKeyBase64);
            Cipher cipher = Cipher.getInstance(DEFAULT_ENCRYPT_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] dataBytes = CodeUtil.base64Decode(contentBase64);
            dataBytes = cipher.doFinal(dataBytes);
            return new String(dataBytes, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            throw new RuntimeException("RSA解密失败", e);
        }
    }

    /**
     * 生成RSA公私密钥对
     * @return
     */
    public static Map<String, String> genKeyPair(boolean rsa256) {
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(rsa256 ? KEY_SIZE_RSA2 : KEY_SIZE_RSA1);
            KeyPair keyPair = keyPairGen.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            Map<String, String> keyMap = new HashMap<>(2);
            keyMap.put(PUBLIC_KEY, CodeUtil.base64Encode(publicKey.getEncoded()));
            keyMap.put(PRIVATE_KEY, CodeUtil.base64Encode(privateKey.getEncoded()));
            return keyMap;
        } catch (Throwable e) {
            throw new UtilException("生成RSA密钥对出现异常", e);
        }
    }

    public static RSAPublicKey parsePublicKey(String publicKeyBase64) throws Exception {
        byte[] keyBytes = CodeUtil.base64Decode(publicKeyBase64);
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return (RSAPublicKey) keyFactory.generatePublic(x509KeySpec);
    }

    public static RSAPrivateKey parsePrivateKey(String privateKeyBase64) throws Exception {
        byte[] keyBytes = CodeUtil.base64Decode(privateKeyBase64);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        return (RSAPrivateKey) keyFactory.generatePrivate(pkcs8KeySpec);
    }

    /**
     * 校验是否是有效的RSA公钥
     * @param publicKeyBase64 经过base64编码过的RSA公钥
     * @return
     */
    public static boolean validPublicKey(String publicKeyBase64) {
        try {
            parsePublicKey(publicKeyBase64);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 校验是否是有效的RSA私钥
     * @param privateKeyBase64 经过base64编码过的RSA私钥
     * @return
     */
    public static boolean validPrivateKey(String privateKeyBase64) {
        try {
            parsePrivateKey(privateKeyBase64);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * 分段加解密
     * @param cipher
     * @param opMode
     * @param dataBytes
     * @param keySize
     * @return
     * @throws Exception
     */
    private static byte[] rsaSplitCodec(Cipher cipher, int opMode, byte[] dataBytes, int keySize) throws Exception {
        int maxBlock;
        if (opMode == Cipher.DECRYPT_MODE) {
            maxBlock = keySize / 8;
        } else {
            maxBlock = keySize / 8 - 11;
        }

        int offSet = 0, i = 0, dataLength = dataBytes.length;
        byte[] buff;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            // 对数据分段加密/解密
            while (dataLength > offSet) {
                if (dataLength - offSet > maxBlock) {
                    buff = cipher.doFinal(dataBytes, offSet, maxBlock);
                } else {
                    buff = cipher.doFinal(dataBytes, offSet, dataLength - offSet);
                }
                out.write(buff, 0, buff.length);
                i++;
                offSet = i * maxBlock;
            }
            return out.toByteArray();
        } finally {
            out.close();
        }
    }

    public static void main(String[] args) {
        Map<String, String> keyPair = genKeyPair(true);
        System.out.println(keyPair.toString());

        keyPair = genKeyPair(false);
        System.out.println(keyPair.toString());
    }
}
