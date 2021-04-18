package com.numtimequantity.quantity.utils;

import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/*交易所签名*/
public class ShaUtils {
    public static final String ENCODE_TYPE_HMAC_SHA_256 ="HmacSHA256";
    public static final String ENCODE_UTF_8_UPPER ="UTF-8";
    //把secret作为密码将message进行sha256加密
    public  String getSHA256Str(String secret,String message) throws Exception {
        if (StringUtils.isEmpty(secret)){
            return null;
        }
        String encodeStr;
        try{
            //HMAC_SHA256 加密
            Mac HMAC_SHA256 = Mac.getInstance(ENCODE_TYPE_HMAC_SHA_256);
            SecretKeySpec secre_spec = new SecretKeySpec(secret.getBytes(ENCODE_UTF_8_UPPER),ENCODE_TYPE_HMAC_SHA_256);
            HMAC_SHA256.init(secre_spec);
            byte[] bytes = HMAC_SHA256.doFinal(message.getBytes(ENCODE_UTF_8_UPPER));
            if (bytes==null&&bytes.length<1){
                return null;
            }
            //字节转换为16进制字符串
            encodeStr =this.byteToHex(bytes);
            if (StringUtils.isEmpty(encodeStr)){ //如果SHA256为null或者""返回null
                return null;
            }
        }catch (Exception e){
            throw new Exception("get 256 info error ....");
        }
        return encodeStr;
    }
    private  String byteToHex(byte[] bytes){
        if (bytes==null){
            return null;
        }
        StringBuffer stringBuilder = new StringBuffer();
        String temp=null;
        for (int i = 0; i <bytes.length ; i++) {
            temp = Integer.toHexString(bytes[i]&0xff);
            if (temp.length()==1){
                stringBuilder.append("0");
            }
            stringBuilder.append(temp);
        }
        return stringBuilder.toString();
    }
}
