package com.futu.openapi.strategy;

import com.futu.openapi.api.FutuApi;
import com.futu.openapi.api.enums.QotMarketEnum;
import com.futu.openapi.api.enums.QotSubTypeEnum;
import com.futu.openapi.api.model.Stock;
import com.futu.openapi.api.model.StockCache;
import com.futu.openapi.api.model.WarrantData;
import com.futu.openapi.pb.QotCommon;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.TreeMap;

@Controller
@Slf4j
public class TestController
{
	@Autowired
	private FutuApi futuApi;

	@RequestMapping("/queryWarrant")
	@ResponseBody
	public String queryWarrant(@RequestParam("code") String code)
	{
		futuApi.queryWarrantData(QotCommon.WarrantType.WarrantType_Bull, QotCommon.SortField.SortField_Premium, code,
				QotMarketEnum.HK);
		futuApi.sub(code, QotMarketEnum.HK, QotSubTypeEnum.OrderBook);
		futuApi.regQotPush(code, QotMarketEnum.HK, QotSubTypeEnum.OrderBook);
		futuApi.queryOrderBook(code, QotMarketEnum.HK, 1);
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					log.error(e.getMessage(), e);
				}
				if (StockCache.stockMapByCode == null || StockCache.stockMapByCode.get(code) == null)
				{
					return;
				}
				for(Stock stock : StockCache.stockMapByCode.values())
				{
					if (!(stock instanceof WarrantData))
					{
						continue;
					}
					WarrantData warrantData = (WarrantData) stock;
					futuApi.sub(warrantData.getCode(), QotMarketEnum.HK, QotSubTypeEnum.OrderBook);
					futuApi.regQotPush(warrantData.getCode(), QotMarketEnum.HK, QotSubTypeEnum.OrderBook);
					futuApi.queryOrderBook(warrantData.getCode(), QotMarketEnum.HK, 1);
				}

			}
		}).start();
		return "success";
	}

	@RequestMapping("/showWarrant")
	@ResponseBody
	public String showWarrant(@RequestParam("code") String code)
	{
		Gson gson = new Gson();
		try
		{
			if (StockCache.stockMapByCode == null || StockCache.stockMapByCode.get(code) == null)
			{
				return "没有初始化完成";
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
			return gson.toJson(sortMap);
		} finally
		{
		}
	}

}
