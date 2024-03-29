package com.numtimequantity.quantity.fileThread;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

//用来给交易对排名的线程 输出在8小时内单分钟阳线成交额大于3000万RMB的交易对和时间,并进行排名
@Component
@Slf4j
@Data
@ConfigurationProperties(prefix = "topsymbol")
public class TopSymbolThread implements Runnable{
    private String SymbolStr;
    private Boolean topSymbolThreadIf=false;//记录线程是否启动了
    private ArrayList<HashMap> topSymbolList=new ArrayList<>();//按照大于3000万的排名存储交易对
    private Boolean http_proxy_if;//是否开启http代理,本地开发测试时开启,因为需要翻墙,部署的时候关闭就行了
    private int port;//如果开启http代理,http端口号,根据自己的翻墙软件来定


    public RestTemplate getRestTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        /*以下三句代码为了翻墙，实现访问国外api，打包部署的时候可以删掉*/
        if (this.getHttp_proxy_if()){
            SimpleClientHttpRequestFactory reqfac = new SimpleClientHttpRequestFactory();
            reqfac.setProxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("127.0.0.1", this.getPort())));
            reqfac.setConnectTimeout(3000);//连接主机的超时时间
            reqfac.setReadTimeout(3000);//从主机读取数据的超时时间 只设置了ConnectionTimeout没有设置ReadTimeout，结果导致线程卡死。
            restTemplate.setRequestFactory(reqfac);
        }
        return restTemplate;
    }

    @Override
    public void run() {
        while (this.topSymbolThreadIf){
            try {
                Thread.sleep(60*1000*5);//将休眠写在前面，以免报错时出现死循环
                String[] split = this.getSymbolStr().split("\\|");
                ArrayList<HashMap> topSymbol = new ArrayList<>();//用来存每一个交易对的map
                for (String symbol:split){
                    try {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        ResponseEntity<ArrayList> forEntity = this.getRestTemplate().getForEntity(URI.create("https://api.binance.com/api/v3/klines?symbol=" + symbol + "&interval=1m&limit=480"), ArrayList.class);
                        List<List> li = forEntity.getBody();
                        long time = 0;
                        ArrayList<Double> bigList = new ArrayList<>();//三根最大成交量的阳线
                        ArrayList<Double> minList = new ArrayList<>();//三根最大成交量的阴线
                        Double volume=0.0;
                        Double minVolume=0.0;
                        long minTime = 0;//用来记录时间戳
                        Double sum=0.0;//统计最近四小时的涨幅
                        for (int sumI=0;sumI<li.size();sumI++){
                            sum=sum+new BigDecimal(li.get(sumI).get(4).toString()).doubleValue()-new BigDecimal(li.get(sumI).get(1).toString()).doubleValue();

                        }
                        //选出这个交易对480根k线中成交量最大的3根
                        for (int n=0;n<3;n++){
                            int ia=0;
                            int ib=0;
                            for (int i=0;i<li.size();i++){
                                List list = li.get(i);
                                if(this.getDoubleFromStr(list.get(4).toString())-this.getDoubleFromStr(list.get(1).toString())>0
                                        &&volume<this.getDoubleFromStr(list.get(7).toString())*6.4/1000000){
                                    volume=this.getDoubleFromStr(list.get(7).toString())*6.4/1000000;//单位是百万人民币  汇率6.4
                                    ia=i;
                                    time=(long)list.get(0);
                                }else if (minVolume<this.getDoubleFromStr(list.get(7).toString())*6.4/1000000){
                                    minVolume=this.getDoubleFromStr(list.get(7).toString())*6.4/1000000;//单位是百万人民币  汇率6.4
                                    ib=i;
                                }
                            }
                            li.remove(ia);
                            li.remove(ib);
                            bigList.add(volume);
                            minList.add(minVolume);
                        }
                        Double topVol=(bigList.get(0)+bigList.get(1)+bigList.get(2))/(minList.get(0)+minList.get(1)+minList.get(2));
                        //成交额最大的三根阳线的成交量 除以最大三根阴线成交量
                        hashMap.put("bigVol",new BigDecimal(topVol).setScale(2, RoundingMode.HALF_DOWN).doubleValue());//将最大的成交量值存进去, 单位百万  四舍五入保留1位小数
                        /*先不传异动时的时间戳给前端  但时间戳还是非常有用的  未来开发会用到*/
                        hashMap.put("time",time);//把发生异动的时间存进去
                        //总涨幅
                        hashMap.put("sumVol",new BigDecimal(sum).setScale(2, RoundingMode.HALF_DOWN).doubleValue());//480根k线当中成交量最大的阴线 单位百万 四舍五入保留1位小数
                        //三根最大分钟阳线的成交额  单位百万
                        hashMap.put("volMoney",new BigDecimal(bigList.get(0)+bigList.get(1)+bigList.get(2)).setScale(2, RoundingMode.HALF_DOWN).doubleValue());
                        /*先不传异动时的时间戳给前端  但时间戳还是非常有用的  未来开发会用到*/
                        //hashMap.put("minTime",minTime);//砸盘的时间戳
                        hashMap.put("symbol",symbol.substring(0,symbol.length()-4));//把币种名称存进去   减掉了后面的"USDT"
                        topSymbol.add(hashMap);//存储一个完成
                    }catch (Exception e){
                        //log.info("排名线程遍历交易对时出现报错{}",e);
                    }
                }
                ArrayList<HashMap> topSymbolInfo = new ArrayList<>();
                //接下来对topSymbol的成交量进行从大到小排序
                int size = topSymbol.size();
                log.debug("在交易对排序前看下，整理好的交易对列表的数量是多少size{}",size);
                for (int index=0;index<size;index++){
                    Double maxVol=0.0;//标记最大值
                    int num = 0; //标记最大值map在list当中的索引
                    for (int del=0;del<topSymbol.size();del++){
                        if (maxVol<(Double) topSymbol.get(del).get("bigVol")){
                            maxVol=(Double) topSymbol.get(del).get("bigVol");//选出阳线中成交量最大的值
                            num=del;//记录索引值
                        }
                    }
                    //遍历完得到最大值和索引
                    topSymbolInfo.add(topSymbol.get(num));//按顺序放入最大的值  添加值末尾
                    topSymbol.remove(num);//删掉选出来的索引
                }
                log.debug("排好序后看下排名结果{}",topSymbolInfo);
            //
            //    this.topSymbolList=topSymbolInfo;//再赋值
            //    log.debug("排序好交易对为{}",topSymbolInfo);
                /*
                * topSymbolInfo.get(i)中的数据:
                * topSymbolInfo.get(i).get("symbol")//交易对名称
                * topSymbolInfo.get(i).get("time") //异动时间
                * topSymbolInfo.get(i).get("bigVol")//异动时的成交额
                * */
                /*测试代码段↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓*/
               /* for (int k=0;k<topSymbolInfo.size();k++){
                    HashMap h = topSymbolInfo.get(k);
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time = sf.format(h.get("time"));
                    System.out.println("排名第"+(k+1)+"名币种为:"+h.get("symbol")+" 成交额为:"+h.get("bigVol")+"百万  异动时间为:"+time);
                }*/
                /*测试代码段↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑*/

                //选出比例是1.3倍的值，如果成交量最大的阳线的量除以成交量最大的阴线的成交量大于1.1倍 则把这个交易对存起来
                this.topSymbolList.clear();//先清空
                for (int iv=0;iv<topSymbolInfo.size();iv++){
                    Double theBigVol = (Double)topSymbolInfo.get(iv).get("bigVol");//8小时内分钟k成交量最大三根除以成交量最小三根的比值
                    Double theSumVol = (Double)topSymbolInfo.get(iv).get("sumVol");//8小时总涨幅
                    Double theVolMoney = (Double)topSymbolInfo.get(iv).get("volMoney");
                    long theTime = (long) topSymbolInfo.get(iv).get("time");
                    //最近8小时收涨 并且 最高买入除以最高卖出大于1.3倍 并且 买入大于3000万  并且  已经过去一小时了
                    if (theSumVol>0&&theBigVol>1.3&&theVolMoney>30&&new Date().getTime()-theTime>1000*60*60){
                        this.topSymbolList.add(topSymbolInfo.get(iv));//存起来
                        log.debug("龙虎榜合格数据为{}",topSymbolInfo.get(iv));
                    }
                }
                log.debug("打印最终龙虎榜数据：{}",this.topSymbolList);
                this.topSymbolThreadIf=true;
            }catch (Exception e){
                log.info("现货排名线程出现报错{}",e);
            }
        }
        this.topSymbolThreadIf=false;
    }

    private Double getDoubleFromStr(String s){return new BigDecimal(s).doubleValue();}
}
