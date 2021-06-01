package com.numtimequantity.quantity.fileThread;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
            restTemplate.setRequestFactory(reqfac);
        }
        return restTemplate;
    }

    @Override
    public void run() {
        while (this.topSymbolThreadIf){
            try {
                Thread.sleep(6000);
                String[] split = this.getSymbolStr().split("\\|");
                ArrayList<HashMap> topSymbol = new ArrayList<>();//用来存每一个交易对的map
                for (String symbol:split){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    ResponseEntity<ArrayList> forEntity = this.getRestTemplate().getForEntity(URI.create("https://api.binance.com/api/v3/klines?symbol=" + symbol + "&interval=1m&limit=480"), ArrayList.class);
                    List<List> li = forEntity.getBody();
                    Double volume=0.0;
                    long time = 0;
                    Double minVolume=0.0;//用来记录成交量最大的那根阴线的成交量
                    long minTime = 0;//用来记录时间戳
                    //选出这个交易对480根k线中成交量最大的那根
                    for (int i=0;i<479;i++){//不算当前这根,所以是479
                        List list = li.get(i);
                        if(this.getDoubleFromStr(list.get(4).toString())-this.getDoubleFromStr(list.get(1).toString())>0
                            &&volume<this.getDoubleFromStr(list.get(7).toString())*6.4/10000000){
                            volume=this.getDoubleFromStr(list.get(7).toString())*6.4/10000000;//单位是千万人民币  汇率6.4
                            time=(long)list.get(0);
                        }
                        //选出成交量最大的那根阴线
                        if(this.getDoubleFromStr(list.get(4).toString())-this.getDoubleFromStr(list.get(1).toString())<0
                                &&volume<this.getDoubleFromStr(list.get(7).toString())*6.4/10000000){
                            minVolume=this.getDoubleFromStr(list.get(7).toString())*6.4/10000000;//单位是千万人民币  汇率6.4
                            minTime=(long)list.get(0);
                        }
                    }
                    hashMap.put("bigVol",volume);//将最大的成交量值存进去,单位千万
                    hashMap.put("time",time);//把发生异动的时间存进去
                    hashMap.put("minVol",minVolume);//480根k线当中成交量最大的阴线
                    hashMap.put("minTime",minTime);//时间戳
                    hashMap.put("symbol",symbol);//把交易对名称存进去
                    topSymbol.add(hashMap);//存储一个完成
                }
                ArrayList<HashMap> topSymbolInfo = new ArrayList<>();
                //接下来对topSymbol的成交量进行从大到小排序
                int size = topSymbol.size();
                for (int index=0;index<size;index++){
                    Double maxVol=0.0;//标记最大值
                    int num = 0; //标记最大值map在list当中的索引
                    for (int del=0;del<topSymbol.size();del++){
                        if (maxVol<(Double) topSymbol.get(del).get("bigVol")){
                            maxVol=(Double) topSymbol.get(del).get("bigVol");//选出阳线中成交量最大的值
                            num=del;//记录索引值
                        }
                    }
                    //如果成交量最大的阳线的量除以成交量最大的阴线的成交量大于1.3倍 则把这个交易对存起来
                    if ((Double)topSymbol.get(num).get("bigVol")/(Double)topSymbol.get(num).get("minVol")>1.1){
                        //遍历完得到最大值和索引
                        topSymbolInfo.add(topSymbol.get(num));//按顺序放入最大的值  添加值末尾
                        topSymbol.remove(num);//删掉选出来的索引
                    }
                }
                this.topSymbolList.clear();//先清空
                this.topSymbolList=topSymbolInfo;//再赋值
                log.debug("排序好交易对为{}",topSymbolInfo);
                /*
                * topSymbolInfo.get(i)中的数据:
                * topSymbolInfo.get(i).get("symbol")//交易对名称
                * topSymbolInfo.get(i).get("time") //异动时间
                * topSymbolInfo.get(i).get("bigVol")//异动时的成交额
                * */
                /*测试代码段↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓*/
                for (int k=0;k<topSymbolInfo.size();k++){
                    HashMap h = topSymbolInfo.get(k);
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time = sf.format(h.get("time"));
                    System.out.println("排名第"+(k+1)+"名币种为:"+h.get("symbol")+" 成交额为:"+h.get("bigVol")+"千万  异动时间为:"+time);
                }
                /*测试代码段↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑*/
                    Thread.sleep(60000);
                this.topSymbolThreadIf=true;
            }catch (Exception e){
                log.debug("线程出现报错");
            }
        }
        this.topSymbolThreadIf=false;
    }

    private Double getDoubleFromStr(String s){return new BigDecimal(s).doubleValue();}
}
