package com.futu.openapi;

import static org.junit.Assert.assertTrue;

import com.futu.openapi.api.FutuApi;
import com.futu.openapi.api.dto.OptionChainItemDto;
import com.futu.openapi.api.enums.OptionTypeEnum;
import com.futu.openapi.api.enums.QotMarketEnum;
import com.futu.openapi.api.enums.QotSubTypeEnum;
import com.futu.openapi.api.listener.impl.GetBasicQotListenerImpl;
import com.futu.openapi.api.listener.impl.GetOptionChainQotListenerImpl;
import com.futu.openapi.api.listener.impl.GetOrderBookQotListenerImpl;
import com.futu.openapi.api.model.Option;
import com.futu.openapi.api.model.Stock;
import com.futu.openapi.api.model.StockCache;
import com.futu.openapi.api.model.WarrantData;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.QotGetWarrant;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * Unit test for simple App.
 */
public class AppTest
{

	/**
	 * Rigorous Test :-)
	 */
	@Test
	public void queryAALOptionChain() throws InterruptedException
	{
		String code = "BABA";
		FutuApi futuApi = new FutuApi();
		futuApi.queryOptionChain(code, QotMarketEnum.US, "2020-06-05", "2020-06-05", OptionTypeEnum.CALL);
		futuApi.sub(code, QotMarketEnum.US, QotSubTypeEnum.BasicValue);
		futuApi.queryBasicQot(code, QotMarketEnum.US);

		Thread.sleep(1000 * 3);
		List<Option> options = StockCache.stockMapByCode.get(code).getOptions();
		for (Option option : options)
		{
			Thread.sleep(1500 * 1);
			futuApi.sub(option.getCode(), QotMarketEnum.US, QotSubTypeEnum.OrderBook);
			futuApi.queryOrderBook(option.getCode(), QotMarketEnum.US, 1);
		}
		//        futuApi.sub("00700",QotMarketEnum.HK, QotSubTypeEnum.BasicValue);
		//        futuApi.queryBasicQot("00700",QotMarketEnum.HK);

		Thread.sleep(1000 * 1);

		System.out.println(StockCache.stockMapByCode);

		TreeMap<BigDecimal, String> sortMap = new TreeMap<>();
		for (Option option : StockCache.stockMapByCode.get(code).getOptions())
		{
			BigDecimal prefer = option.getStrikePrice().add(option.getOrderBook().getFirstSellPrice());
			sortMap.put(prefer, option.getCode());
		}
		System.out.println(sortMap);
		Thread.sleep(1000 * 600);
	}

	@Test
	public void queryWarrant() throws InterruptedException
	{
		FutuApi futuApi = new FutuApi();
		futuApi.queryWarrantData(QotCommon.WarrantType.WarrantType_Bull, QotCommon.SortField.SortField_Premium, "09988",
				QotMarketEnum.HK);
		Thread.sleep(1000 * 600);
	}

	@Test
	public void regQotPush() throws InterruptedException
	{
		final String code = "63800";
		FutuApi futuApi = new FutuApi();
		futuApi.queryWarrantData(QotCommon.WarrantType.WarrantType_Bull, QotCommon.SortField.SortField_Premium, "09988",
				QotMarketEnum.HK);
		futuApi.sub(code, QotMarketEnum.HK, QotSubTypeEnum.OrderBook);
		futuApi.regQotPush(code, QotMarketEnum.HK, QotSubTypeEnum.OrderBook);
		futuApi.queryOrderBook(code, QotMarketEnum.HK, 1);

		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				while (true)
				{
					try
					{
						if (StockCache.stockMapByCode == null || StockCache.stockMapByCode.get(code) == null)
						{
							System.out.println("没有初始化完成");
							continue;
						}
						TreeMap<BigDecimal, String> sortMap = new TreeMap<>();
						for(Stock stock : StockCache.stockMapByCode.values())
						{
							if(!(stock instanceof WarrantData))
							{
								continue;
							}
							WarrantData warrantData = (WarrantData) stock;
							BigDecimal prefer = BigDecimal.ZERO;
							if (warrantData.getOrderBook().getFirstSellPrice().compareTo(BigDecimal.ZERO) > 0)
							{
								prefer = new BigDecimal(String.valueOf(warrantData.getStrikePrice()
										.add(warrantData.getConversionRatio().multiply(warrantData.getOrderBook().getFirstSellPrice()))));
							} else
							{
								prefer = new BigDecimal(String.valueOf(warrantData.getStrikePrice()
										.add(warrantData.getConversionRatio().multiply(warrantData.getBasicQot().getCurPrice()))));
							}
							sortMap.put(prefer, warrantData.getCode());
						}
						System.out.println(sortMap);
					} finally
					{
						try
						{
							Thread.sleep(1000 * 10);
						} catch (InterruptedException e)
						{
							e.printStackTrace();
						}
					}

				}
			}
		}).start();
		Thread.sleep(1000 * 600);

	}
}
