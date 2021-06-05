package com.numtimequantity.quantity.quantitycontroller;


import com.google.gson.Gson;
import com.numtimequantity.quantity.utils.RSAUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.Map;

/*拦截器，拦截所有请求*/
@Slf4j
public class InterceptAll implements HandlerInterceptor {
    //返回是true是方行    返回是false是拦截
    /*拦截器可以判断每一个接口必须含带哪些参数，以及参数的长度，进行拦截*/
    //只检验地址和时间戳
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)  {
        try {
            String contextPat = request.getRequestURI();//访问的路径
            Map<String, String[]> parameterMap = request.getParameterMap();//访问的所有参数键值对
            /********************************************************************************/
            //管理员权限
            if (contextPat.equals("/settingCode")&&request.getParameter("aaaaaa").equals("bbbbbb")){
                return true;
            }
            if (contextPat.equals("/settingBuyObject")&&request.getParameter("cccccc").equals("dddddd")){
                return true;
            }
            //测试的
            if (contextPat.equals("/disperse")){
                if ("abcdefg".equals(request.getParameter("password"))){
                    log.debug("获取cpu使用率接口通过");
                    return true;
                }
            }
            /********************************************************************************/
            //如果没有"privkey"参数 或者 "privKey"参数为空   则返回false   "privKey"就是加密的时间戳
            if (!parameterMap.containsKey("priKey")||request.getParameter("priKey")==null){
                return false;
            }
            /**
             * 登录之前的接口,只验证时间戳 ,如果是这些地址,时间戳验证通过就放行
             * 如果访问的不是"/disperse"分布式cpu使用率
             * 并且也不是"/start"开始与庄共舞
             * 并且也不是"/closeLine"关闭与庄共舞
             * 并且也不是"/earningBDancer"与庄共舞利润或者是账户总额
             * 并且也不是"/earningUni"有你共舞
             * 并且也不是"/addMinute" 给量化续分数数
             * 则返回false
             */
            if(contextPat.equals("/disperse")||contextPat.equals("/earningBDancer")||contextPat.equals("/earningUni")
                    ||contextPat.equals("/start")||contextPat.equals("/closeLine")||contextPat.equals("/addMinute")){
                RSAUtil rsaUtil = new RSAUtil();//解密
                //System.out.println("看下参数");
                //System.out.println(request.getParameter("uuid"));
                //System.out.println(request.getParameter("priKey"));
                String sessPri="MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAI5deQzcAuTvs5uRHHZ6DmG8P8eFhrx2hHURDdYfUzR8gh1+q7ma3V/xxONAGyM49tOCcpDUjTWAwRvyBnEh2HMqwKs0vWC1p5lcENxONXGLYY+crlnTZl1NW++zhbL57j9X3lWHHTEFB74g9l9Gy1QhxbmSuj6AEgTbewEZnxHvAgMBAAECgYEAhK93t9WCWQ9TLaW0inO93bePFg1MA3DOiTFI3Q07BksZFhZOROGie96gq1C/OjfITF9jGbsQlRIYaUxMVrq9uUtDLiw3uQOHu4atCQdyzTLfdSa8PmN2lgNEeAWq5SENA8fyqziRYFVFck5AZHU6XzICPmcAqc8lwZ4XhQa0K6kCQQDSsRqbKdKAIHM9Wjm3iCavThiWBsf3FPZy1gi4ZYVGE8nK7dwGtqXAzWfgaW98VUNsOVoo7Jr7o6qvtRrm8tqVAkEArPriRGEGpSTHLiFdN/7HQjylqybO/WcvdsJKmqqllAZt5t8NICtLRwUmE3nVqRzlivkEsMSFAru+L2f8y7QdcwJBAKygwZz6gmfKrsFJKNswqgme4lQiUDspKNhkeBalz7HgSsmDZHD3vA2h/weHO/pSXgDRaQb4/e9KZXK738P0nZUCQQCmFUxicvY6YZGMTSR/uvP+ONSn+98JlqUP1YRj9Cx+b53d6ZNkq6zfR6ZDVNs8QC50vJswTb+X3ELCqYki3JDZAkBa01KNIHrK719pv+21rIYF+5cMGZzNnPGpxF2rqKTGOQ2eJ1M93UivIFSpFfJZXkVQ9Ty+ivQv+4cUdKP8GgBD";
                String priKey = rsaUtil.privateDecrypt(request.getParameter("priKey"),sessPri);
                //System.out.println("解密后的字符串"+priKey);
                //这里只检验时间戳  如果在五秒以内不超时则拦截器放行
                if (new Date().getTime()-Long.parseLong(priKey)<15000){
                    //System.out.println("拦截器放行");
                    //System.out.println(new Date().getTime()-Long.parseLong(priKey));
                    return true;
                }
            }
            /**
             * 手机端访问过来的接口 如果访问的是以下接口并且验证通过则返回true
             * "/selectQuaIf"  查询量化是否开启了
             * "/selectQuantityInfo" 折线图和仪表盘数据
             * "/getTopSymbol"  查询交易对排名接口
             */
            if (contextPat.equals("/selectQuaIf")||contextPat.equals("/selectQuantityInfo")
                    ||contextPat.equals("/getTopSymbol")){
                RSAUtil rsaUtil = new RSAUtil();//解密
                String sessPri="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALTVcZu6yAmwhNwFna/bL0SlJBb0ImI77SC+24qo3pPF40bf9MIH6IQeEpGKowFaMdpWZ4fk//MX4YjNE+1bBC5vnVDwhiuoNTmkH7SR5ryf1oNUMVcAIG/KMHsAEbJzjfhy/iTDaCy92bkIupjJG+aTOZFncnFHo2P3nnhU3O5DAgMBAAECgYAsrvlNo8++JTIPNAwM4jpB2hSa32SHpYsopkCVeLjXBvTsFHbkYxPfYZbxA7LXqLPc4bDfcYSVSvdd2F7uLalQbY8VAbGFvMV1Eals4cwbUjjwhsHZNH3sZ6hmj7LbNXp0450DM3PgfSMSuTdbFp5rQct7iwgD4DtaBj35DM2vIQJBAOXs9ZWk9wUVT5Oi3kMEUtwwTFGoBCmg0N6GbbFGjxrTmaWmQ3bD8uALaALNZlA6haT8SxAm2Qeo/0L4MB/Q7CkCQQDJV0ioRNazEnrRwJ1Y5Cp+f+I4Bp73JANfjuPLJqdnb16sr1GgQCDvvU8XB6fNTfvZTCUiqRo42E58TwgDXZSLAkBpdBbm/XQ2JqIKyoY6In+GcbhvMypFlXZ2uR0SU5RK74XmzazfiduZGmIn9uDYJx8onnYnAEpGEyKQKpiX3xCRAkEAsUq/uxRq7mKObhcrNvQrixq3K9iAsGUw5zte0Suna4iBGJSEzxTJK/JKK6BdHYbXB2BqrtuzMG0gp4u8JaKIwwJADvNypTWM8u2rvC8TcMCnst8mjp87+j1nXFWZQ9CKkydh2clKARNjEe3Sof2c0PeIN59uEX0WSJrAdJ4vKIlPJQ==";
                //System.out.println("看下参数");
                //System.out.println(request.getParameter("uuid"));
                //System.out.println(request.getParameter("priKey"));
                String priKey = rsaUtil.privateDecrypt(request.getParameter("priKey"),sessPri);
                //System.out.println("解密后的字符串"+priKey);
                //这里只检验时间戳  如果在五秒以内不超时则拦截器放行
                if (new Date().getTime()-Long.parseLong(priKey)<15000){
                    //System.out.println("拦截器放行");
                    //System.out.println(new Date().getTime()-Long.parseLong(priKey));
                    return true;
                }
            }

            return false;
        }catch (Exception e){
            log.info("拦截器处出现异常{}",e);
        }
        return false;
    }
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
    }
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
    }

    /*标准的json转Map*/
    private Map getToHashMap(String mapStr){
        try {
            Gson gson = new Gson();
            return gson.fromJson(mapStr, Map.class);
        }catch (Exception e){
            return null;
        }
    }
}
