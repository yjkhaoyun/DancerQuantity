package com.numtimequantity.quantity.fileThread;



import com.alibaba.ttl.TransmittableThreadLocal;
import com.numtimequantity.quantity.bankDancerMethod.GlobalFun;
import lombok.Data;
import org.springframework.stereotype.Component;

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
@Data
@Component
public  class BankDancerThread  implements Runnable {
    private GlobalBuyObject globalBuyObject;//公共趋势判断对象
    private GlobalFun globalFun;
    private volatile ConcurrentHashMap<String, Boolean> lineThreadIf=new ConcurrentHashMap<>(); //线程开关控制 uuid 和 true||false  HashMap最多可以存1万条数据
    private ConcurrentHashMap<String,Integer> quaOutTimeThread =  new ConcurrentHashMap<>();//量化剩余的时长(分钟) 用来控制防止量化超时
    private ConcurrentHashMap<String,Integer> quaStartTimeThread = new ConcurrentHashMap<>();//量化开始的时间戳
    //线程副本区,父线程给子线程传值,平级线程不可见  一共有三个值   uuid,a和k 比如{uuid:"",a:2.2,k:2.2} 除了能存HashMap,也能存别的格式
    private ThreadLocal<HashMap<String,String>> threadLocal=new TransmittableThreadLocal<>();


