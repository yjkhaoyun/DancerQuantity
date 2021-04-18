package com.numtimequantity.quantity.utils;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 生成验证码图片
 * 使用全是jdk15.0.2自带的包
 */
public class CreateValidateCode {

    private BufferedImage image;// 图像
    private String str;// 验证码
    private  char code[] = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789".toCharArray();

    public static final String SESSION_CODE_NAME = "code";

    public CreateValidateCode() {
        init();// 初始化属性
    }

    /*
     * 取得RandomNumUtil实例
     */
    private CreateValidateCode Instance() {
        return new CreateValidateCode();
    }

    /*
     * 取得验证码图片
     */
    private BufferedImage getImage() {
        return this.image;
    }

    /*
     * 取得图片的验证码
     */
    public String getString() {
        return str.toLowerCase();
    }

    private void init() {
        // 在内存中创建图象
        int width = 60, height = 20;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // 获取图形上下文
        Graphics g = image.getGraphics();
        // 生成随机类
        Random random = new Random();
        // 设定背景色
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        // 设定字体
        g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        // 随机产生155条干扰线，使图象中的认证码不易被其它程序探测到
        g.setColor(getRandColor(160, 200));
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
        // 取随机产生的认证码(4位数字)
        String sRand = "";
        for (int i = 0; i < 4; i++) {
            String rand = String.valueOf(code[random.nextInt(code.length)]);
            sRand += rand;
            // 将认证码显示到图象中
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            // 调用函数出来的颜色相同，可能是因为种子太接近，所以只能直接生成
            g.drawString(rand, 13 * i + 6, 16);
        }
        // 赋值验证码

        this.str = sRand;

        // 图象生效
        g.dispose();

        /* 赋值图像 */
        this.image = image;
    }

    /**
     * @param fc
     * @param bc
     * @return 给定范围获得随机颜色
     */
    private Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) {
            fc = 255;
        }
        if (bc > 255) {
            bc = 255;
        }
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }


    /**
     * 将BufferedImage转成Base64字符串
     * 备注:所有图片格式的base64字符串都是这样的格式data:image/jpg;base64,iVBORw0KGgoAAAANSUhEUgAAADwAAAAUCAIAAABeYcl+AAAB7klEQVR4XqWVQU4dQQxE/42yZJEFETvWROxZcbE+QI7AETjJiCNkSIXiTVVPA0Iqjdx22a7x9/S/bM8vwnj6Y3uNZu4eocmfhCsEFAryJTK7nPwMndHMDHLrmIIS1+1eRX/IZr9170UoOAuI0zXtvPhsVyREp+jafB7X6OLrsvbkpKPc71+3BMk7Hq4fF4iyNq5+/AwP+64hzmHSrLLbEmo/dQsWR8g5FbH9UyzoqOdUN6NRJydt3nS0oZvjlMHX6Mo7LDqkqCkFMMQKw6KZOUoc0SEXnQ6e0IA96a7gsrK7i/C+Hsy0sqgyjjtDeCvMb463InSr+P3dzY79aCME+HlYD4dbtEtQNKMfjnm8iZZhW0UklLpbsY3/fy4sTWWKMmc66VAsfiRytNMN4YDPish+/0d0YBwXNwgRGqXYYNbAJ0iYObAV6zqDoqk71oMGf4QBxVGX2I5jFizaiRTtdj5S5OuHSJ4ZvSF0Gvz++IyP8ky0U7ZaD/tpC4crz1AsNuFsMboHF2Z7uyvYyE6+TKwHG4XIuWhDQg3qk7Iz+AV6g8Op94nbw+iJDN4eod5OK2b0++hhSclUjKOy8/Y4o3rSC7JTpnajoxTdUWNyezQc6j0JwpfQTSnG6smUMRHdBpPZo9G53XXKp6clRam/QlLsNVf3vYIAAAAASUVORK5CYII=
     * @return
     * @throws IOException
     */
    public String getPngBase64() throws IOException {
        BufferedImage image = this.getImage();
        ByteArrayOutputStream OutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", OutputStream);
        //ImageIO.createImageOutputStream(OutputStream);
        byte[] bytes = OutputStream.toByteArray();
        Base64.Encoder encoder = Base64.getEncoder();
        String pngBase64 = encoder.encodeToString(bytes);
        pngBase64.replaceAll("\n","").replaceAll("\r","");//删除"\n"和"\r"
        pngBase64="data:image/png;base64,"+pngBase64;
        return pngBase64;
    }


}