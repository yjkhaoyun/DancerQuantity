package com.numtimequantity.quantity.bankDancerInterface;

import java.util.concurrent.ConcurrentHashMap;

public interface ImplBankDancer {
    /**
     * 最新价
     * @return  最新价
     */
    Double lastPrice();

    /**
     * 辅助函数
     * @param price  最新价
     * @param newPositionPrice 建仓成本价
     * @param k 跨度
     * @return 返回低的那个减去k的值
     */
    Double getPriceNewduo(Double price,Double newPositionPrice,Double k);

    /**
     * 市价做多
     * @param a 做多买入期货的数量
     * @return 返回订单号
     */
    String marketBuy(Double a);

    /**
     * 市价开空
     * @param a 开空下单数量
     * @return  返回订单号
     */
    String marketSell(Double a);

    /**
     * 市价平多
     * @param aNum  平多的数量
     * @return 返回订单号
     */
    String marketCloseBuy(Double aNum);

    /**
     * 双向持仓模式下平仓全部
     * @param positionSide  平多头持仓时输入LONG    平空头持仓输入SHORT
     * @return 返回订单号
     */
    String marketCloseAllProfit(String positionSide);

    /*获取两头的持仓数量*/
    ConcurrentHashMap position();


    /*获取账户总余额*/
    Double account();

    /**
     * 查询持仓方向
     * @return  返回true为双向持仓   false为单向持仓
     */
    Boolean getWay();

    /**
     * 设置持仓方向
     * @return  返回success表示设置成功
     */
    String setWay();

    /**
     * 获取期货合约的订单状态
     * @param orderId
     * @return
     */
    ConcurrentHashMap come(String orderId);


    /**
     * 返回合约账户的真实余额,用这个真实余额减去几天前的余额就是这几天的实际盈利情况 2021年3月7日添加
     * @return
     */
    Double accountNow(String time);
}
