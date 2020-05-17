package com.futu.openapi.strategy;

import com.futu.openapi.api.FutuApi;
import com.futu.openapi.api.enums.QotMarketEnum;
import com.futu.openapi.api.enums.QotSubTypeEnum;
import com.futu.openapi.api.model.Stock;
import com.futu.openapi.api.model.StockCache;
import com.futu.openapi.api.model.WarrantData;
import com.futu.openapi.pb.QotCommon;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

@Controller
@Slf4j
public class WarrantController extends BaseController
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
		futuApi.sub(code, QotMarketEnum.HK, QotSubTypeEnum.BasicValue);
		futuApi.queryBasicQot(code, QotMarketEnum.HK);
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{

				if (StockCache.stockMapByCode == null || StockCache.stockMapByCode.get(code) == null)
				{
					return;
				}
				for (Stock stock : StockCache.stockMapByCode.values())
				{
					if (!(stock instanceof WarrantData))
					{
						continue;
					}
					try
					{
						Thread.sleep(1000);
					} catch (InterruptedException e)
					{
						log.error(e.getMessage(), e);
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
	public String showWarrant(@RequestParam("code") String code,
			@RequestParam(value = "preferAmount", required = false) Double preferStockAmountD)
	{
		try
		{
			if (StockCache.stockMapByCode == null || StockCache.stockMapByCode.get(code) == null)
			{
				return "没有初始化完成";
			}
			BigDecimal preferStockAmount = null;
			if (preferStockAmountD != null)
			{
				preferStockAmount = new BigDecimal(String.valueOf(preferStockAmountD));
			}
			TreeMap<BigDecimal, String> sortMap = new TreeMap<>();
			for (Stock stock : StockCache.stockMapByCode.values())
			{
				if (!(stock instanceof WarrantData))
				{
					continue;
				}
				WarrantData warrantData = (WarrantData) stock;
				if (!warrantData.getOwnerCode().equalsIgnoreCase(code))
				{
					continue;
				}
				BigDecimal prefer = BigDecimal.ZERO;
				BigDecimal curPrice = null;
				if (warrantData.getOrderBook().getFirstSellPrice().compareTo(BigDecimal.ZERO) > 0)
				{
					curPrice = warrantData.getOrderBook().getFirstSellPrice();
				} else
				{
					curPrice = warrantData.getBasicQot().getCurPrice();
				}
				prefer = new BigDecimal(
						String.valueOf(warrantData.getStrikePrice().add(warrantData.getConversionRatio().multiply(curPrice))));

				sortMap.put(prefer, warrantData.getCode());
			}
			List<JsonObject> result = new ArrayList<>();
			TreeMap<BigDecimal, JsonObject> profitResult = new TreeMap<>(new Comparator<BigDecimal>()
			{

				@Override
				public int compare(BigDecimal o1, BigDecimal o2)
				{
					return o2.compareTo(o1);
				}
			});
			for (String warrantCode : sortMap.values())
			{
				WarrantData warrantData = (WarrantData) StockCache.stockMapByCode.get(warrantCode);
				JsonObject jsonObject = gson.fromJson(gson.toJson(warrantData), JsonObject.class);
				result.add(jsonObject);
				BigDecimal profit = null;
				BigDecimal curPrice = null;
				if (warrantData.getOrderBook().getFirstSellPrice().compareTo(BigDecimal.ZERO) > 0)
				{
					curPrice = warrantData.getOrderBook().getFirstSellPrice();
				} else
				{
					curPrice = warrantData.getBasicQot().getCurPrice();
				}
				if (preferStockAmount != null)
				{
					BigDecimal preferWarrantPrice = preferStockAmount.subtract(warrantData.getStrikePrice())
							.divide(warrantData.getConversionRatio(), 5, RoundingMode.HALF_UP);
					profit = preferWarrantPrice.subtract(curPrice).divide(curPrice, 2, RoundingMode.HALF_UP)
							.multiply(new BigDecimal(100));
					BigDecimal ownerCurPrice = StockCache.stockMapByCode.get(warrantData.getOwnerCode()).getBasicQot()
							.getCurPrice();
					jsonObject.addProperty("正股价", ownerCurPrice);
					jsonObject.addProperty("回收价差距", ownerCurPrice.subtract(warrantData.getRecoveryPrice()));

					jsonObject.addProperty("curPrice", curPrice);
					jsonObject.addProperty("预期涡轮价格", preferWarrantPrice);
					jsonObject.addProperty("盈利", profit + "%");
					jsonObject.addProperty("回收价差率", ownerCurPrice.subtract(warrantData.getRecoveryPrice())
							.divide(ownerCurPrice, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100))
							.setScale(2, RoundingMode.HALF_UP) + "%");
					profitResult.put(profit, jsonObject);
				}
			}

			if (profitResult.isEmpty())
			{
				return htmlRsp(result);
			}
			return htmlRsp(profitResult.values());
		} finally
		{
		}
	}

}
