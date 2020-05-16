package com.futu.openapi.api.model;

import com.futu.openapi.api.dto.GetBasicQotReplyDto;
import com.futu.openapi.api.dto.GetOptionChainReplyDto;
import com.futu.openapi.api.dto.GetOrderBookReplyDto;
import com.futu.openapi.api.dto.GetWarrantReplyDto;
import com.futu.openapi.api.enums.StockTypeEnum;
import com.futu.openapi.pb.QotGetWarrant;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class Stock
{
	private String code;

	private StockTypeEnum stockType;

	private String ownerCode;

	private Stock ownerStock;

	private GetBasicQotReplyDto basicQot;

	private List<Option> options;

	private GetOrderBookReplyDto orderBook;

	private List<QotGetWarrant.WarrantData> warrantData;

}
