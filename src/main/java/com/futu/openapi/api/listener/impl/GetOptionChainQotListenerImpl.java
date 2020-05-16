package com.futu.openapi.api.listener.impl;

import com.futu.openapi.api.dto.BaseReplyDto;
import com.futu.openapi.api.dto.GetOptionChainReplyDto;
import com.futu.openapi.api.dto.OptionChainItemDto;
import com.futu.openapi.api.enums.QotMarketEnum;
import com.futu.openapi.api.enums.QotSubTypeEnum;
import com.futu.openapi.api.listener.GetOptionChainQotListener;
import com.futu.openapi.api.model.Option;
import com.futu.openapi.api.model.StockCache;

public class GetOptionChainQotListenerImpl implements GetOptionChainQotListener
{

	@Override
	public void doReply(GetOptionChainReplyDto message)
	{
		StockCache.putOptionChain(message);

	}
}
