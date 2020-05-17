package com.futu.openapi.api.model;

import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
public class Option extends Stock
{
	private BigDecimal strikePrice;

	private String strikeTime;

}
