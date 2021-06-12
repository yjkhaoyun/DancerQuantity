package com.numtimequantity.quantity.quantitycontroller;


import com.numtimequantity.quantity.bankDancerMethod.GlobalFun;
import com.numtimequantity.quantity.fileThread.BankDancerThread;
import com.numtimequantity.quantity.fileThread.GlobalBuyObject;
import com.numtimequantity.quantity.fileThread.TopSymbolThread;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    @Autowired
    TopSymbolThread topSymbolThread;
    /**
     *
     *  访问测试连接 http://localhost/start?uuid=jjj&a=0.001&k=50
     * @param request 传一个uuid
     * @return 开始运行量化程序,支持多线程
     */
    @PostMapping("/start")
    @ResponseBody
    public String oneP(HttpServletRequest request){
        //System.out.println("上面start方法执行了");
        try {
            if ("0".equals(request.getParameter("quaId"))){//如果是第一个量化策略 bankDancer
                //如果uuid存在并且值为true则这个量化线程在运行中
                if (bankDancerThread.getLineThreadIf().containsKey(request.getParameter("uuid"))&&
                        bankDancerThread.getLineThreadIf().get(request.getParameter("uuid"))){//如果有key并且key的值是true
                    return "has";//程序已经在运行中
                }else {
                    //量化开始的时间戳
                    long startTime = new BigDecimal(request.getParameter("startTime")).longValue();
                    //量化剩余的分钟数
                    int minute = new BigDecimal(request.getParameter("minute")).intValue();
                    String apiKey = request.getParameter("apiKey");
                    String secretKey = request.getParameter("secretKey");
                    String a = request.getParameter("a");
                    String k = request.getParameter("k");
                    String acc = request.getParameter("acc");//前端传过来的  开启量化时选择的金额 500u 1000u ...
                    String uuid = request.getParameter("uuid");

                    //获取最新价   使用了权重1点
                    bankDancerThread.setGlobalBuyObject(globalBuyObject);
                    ArrayList<String[]> list = new ArrayList<>();
                    String arr[] = {Long.toString(new Date().getTime()),"0"};//初始化一个字符串数组  前端的折线图数据
                    list.add(arr);//将指定元素添加到末尾
                    /***保存开启的金额和等级********************/
                    HashMap<String, String> moneyAndLvMap = new HashMap<>();
                    moneyAndLvMap.put("money",acc);
                    moneyAndLvMap.put("lv",a);
                    bankDancerThread.getQuaMoneyAndLv().put(uuid,moneyAndLvMap);
                    /******************************************/
                    bankDancerThread.getInfo().put(uuid,list);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put("uuid",uuid);
                    hashMap.put("a",a);
                    hashMap.put("k",k);
                    hashMap.put("acc",acc);
                    hashMap.put("apiKey",apiKey);
                    hashMap.put("secretKey",secretKey);

                    bankDancerThread.getThreadLocal().set(hashMap);//将量化参数存进去
                    //System.out.println("看下这个getThreadLocal得uuid值");
                    //System.out.println(bankDancerThread.getThreadLocal().get());
                    bankDancerThread.getLineThreadIf().put(uuid,true);//设定开关控制
                    bankDancerThread.getQuaOutTimeThread().put(uuid,minute);//量化剩余的分钟数
                    bankDancerThread.getQuaStartTimeThread().put(uuid,startTime);//量化开始的时间戳
                    Thread thread = new Thread(bankDancerThread);
                    thread.start();
                    return "ok";//量化程序开始运行
                }
            }else if ("1".equals(request.getParameter("quaId"))){//如果是第二个量化

            }else if ("2".equals(request.getParameter("quaId"))){//如果是第三个量化

            }

        }catch (Exception e){
            log.info("开启量化时报错{}",e);
        }
        return null;
    }




    /**
     * 关闭量化程序线程 (用户关闭自己的量化策略程序)
     * http://localhost/closeLine?uuid=jjj
     * @param request  关闭与庄共舞量化线程
     * @return 关闭量化程序线程 (用户关闭自己的量化策略程序)
     */
    @PostMapping("/closeLine")
    @ResponseBody
    public String closeLine(HttpServletRequest request){
        log.debug("来到了关闭线程接口");
        String uuid = request.getParameter("uuid");
        if ("0".equals(request.getParameter("quaId"))){//关闭第一个量化策略
            if (!bankDancerThread.getLineThreadIf().containsKey(uuid)){
                return "noExist";//uuid值不存在map中
            }else if (!bankDancerThread.getLineThreadIf().get(uuid)){
                return "exist";
            }else{
                bankDancerThread.getLineThreadIf().put(uuid,false);//停止run线程
                if (bankDancerThread.getQuaMoneyAndLv().containsKey(uuid)){//关闭时删掉这个uuid的键值对
                    bankDancerThread.getQuaMoneyAndLv().remove(uuid);
                }
                return "ok";//成功关闭量化机器人！
            }
        }else if ("1".equals(request.getParameter("quaId"))){//如果是第二个量化

        }else if ("2".equals(request.getParameter("quaId"))){//如果是第三个量化

        }
        return null;
    }

    /**
     *
     * @return  给量化追加分钟数时长
     */
    @PostMapping("/addMinute")  //注意先启动buyObject线程才能有值
    @ResponseBody
    public String addMinute(HttpServletRequest request){
        String uuid = request.getParameter("uuid");
        String type = request.getParameter("type");
        Boolean lineThreadIf = bankDancerThread.getLineThreadIf().containsKey(uuid)?bankDancerThread.getLineThreadIf().get(uuid):false;
        Long startTime = bankDancerThread.getQuaStartTimeThread().containsKey(uuid)?bankDancerThread.getQuaStartTimeThread().get(uuid):new Date().getTime();
        Integer outMinute = bankDancerThread.getQuaOutTimeThread().containsKey(uuid)?bankDancerThread.getQuaOutTimeThread().get(uuid):0;
        //bankDancer追加时长
        if (type.equals("bank") && lineThreadIf && (new Date().getTime()-startTime)/(1000*60)<outMinute){
            int minute = new BigDecimal(request.getParameter("minute")).intValue();
            Integer timeMinute = bankDancerThread.getQuaOutTimeThread().get(uuid);
            bankDancerThread.getQuaOutTimeThread().put(uuid,timeMinute+minute);
            return "ok";

        //uniDancer追加时长
        }else if (type.equals("uni")){

        }else if (type.equals("chain")){

        }

        return "";
    }

    /**
     *http://localhost:801/disperse
     * @return 分布式接口,返回url和cpu使用率 和服务器运行了多少个量化  供中心服务器线程每分钟遍历访问
     */
    @PostMapping("/disperse")  //注意先启动buyObject线程才能有值
    @ResponseBody
    public HashMap disperse(){
        HashMap<String, Object> hashMap = new HashMap<>();
        if (globalBuyObject.getBuyObject().get("totalCpu")==null){
            hashMap.put("tCpu",0.0);//cpu的使用率
        }else {
            hashMap.put("tCpu",globalBuyObject.getBuyObject().get("totalCpu"));//cpu的使用率
        }
        hashMap.put("qNum",bankDancerThread.getLineThreadIf().size());//这台服务器运行量化的数量数
        log.debug("来到了获取服务器cpu占用率接口{}",hashMap);
        return hashMap;
    }

    /**
     *
     * @return 查询量化是否开启了  用于手机端查询
     */
    @PostMapping("/selectQuaIf")
    @ResponseBody
    public HashMap selectQuaIf(HttpServletRequest request){
        String quaId = request.getParameter("quaId");
        String uuid = request.getParameter("uuid");
        HashMap<String, String> hashMap = new HashMap<>();
        Boolean bankDancerIf = bankDancerThread.getLineThreadIf().containsKey(uuid)?bankDancerThread.getLineThreadIf().get(uuid):false;
        if ("0".equals(quaId)&&bankDancerIf){//如果是查bankDancer量化
            hashMap.put("mes","ok");
            hashMap.put("money",bankDancerThread.getQuaMoneyAndLv().get("uuid").get("money"));
            hashMap.put("lv",bankDancerThread.getQuaMoneyAndLv().get("uuid").get("money"));
            return hashMap;
        }else if ("1".equals(quaId)){

        }else if ("2".equals(quaId)){

        }
        hashMap.put("mes","no");
        return hashMap;
    }

    /**
     *
     * @return  查询仪表盘的数据
     */
    @PostMapping("/selectQuantityInfo")
    @ResponseBody
    public HashMap selectQuantityInfo(HttpServletRequest request){
        String uuid = request.getParameter("uuid");
        String quaId = request.getParameter("quaId");
        String startTime = request.getParameter("startTime");
        int lineNum = new BigDecimal(request.getParameter("lineNum")).intValue();
        Boolean bankDancerIf = bankDancerThread.getLineThreadIf().containsKey(uuid)?bankDancerThread.getLineThreadIf().get(uuid):false;
        if ("0".equals(quaId)&&bankDancerIf){//量化在运行着 并且传过来的参数是"0"
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("last",globalBuyObject.getBuyObject().get("lastNum"));
            hashMap.put("min",globalBuyObject.getBuyObject().get("minNumber"));
            hashMap.put("num",globalBuyObject.getBuyObject().get("number"));
            hashMap.put("vol",globalBuyObject.getBuyObject().get("volume"));
            if (startTime.equals(bankDancerThread.getInfo().get(uuid).get(0)[0])){//如果前端第一根数据跟后端匹配
                ArrayList<String[]> list = new ArrayList<>();
                for (int i=0;i<bankDancerThread.getInfo().get(uuid).size()-lineNum;i++){
                    list.add(bankDancerThread.getInfo().get(uuid).get(lineNum+i));
                }
                hashMap.put("line",list);
                hashMap.put("lineStatus","part");//只传一部分给前端
            }else {//不匹配则全传过去
                hashMap.put("line",bankDancerThread.getInfo().get(uuid));
                hashMap.put("lineStatus","all");
            }
            return hashMap;
        }
        return null;
    }

    /**
     *
     * @return 查询最近8小时内  分钟阳线最大的成交量 比分钟阴线最大的成交量 大1.3倍的交易对
     */
    @PostMapping("/getTopSymbol")
    @ResponseBody
    public ArrayList getTopSymbol(HttpServletRequest request){
        return topSymbolThread.getTopSymbolList();
    }


    /**
     *
     * @return 与庄共舞收益图
     */
    @PostMapping("/earningBDancer")
    @ResponseBody
    public HashMap earningBDancer(){
        return null;
    }

    /**
     *
     * @return 有你共舞收益图
     */
    @PostMapping("/earningUni")
    @ResponseBody
    public HashMap earningUni(){
        return null;
    }
}