    /**
     * 把与庄共舞策略搬到这来
     */
    @Override
    public void run() {//通过父线程的    this.runIfMap.get(this.threadLocal.get().get("uuid"))     来控制开关
            int globalI;
            Double account;
            Long time;
            while (this.getLineIf()){//this.runIfMap.get(this.threadLocal.get().get("uuid"))
                try {
                    Double a = this.getA();
                    Double k = this.getK();//this.getDouble((String) this.threadLocal.get().get("k"));
                    System.out.println("量化内部的值");
                    System.out.println("a  "+a+"k  "+k);
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
                    Long marketTime=0L;
                    Long recorderTime=0L;
                    Double newLastPrice;
                    Double myPosition=0.0;
                    Boolean construction = false;
                    Boolean firstIf = true;
                    ConcurrentHashMap buyObject=null;

                    /*存储策略启动时的余额*/
                    System.out.println(globalFun.account());
                    account = globalFun.account();
                    /*存储策略启动时的时间戳*/
                    time = new Date().getTime();
                    /*设置持仓方向为双向持仓*/
                    if (!globalFun.getWay()){
                        globalFun.setWay();
                    }
                    /*初始化建仓程序*/
                    while (0.0==myPosition && this.getLineIf()){
                        if(firstIf){
                            while (this.getLineIf()){
                                //buyObject=this.getBuyObject();
                                System.out.println("策略指标函数的值→→→→→→→→→→→→→→→→→→→→→→→→"+globalBuyObject.getBuyObject());
                                if((int)globalBuyObject.getBuyObject().get("number")>=1&&(int)globalBuyObject.getBuyObject().get("minNumber")>=5&&(int)globalBuyObject.getBuyObject().get("lastNum")>9){
                                    break;
                                }else {
                                    System.out.println("目前为下跌程序");
                                }
                                try {
                                    if (!this.getLineIf()){
                                        break;
                                    }
                                    Thread.sleep(30000);
                                    System.out.println("策略内线程名"+Thread.currentThread().getName());
                                }catch (Exception e){
                                    System.out.println(e);
                                }
                            }
                            firstIf = false;
                        }
                        construction = true;
                        if (!this.getLineIf()){
                            break;
                        }
                        if ((int)globalBuyObject.getBuyObject().get("minNumber")>=5){
                            buyid = globalFun.marketBuy(2*a);//市价开多
                            buyid_ = globalFun.marketSell(a);//市价开空
                            System.out.println("市价追涨");
                        }else {
                            newLastPrice = globalFun.lastPrice();
                            if(sun==0.0){
                                ying = newLastPrice + k + 1;
                                sun = newLastPrice -k - 1;
                            }
                            if (newLastPrice > sun + k ){
                                sun = newLastPrice - k;
                                System.out.println("等待下单中,勿追涨...");
                            }
                            if(newLastPrice<sun){
                                buyid = globalFun.marketBuy(2*a);
                                buyid_ = globalFun.marketSell(a);
                                System.out.println("市价成交");
                            }
                        }
                        try {
                            Thread.sleep(60000);
                        }catch (Exception e){

                        }
                        myPosition = globalFun.position().get("up");

                    }
                    if (!this.getLineIf()){
                        break; //while后面紧跟停止跳出
                    }

                    recorderTime = (Long) globalBuyObject.getBuyObject().get("time");
                    newLastPrice = globalFun.lastPrice();
                    Double newPositionPrice = globalFun.position().get("upPrice");

                    ArrayList<Double> yings = new ArrayList<>();
                    ArrayList<Double> suns = new ArrayList<>();
                    int ii;
                    //ii 0的值不可能是空值
                    BigDecimal bigDecimal = new BigDecimal(myPosition/(2*a));
                    ii = bigDecimal.setScale(0, RoundingMode.DOWN).intValue();
                    globalI=ii;
                    if (ii>0){
                        for (int size=0;size<ii;size++){
                            yings.add(0.0);
                            suns.add(0.0);
                        }
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
                        //当前实际盈亏
                        Double allAccountProfit = this.getDoubleNum(2,globalFun.accountNow(Long.toString(time))-account);//真实余额减去策略启动时的余额等于真实盈亏
                        System.out.println("收益:"+allAccountProfit+"USDT");

                        //打印收益

                        myPosition = globalFun.position().get("up");//持仓张数,持仓数量
                        /*↓ 多头情况上下通道下单↓*/
                        if(0 != myPosition && this.getLineIf()){ //
                            if(ii-globalI>0){ //交易一圈回来 ii在后面会变化
                                Double averagePrice = 0.0;
                                for( int i = globalI; i < ii; i++){
                                    averagePrice = averagePrice + yings.get(i-1); //计算每次止盈价的平均值
                                }
                                ying = averagePrice/(ii-globalI+1);
                                System.out.println("循环第: "+(ii-globalI)+"圈"+"ying的值是:"+ying);
                            }else {
                                ying = yings.get(ii-1);
                                System.out.println("快止盈的时候ying的值是:"+ying);
                            }
                            /*循环指标判断程序*/
                            while (this.getLineIf()){

                                try{
                                    Thread.sleep(20000);
                                }catch (Exception e){

                                }
                                newLastPrice = globalFun.lastPrice();
                                if (newLastPrice<suns.get(ii-1)&&(int)globalBuyObject.getBuyObject().get("lastNum")>9){
                                    globalFun.marketCloseAllProfit("SHORT"); //市价平空,平仓全部
                                }
                                if (suns.get(ii-1)+k-newLastPrice>(suns.get(ii-1)+k)*0.08){ //现价低于止损价x%时止损
                                    globalFun.marketCloseAllProfit("LONG");//平多头时输入LONG
                                    System.out.println("触发止损1");
                                    break;
                                }
                                if ((Long)globalBuyObject.getBuyObject().get("time")-recorderTime<15*60*1000){//现在时间减去上一次时间大于15分钟
                                    try{
                                        Thread.sleep(60000);
                                    }catch (Exception e){

                                    }
                                }else if (!(Boolean) globalBuyObject.getBuyObject().get("buyIfOk")){ //如果下单条件不成立则进来看看需不需要止损
                                    //8小时内"buyIfOk"为通过的次数小于3次  并且是  初始化仓位   并且  现价比止盈价高
                                    if (ii <=globalI && newLastPrice > ying){
                                        miniimaxIf = 3; //暂时先让三级循环一区执行此情况
                                        break;
                                    }else if (ii > globalI && newLastPrice > yings.get(ii-1)){
                                        miniimaxIf = 4;
                                        break;
                                    }
                                    try{
                                        Thread.sleep(40000);
                                    }catch (Exception e){

                                    }
                                }else if ((int)globalBuyObject.getBuyObject().get("number")>=1&&(int)globalBuyObject.getBuyObject().get("minNumber")>=5&&(Double)globalBuyObject.getBuyObject().get("volume")>=3.0){//尝试性操作,8小时符合条件的次数在1次或以上就下单2021年3月7日添加
                                    if (ii<=globalI){ //初始化仓位时回到三级循环一区
                                        miniimaxIf = 1;
                                        break;
                                    }else {//有额外仓位时 进入三级循环二区
                                        miniimaxIf = 2;
                                        break;
                                    }
                                }
                                System.out.println("检测时间间隔:"+((Long)globalBuyObject.getBuyObject().get("time")-recorderTime));
                                System.out.println("策略内线程名"+Thread.currentThread().getName());
                            }
                            if (!this.getLineIf()){
                                break; //while后面紧跟停止跳出
                            }
                        }else {
                            break;
                        }
                        ConcurrentHashMap<String, Double> positionNew = globalFun.position();//更新最新持仓信息

                        if (!this.getLineIf()){
                            break;
                        }
                        if (miniimaxIf == 3){
                            globalFun.marketCloseAllProfit("LONG");//平多头时输入LONG  平仓全部
                            if (positionNew.get("down")!=0.0){
                                globalFun.marketCloseAllProfit("SHORT");//平空头时输入SHORT  平仓全部
                            }
                            System.out.println("触发三级三区的止盈");
                            ii--;
                        }else if (miniimaxIf == 4){
                            globalFun.marketCloseBuy(2*a);
                            if (positionNew.get("down")!=0.0){
                                globalFun.marketCloseAllProfit("SHORT");//平空头时输入SHORT  平仓全部
                            }
                            ii--;
                            System.out.println("三级循环四区止盈");
                        }
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
                                try{
                                    Thread.sleep(15000);
                                }catch (Exception e){

                                }
                                sellidAllAttribute = globalFun.come(sellidAll);
                                System.out.println("止盈平仓全部 止盈3-1");
                            }else if (newLastPrice < suns.get(ii-1)){
                                buyid = globalFun.marketBuy(2*a);//市价开多
                                globalFun.marketSell(a);
                                buyidOk = true;
                                try{
                                    Thread.sleep(15000);
                                }catch (Exception e){

                                }
                                buyidAttribute = globalFun.come(buyid);
                                System.out.println("市价买入 建仓3-1");
                            }
                            /*判断两个订单状态*/
                            if (buyidOk){
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
                            }else  if (sellidAllOk){
                                ii--;
                                yings.add(ii-1,globalFun.getDouble((String)sellidAllAttribute.get("avgPrice"))+k);
                                suns.add(ii-1,globalFun.getDouble((String)sellidAllAttribute.get("avgPrice"))-k);
                                recorderTime = (Long) globalBuyObject.getBuyObject().get("time");
                                break;
                            }
                            try {
                                Thread.sleep(3000);
                            }catch (Exception e){}
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
                                try {
                                    Thread.sleep(15000);
                                }catch (Exception e){

                                }
                                sellidAttribute = globalFun.come(sellid);
                            }else if (newLastPrice<suns.get(ii-1)){
                                buyid = globalFun.marketBuy(2*a);
                                globalFun.marketSell(a);
                                buyidOk2 = true;
                                try {
                                    Thread.sleep(15000);
                                }catch (Exception e){

                                }
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
                            try {
                                Thread.sleep(3000);
                            }catch (Exception e){}
                            newLastPrice = globalFun.lastPrice();
                        }
                    }
                }catch (Exception e){}

            }
            System.out.println("策略线程被终止");

    }
    /*每个线程的的开关控制器*/
    private Boolean getLineIf(){
        //if (this.lineThreadIf.get(this.getThreadLocal().get().get("uuid"))&&this.quaOutTimeThread){}

        return this.lineThreadIf.get(this.getThreadLocal().get().get("uuid"));
    }
    private Double getA(){
        return new BigDecimal(this.getThreadLocal().get().get("a")).doubleValue() ; //下单额
    }
    private Double getK(){
        return new BigDecimal(this.getThreadLocal().get().get("k")).doubleValue() ; //跨度
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
}

