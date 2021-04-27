package com.numtimequantity.quantity;

import com.numtimequantity.quantity.fileThread.TopSymbolThread;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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

}
