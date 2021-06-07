package com.numtimequantity.quantity;

import com.numtimequantity.quantity.fileThread.TopSymbolThread;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Data
@Slf4j
@SpringBootTest
class QuantityApplicationTests {
@Autowired
TopSymbolThread topSymbolThread;
    @Test
    void contextLoads() {
        /*topSymbolThread.setTopSymbolThreadIf(true);
        Thread thread = new Thread(topSymbolThread);
        thread.start();*/
    }
    @Test
    void test2(){
        /*String arr[] = {Long.toString(new Date().getTime()),"0"};
        System.out.println(arr.getClass());
        ArrayList<String[]> list = new ArrayList<>();
        list.add(arr);//将指定元素添加到末尾

        System.out.println(list.get(0)[0]);*/
    }
    @Test
    void buyIfTest(){
        SimpleClientHttpRequestFactory simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(5000);//连接主机的超时时间
        simpleClientHttpRequestFactory.setReadTimeout(10000);//从主机读取数据的超时时间 只设置了ConnectionTimeout没有设置ReadTimeout，结果导致线程卡死。
        RestTemplate restTemplate = new RestTemplate(simpleClientHttpRequestFactory);
        SimpleClientHttpRequestFactory reqfac = new SimpleClientHttpRequestFactory();
        reqfac.setProxy(new Proxy(Proxy.Type.HTTP,new InetSocketAddress("127.0.0.1", 1080)));
        restTemplate.setRequestFactory(reqfac);
        ResponseEntity<ArrayList> forEntity = restTemplate.getForEntity(URI.create("https://api.binance.com/api/v3/klines?symbol=BTCUSDT&interval=1m&limit=480"), ArrayList.class);

        List<List> kRecords2 = forEntity.getBody();
        int okNumber = 0;
        int minNumber = 0;
        Double sum = 0.0;
        int inum = 0;
        Double hours = 0.0;
        Double hours2 = 0.0;
        Double hours3 = 0.0;
        Double[] rFive = {0.0,0.0,0.0};
        Boolean volumeIf = false;

        //取当前价格的千分之5作为一个常数
        Double zhangfu = new BigDecimal(kRecords2.get(0).get(4).toString()).doubleValue()/1000 * 5;
        //System.out.println("打印涨幅:"+zhangfu);

        Double divisionA=0.0;//计算比例用的
        Double divisionB=0.0;
        for (int i=180;i<kRecords2.size();i++){
            sum=0.0;
            inum=0;
            hours=0.0;
            hours2=0.0;
            hours3=0.0;
            rFive[0]=0.0;
            rFive[1]=0.0;
            rFive[2]=0.0;
            Boolean verdict = false;
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
            //两根1小时k线
            for(int iik=0;iik<60;iik++){//1小时k第二根为阳线
                hours=hours+(new BigDecimal(kRecords2.get(i-120+iik).get(4).toString()).doubleValue()-new BigDecimal(kRecords2.get(i-120+iik).get(1).toString()).doubleValue());

            }
            for(int iik2=60;iik2<120;iik2++){//第一根两小时k为阳线
                hours2=hours2+(new BigDecimal(kRecords2.get(i-180+iik2).get(4).toString()).doubleValue()-new BigDecimal(kRecords2.get(i-180+iik2).get(1).toString()).doubleValue());
            }
            //两小时k线涨幅
            for (int iik3=0;iik3<120;iik3++){
                hours=hours3+(new BigDecimal(kRecords2.get(i-120+iik3).get(4).toString()).doubleValue()-new BigDecimal(kRecords2.get(i-120+iik3).get(1).toString()).doubleValue());
            }
            //综合条件判断满足条件总次数  在统计8小时的时候把 hours <0 加上了,统计当前时不加
            if (inum>5 || sum<zhangfu ||!volumeIf||
                    hours <0 || hours2<0 || hours3<0){
            }else {
                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = sf.format(kRecords2.get(i).get(0));
                log.debug("看看合格那次的时间是:{}",time);
                okNumber++;
            }
            if (inum<=5 && i<kRecords2.size()-15){
                i=i+14;
                minNumber++;
            }
            //计算比例用的
            if (new BigDecimal(kRecords2.get(i).get(4).toString()).doubleValue()-new BigDecimal(kRecords2.get(i).get(1).toString()).doubleValue()>0){
                divisionA=divisionA+new BigDecimal(kRecords2.get(i).get(7).toString()).doubleValue();//收阳的成交量
            }else {
                divisionB=divisionB+new BigDecimal(kRecords2.get(i).get(7).toString()).doubleValue();//收阴的成交量
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
        //成交量单位:千万
        log.debug("volume{}",new BigDecimal(volume).setScale(2, RoundingMode.HALF_DOWN).doubleValue());
        log.debug("number{}",okNumber);
        log.debug("minNumber{}",minNumber);
        log.debug("lastNum{}",(15-inum));
        log.debug("看下近8小时的多空比例值,如果这个值大于1说明多头{}",divisionA/divisionB);

        if (rFive[0]<0 || rFive[1]<0 || rFive[2]<0 ||
                inum>5 || sum < zhangfu ||
                hours2 <0 || hours3<0){
            //System.out.println("现在是否合格:false");
        }
    }
}
