package com.numtimequantity.quantity.utils;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * 需要jdk15.0.2
 */
@Component
public class RSAUtil {
    public  final String SIGN_ALGORITHMS = "SHA256WithRSA";
    public  final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /** */
    /**
     * RSA最大加密明文大小
     */
    private  final int MAX_ENCRYPT_BLOCK = 117;

    /** */
    /**
     * RSA最大解密密文大小
     */
    private  final int MAX_DECRYPT_BLOCK = 128;

    /**
     * 生成秘钥对实例
     * 返回一组秘钥对实例,其中 keyPair.getPublic() 是公钥实例, keyPair.getPrivate() 是私钥实例
     * @return keyPair
     * @throws Exception
     */
    public  KeyPair getKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(1024);
        return generator.generateKeyPair();
    }

    /*私钥中提起公钥,待完善*/

    /**
     * 生成秘钥字符串
     * 如果传keyPair.getPublic()返回公钥字符串,如果传keyPair.getPrivate()返回私钥字符串
     * @param key  公钥或私钥的实例
     * @return
     */
    public String getKeyStr(Key key){
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * 签名
     *
     * @param content
     * @param privateKey
     * @return
     */
    public  String sign(String content, String privateKey) {
        try {
            PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey));
            KeyFactory keyf = KeyFactory.getInstance("RSA");
            PrivateKey priKey = keyf.generatePrivate(priPKCS8);
            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);
            signature.initSign(priKey);
            signature.update(content.getBytes(DEFAULT_CHARSET));
            byte[] signed = signature.sign();
            return Base64.getEncoder().encodeToString(signed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 验签
     *
     * @param content
     * @param sign
     * @param publicKey
     * @return
     */
    public  boolean verify(String content, String sign, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] encodedKey = Base64.getDecoder().decode(publicKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

            Signature signature = Signature.getInstance(SIGN_ALGORITHMS);

            signature.initVerify(pubKey);
            signature.update(content.getBytes(DEFAULT_CHARSET));

            return signature.verify(Base64.getDecoder().decode(sign));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 将公钥字符串转换成公钥实例
     * @param key
     * @return
     * @throws Exception
     */
    public  PublicKey getPublicKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    /**
     * 将私钥字符串转化成私钥实例
     * @param key
     * @return
     * @throws Exception
     */
    public  PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;
    }


    /**
     * 公钥分段加密
     *
     * @param content
     * @param publicKeyStr
     * @return
     * @throws Exception
     */
    public  String publicEncrpyt(String content, String publicKeyStr) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(1, getPublicKey(publicKeyStr));
        byte[] bytes = content.getBytes(DEFAULT_CHARSET);

        int inputLen = bytes.length;
        int offSet = 0;
        byte[] cache;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(bytes, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(bytes, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return Base64.getEncoder().encodeToString(encryptedData);
    }


    /**
     * 私钥分段加密
     *
     * @param content
     * @param privateKeyStr
     * @return
     * @throws Exception
     */
    public  String privateEncrpyt(String content, String privateKeyStr) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(1, getPrivateKey(privateKeyStr));
        byte[] bytes = content.getBytes(DEFAULT_CHARSET);

        int inputLen = bytes.length;
        int offSet = 0;
        byte[] cache;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(bytes, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(bytes, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();
        return Base64.getEncoder().encodeToString(encryptedData);
    }


    /**
     * 私钥分段解密
     *
     * @param content
     * @param privateKeyStr
     * @return
     * @throws Exception
     */
    public    String privateDecrypt(String content, String privateKeyStr) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(2, getPrivateKey(privateKeyStr));
        byte[] bytes = Base64.getDecoder().decode(content);
        int inputLen = bytes.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(bytes, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(bytes, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return new String(decryptedData);
    }


    /**
     * 公钥分段解密
     *
     * @param content
     * @param publicKeyStr
     * @return
     * @throws Exception
     */
    public  String publicDecrypt(String content, String publicKeyStr) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(2, getPublicKey(publicKeyStr));
        byte[] bytes = Base64.getDecoder().decode(content);
        int inputLen = bytes.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher.doFinal(bytes, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(bytes, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();
        return new String(decryptedData);
    }

    /*私钥解密*/
    public String decodePri(String s){
        //2021年3月28日自己生成的私钥  对应的公钥是:  MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC01XGbusgJsITcBZ2v2y9EpSQW9CJiO+0gvtuKqN6TxeNG3/TCB+iEHhKRiqMBWjHaVmeH5P/zF+GIzRPtWwQub51Q8IYrqDU5pB+0kea8n9aDVDFXACBvyjB7ABGyc434cv4kw2gsvdm5CLqYyRvmkzmRZ3JxR6Nj9554VNzuQwIDAQAB
        String privateKey ="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALTVcZu6yAmwhNwFna/bL0SlJBb0ImI77SC+24qo3pPF40bf9MIH6IQeEpGKowFaMdpWZ4fk//MX4YjNE+1bBC5vnVDwhiuoNTmkH7SR5ryf1oNUMVcAIG/KMHsAEbJzjfhy/iTDaCy92bkIupjJG+aTOZFncnFHo2P3nnhU3O5DAgMBAAECgYAsrvlNo8++JTIPNAwM4jpB2hSa32SHpYsopkCVeLjXBvTsFHbkYxPfYZbxA7LXqLPc4bDfcYSVSvdd2F7uLalQbY8VAbGFvMV1Eals4cwbUjjwhsHZNH3sZ6hmj7LbNXp0450DM3PgfSMSuTdbFp5rQct7iwgD4DtaBj35DM2vIQJBAOXs9ZWk9wUVT5Oi3kMEUtwwTFGoBCmg0N6GbbFGjxrTmaWmQ3bD8uALaALNZlA6haT8SxAm2Qeo/0L4MB/Q7CkCQQDJV0ioRNazEnrRwJ1Y5Cp+f+I4Bp73JANfjuPLJqdnb16sr1GgQCDvvU8XB6fNTfvZTCUiqRo42E58TwgDXZSLAkBpdBbm/XQ2JqIKyoY6In+GcbhvMypFlXZ2uR0SU5RK74XmzazfiduZGmIn9uDYJx8onnYnAEpGEyKQKpiX3xCRAkEAsUq/uxRq7mKObhcrNvQrixq3K9iAsGUw5zte0Suna4iBGJSEzxTJK/JKK6BdHYbXB2BqrtuzMG0gp4u8JaKIwwJADvNypTWM8u2rvC8TcMCnst8mjp87+j1nXFWZQ9CKkydh2clKARNjEe3Sof2c0PeIN59uEX0WSJrAdJ4vKIlPJQ==";
        try {
            return privateDecrypt(s, privateKey);
        }catch (Exception e){}
            return null;
    }

    public  void envsdeCode(String s) throws Exception {
        //2021年3月28日自己生成的私钥  对应的公钥是:  MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC01XGbusgJsITcBZ2v2y9EpSQW9CJiO+0gvtuKqN6TxeNG3/TCB+iEHhKRiqMBWjHaVmeH5P/zF+GIzRPtWwQub51Q8IYrqDU5pB+0kea8n9aDVDFXACBvyjB7ABGyc434cv4kw2gsvdm5CLqYyRvmkzmRZ3JxR6Nj9554VNzuQwIDAQAB
        String privateKey ="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALTVcZu6yAmwhNwFna/bL0SlJBb0ImI77SC+24qo3pPF40bf9MIH6IQeEpGKowFaMdpWZ4fk//MX4YjNE+1bBC5vnVDwhiuoNTmkH7SR5ryf1oNUMVcAIG/KMHsAEbJzjfhy/iTDaCy92bkIupjJG+aTOZFncnFHo2P3nnhU3O5DAgMBAAECgYAsrvlNo8++JTIPNAwM4jpB2hSa32SHpYsopkCVeLjXBvTsFHbkYxPfYZbxA7LXqLPc4bDfcYSVSvdd2F7uLalQbY8VAbGFvMV1Eals4cwbUjjwhsHZNH3sZ6hmj7LbNXp0450DM3PgfSMSuTdbFp5rQct7iwgD4DtaBj35DM2vIQJBAOXs9ZWk9wUVT5Oi3kMEUtwwTFGoBCmg0N6GbbFGjxrTmaWmQ3bD8uALaALNZlA6haT8SxAm2Qeo/0L4MB/Q7CkCQQDJV0ioRNazEnrRwJ1Y5Cp+f+I4Bp73JANfjuPLJqdnb16sr1GgQCDvvU8XB6fNTfvZTCUiqRo42E58TwgDXZSLAkBpdBbm/XQ2JqIKyoY6In+GcbhvMypFlXZ2uR0SU5RK74XmzazfiduZGmIn9uDYJx8onnYnAEpGEyKQKpiX3xCRAkEAsUq/uxRq7mKObhcrNvQrixq3K9iAsGUw5zte0Suna4iBGJSEzxTJK/JKK6BdHYbXB2BqrtuzMG0gp4u8JaKIwwJADvNypTWM8u2rvC8TcMCnst8mjp87+j1nXFWZQ9CKkydh2clKARNjEe3Sof2c0PeIN59uEX0WSJrAdJ4vKIlPJQ==";

        System.out.println("打印参数,公钥加密后值"+s);
        //私钥分段解密后
        String s1 = privateDecrypt(s, privateKey);
        System.out.println("私钥解密后"+s1);


        //公钥加密-->私钥签名-->公钥验签-->私钥解密
       /* String s = publicEncrpyt(content, publicKey);
        System.out.println("公钥加密后"+s);

        //签名     Authorization
        String sign = sign(s, privateKey);
        System.out.println("私钥签名后："+sign);

        //验签
        boolean verify = verify(s, sign, publicKey);
        System.out.println("用公钥验签后"+verify);

        String s1 = privateDecrypt(s, privateKey);
        System.out.println("解密后"+s1 );*/


        /*String s = privateEncrpyt(content, privateKey);
        System.out.println("公钥加密后" + s);

        String sign = sign(s, privateKey);
        System.out.println("私钥签名后：" + sign);

        boolean verify = verify(s, sign, publicKey);
        System.out.println("用公钥验签后" + verify);

        String s1 = publicDecrypt(s, publicKey);
        System.out.println("解密后" + s1);*/
    }


}