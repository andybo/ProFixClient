package com.btcc;

import com.btcc.fix.message.AccReqID;
import com.btcc.fix.message.AccountInfoRequest;
import quickfix.Group;
import quickfix.UtcTimeStampField;
import quickfix.field.*;
import quickfix.fix44.*;

import java.util.Date;
import java.util.List;

/**
 * Created by zhenning on 15/9/6.
 */
public class MessageProvider {

    public static AccountInfoRequest createAccountInfoRequest(String account, String accReqID) {
        AccountInfoRequest accountInfoRequest = new AccountInfoRequest();

        accountInfoRequest.set(new Account(account));
        accountInfoRequest.set(new AccReqID(accReqID));
        return accountInfoRequest;
    }

    public static SecurityListRequest createSecurityListRequest(String reqID)
    {
        SecurityListRequest securityListRequest = new SecurityListRequest();
        securityListRequest.set(new SecurityListRequestType(SecurityListRequestType.SYMBOL));
        securityListRequest.set(new SecurityReqID(reqID));

        return securityListRequest;
    }

    public static MarketDataRequest createMarketDataRequest(String symbol, char subscriptionRequestType, String mDReqID) {
        MarketDataRequest tickerRequest = new MarketDataRequest();

        MarketDataRequest.NoRelatedSym noRelatedSym = new MarketDataRequest.NoRelatedSym();
        noRelatedSym.set(new Symbol(symbol));
        tickerRequest.addGroup(noRelatedSym);

        tickerRequest.set(new MDReqID(mDReqID));
        tickerRequest.set(new SubscriptionRequestType(subscriptionRequestType));
        tickerRequest.set(new MarketDepth(3));

        addMDType(tickerRequest, MDEntryType.BID);
//        addMDType(tickerRequest, MDEntryType.OFFER);
//        addMDType(tickerRequest, MDEntryType.TRADE);
//        addMDType(tickerRequest, MDEntryType.INDEX_VALUE);
//        addMDType(tickerRequest, MDEntryType.OPENING_PRICE);
//        addMDType(tickerRequest, MDEntryType.CLOSING_PRICE);
//        addMDType(tickerRequest, MDEntryType.SETTLEMENT_PRICE);
//        addMDType(tickerRequest, MDEntryType.TRADING_SESSION_HIGH_PRICE);
//        addMDType(tickerRequest, MDEntryType.TRADING_SESSION_LOW_PRICE);
//        addMDType(tickerRequest, MDEntryType.TRADING_SESSION_VWAP_PRICE);
//        addMDType(tickerRequest, MDEntryType.IMBALANCE);
//        addMDType(tickerRequest, MDEntryType.TRADE_VOLUME);
//        addMDType(tickerRequest, MDEntryType.OPEN_INTEREST);

        return tickerRequest;
    }

    public static MarketDataRequest createMarketDataRequest(String symbol, char subscriptionRequestType, String mDReqID, int marketdepth, List<Character> mdTypes) {
        MarketDataRequest tickerRequest = new MarketDataRequest();

        MarketDataRequest.NoRelatedSym noRelatedSym = new MarketDataRequest.NoRelatedSym();
        noRelatedSym.set(new Symbol(symbol));
        tickerRequest.addGroup(noRelatedSym);

        tickerRequest.set(new MDReqID(mDReqID));
        tickerRequest.set(new SubscriptionRequestType(subscriptionRequestType));
        tickerRequest.set(new MarketDepth(marketdepth));

        for(Character mdtype : mdTypes)
        {
            addMDType(tickerRequest, mdtype);
        }

        return tickerRequest;
    }

    private static void addMDType(MarketDataRequest tickerRequest, char type) {
        MarketDataRequest.NoMDEntryTypes g0 = new MarketDataRequest.NoMDEntryTypes();
        g0.set(new MDEntryType(type));
        tickerRequest.addGroup(g0);
    }

    public static NewOrderSingle createNewOrderSingle(String account, String clOrdID, char side, char ordertype, double price, double amount, String symbol, char timeInForce)
    {
        NewOrderSingle newOrderSingleRequest = new NewOrderSingle();
        newOrderSingleRequest.set(new Account(account));
        newOrderSingleRequest.set(new ClOrdID(clOrdID));

        //如果买入 ,OrdType 为1 price 意思为市价单 买30块钱的币  OrderQty无意义
        //如果买入, OrdType 为2 price 意思 买币单价为30  OrderQty表示购买数量
        //如果卖出, OrdType 为1 price 无含义,意思为市价卖,OrderQty为卖出数量
        //如果卖出, OrdType 为2 price 为卖出单价 OrderQty为卖出数量
        newOrderSingleRequest.set(new Side(side));
        newOrderSingleRequest.set(new Symbol(symbol));
        newOrderSingleRequest.set(new OrdType(ordertype));
        if(ordertype == OrdType.STOP){
            newOrderSingleRequest.set(new StopPx(price));
        }else if(ordertype == OrdType.LIMIT){
            newOrderSingleRequest.set(new Price(price));
        }
        newOrderSingleRequest.set(new OrderQty(amount));
        newOrderSingleRequest.set(new TransactTime());
        newOrderSingleRequest.set(new TimeInForce(timeInForce));
        return newOrderSingleRequest;
    }

