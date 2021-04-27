package com.numtimequantity.quantity.fileThread;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

//用来给交易对排名的线程 输出在8小时内单分钟阳线成交额大于3000万RMB的交易对和时间,并进行排名
@Component
@Slf4j
@Data
@ConfigurationProperties(prefix = "topsymbol")
public class TopSymbolThread implements Runnable{
    String SymbolStr;

    @Override
    public void run() {

    }
}
