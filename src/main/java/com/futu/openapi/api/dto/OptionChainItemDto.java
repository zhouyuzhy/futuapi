package com.futu.openapi.api.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OptionChainItemDto
{
	private String securityCode;

	private BigDecimal strikePrice;

	private String strikeTime;

	private String ownerSecurityCode;
}
