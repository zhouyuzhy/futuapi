package com.futu.openapi.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetOrderBookReplyDto extends BaseReplyDto
{
	private BigDecimal firstBuyPrice;

	private BigDecimal firstSellPrice;
}
