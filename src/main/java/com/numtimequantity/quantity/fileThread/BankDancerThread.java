package com.numtimequantity.quantity.fileThread;



import com.alibaba.ttl.TransmittableThreadLocal;
import com.numtimequantity.quantity.bankDancerMethod.GlobalFun;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 与庄共舞线程,为了不在线程类里使用@Autowired,有两种方式开启多线程
 * 1.将线程类加入spring容器,在开启线程前先给属性赋值进行初始化
 * 2.创建线程类对象时利用构造函数传参,让线程的属性享受bean中的公共变量
 */
@Slf4j
@Data
@Component
public  class BankDancerThread  implements Runnable {
    private GlobalBuyObject globalBuyObject;//公共趋势判断对象
    private volatile ConcurrentHashMap<String, Boolean> lineThreadIf=new ConcurrentHashMap<>(); //线程开关控制 uuid 和 true||false  HashMap最多可以存1万条数据
    private ConcurrentHashMap<String,Integer> quaOutTimeThread =  new ConcurrentHashMap<>();//量化剩余的时长(分钟数) 用来控制防止量化超时
    private ConcurrentHashMap<String,Long> quaStartTimeThread = new ConcurrentHashMap<>();//量化开始的时间戳
    //线程副本区,父线程给子线程传值,平级线程不可见  一共有六个值   uuid,a和k acc api 和 secretApi 比如{uuid:"",a:2.2,k:2.2} 除了能存HashMap,也能存别的格式
    private ThreadLocal<HashMap<String,String>> threadLocal=new TransmittableThreadLocal<>();
    private HashMap<String,ArrayList<String[]>> info = new HashMap<>();//折线图的数据
    //用来存储开启的是多少金额什么等级的量化
    private HashMap<String,HashMap<String,String>> quaMoneyAndLv = new HashMap<>();

