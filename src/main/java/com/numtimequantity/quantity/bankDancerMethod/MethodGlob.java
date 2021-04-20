package com.numtimequantity.quantity.bankDancerMethod;


import com.numtimequantity.quantity.utils.ShaUtils;
import lombok.Data;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 底层函数类
 */
@Data
public class MethodGlob{
    /*币安交易所Api的key  注意:这两个key最初是没有值的,需要在最尾端的GlobalFun孙子类中构造赋值*/
    protected String api_key; //= "mdLymK19LcGbdaKStaX0AZpdPi65TpRtIA7tpK7LnyythJlruFlD10P5gvSImdtq";
    /*币安Api_Key的秘钥*/
    protected String secretkey; //= "lvvxR6ND3DdbEkUa0JE2QgAwVMhpPEPL5K3KUpRKbHn0bXxVNzqN9PXqFV27UDNT";
    private String GUrl = "https://fapi.binance.com";
    protected RestTemplate restTemplate;

    /**
     * 签名部分，公共方法类
     * @param arguments 可变参数，第一个参数为api接口，字符串类型;第二个参数为方法的类型"GET"、"POST"、"DELETE";第三个参数为
     *                  请求接口需要的参数，map<String,String>类型为了多线程安全请使用ConcurrentHashMap<String,String>。
     * @return 返回一个map对象
     * @throws Exception  暂时没有捕获异常，直接抛出
     */
    protected <T> T publicM(Class<T> type,Object...arguments){
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
           String signature = shaUtils.getSHA256Str(this.secretkey,dataBody);
           dataBody = dataBody+"&signature="+signature;

           HttpHeaders httpHeaders = new HttpHeaders(); //导入springboot的headers  设置请求头
           httpHeaders.add("X-MBX-APIKEY",this.api_key);
           httpHeaders.add("Content-Type","application/x-www-form-urlencoded");
           /*参数一：post请求时参数放在Body里，用字符串格式如symbol=BTCUSDT&side=BUY&quantity=0.001    参数二：HttpHeaders是MultiValueMap<String, String>的实现类，*/
           HttpEntity<String> httpEntity = new HttpEntity<>(dataBody,httpHeaders);

           String url = this.GUrl + arguments[0];
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
