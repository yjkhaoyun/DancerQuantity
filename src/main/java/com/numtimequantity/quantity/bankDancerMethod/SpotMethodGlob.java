package com.numtimequantity.quantity.bankDancerMethod;


import com.numtimequantity.quantity.utils.ShaUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 现货的底层签名类
 */
public class SpotMethodGlob extends MethodGlob {
    private String GUrlSpot = "https://api.binance.com";
    protected <T> T publicBinance(Class<T> type,Object...arguments){
        String dataBody = "";
        if(arguments.length>=3){
            ConcurrentHashMap<String, String> parame = ConcurrentHashMap.class.cast(arguments[2]);//将arguments[2]转换为map
            for (String keyItem:parame.keySet()){
                dataBody = dataBody + keyItem + "=" + parame.get(keyItem) + "&";
            }
        }
        dataBody = dataBody + "timestamp=" + Long.toString(new Date().getTime());

        try {
            ShaUtils shaUtils = new ShaUtils();
            String signature = shaUtils.getSHA256Str(super.secretkey,dataBody);
            dataBody = dataBody+"&signature="+signature;

            HttpHeaders httpHeaders = new HttpHeaders(); //导入springboot的headers  设置请求头
            httpHeaders.add("X-MBX-APIKEY",super.api_key);
            httpHeaders.add("Content-Type","application/x-www-form-urlencoded");
            /*参数一：post请求时参数放在Body里，用字符串格式如symbol=BTCUSDT&side=BUY&quantity=0.001    参数二：HttpHeaders是MultiValueMap<String, String>的实现类，*/
            HttpEntity<String> httpEntity = new HttpEntity<>(dataBody,httpHeaders);

            String url = this.GUrlSpot + arguments[0];
            RestTemplate restTemplate = super.getRestTemplate();
            if(arguments[1].equals("GET")){
                url = url + "?" +dataBody;
                ResponseEntity<T> responseEntity = restTemplate.exchange(URI.create(url), HttpMethod.GET, httpEntity, type);
                return responseEntity.getBody();
            }else if(arguments[1].equals("POST")){
                ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, type);//httpEntity里包含里请求头和参数
                return responseEntity.getBody();
            }else if(arguments[1].equals("DELETE")){
                ResponseEntity<T> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, type);
                return responseEntity.getBody();
            }
        }catch (Exception e){
            System.out.println("错误的请求,捕获到异常："+e);
        }
        return null;
    }
}