    /**
     * 把与庄共舞策略搬到这来
     */
    @Override
    public void run() {//通过父线程的    this.runIfMap.get(this.threadLocal.get().get("uuid"))     来控制开关
            /********设置超时时间************/
            SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
            simpleClientHttpRequestFactory.setConnectTimeout(5000);//连接主机的超时时间
            simpleClientHttpRequestFactory.setReadTimeout(10000);//从主机读取数据的超时时间 只设置了ConnectionTimeout没有设置ReadTimeout，结果导致线程卡死。
            /****************************/
            RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
            String uuid = this.getThreadLocal().get().get("uuid");
            Double a = new BigDecimal(this.getThreadLocal().get().get("a")).doubleValue();
            Double dou_K = new BigDecimal(this.getThreadLocal().get().get("k")).doubleValue();//传过来的k是个百分数   比如1.5就是1.5%;
            Double acc = new BigDecimal(this.getThreadLocal().get().get("acc")).doubleValue();//前端传来的，选择的量化金额  500u 1000u
            String apiKey = this.getThreadLocal().get().get("apiKey");
            String secretKey = this.getThreadLocal().get().get("secretKey");
            GlobalFun globalFun = new GlobalFun(restTemplate, apiKey, secretKey);
            Double k = 0.0;

            int globalI=0;
            Double account=0.0;
            Long time;//用于计算存在划转时启动时候的真是余额
            /*存储策略启动时的余额*/
            try {
                k = globalFun.lastPrice() * dou_K / 100;
                Thread.sleep(30000);
                //第1处权重 权重：5  期货每分钟权重上限2400
                account = globalFun.account();
                if (account<acc){//如果账户真实余额小于前端选择的余额量化就不能开启
                    this.lineThreadIf.put(uuid,false);
                }
                log.info("开启量化成功，开启者uuid是：{}",uuid+" a是"+a+"  k是"+ k);
            }catch (Exception e){
                log.info("uuid{}",uuid);
                log.info("开启量化时报错终止{}",e);
                this.lineThreadIf.put(uuid,false);
            }

            while (this.getLineIf()){//this.runIfMap.get(this.threadLocal.get().get("uuid"))
                //System.out.println("剩余分钟数");
                //System.out.println(this.quaOutTimeThread.get(this.getThreadLocal().get().get("uuid")));
                try {
                    Double ying = 0.0;
                    Double sun = 0.0;
                    String sellid;
                    String buyid;
                    String buyid_;
                    String sellidAll;
                    int miniimaxIf;
                    ConcurrentHashMap sellidAttribute=new ConcurrentHashMap();
                    ConcurrentHashMap buyidAttribute=new ConcurrentHashMap();
                    ConcurrentHashMap sellidAllAttribute=new ConcurrentHashMap();
                    Long recorderTime=0L;
                    Double newLastPrice;
                    Double myPosition=0.0;
                    Boolean construction = false;
                    Boolean firstIf = true;


                    /*存储策略启动时的时间戳*/
                    time = new Date().getTime();
                    /*设置持仓方向为双向持仓  查询期货权重接口  */
                    //第2处权重 权重：1  期货每分钟权重上限2400
                    try {
                        globalFun.setWay();
                    }catch (Exception e){ }
                    log.info("进入初始化建仓前记录uuid为{}",uuid);
                    /*初始化建仓程序*/
                    while (0.0==myPosition && this.getLineIf()){
                        if(firstIf){
                            while (this.getLineIf() && 0.0==myPosition){
                                try {
                                    //System.out.println("策略指标函数的值→→→→→→→→→→→→→→→→→→→→→→→→"+globalBuyObject.getBuyObject());
                                    if((Boolean) globalBuyObject.getBuyObject().get("buyIfOk")&&(int)globalBuyObject.getBuyObject().get("minNumber")>=5){
                                        //System.out.println("初始化检测指标合格,跳出循环");
                                        break;
                                    }else {
                                        //System.out.println("目前为下跌程序");
                                    }
                                    this.quantitySleep30();//休眠30秒
                                    this.quantitySleep30();//休眠30秒
                                }catch (Exception e){
                                    log.info("初始化建仓循环检测指标处报错{}",e);
                                }
                                try {
                                    //第3处权重 权重：5  期货每分钟权重上限2400
                                    myPosition = globalFun.position().get("up");
                                }catch (Exception e){
                                    log.info("初始化建仓里面的获取持仓出现报错{}",e);
                                    myPosition=0.0;
                                }
                            }
                            firstIf = false;
                        }
                        construction = true;
                        //////////////////////////////////
                        if (!this.getLineIf()||0.0!=myPosition){
                            //System.out.println("第一个跳出程序");
                            break;
                        }
                        if ((int)globalBuyObject.getBuyObject().get("minNumber")>=5){
                            /******************************************/
                            buyid = globalFun.marketBuy(2*a);//市价开多
                            buyid_ = globalFun.marketSell(a);//市价开空
                            //System.out.println("市价追涨");
                        }else {
                            //第4处权重 权重：1  期货每分钟权重上限2400
                            newLastPrice = globalFun.lastPrice();
                            if(sun==0.0){
                                ying = newLastPrice + k + 1;
                                sun = newLastPrice -k - 1;
                            }
                            if (newLastPrice > sun + k ){
                                sun = newLastPrice - k;
                                //System.out.println("等待下单中,勿追涨...");
                            }
                            if(newLastPrice<sun){
                                buyid = globalFun.marketBuy(2*a);
                                buyid_ = globalFun.marketSell(a);
                                //System.out.println("市价成交");
                            }
                        }
                        this.quantitySleep30();//休眠30秒
                        this.quantitySleep30();//休眠30秒
                        try {
                            //第5处权重 权重：5  期货每分钟权重上限2400
                            myPosition = globalFun.position().get("up");
                        }catch (Exception e){
                            log.info("初始化建仓的尾部获取持仓处出现报错{}",e);
                            myPosition=0.0;
                        }
                    }
                    this.quantitySleep30();//休眠30秒
                    this.quantitySleep30();//休眠30秒
                    if (!this.getLineIf()){
                        break; //while后面紧跟停止跳出
                    }
                    try {
                        recorderTime = (Long) globalBuyObject.getBuyObject().get("time");
                    }catch (Exception e){
                        recorderTime = new Date().getTime();
                        log.info("获取公共指标函数的时间戳强制转成Long类型时报错{}",e);
                    }
                    //第6处权重 权重：1  期货每分钟权重上限2400
                    newLastPrice = globalFun.lastPrice();
                    //第7处权重 权重：5  期货每分钟权重上限2400
                    Double newPositionPrice = globalFun.position().get("upPrice");
                    this.quantitySleep30();//休眠30秒
                    this.quantitySleep30();//休眠30秒
                    ArrayList<Double> yings = new ArrayList<>();
                    ArrayList<Double> suns = new ArrayList<>();
                    int ii;
                    //ii 0的值不可能是空值
                    BigDecimal bigDecimal = new BigDecimal(myPosition/(2*a));
                    ii = bigDecimal.setScale(0, RoundingMode.UP).intValue();//进一法  ii不可能等于0
                    globalI=ii;
                    for (int size=0;size<ii;size++){//能到这一步ii肯定是大于0的
                        yings.add(0.0);
                        suns.add(0.0);
                    }
                    yings.set(ii-1,globalFun.getPriceNewduo(newLastPrice,newPositionPrice,k));
                    if (construction==true){//如果执行了建仓程序
                        if(newLastPrice<newPositionPrice){
                            suns.set(ii-1,newLastPrice-k);
                        }else {
                            suns.set(ii-1,newPositionPrice-k);
                        }

                    }else {
                        suns.set(ii-1,newLastPrice-k);
                    }

                    /*二级循环*/
                    while (this.getLineIf()){
                        miniimaxIf = 0;
                        //当前实际盈亏 第8处权重 权重：5  现货每分钟权重上限1200
                        Double allAccountProfit = this.getDoubleNum(2,globalFun.accountNow(Long.toString(time))-account);//真实余额减去策略启动时的余额等于真实盈亏
                        log.info("uuid为{}",uuid);
                        log.info("进入二级循环指标前打印实际盈亏{}",allAccountProfit);
                        //System.out.println("收益:"+allAccountProfit+"USDT");
                        //打印收益    第9处权重 权重：5  期货每分钟权重上限2400
                        myPosition = globalFun.position().get("up");//持仓张数,持仓数量
                        this.quantitySleep30();//休眠30秒
                        this.quantitySleep30();//休眠30秒
                        //存储实际盈亏
                        String arr[] = {Long.toString(new Date().getTime()),allAccountProfit.toString()};
                        this.getInfo().get(uuid).add(arr);

                        /*↓ 多头情况上下通道下单↓*/
                        if(0.0 != myPosition && this.getLineIf()){ //
                            if(ii-globalI>0){ //交易一圈回来 ii在后面会变化
                                Double averagePrice = 0.0;
                                for( int i = globalI; i < ii; i++){
                                    averagePrice = averagePrice + yings.get(i-1); //计算每次止盈价的平均值
                                }
                                ying = averagePrice/(ii-globalI+1);
                                //System.out.println("循环第: "+(ii-globalI)+"圈"+"ying的值是:"+ying);
                            }else {
                                ying = yings.get(ii-1);
                                //System.out.println("快止盈的时候ying的值是:"+ying);
                            }

                            /*循环指标判断程序*/
                            Boolean closeAllDownIf = true;//用来控制下面的平全部空仓程序只执行一次
                            while (this.getLineIf()){
                                try {
                                    this.quantitySleep30();//休眠30秒
                                    this.quantitySleep30();//休眠30秒
                                    //第10处权重 权重：1  期货每分钟权重上限2400
                                    newLastPrice = globalFun.lastPrice();
                                    if (closeAllDownIf&&newLastPrice<suns.get(ii-1)&&(int)globalBuyObject.getBuyObject().get("lastNum")>9){
                                        globalFun.marketCloseAllProfit("SHORT"); //市价平空,平仓全部
                                        closeAllDownIf=false;//用来控制这里的平全部空仓程序只执行一次  有时空仓可能没有仓位，所以允许一次容错
                                    }
                                    if (suns.get(ii-1)+k-newLastPrice>(suns.get(ii-1)+k)*0.08){ //现价低于止损价x%时止损
                                        globalFun.marketCloseAllProfit("LONG");//平多头时输入LONG
                                        //System.out.println("触发止损1");
                                        break;
                                    }
                                    if ((Long)globalBuyObject.getBuyObject().get("time")-recorderTime<15*60*1000){//现在时间减去上一次时间大于15分钟
                                        this.quantitySleep30();//休眠30秒
                                        this.quantitySleep30();//休眠30秒
                                    }else if (!(Boolean) globalBuyObject.getBuyObject().get("buyIfOk")){ //如果下单条件不成立则进来看看需不需要止损
                                        if (ii <=globalI && newLastPrice > ying){
                                            miniimaxIf = 3; //暂时先让三级循环一区执行此情况
                                            break;
                                        }else if (ii > globalI && newLastPrice > yings.get(ii-1)){
                                            miniimaxIf = 4;
                                            break;
                                        }
                                        this.quantitySleep30();//休眠30秒
                                    }else if ((int)globalBuyObject.getBuyObject().get("number")>=2){//20210610尝试性操作,目前还在犹豫是3还是2    8小时符合条件的次数在3次或以上就下单2021年6月8日添加
                                        if (ii<=globalI){ //初始化仓位时回到三级循环一区
                                            miniimaxIf = 1;
                                            break;
                                        }else {//有额外仓位时 进入三级循环二区
                                            miniimaxIf = 2;
                                            break;
                                        }
                                    }
                                    //System.out.println("检测时间间隔:"+((Long)globalBuyObject.getBuyObject().get("time")-recorderTime));
                                }catch (Exception e){
                                    log.info("中间循环检测部分报错,忽略继续循环{}",e);
                                }

                            }
                            if (!this.getLineIf()){
                                break; //while后面紧跟停止跳出
                            }
                        }else {
                            break;
                        }
                        //第11处权重 权重：5  期货每分钟权重上限2400
                        ConcurrentHashMap<String, Double> positionNew = globalFun.position();//更新最新持仓信息
                        if (!this.getLineIf()){
                            break;
                        }
                        log.info("已经跳出了二级循环,打印看下进到了哪个区{}",miniimaxIf);
                        log.info("uuid为{}",uuid);

                        if (miniimaxIf == 3){
                            globalFun.marketCloseAllProfit("LONG");//平多头时输入LONG  平仓全部
                            if (positionNew.get("down")!=0.0){
                                globalFun.marketCloseAllProfit("SHORT");//平空头时输入SHORT  平仓全部
                            }
                            ii--;
                        }else if (miniimaxIf == 4){
                            globalFun.marketCloseBuy(2*a);
                            if (positionNew.get("down")!=0.0){
                                globalFun.marketCloseAllProfit("SHORT");//平空头时输入SHORT  平仓全部
                            }
                            ii--;
                        }

                        //第12处权重 权重：1  期货每分钟权重上限2400
                        newLastPrice = globalFun.lastPrice();//更新最新价格
                        /*↓三级循环1区↓*/
                        Boolean ifsuns = true;
                        while (miniimaxIf == 1 && this.getLineIf()){
                            Double originalSun = suns.get(ii-1);
                            //刚进来时现价分三种情况:1.newLastPrice 位于上半部分  2.newLastPrice 位于下半部分  3.newLastPrice 位于suns.get(ii)下面
                            if (ifsuns && newLastPrice - k < suns.get(ii-1)){ //情况2 刚进来时允许通过一次
                                suns.add(ii-1,newLastPrice-k-6);
                            }

                            if (newLastPrice-k-suns.get(ii-1)>5 && originalSun-5>suns.get(ii-1)){//情况2第一次时通过  情况1并且suns[ii]有被调低过时通过  情况3会被拦截
                                suns.add(ii-1,newLastPrice - k);
                                ifsuns = false;
                            }
                            buyidAttribute.clear(); //清空两个变量的值
                            sellidAllAttribute.clear();
                            Boolean sellidAllOk = false;
                            Boolean buyidOk = false;
                            if (newLastPrice>ying){
                                sellidAll = globalFun.marketCloseAllProfit("LONG");//市价平掉多头所以订单
                                if (positionNew.get("down")!=0.0){
                                    globalFun.marketCloseAllProfit("SHORT");//平空头时输入SHORT  平仓全部
                                }
                                sellidAllOk = true;
                                //第13处权重 权重：1  期货每分钟权重上限2400
                                sellidAllAttribute = globalFun.come(sellidAll);
                            }else if (newLastPrice < suns.get(ii-1)){
                                buyid = globalFun.marketBuy(2*a);//市价开多
                                globalFun.marketSell(a);
                                buyidOk = true;
                                //并列第13处权重 权重：1  期货每分钟权重上限2400
                                buyidAttribute = globalFun.come(buyid);
                            }
                            /*判断两个订单状态*/
                            if (buyidOk){//又补了一份仓
                                ii++;
                                if (yings.size()<=ii-1){
                                    yings.add(globalFun.getDouble((String) buyidAttribute.get("avgPrice"))+k);
                                    suns.add(globalFun.getDouble((String) buyidAttribute.get("avgPrice"))-k);
                                }else{
                                    yings.add(ii-1,globalFun.getDouble((String) buyidAttribute.get("avgPrice"))+k);
                                    suns.add(ii-1,globalFun.getDouble((String) buyidAttribute.get("avgPrice"))-k);
                                }
                                recorderTime = (Long) globalBuyObject.getBuyObject().get("time");
                                break;
                            }else  if (sellidAllOk){//平仓全部了，可以什么都不做
                                ii--;//清空了仓位以后以后把ii清零
                                recorderTime = (Long) globalBuyObject.getBuyObject().get("time");
                                break;
                            }
                            this.quantitySleep30();//休眠30秒
                            this.quantitySleep30();//休眠30秒
                            this.quantitySleep30();//休眠30秒
                            newLastPrice = globalFun.lastPrice();
                        }
                        /*↓ 三级循环2区 ↓*/
                        Boolean ifsuns2 = true;
                        while (miniimaxIf == 2&&this.getLineIf()){
                            Double originalSun2 = suns.get(ii-1);
                            if (ifsuns2 && newLastPrice-k<suns.get(ii-1)){
                                suns.add(ii-1,newLastPrice-k+6);
                            }
                            /*能进来就是市价成交,以进不进来为风险控制,不存在现价比sun[ii]低还得慢慢追涨的情况*/
                            if (newLastPrice-k-suns.get(ii-1)>5 && originalSun2-5>suns.get(ii-1)){
                                suns.add(ii-1,newLastPrice-k);
                                ifsuns2 = false;
                            }
                            sellidAttribute.clear();
                            buyidAttribute.clear();
                            Boolean sellidOk = false;
                            Boolean buyidOk2 = false;
                            if (newLastPrice>yings.get(ii-1)){
                                sellid = globalFun.marketCloseBuy(2*a);
                                if (positionNew.get("down")!=0.0){
                                    globalFun.marketCloseAllProfit("SHORT");//平空头时输入SHORT  平仓全部
                                }
                                sellidOk = true;
                                this.quantitySleep30();//休眠30秒
                                this.quantitySleep30();//休眠30秒
                                sellidAttribute = globalFun.come(sellid);
                            }else if (newLastPrice<suns.get(ii-1)){
                                buyid = globalFun.marketBuy(2*a);
                                globalFun.marketSell(a);
                                buyidOk2 = true;
                                this.quantitySleep30();//休眠30秒
                                this.quantitySleep30();//休眠30秒
                                buyidAttribute = globalFun.come(buyid);
                            }
                            //如果成交跳出
                            if (sellidOk){
                                ii--;
                                recorderTime = (Long) globalBuyObject.getBuyObject().get("time");
                                break;
                            }else if (buyidOk2){
                                ii++;
                                if (yings.size()<=ii-1){
                                    yings.add(globalFun.getDouble((String)buyidAttribute.get("avgPrice"))+k);
                                    suns.add(globalFun.getDouble((String)buyidAttribute.get("avgPrice"))-k);
                                }else {
                                    yings.add(ii-1,globalFun.getDouble((String)buyidAttribute.get("avgPrice"))+k);
                                    suns.add(ii-1,globalFun.getDouble((String)buyidAttribute.get("avgPrice"))-k);
                                }
                                recorderTime = (Long) globalBuyObject.getBuyObject().get("time");
                                break;
                            }
                            this.quantitySleep30();//休眠30秒
                            this.quantitySleep30();//休眠30秒
                            newLastPrice = globalFun.lastPrice();
                        }
                    }
                }catch (Exception e){
                    log.info("报错 量化程序终止,忽略继续重新开始循环运行{}",e);
                }
            }
            this.quaOutTimeThread.remove(uuid);
            this.quaStartTimeThread.remove(uuid);
            this.lineThreadIf.remove(uuid);
            this.getInfo().remove(uuid);
            this.threadLocal.remove();
            //System.out.println("策略线程被终止");
    }

    /*每个线程的的开关控制器  这里包含了量化是否过期 通过开关控制和超时时间来控制量化线程是否终止  */
    private Boolean getLineIf(){
        String uuid = this.getThreadLocal().get().get("uuid");
        //System.out.println("打印控制器的两个值看下 正常上面比下面小为****:");
       // System.out.println((new Date().getTime()/1000-this.quaStartTimeThread.get(uuid))/1000);
       // System.out.println(this.quaOutTimeThread.get(uuid));
        if (this.lineThreadIf.get(uuid)&&
                (new Date().getTime()-this.quaStartTimeThread.get(uuid))/(1000*60)<this.quaOutTimeThread.get(uuid)){
            return true;
        }
        return false;
    }

    /**
     * 将小数保留小数点后面的位数
     * @param m  小数点后面的位数
     * @param a  小数
     * @return
     */
    public Double getDoubleNum(int m,Double a){
        return new BigDecimal(a).setScale(m, RoundingMode.HALF_DOWN).doubleValue();
    }

    /**
     * 休眠30秒
     */
    private void quantitySleep30(){
        try {
            Thread.sleep(35000);//休眠35秒
        }catch (Exception e){}
    }
}

