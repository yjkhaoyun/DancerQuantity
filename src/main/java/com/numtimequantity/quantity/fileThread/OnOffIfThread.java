package com.numtimequantity.quantity.fileThread;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 此类是控制量化线程的开关和启动时向量化线程内传参
 */
@Component
@Data
public class OnOffIfThread {
    private volatile ConcurrentHashMap<String, Boolean> lineIf; //线程开关控制 uuid 和 true||false  HashMap最多可以存1万条数据
    /**
     * 线程副本区,父线程给子线程传值,平级线程不可见  一共有三个值   uuid,a和k 比如{uuid:"",a:2.2,k:2.2}
     * 除了能存HashMap,也能存别的格式
     */
    private ThreadLocal<HashMap<String,Object>> threadLocal;
    OnOffIfThread(){
        this.lineIf=new ConcurrentHashMap<>();
        this.threadLocal=new TransmittableThreadLocal<>();
    }
}
