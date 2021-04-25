package com.numtimequantity.quantity.quantitycontroller;


import com.numtimequantity.quantity.fileThread.GlobalBuyObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 此类只有管理员可以访问
 */
@Slf4j
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
        /*启动公共趋势函数判断线程*/
        if (globalBuyObject.getBuyObjectThreadIf()){
            log.debug("指标函数已经在运行了");
        }else {
            globalBuyObject.setBuyObjectThreadIf(true);//设为true才能运行
            Thread thread = new Thread(globalBuyObject);
            thread.start();
            log.debug("公共趋势判断函数线程启动成功{}",globalBuyObject);
        }

        return "趋势判断函数的公共线程启动成功";
    }

}
