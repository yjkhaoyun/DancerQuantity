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
        log.debug("拦截器{}",request.getRequestURI());
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
            if (contextPat.equals("/start")){
                return true;
            }
            //测试的
            if (contextPat.equals("/disperse")){
                return true;
            }
            /********************************************************************************/
            //如果没有"privkey"参数 或者 "privKey"参数为空则返回false   "privKey"就是加密的时间戳
            if (!parameterMap.containsKey("priKey")||request.getParameter("priKey")==null){
                return false;
            }
            /**
             * 登录之前的接口,只验证时间戳 ,如果是这些地址,时间戳验证通过就放行
             * 如果访问的不是"/disperse"分布式cpu使用率
             * 并且也不是"/start"开始与庄共舞
             * 并且也不是"/closeLine"关闭与庄共舞
             * 并且也不是"/earningBDancer"与庄共舞
             * 并且也不是"/earningUni"有你共舞
             * 则返回false
             */
            if(contextPat.equals("/disperse")||contextPat.equals("/earningBDancer")||contextPat.equals("/earningUni")
                    ||contextPat.equals("/start")||contextPat.equals("/closeLine")){
                RSAUtil rsaUtil = new RSAUtil();//解密
                String priKey = rsaUtil.decodePri(request.getParameter("priKey"));
                System.out.println("解密后的字符串"+priKey);
                //这里只检验时间戳  如果在五秒以内不超时则拦截器放行
                if (new Date().getTime()-Long.parseLong(priKey)<5000){
                    System.out.println("拦截器放行");
                    System.out.println(new Date().getTime()-Long.parseLong(priKey));
                    return true;
                }
            }
            /**
             * 登录之后的接口,需要验证时间戳和session字符串
             */
            return false;
        }catch (Exception e){
            System.out.println("错误信息"+e);

        }
        System.out.println("拦截器拦截");
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
