package com.futu.openapi.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class GetOptionChainReplyDto extends BaseReplyDto
{
	private List<OptionChainItemDto> optionChainItemDtoList;
}
