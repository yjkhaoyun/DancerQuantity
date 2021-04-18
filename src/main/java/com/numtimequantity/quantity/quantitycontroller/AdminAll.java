package com.numtimequantity.quantity.quantitycontroller;


import com.numtimequantity.quantity.fileThread.GlobalBuyObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 此类只有管理员可以访问
 */
@Controller
public class AdminAll {

    @Autowired
    GlobalBuyObject globalBuyObject;


    /**
     * 只执行一次
     * 整套程序部署后,第一步先开启此线程,作用全局行情判断
     *  http://localhost:801/settingBuyObject?cccccc=dddddd
     * @return
     */
    @RequestMapping("/settingBuyObject")
    @ResponseBody
    public String settingThreadTicker(){
        System.out.println(globalBuyObject);
        System.out.println(globalBuyObject.getBuyObjectThreadIf()); //公共趋势判断函数开关控制
        if (globalBuyObject.getBuyObjectThreadIf()){
            return "指标函数已经在运行了";
        }
        globalBuyObject.setBuyObjectThreadIf(true);//设为true才能运行
        System.out.println(globalBuyObject);

        //FileThreads.GlobalTicker globalTicker = new FileThreads.GlobalTicker();
        Thread thread = new Thread(globalBuyObject);
        thread.start();
        return "趋势判断函数的公共线程启动成功";
    }

}