    public static OrderCancelRequest createOrderCancelRequest(String account, String clOrdID, String symbol, String orderid) {
        OrderCancelRequest orderCancelRequest = new OrderCancelRequest();

        orderCancelRequest.set(new ClOrdID(clOrdID));
        orderCancelRequest.set(new OrigClOrdID("1231234"));//必填，但无意义
        orderCancelRequest.set(new Side('1'));//必填，但无意义

        orderCancelRequest.set(new Account(account));
        orderCancelRequest.set(new Symbol(symbol));
        orderCancelRequest.set(new OrderID(orderid));//订单编号
        orderCancelRequest.set(new TransactTime(new Date()));
        return orderCancelRequest;
    }

    public static OrderMassStatusRequest createOrderMassStatusRequest(String account, String symbol, String reqID) {
        OrderMassStatusRequest orderMassStatusRequest = new OrderMassStatusRequest();
        orderMassStatusRequest.set(new MassStatusReqID(reqID));
        orderMassStatusRequest.set(new MassStatusReqType(MassStatusReqType.STATUS_FOR_ALL_ORDERS));
        orderMassStatusRequest.set(new Side(Side.BUY));//必填，但无意义

        orderMassStatusRequest.set(new Account(account));
        orderMassStatusRequest.set(new Symbol(symbol));//TODO ws message 目前不区分
        return orderMassStatusRequest;
    }

    public static OrderStatusRequest createOrderStatusRequest(String account, String symbol, String orderId, String clOrderId) {
        OrderStatusRequest orderStatusRequest = new OrderStatusRequest();
        orderStatusRequest.set(new Account(account));
        orderStatusRequest.set(new Symbol(symbol));
        orderStatusRequest.set(new OrderID(orderId));//订单编号
        orderStatusRequest.set(new ClOrdID(clOrderId));
        orderStatusRequest.set(new Side(Side.BUY));//必填，但无意义
        return orderStatusRequest;
    }

    public static Message createOrderMassStatus2Request(String account, String symbol, String reqID) {
        Message orderMassStatus2Request = new Message();
        orderMassStatus2Request.getHeader().setField(new MsgType("AF2"));
        orderMassStatus2Request.setField(new Account(account));
        orderMassStatus2Request.setField(new Symbol(symbol));
        orderMassStatus2Request.setField(new MassStatusReqID(reqID));
        orderMassStatus2Request.setField(new Account(account));
        orderMassStatus2Request.setField(new Symbol(symbol));
        orderMassStatus2Request.setField(new Side(Side.SELL));//选填，但无意义

        UtcTimeStampField startTime = new UtcTimeStampField(9010);
//        startTime.setValue(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 10));
        startTime.setValue(new Date(0));
        orderMassStatus2Request.setField(startTime);

        UtcTimeStampField endTime = new UtcTimeStampField(9011);
        orderMassStatus2Request.setField(endTime);

        addOrdStatus(orderMassStatus2Request, new OrdStatus(OrdStatus.PENDING_NEW));
        addOrdStatus(orderMassStatus2Request, new OrdStatus(OrdStatus.NEW));
        addOrdStatus(orderMassStatus2Request, new OrdStatus(OrdStatus.FILLED));
        addOrdStatus(orderMassStatus2Request, new OrdStatus(OrdStatus.DONE_FOR_DAY));
        addOrdStatus(orderMassStatus2Request, new OrdStatus(OrdStatus.CANCELED));
        addOrdStatus(orderMassStatus2Request, new OrdStatus(OrdStatus.PENDING_CANCEL));
        addOrdStatus(orderMassStatus2Request, new OrdStatus(OrdStatus.PENDING_NEW));
        addOrdStatus(orderMassStatus2Request, new OrdStatus('S'));  //DoneBySystem
        addOrdStatus(orderMassStatus2Request, new OrdStatus('G'));  //CancelledBySystem

        return orderMassStatus2Request;
    }

    public static Message createOrderMassStatus2Request(String account, String symbol, String reqID, long startTimeMillis, long endTimeMillis, List<Character> orderStatuses) {
        Message orderMassStatus2Request = new Message();
        orderMassStatus2Request.getHeader().setField(new MsgType("AF2"));
        orderMassStatus2Request.setField(new Account(account));
        orderMassStatus2Request.setField(new Symbol(symbol));
        orderMassStatus2Request.setField(new MassStatusReqID(reqID));
        orderMassStatus2Request.setField(new Account(account));
        orderMassStatus2Request.setField(new Symbol(symbol));

        UtcTimeStampField startTime = new UtcTimeStampField(9010);
        startTime.setValue(new Date(startTimeMillis));
        orderMassStatus2Request.setField(startTime);

        UtcTimeStampField endTime = new UtcTimeStampField(9011);
        endTime.setValue(new Date(endTimeMillis));
        orderMassStatus2Request.setField(endTime);

        for(Character orderStatus : orderStatuses)
        {
            addOrdStatus(orderMassStatus2Request, new OrdStatus(orderStatus));
        }

        return orderMassStatus2Request;
    }

    private static void addOrdStatus(Message orderMassStatus2Request, OrdStatus field2) {
        Group ordStatusGroup1 = new Group(9020, 39, new int[]{39, 0});
        ordStatusGroup1.setField(field2);
        orderMassStatus2Request.addGroup(ordStatusGroup1);
    }
}
