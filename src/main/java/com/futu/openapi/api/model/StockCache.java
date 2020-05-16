package com.futu.openapi.api.model;

import com.futu.openapi.api.dto.GetBasicQotReplyDto;
import com.futu.openapi.api.dto.GetOptionChainReplyDto;
import com.futu.openapi.api.dto.GetOrderBookReplyDto;
import com.futu.openapi.api.dto.GetWarrantReplyDto;
import com.futu.openapi.api.dto.OptionChainItemDto;
import com.futu.openapi.api.enums.StockTypeEnum;
import com.futu.openapi.pb.QotGetWarrant;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StockCache
{

	public static Map<String, Stock> stockMapByCode = new ConcurrentHashMap<>();

	public static void putBasicQot(GetBasicQotReplyDto basicQotReplyDto)
	{
		Stock stock = createIfNotExist(basicQotReplyDto.getSecurityCode());
		stock.setBasicQot(basicQotReplyDto);
	}

	public static void putOptionChain(GetOptionChainReplyDto optionChainReplyDto)
	{
		Stock stock = createIfNotExist(optionChainReplyDto.getSecurityCode());
		List<Option> options = new ArrayList<>();
		stock.setOptions(options);
		for (OptionChainItemDto optionChainItemDto : optionChainReplyDto.getOptionChainItemDtoList())
		{
			Option option = new Option();
			option.setStockType(StockTypeEnum.OPTION);
			option.setCode(optionChainItemDto.getSecurityCode());
			option.setOwnerCode(optionChainReplyDto.getSecurityCode());
			option.setOwnerStock(stock);
			option.setStrikePrice(optionChainItemDto.getStrikePrice());
			stockMapByCode.putIfAbsent(option.getCode(), option);
			options.add(option);
		}
	}

	public static void putOrderBook(GetOrderBookReplyDto getOrderBookReplyDto)
	{
		Stock stock = createIfNotExist(getOrderBookReplyDto.getSecurityCode());
		stock.setOrderBook(getOrderBookReplyDto);
	}

	public static void putWarrantData(GetWarrantReplyDto getWarrantReplyDto)
	{
		Stock stock = createIfNotExist(getWarrantReplyDto.getSecurityCode());
		stock.setWarrantData(getWarrantReplyDto.getWarrantDataList());
		for (QotGetWarrant.WarrantData warrantData : getWarrantReplyDto.getWarrantDataList())
		{
			WarrantData stockWarrant = new WarrantData();
			stockWarrant.setStockType(StockTypeEnum.WARRANT);
			stockWarrant.setCode(warrantData.getStock().getCode());
			stockWarrant.setOwnerCode(warrantData.getOwner().getCode());
			stockWarrant.setOwnerStock(stock);
			stockWarrant.setStrikePrice(new BigDecimal(String.valueOf(warrantData.getStrikePrice())));
			stockWarrant.setStrikeTime(warrantData.getMaturityTime());
			stockWarrant.setRecoveryPrice(new BigDecimal(String.valueOf(warrantData.getRecoveryPrice())));
			GetOrderBookReplyDto getOrderBookReplyDto = new GetOrderBookReplyDto();
			getOrderBookReplyDto.setFirstBuyPrice(new BigDecimal(String.valueOf(warrantData.getBidPrice())));
			getOrderBookReplyDto.setFirstSellPrice(new BigDecimal(String.valueOf(warrantData.getAskPrice())));
			getOrderBookReplyDto.setSecurityCode(warrantData.getStock().getCode());
			stockWarrant.setOrderBook(getOrderBookReplyDto);
			GetBasicQotReplyDto getBasicQotReplyDto = new GetBasicQotReplyDto();
			getBasicQotReplyDto.setSecurityCode(warrantData.getStock().getCode());
			getBasicQotReplyDto.setCurPrice(new BigDecimal(String.valueOf(warrantData.getCurPrice())));
			stockWarrant.setBasicQot(getBasicQotReplyDto);
			stockWarrant.setConversionRatio(new BigDecimal(String.valueOf(warrantData.getConversionRatio())));
			stockMapByCode.putIfAbsent(stockWarrant.getCode(), stockWarrant);
		}
	}

	private static Stock createIfNotExist(String code)
	{
		if (stockMapByCode.get(code) == null)
		{
			Stock stock = new Stock();
			stock.setStockType(StockTypeEnum.STOCK);
			stock.setCode(code);
			stockMapByCode.putIfAbsent(code, stock);
		}
		return stockMapByCode.get(code);
	}
}
