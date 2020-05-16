package com.futu.openapi.api;

import com.futu.openapi.FTAPI;
import com.futu.openapi.FTAPI_Conn_Qot;
import com.futu.openapi.api.enums.OptionTypeEnum;
import com.futu.openapi.api.enums.QotMarketEnum;
import com.futu.openapi.api.enums.QotSubTypeEnum;
import com.futu.openapi.api.listener.QotListener;
import com.futu.openapi.api.listener.impl.GetBasicQotListenerImpl;
import com.futu.openapi.api.listener.impl.GetOptionChainQotListenerImpl;
import com.futu.openapi.api.listener.impl.GetOrderBookQotListenerImpl;
import com.futu.openapi.api.listener.impl.GetWarrantQotListenerImpl;
import com.futu.openapi.api.spi.ConnSpi;
import com.futu.openapi.api.spi.QotSpi;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.QotGetBasicQot;
import com.futu.openapi.pb.QotGetOptionChain;
import com.futu.openapi.pb.QotGetOrderBook;
import com.futu.openapi.pb.QotGetWarrant;
import com.futu.openapi.pb.QotRegQotPush;
import com.futu.openapi.pb.QotSub;
import com.futu.openapi.util.MD5Util;

import java.util.Arrays;
import java.util.List;

public class FutuApi
{

	static String opendIP = "127.0.0.1";

	static int opendPort = 11111;

	FTAPI_Conn_Qot qot = new FTAPI_Conn_Qot();

	public FutuApi()
	{
		this(Arrays
				.asList(new GetBasicQotListenerImpl(), new GetOptionChainQotListenerImpl(), new GetOrderBookQotListenerImpl(),
						new GetWarrantQotListenerImpl()));
	}

	public FutuApi(List<QotListener> qotListeners)
	{
		FTAPI.init();
		qot.setClientInfo("javaclient", 1);  //设置客户端信息
		qot.setConnSpi(new ConnSpi());  //设置连接回调
		qot.setQotSpi(new QotSpi(qotListeners));   //设置行情回调
		qot.initConnect(opendIP, (short) opendPort, false);
	}

	public void queryOptionChain(String stockCode, QotMarketEnum qotMarketEnum, String beginTime, String endTime,
			OptionTypeEnum optionTypeEnum)
	{
		QotCommon.Security sec = QotCommon.Security.newBuilder().setCode(stockCode).setMarket(
				qotMarketEnum == QotMarketEnum.HK ?
						QotCommon.QotMarket.QotMarket_HK_Security.getNumber() :
						QotCommon.QotMarket.QotMarket_US_Security.getNumber()).build();
		QotGetOptionChain.DataFilter dataFilter = QotGetOptionChain.DataFilter.newBuilder().setGammaMin(0).build();
		QotGetOptionChain.C2S c2s = QotGetOptionChain.C2S.newBuilder().setOwner(sec).setBeginTime(beginTime).setEndTime(endTime)
				.setType(optionTypeEnum == OptionTypeEnum.CALL ?
						QotCommon.OptionType.OptionType_Call_VALUE :
						QotCommon.OptionType.OptionType_Put_VALUE).setDataFilter(dataFilter).build();
		QotGetOptionChain.Request req = QotGetOptionChain.Request.newBuilder().setC2S(c2s).build();
		qot.getOptionChain(req);
	}

	public void queryWarrantData(QotCommon.WarrantType warrantType, QotCommon.SortField sortField, String stockCode,
			QotMarketEnum qotMarketEnum)
	{
		QotCommon.Security sec = QotCommon.Security.newBuilder().setCode(stockCode).setMarket(
				qotMarketEnum == QotMarketEnum.HK ?
						QotCommon.QotMarket.QotMarket_HK_Security.getNumber() :
						QotCommon.QotMarket.QotMarket_US_Security.getNumber()).build();
		QotGetWarrant.C2S c2s = QotGetWarrant.C2S.newBuilder().setOwner(sec).setBegin(1).setNum(200)
				.addTypeList(warrantType.getNumber()).setSortField(sortField.getNumber()).setAscend(true).build();
		QotGetWarrant.Request req = QotGetWarrant.Request.newBuilder().setC2S(c2s).build();
		qot.getWarrant(req);
	}

