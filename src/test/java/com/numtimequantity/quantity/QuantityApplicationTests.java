package com.numtimequantity.quantity;

import com.numtimequantity.quantity.fileThread.TopSymbolThread;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;

@SpringBootTest
class QuantityApplicationTests {
@Autowired
TopSymbolThread topSymbolThread;
    @Test
    void contextLoads() {
        topSymbolThread.setTopSymbolThreadIf(true);
        Thread thread = new Thread(topSymbolThread);
        thread.start();
    }
    @Test
    void test2(){
        String arr[] = {Long.toString(new Date().getTime()),"0"};
        System.out.println(arr.getClass());
        ArrayList<String[]> list = new ArrayList<>();
        list.add(arr);//将指定元素添加到末尾

        System.out.println(list.get(0)[0]);
    }
}
