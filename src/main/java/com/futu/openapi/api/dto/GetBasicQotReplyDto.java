package com.futu.openapi.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GetBasicQotReplyDto extends BaseReplyDto
{
	private BigDecimal curPrice;
}
