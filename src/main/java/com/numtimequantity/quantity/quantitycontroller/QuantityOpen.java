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
import java.math.BigDecimal;
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
            if ("0".equals(request.getParameter("quaId"))){//如果是第一个量化策略
                //如果uuid存在并且值为true则这个量化线程在运行中
                if (bankDancerThread.getLineThreadIf().containsKey(request.getParameter("uuid"))&&
                        bankDancerThread.getLineThreadIf().get(request.getParameter("uuid"))){//如果有key并且key的值是true
                    System.out.println("打印uuid的状态");
                    return "has";//程序已经在运行中
                }else {
                    //量化开始的时间戳
                    long startTime = new BigDecimal(request.getParameter("startTime")).longValue();
                    //量化剩余的分钟数
                    int minute = new BigDecimal(request.getParameter("minute")).intValue();
                    String apiKey = request.getParameter("apiKey");
                    String secretKey = request.getParameter("secretKey");
                    String uuid = request.getParameter("uuid");
                    System.out.println("start方法执行了");
                    System.out.println(apiKey);
                    System.out.println(secretKey);
                    bankDancerThread.setGlobalFun(new GlobalFun(globalBuyObject.getRestTemplate(), apiKey, secretKey));
                    bankDancerThread.setGlobalBuyObject(globalBuyObject);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("uuid",uuid);
                    hashMap.put("a",request.getParameter("a"));
                    hashMap.put("k",request.getParameter("k"));
                    bankDancerThread.getThreadLocal().set(hashMap);
                    System.out.println("看下这个getThreadLocal得uuid值");
                    System.out.println(bankDancerThread.getThreadLocal().get());
                    bankDancerThread.getLineThreadIf().put(uuid,true);//设定开关控制
                    bankDancerThread.getQuaOutTimeThread().put(uuid,minute);
                    bankDancerThread.getQuaStartTimeThread().put(uuid,startTime);
                    Thread thread = new Thread(bankDancerThread);
                    thread.start();
                    return "ok";//量化程序开始运行
                }
            }else if ("1".equals(request.getParameter("quaId"))){//如果是第二个量化

            }else if ("2".equals(request.getParameter("quaId"))){//如果是第三个量化

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
    public String closeLine(HttpServletRequest request){
        if ("0".equals(request.getParameter("quaId"))){//关闭第一个量化策略
            if (!bankDancerThread.getLineThreadIf().containsKey(request.getParameter("uuid"))){
                return "noExist!";//uuid值不存在map中
            }else if (!bankDancerThread.getLineThreadIf().get(request.getParameter("uuid"))){
                return "exist";
            }else{
                bankDancerThread.getLineThreadIf().put(request.getParameter("uuid"),false);//停止run线程
                System.out.println("设置完后看下值,线程关闭");
                System.out.println(bankDancerThread.getLineThreadIf());
                return "ok";//成功关闭量化机器人！
            }
        }else if ("1".equals(request.getParameter("quaId"))){//如果是第二个量化

        }else if ("2".equals(request.getParameter("quaId"))){//如果是第三个量化

        }
        return null;
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