	public void sub(String stockCode, QotMarketEnum qotMarketEnum, QotSubTypeEnum qotSubTypeEnum)
	{
		QotCommon.Security sec = QotCommon.Security.newBuilder().setCode(stockCode).setMarket(
				qotMarketEnum == QotMarketEnum.HK ?
						QotCommon.QotMarket.QotMarket_HK_Security.getNumber() :
						QotCommon.QotMarket.QotMarket_US_Security.getNumber()).build();
		int subType = 0;
		if (qotSubTypeEnum == QotSubTypeEnum.OrderBook)
		{
			subType = QotCommon.SubType.SubType_OrderBook_VALUE;
		} else if (qotSubTypeEnum == QotSubTypeEnum.BasicValue)
		{
			subType = QotCommon.SubType.SubType_Basic_VALUE;
		}
		QotSub.C2S c2s = QotSub.C2S.newBuilder().addSecurityList(sec).addSubTypeList(subType).setIsSubOrUnSub(true)
				.setIsRegOrUnRegPush(true).setIsFirstPush(true).build();
		QotSub.Request req = QotSub.Request.newBuilder().setC2S(c2s).build();
		qot.sub(req);
	}

	public void regQotPush(String stockCode, QotMarketEnum qotMarketEnum, QotSubTypeEnum qotSubTypeEnum)
	{
		QotCommon.Security sec = QotCommon.Security.newBuilder().setCode(stockCode).setMarket(
				qotMarketEnum == QotMarketEnum.HK ?
						QotCommon.QotMarket.QotMarket_HK_Security.getNumber() :
						QotCommon.QotMarket.QotMarket_US_Security.getNumber()).build();
		int subType = 0;
		if (qotSubTypeEnum == QotSubTypeEnum.OrderBook)
		{
			subType = QotCommon.SubType.SubType_OrderBook_VALUE;
		} else if (qotSubTypeEnum == QotSubTypeEnum.BasicValue)
		{
			subType = QotCommon.SubType.SubType_Basic_VALUE;
		}
		QotRegQotPush.C2S c2s = QotRegQotPush.C2S.newBuilder().addSecurityList(sec).addSubTypeList(subType)
				.setIsRegOrUnReg(true).setIsFirstPush(true).build();
		QotRegQotPush.Request req = QotRegQotPush.Request.newBuilder().setC2S(c2s).build();
		qot.regQotPush(req);
	}

	public void queryOrderBook(String stockCode, QotMarketEnum qotMarketEnum, int num)
	{
		QotCommon.Security sec = QotCommon.Security.newBuilder().setCode(stockCode).setMarket(
				qotMarketEnum == QotMarketEnum.HK ?
						QotCommon.QotMarket.QotMarket_HK_Security.getNumber() :
						QotCommon.QotMarket.QotMarket_US_Security.getNumber()).build();
		QotGetOrderBook.C2S c2s = QotGetOrderBook.C2S.newBuilder().setSecurity(sec).setNum(num).build();
		QotGetOrderBook.Request req = QotGetOrderBook.Request.newBuilder().setC2S(c2s).build();
		qot.getOrderBook(req);
	}

	public void queryBasicQot(String stockCode, QotMarketEnum qotMarketEnum)
	{
		QotCommon.Security sec1 = QotCommon.Security.newBuilder().setCode(stockCode).setMarket(
				qotMarketEnum == QotMarketEnum.HK ?
						QotCommon.QotMarket.QotMarket_HK_Security.getNumber() :
						QotCommon.QotMarket.QotMarket_US_Security.getNumber()).build();
		QotGetBasicQot.C2S c2s = QotGetBasicQot.C2S.newBuilder().addSecurityList(sec1).build();
		QotGetBasicQot.Request req = QotGetBasicQot.Request.newBuilder().setC2S(c2s).build();
		qot.getBasicQot(req);
	}
}
