package com.numtimequantity.quantity.quantitycontroller;


import com.numtimequantity.quantity.bankDancerMethod.GlobalFun;
import com.numtimequantity.quantity.fileThread.BankDancerThread;
import com.numtimequantity.quantity.fileThread.GlobalBuyObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

/**
 * 此类用于控制
 * 量化策略的启动与关闭接口;cpu使用率和折线图接口   四个接口
 */
@Slf4j
@Controller
public class QuantityOpen {
    @Autowired
    GlobalBuyObject globalBuyObject;
    @Autowired
    BankDancerThread bankDancerThread;
    /**
     *
     *  访问测试连接 http://localhost/start?uuid=jjj&a=0.001&k=50
     * @param request 传一个uuid
     * @return 开始运行量化程序,支持多线程
     */
    @PostMapping("/start")
    @ResponseBody
    public String oneP(HttpServletRequest request){
        System.out.println("上面start方法执行了");
        try {
            //这里出现了报错java.lang.ThreadLocal.get()" is null 明天解决20210518
            if (bankDancerThread.getLineThreadIf().containsKey(request.getParameter("uuid"))&&
                    bankDancerThread.getLineThreadIf().get(request.getParameter("uuid"))){//如果有key并且key的值是true
                System.out.println("打印uuid的状态");
                return "has";//程序已经在运行中
            }else {

                String apiKey = request.getParameter("apiKey");
                String secretKey = request.getParameter("secretKey");
                System.out.println("start方法执行了");
                System.out.println(apiKey);
                System.out.println(secretKey);
                bankDancerThread.setGlobalFun(new GlobalFun(globalBuyObject.getRestTemplate(), apiKey, secretKey));
                bankDancerThread.setGlobalBuyObject(globalBuyObject);
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("uuid",request.getParameter("uuid"));
                hashMap.put("a",request.getParameter("a"));
                hashMap.put("k",request.getParameter("k"));
                bankDancerThread.getThreadLocal().set(hashMap);
                System.out.println("看下这个getThreadLocal得uuid值");
                System.out.println(bankDancerThread.getThreadLocal().get());
                bankDancerThread.getLineThreadIf().put(request.getParameter("uuid"),true);
                Thread thread = new Thread(bankDancerThread);
                thread.start();
                return "ok";//量化程序开始运行
            }
        }catch (Exception e){
            System.out.println("start报错");
            System.out.println(e);
        }
        return null;
    }




    /**
     * 关闭量化程序线程 (用户关闭自己的量化策略程序)
     * http://localhost/closeLine?uuid=jjj
     * @param request  关闭与庄共舞量化线程
     * @return 关闭量化程序线程 (用户关闭自己的量化策略程序)
     */
    @RequestMapping("/closeLine")
    @ResponseBody
    public String getLine(HttpServletRequest request){
        if (!bankDancerThread.getLineThreadIf().containsKey(request.getParameter("uuid"))){
            return "请求错误!";//uuid值不存在map中
        }else if (!bankDancerThread.getLineThreadIf().get(request.getParameter("uuid"))){
            return "线程已经处于关闭状态";
        }else{
            bankDancerThread.getLineThreadIf().put(request.getParameter("uuid"),false);//停止run线程
            System.out.println("设置完后看下值,线程关闭");
            System.out.println(bankDancerThread.getLineThreadIf());
            return "成功关闭量化机器人！";
        }
    }

    /**
     *http://localhost:801/disperse
     * @return 分布式接口,返回url和cpu使用率   供中心服务器线程每分钟遍历访问
     */
    @RequestMapping("/disperse")  //注意先启动buyObject线程才能有值
    @ResponseBody
    public Double disperse(){
        log.debug("来到了获取服务器cpu占用率接口");
        return (Double) globalBuyObject.getBuyObject().get("totalCpu");
    }

    /**
     *
     * @return 与庄共舞收益图
     */
    @RequestMapping("/earningBDancer")
    @ResponseBody
    public HashMap earningBDancer(){
        return null;
    }

    /**
     *
     * @return 有你共舞收益图
     */
    @RequestMapping("/earningUni")
    @ResponseBody
    public HashMap earningUni(){
        return null;
    }
}
