package com.numtimequantity.quantity.config;


import com.numtimequantity.quantity.quantitycontroller.InterceptAll;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;



/*绑定yml的配置类*/
@ConfigurationProperties(prefix = "usertable")
@Data
@Configuration
public class InterceptConfig implements WebMvcConfigurer {

    private Boolean referral_all;//是否开启统计9级团队人数
    private Boolean direct_if;//是否开启直推返佣
    private Boolean team_if;//是否开启团队返佣
    private Double direct_proportion;//直推的返佣比例 0.1代表10%
    private Double team_proportion; //团队的返佣比例
    private Boolean proportion_if; //是否开启返佣 true为开启   false为关闭
    private Boolean cash_out_happy_card; //提现欢乐卡是否开启,true为开启当开启时卡密会显示在提现验证页面,当关闭时会显示欢乐卡已用尽,请向管理员索取
    private String sms_key;//短信验证的key
    private String sms_url;//短信验证的url
    private String uid; //中国网建的用户名
    private String session_pri_key;
    private String session_pub_key;

   /* public Double getDoubleMinNum(int m,Double a){//m为小数保留的位数   a为输入进去的小数 四舍五入法
        return new BigDecimal(a).setScale(m, RoundingMode.HALF_DOWN).doubleValue();
    }*/
    /*判断手机号是否正确*/
/*
    public boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }
*/

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //拦截所有请求 下面new的是配置的拦截器，主要修改成自己的
        registry.addInterceptor(new InterceptAll()).addPathPatterns("/**");
    }
}
