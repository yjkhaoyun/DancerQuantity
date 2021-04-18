package com.numtimequantity.quantity.bankDancerMethod;

import com.google.gson.Gson;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActiveMethod extends SpotMethodGlob  {
    protected Gson gson = new Gson();

    /**
     * 将小数转化为字符串，四舍五入保留m位小数
     * @param m 保留小数的位数
     * @param a
     * @return
     */
    private String getStr(int m,Double a){
        String s = new BigDecimal(a).setScale(m, RoundingMode.HALF_DOWN).toString();
        return s;
    }

    /*获取服务器时间*/
    public Long getTime_(){
        ConcurrentHashMap<String,Long> concurrentHashMap = this.publicM(ConcurrentHashMap.class, "/fapi/v1/time", "GET");
        return concurrentHashMap.get("serverTime");
    }
    /*设置持仓模式为双向  成功时候返回 {"code": 200,"msg": "success"} */
    protected String setWay_(){
        ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("dualSidePosition","true"); //true为双向，false为单项
        ConcurrentHashMap concurrentHashMap = this.publicM(ConcurrentHashMap.class, "/fapi/v1/positionSide/dual", "POST", concurrent);
        return (String) concurrentHashMap.get("msg");
    }

    /**
     * 双向持仓模式下，市价买入期货
     * @param a 下单数量 Double小数类型
     * @return 返回订单号
     */
    protected ConcurrentHashMap marketBuy_(Double a){
        ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("symbol","BTCUSDT");
        concurrent.put("side","BUY");
        concurrent.put("type","MARKET");
        concurrent.put("positionSIde","LONG");
        concurrent.put("quantity",this.getStr(3,a));
        return this.publicM(ConcurrentHashMap.class, "/fapi/v1/order", "POST", concurrent);
        //{symbol=BTCUSDT, side=BUY, executedQty=0, orderId=10632724220, avgPrice=0.00000, origQty=0.001, clientOrderId=3A71IKyMpmrJWTPdTp7zXA, positionSide=LONG, cumQty=0, updateTime=1608549176152, type=MARKET, priceProtect=false, closePosition=false, stopPrice=0, origType=MARKET, reduceOnly=false, price=0, cumQuote=0, timeInForce=GTC, workingType=CONTRACT_PRICE, status=NEW}
    }

    /**
     * 双向持仓模式下，市价做空期货
     * @param a 下单数量Double小数类型
     * @return 返回订单号
     */
    protected ConcurrentHashMap marketSell_(Double a){
        ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("symbol","BTCUSDT");
        concurrent.put("side","SELL");
        concurrent.put("type","MARKET");
        concurrent.put("positionSIde","SHORT");
        concurrent.put("quantity",this.getStr(3,a));
        return this.publicM(ConcurrentHashMap.class,"/fapi/v1/order", "POST", concurrent);
        //{symbol=BTCUSDT, side=BUY, executedQty=0, orderId=10632724220, avgPrice=0.00000, origQty=0.001, clientOrderId=3A71IKyMpmrJWTPdTp7zXA, positionSide=LONG, cumQty=0, updateTime=1608549176152, type=MARKET, priceProtect=false, closePosition=false, stopPrice=0, origType=MARKET, reduceOnly=false, price=0, cumQuote=0, timeInForce=GTC, workingType=CONTRACT_PRICE, status=NEW}
    }

    /**
     * 双向持仓模式下市价平多卖出期货
     * @param a 传入下单额，最小0.001
     * @return 返回map
     */
    protected ConcurrentHashMap maketCloseBuy_(Double a){
        ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("symbol","BTCUSDT");
        concurrent.put("side","SELL");
        concurrent.put("type","MARKET");
        concurrent.put("positionSIde","LONG");
        concurrent.put("quantity",this.getStr(3,a));
        return this.publicM(ConcurrentHashMap.class,"/fapi/v1/order", "POST", concurrent);
    }

    /**
     *
     双向持仓下市价平仓，可用于平仓全部，平两个方向，返回订单号
     * @param side 下单方向String类型，持有多仓下SELL单，持有空仓下BUY单
     * @param quantity 平仓数量，Double小数类型，最小0.001
     * @param positionSide 持仓方向，String类型LONG多仓，SHORT空仓
     * @return  返回订单号字符串
     */
    protected String marketCloseAllProfit_(String side,Double quantity,String positionSide){
        ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("symbol","BTCUSDT");
        concurrent.put("type","MARKET");
        concurrent.put("positionSide",positionSide);
        concurrent.put("quantity",this.getStr(3,quantity));
        concurrent.put("side",side);
        ConcurrentHashMap concurrentHashMap = this.publicM(ConcurrentHashMap.class, "/fapi/v1/order", "POST", concurrent);
        BigDecimal bigDecimal = new BigDecimal(concurrentHashMap.get("orderId").toString());
        return  Long.toString(bigDecimal.longValue());
    }
    /*获取期货最新价格*/
    protected ConcurrentHashMap lastPrice_(){
        ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("symbol","BTCUSDT");
        return this.publicM(ConcurrentHashMap.class,"/fapi/v1/ticker/price", "GET", concurrent); //{symbol=BTCUSDT, price=24004.86, time=1608534709582}
    }

    /*查询持仓方向*/
    protected ConcurrentHashMap getWay_(){
        ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("symbol","BTCUSDT");
        return this.publicM(ConcurrentHashMap.class, "/fapi/v1/positionSide/dual", "GET", concurrent);
    }


    /*双向持仓模式下查询两个方向的持仓情况*/
    protected List getPositionNum_(){ //返回 List
        ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("symbol","BTCUSDT");
        return this.publicM(List.class, "/fapi/v2/positionRisk", "GET", concurrent);
    }
    /*查询账户余额，返回原始数据*/
    protected HashMap getAccount_(){
        CopyOnWriteArrayList<HashMap> accountList = this.publicM(CopyOnWriteArrayList.class, "/fapi/v2/balance", "GET");
        System.out.println(accountList.get(0));                                                              //accountList.get(0)是LinkedTreeMap类型 用gson.tojson(map)方法，将LinkedTreeMap对象转成String
        System.out.println(accountList.get(0).get("balance"));
        return accountList.get(0);
    }

    /**
     * 撤销一个订单  这个方法暂时用不到
     * @param orderId 传入订单号
     * @return 返回订单号
     */
    protected String CancelOrder_(String orderId){
       /* ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("symbol","BTCUSDT");
        concurrent.put("orderId",orderId);
        String orderJson = this.publicM("/fapi/v1/order", "DELETE", concurrent);
        ConcurrentHashMap concurrentHashMap = this.gson.fromJson(orderJson, ConcurrentHashMap.class);
        BigDecimal bigDecimal = new BigDecimal(concurrentHashMap.get("orderId").toString());
        return  Long.toString(bigDecimal.longValue());*/
        return null;
    }



    /**
     * 获取合约的订单状态
     * @param orderId
     * @return  返回HashMap
     */
    protected ConcurrentHashMap getOrder(String orderId){
        ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("symbol","BTCUSDT");
        concurrent.put("orderId",orderId);
        return super.publicM(ConcurrentHashMap.class, "/fapi/v1/order", "GET", concurrent);
    }





    /**
     *这是一个现货接口
     * @param time
     * 用法举例:
     *      ConcurrentHashMap transfer = activeMethod.getTransfer("1613235779219");
     * 		//其实接收过来"rows"的值已经是ArrayList类型了,进行声明转换  这就是RestTemplate的强大
     * 		ArrayList rows = (ArrayList)transfer.get("rows");
     * 		//其实接收过来rows.get(i)就已经是HashMap类型了,进行声明转换   这就是RestTemplate的强大
     * 		HashMap h = (HashMap)rows.get(0);
     * @return  返回一个HashMap 格式是{"total":7,"rows":"[{},{}...]"}  rows后面是ArrayList类型包裹着HashMap类型,已经帮转好了,用时候只需声明转换下
     */
    protected ConcurrentHashMap getTransfer(String time){
        ConcurrentHashMap<String, String> concurrent = new ConcurrentHashMap<>();
        concurrent.put("asset","USDT");
        concurrent.put("startTime",time);
        return super.publicBinance(ConcurrentHashMap.class, "/sapi/v1/futures/transfer", "GET", concurrent);
    }


}
