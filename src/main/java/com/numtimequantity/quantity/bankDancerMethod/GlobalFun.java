package com.numtimequantity.quantity.bankDancerMethod;



import com.numtimequantity.quantity.bankDancerInterface.ImplBankDancer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GlobalFun extends ActiveMethod implements ImplBankDancer {
    /*构造方法，传入两个key的值*/
    public GlobalFun(RestTemplate restTemplate, String api_key, String secretkey){
        this.setApi_key(api_key);
        this.setSecretkey(secretkey);
        this.setRestTemplate(restTemplate);
    };

    /**
     *
     * @param s 传入Double字符串
     * @return 返回long字符串
     */
    private String getStrLong(String s){
        return Long.toString(new BigDecimal(s).longValue());
    };

    /**
     *
     * @param s  传入Double字符串
     * @return  返回Double类型
     */
    public Double getDouble(String s){
        return new BigDecimal(s).doubleValue();
    }



    /**
     * 获取最新价  权重：1  期货每分钟权重上限2400   查询期货权重接口 https://fapi.binance.com/fapi/v1/exchangeInfo
     * @return  最新价格
     */
    @Override
    public Double lastPrice() {
        try {
            ConcurrentHashMap concurrentHashMap = super.lastPrice_();
            return new BigDecimal(concurrentHashMap.get("price").toString()).doubleValue();
        }catch (Exception e){
            log.info("获取最新价异常{}",e);
        }
        return null;
    }

    @Override
    public Double getPriceNewduo(Double price, Double newPositionPrice, Double k) {
        Double getPriceNew = 0.0;
        if (price > newPositionPrice + k){
            getPriceNew = price + k;
        }else {
            getPriceNew = newPositionPrice + k;
        }
        return getPriceNew;
    }

    /**
     *  开多下单
     * @param a 做多买入期货的数量
     * @return 返回订单号
     */
    @Override
    public String marketBuy(Double a) {
        try {
            ConcurrentHashMap concurrentHashMap = super.marketBuy_(a);
            return this.getStrLong(concurrentHashMap.get("orderId").toString());
        }catch (Exception e){
            log.info("开多时异常{}",e);
        }
        return null;
    }

    /**
     * 开空下单
     * @param a 开空下单数量
     * @return 返回订单号
     */
    @Override
    public String marketSell(Double a) {
        try {
            ConcurrentHashMap concurrentHashMap = super.marketSell_(a);
            return this.getStrLong(concurrentHashMap.get("orderId").toString());
        }catch (Exception e){
            log.info("开空时异常{}",e);
        }
        return null;
    }

    /**
     * 平掉指定数量的多头持仓
     * @param aNum  平多的数量
     * @return
     */
    @Override
    public String marketCloseBuy(Double aNum) {
        try {
            ConcurrentHashMap concurrentHashMap = super.maketCloseBuy_(aNum);
            return this.getStrLong(concurrentHashMap.get("orderId").toString());
        }catch (Exception e){
            log.info("平多时异常{}",e);
        }
        return null;
    }

    /**
     * 市价平仓全部
     * @param positionSide  平多头持仓时输入LONG    平空头持仓输入SHORT
     * @return
     */
    @Override
    public String marketCloseAllProfit(String positionSide) {
        try {
            ConcurrentHashMap<String,Double> positionClose = this.position();
            Double p = 0.0;
            String orientation=null;
            if (positionSide=="LONG"){
                p=positionClose.get("up");
                orientation="SELL";
            }else if(positionSide=="SHORT"){
                p=positionClose.get("down");
                orientation="BUY";
            }
            return super.marketCloseAllProfit_(orientation,p,positionSide);
        }catch (Exception e){
            log.info("市价平仓全部时异常{}",e);
        }
        return null;
    }
    /**
     * 获取持仓情况  权重：5  期货每分钟权重上限2400   查询期货权重接口 https://fapi.binance.com/fapi/v1/exchangeInfo
     * @return  up多头持仓数量；upPrice多头持仓成本；down空头持仓数量
     */
    @Override
    public ConcurrentHashMap<String,Double> position() {
        try {
            List<HashMap> positionNum_ = super.getPositionNum_();
            ConcurrentHashMap<String, Double> concurrentHashMap = new ConcurrentHashMap<>();
            concurrentHashMap.put("up",new BigDecimal(positionNum_.get(0).get("positionAmt").toString()).doubleValue());
            concurrentHashMap.put("upPrice",new BigDecimal(positionNum_.get(0).get("entryPrice").toString()).doubleValue());
            concurrentHashMap.put("down",new BigDecimal(positionNum_.get(1).get("positionAmt").toString()).doubleValue()* (-1));
            return concurrentHashMap;
        }catch (Exception e){
            log.info("获取持仓异常{}",e);
        }
        return null;
    }

    /**
     * 获取期货账户总余额  权重：5  期货每分钟权重上限2400   查询期货权重接口 https://fapi.binance.com/fapi/v1/exchangeInfo
     * @return
     */
    @Override
    public Double account() {
        try {
            HashMap account_ = this.getAccount_();
            double balance1 = new BigDecimal(account_.get("crossWalletBalance").toString()).doubleValue();
            double balance2 = new BigDecimal(account_.get("crossUnPnl").toString()).doubleValue();
            return balance1+balance2;
        }catch (Exception e){
            log.info("获取当前合约余额异常{}",e);
        }
        return null;
    }

    /**
     * 查询持仓方向 权重：30  期货每分钟权重上限2400   查询期货权重接口 https://fapi.binance.com/fapi/v1/exchangeInfo
     * @return  返回true为双向持仓   false为单向持仓
     */
    @Override
    public Boolean getWay() {
        try {
            ConcurrentHashMap<String,Boolean> way_ = super.getWay_();
            return way_.get("dualSidePosition");
        }catch (Exception e){
            log.info("查询持仓方向异常{}",e);
        }
        return null;
    }

    /**
     *  权重：1  期货每分钟权重上限2400   查询期货权重接口 https://fapi.binance.com/fapi/v1/exchangeInfo
     * 设置持仓方向  设置为双向持仓
     * @return  返回success表示设置成功
     */
    @Override
    public String setWay() {
        try {
            return super.setWay_();
        }catch (Exception e){
            log.info("设置持仓方向异常,这个异常是很正常的{}",e);
        }
        return null;
    }

    /**
     * 权重：1  现货每分钟权重上限1200
     * 获取期货合约的订单状态
     * @param orderId
     * @return   order.status为"FILLED"时为已成交,String类型  ;  order.avgPrice 为成交价,String类型的小数
     */
    @Override
    public ConcurrentHashMap come(String orderId) {
        try {
            Thread.sleep(20000);
            return super.getOrder(orderId);
        }catch (Exception e){
            log.info("获取订单状态时出现异常{}",e);
        }
        return null;
    }

    /**
     * 参数: 一个参数,时间戳   权重：5  现货每分钟权重上限1200   查询现货权重接口 https://api.binance.com/api/v3/exchangeInfo?symbol=BTCUSDT
     * 返回合约账户的真是余额,用这个真实余额减去几天前的余额就是这几天的实际盈利情况 2021年3月7日添加
     * @return
     */
    @Override
    public Double accountNow(String time) {
        try {
            ConcurrentHashMap transfer = super.getTransfer(time);
            Double account = this.account(); //当前最新的合约账户余额
            for(int i=0;i<(int)transfer.get("total");i++){
                ArrayList rows = (ArrayList)transfer.get("rows");
                HashMap h = (HashMap)rows.get(i);
                if ((int)h.get("type")==1 && "USDT".equals(h.get("asset")) ){ //type为2是转出合约   为1是转入合约
                    account=account-this.getDouble((String) h.get("amount"));
                }else if ((int)h.get("type")==2 &&"USDT".equals(h.get("asset"))){
                    account=account+this.getDouble((String) h.get("amount"));
                };
            }
            return account;
        }catch (Exception e){
            log.info("获取合约真实余额时异常{}",e);
        }
        return null;
    }

}
