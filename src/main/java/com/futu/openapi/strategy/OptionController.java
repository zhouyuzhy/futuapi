package com.futu.openapi.strategy;

import com.futu.openapi.api.FutuApi;
import com.futu.openapi.api.enums.OptionTypeEnum;
import com.futu.openapi.api.enums.QotMarketEnum;
import com.futu.openapi.api.enums.QotSubTypeEnum;
import com.futu.openapi.api.model.Option;
import com.futu.openapi.api.model.Stock;
import com.futu.openapi.api.model.StockCache;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Controller
@Slf4j
public class OptionController extends BaseController
{

	@Autowired
	private FutuApi futuApi;

	@RequestMapping("/queryOption")
	@ResponseBody
	public String queryOption(@RequestParam("code") String code,
			@RequestParam(value = "beginTime", required = false) String beginTime,
			@RequestParam(value = "endTime", required = false) String endTime)
	{
		if (StringUtils.isBlank(beginTime))
		{
			Calendar calendar = Calendar.getInstance();
			int weeksInWeekYear = calendar.getWeeksInWeekYear();
			int weekYear = calendar.getWeekYear();
			calendar.setWeekDate(weekYear, weeksInWeekYear + 1, Calendar.FRIDAY);
			beginTime = DateFormatUtils.format(calendar, "yyyy-MM-dd");
		}
		if (StringUtils.isBlank(endTime))
		{
			endTime = beginTime;
		}
		FutuApi futuApi = new FutuApi();
		futuApi.queryOptionChain(code, QotMarketEnum.US, beginTime, endTime, OptionTypeEnum.CALL);
		futuApi.sub(code, QotMarketEnum.US, QotSubTypeEnum.BasicValue);
		futuApi.queryBasicQot(code, QotMarketEnum.US);

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
					e.printStackTrace();
				}
				List<Option> options = StockCache.stockMapByCode.get(code).getOptions();
				for (Option option : options)
				{
					futuApi.sub(option.getCode(), QotMarketEnum.US, QotSubTypeEnum.OrderBook);
					futuApi.queryOrderBook(option.getCode(), QotMarketEnum.US, 1);
				}
			}
		}).start();
		return "success";
	}

	@RequestMapping("/showOption")
	@ResponseBody
	public String showOption(@RequestParam("code") String code,
			@RequestParam(value = "preferAmount", required = false) Double preferAmountD)
	{
		BigDecimal preferAmount = null;
		if (preferAmountD != null)
		{
			preferAmount = new BigDecimal(String.valueOf(preferAmountD));
		}
		if (StockCache.stockMapByCode == null || StockCache.stockMapByCode.get(code) == null)
		{
			return "没有初始化完成";
		}
		TreeMap<BigDecimal, String> sortMap = new TreeMap<>();
		Map<String, BigDecimal> preferMap = new HashMap<>();

		for (Option option : StockCache.stockMapByCode.get(code).getOptions())
		{
			BigDecimal prefer = option.getStrikePrice().add(option.getOrderBook().getFirstSellPrice());
			sortMap.put(prefer, option.getCode());
			preferMap.put(option.getCode(), prefer);
		}
		List<JsonObject> result = new ArrayList<>();
		TreeMap<BigDecimal, Object> profitMap = new TreeMap<>(new Comparator<BigDecimal>()
		{

			@Override
			public int compare(BigDecimal o1, BigDecimal o2)
			{
				return o2.compareTo(o1);
			}
		});
		for (String sortCode : sortMap.values())
		{
			Stock stock = StockCache.stockMapByCode.get(sortCode);
			if(!(stock instanceof Option))
			{
				continue;
			}
			Option option = (Option) stock; 
			JsonObject jsonObject = gson.fromJson(gson.toJson(option), JsonObject.class);
			jsonObject.addProperty("prefer", preferMap.get(sortCode));
			if (preferAmount != null)
			{
				BigDecimal preferPrice = preferAmount.subtract(option.getStrikePrice());
				BigDecimal profit = preferPrice.subtract(option.getOrderBook().getFirstSellPrice()).divide(option.getOrderBook().getFirstSellPrice(),2,BigDecimal.ROUND_HALF_UP)
						.multiply(new BigDecimal(100));
				jsonObject.addProperty("盈利", profit+"%");
				profitMap.put(profit, jsonObject);
			}
			result.add(jsonObject);
		}
		if(profitMap.isEmpty())
		{
			return htmlRsp(result);
		}
		return htmlRsp(profitMap.values());
	}

}
