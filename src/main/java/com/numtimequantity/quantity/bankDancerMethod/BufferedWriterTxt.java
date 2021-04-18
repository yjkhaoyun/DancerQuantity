package com.numtimequantity.quantity.bankDancerMethod;

import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;

/**
 * 主要用于存储日志
 */
@Component
public class BufferedWriterTxt {
    public void bWriter(){
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("log\\lantue.txt"));
            bufferedWriter.write("写入二11111句话");
            bufferedWriter.flush();
            bufferedWriter.close();
        }catch (Exception e){
            System.out.println(e);
        }
    }
}
