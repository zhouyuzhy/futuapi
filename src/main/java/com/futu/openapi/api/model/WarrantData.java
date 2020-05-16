package com.futu.openapi.api.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WarrantData extends Stock
{
	private BigDecimal strikePrice;

	private String strikeTime;

	private BigDecimal recoveryPrice;

	private BigDecimal conversionRatio;

}
