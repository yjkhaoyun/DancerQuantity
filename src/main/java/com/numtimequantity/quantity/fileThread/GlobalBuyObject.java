package com.numtimequantity.quantity.fileThread;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;



/**
 * 运行量化程序的尾端类
 */
/*绑定yml的配置类*/
@ConfigurationProperties(prefix = "httpsetting")
@Component
@Data
@Slf4j
public class GlobalBuyObject  implements Runnable{
    private Boolean http_proxy_if;//是否开启http代理,本地开发测试时开启,因为需要翻墙,部署的时候关闭就行了
    private int port;//如果开启http代理,http端口号,根据自己的翻墙软件来定
    private volatile ConcurrentHashMap<String,Object> buyObject;//公共趋势判断变量
    private Boolean buyObjectThreadIf;//用来标记判断指标函数的公共线程是否在运行
    GlobalBuyObject(){
        this.buyObjectThreadIf=false;
        this.buyObject=new ConcurrentHashMap();
    }

    /**
     * 量化策略的所有方法和公共buyObject方法都是用的这个restTemplate来发请求
     * 如果要设置超时时间请在这里设定
     * @return  restTemplate
     */
    public RestTemplate getRestTemplate(){
        /********设置超时时间************/
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(5000);//连接主机的超时时间
        simpleClientHttpRequestFactory.setReadTimeout(10000);//从主机读取数据的超时时间 只设置了ConnectionTimeout没有设置ReadTimeout，结果导致线程卡死。
        /****************************/
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
        /*以下三句代码为了翻墙，实现访问国外api，打包部署的时候可以删掉*/
        if (this.getHttp_proxy_if()){
            SimpleClientHttpRequestFactory reqfac = new SimpleClientHttpRequestFactory();
            reqfac.setProxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("127.0.0.1", this.getPort())));
            restTemplate.setRequestFactory(reqfac);
        }
        return restTemplate;
    }

    /*用来循环公共的趋势判断函数*/
    @Override
    public void run() {
        while (this.buyObjectThreadIf){
            try {
                Thread.sleep(60500);//将休眠写在前面，以免报错时出现死循环
                this.buyIf();
                //System.out.println("公共线程"+this.getBuyObject());
                //System.out.println(Thread.currentThread());
                this.buyObjectThreadIf=true;
            }catch (Exception e){
                log.info("公共趋势判断函数正常循环时报错{}",e);
            }
        }
        this.buyObjectThreadIf=false;
    }




    /**
     * 获取480根1分钟k线  这个方法没有使用到秘钥
     * @return
     */
    public List<List> kRecords2(){
        //System.out.println("打印kRecords2方法");
        RestTemplate restTemplate = this.getRestTemplate();
        ResponseEntity<ArrayList> forEntity = restTemplate.getForEntity(URI.create("https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=1m&limit=480"), ArrayList.class);
        List<List> li = forEntity.getBody();
        return li;
    }

    /**
     *
     * @param s  传入Double字符串
     * @return  返回Double类型
     */
    public Double getDouble(String s){
        return new BigDecimal(s).doubleValue();
    }
    /**
     * 封装数据方法
     * @return  返回行情判断对象
     */
    public void buyIf(){
        //System.out.println("执行了buyIf()这个方法");

        //
        List<List> kRecords2 = this.kRecords2();
        int okNumber = 0;
        int minNumber = 0;
        Double sum = 0.0;
        int inum = 0;
        Double hours = 0.0;
        Double hours3 = 0.0;
        Boolean volumeIf = false;

        //取当前价格的千分之5作为一个常数
        Double zhangfu = new BigDecimal(kRecords2.get(0).get(4).toString()).doubleValue()/1000 * 5;
        //System.out.println("打印涨幅:"+zhangfu);

        for (int i=120;i<kRecords2.size();i++){
            sum=0.0;
            inum=0;
            hours=0.0;
            hours3=0.0;
            volumeIf = false;
            //15分钟涨幅和15分内1分钟k线阴线的个数
            for (int ik=0;ik<15;ik++){
                Double kValue = new BigDecimal(kRecords2.get(i-15+ik).get(4).toString()).doubleValue()-new BigDecimal(kRecords2.get(i-15+ik).get(1).toString()).doubleValue();
                sum=sum+(kValue);
                if (kValue<0){
                    //15分钟内1分钟k的阴线个数
                    inum++;
                }
                //如果有1根阳线k线的成交量大于3千万人民币 (美元汇率按6.4) 则条件改为true
                if (new BigDecimal(kRecords2.get(i-15+ik).get(7).toString()).doubleValue() * 6.4>30000000 &&kValue>0){
                    volumeIf = true;
                }
            }
            //最近1小时k线涨幅
            for(int iik=0;iik<60;iik++){//1小时k第二根为阳线
                hours=hours+(new BigDecimal(kRecords2.get(i-60+iik).get(4).toString()).doubleValue()-new BigDecimal(kRecords2.get(i-60+iik).get(1).toString()).doubleValue());

            }
            //最近两小时k线涨幅
            for (int iik3=0;iik3<120;iik3++){
                hours3=hours3+(new BigDecimal(kRecords2.get(i-120+iik3).get(4).toString()).doubleValue()-new BigDecimal(kRecords2.get(i-120+iik3).get(1).toString()).doubleValue());
            }
            //计算比例用的
           /* if (new BigDecimal(kRecords2.get(i).get(4).toString()).doubleValue()-new BigDecimal(kRecords2.get(i).get(1).toString()).doubleValue()>0){
            }else {
            }*/
            //综合条件判断满足条件总次数  在统计8小时的时候把 hours <0 加上了,统计当前时不加
            if (inum>5 || sum<zhangfu ||!volumeIf||
                     hours<0 || hours3<0){
            }else {
                okNumber++;
            }
            if (inum<=5 && i<kRecords2.size()-15){
                i=i+14;
                minNumber++;
            }

        }
        //最近这15根1分钟k线,成交量最大的这根阳线成交量是多少,单位人民币千万,美元汇率按6.4(不算正在发生这根)
        Double volume = 0.0;
        for(int iv=kRecords2.size()-1-15;iv<kRecords2.size()-1;iv++){
            if (new BigDecimal(kRecords2.get(iv).get(4).toString()).doubleValue()-new BigDecimal(kRecords2.get(iv).get(1).toString()).doubleValue()>0 &&
                    volume<new BigDecimal(kRecords2.get(iv).get(7).toString()).doubleValue() * 6.4/10000000 ){
                volume = new BigDecimal(kRecords2.get(iv).get(7).toString()).doubleValue() * 6.4/10000000;
            }
        }
        /*************************************************************************************************/
        //成交量单位:千万
        log.debug("volume{}",new BigDecimal(volume).setScale(2, RoundingMode.HALF_DOWN).doubleValue());
        log.debug("number{}",okNumber);
        log.debug("minNumber{}",minNumber);
        log.debug("lastNum{}",(15-inum));

        this.buyObject.clear();//先清空再赋值
        //保留两位小数  单位千万 最近这15根1分钟k线,成交量最大的这根阳线成交量是多少,单位人民币千万,美元汇率按6.4(不算正在发生这根)
        this.buyObject.put("volume",new BigDecimal(volume).setScale(2, RoundingMode.HALF_DOWN).doubleValue());
        this.buyObject.put("number",okNumber);
        this.buyObject.put("minNumber",minNumber);
        this.buyObject.put("lastNum",15-inum); //最后15分钟(当前的15分钟),1分k线阳线的个数
        this.buyObject.put("time",new Date().getTime());
        if (inum>5 || sum < zhangfu ||!volumeIf||
                hours <0 || hours3<0){
            this.buyObject.put("buyIfOk",false);//当前是否合格 合格返回true
        }else {
            this.buyObject.put("buyIfOk",true);
        }
        this.printlnCpuInfo();//加一个cpu负载和内存的统计
        log.debug("看下公共指标函数对象的值{}",this.buyObject);
    }

    private  void printlnCpuInfo(){ //cpu占用的百分比,0.1代表10% 存储到buyObject对象当中
        try {//System.out.println("----------------cpu信息----------------");
            SystemInfo systemInfo = new SystemInfo();
            CentralProcessor processor = systemInfo.getHardware().getProcessor();
            long[] prevTicks = processor.getSystemCpuLoadTicks();
            // 睡眠1s
            TimeUnit.SECONDS.sleep(1);
            long[] ticks = processor.getSystemCpuLoadTicks();
            long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
            long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
            long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
            long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
            long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
            long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
            long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
            long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
            long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
            Double total = 1.0-(idle * 1.0 / totalCpu);
            this.buyObject.put("totalCpu",total);//cpu占用的百分比,0.1代表10%
           /* System.out.println("----------------cpu信息----------------");
            System.out.println("cpu核数:" + processor.getLogicalProcessorCount());
            System.out.println("cpu系统使用率:" + new DecimalFormat("#.##%").format(cSys * 1.0 / totalCpu));
            System.out.println("cpu用户使用率:" + new DecimalFormat("#.##%").format(user * 1.0 / totalCpu));
            System.out.println("cpu当前等待率:" + new DecimalFormat("#.##%").format(iowait * 1.0 / totalCpu));
            System.out.println("cpu当前使用率:" + new DecimalFormat("#.##%").format(1.0-(idle * 1.0 / totalCpu)));
            System.out.println(total);*/
        }catch (Exception e){
            log.info("检测cpu使用率时异常{}",e);
        }
    }
}
