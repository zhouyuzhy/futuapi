package com.futu.openapi.api.listener.impl;

import com.futu.openapi.api.dto.GetOrderBookReplyDto;
import com.futu.openapi.api.listener.GetOrderBookQotListener;
import com.futu.openapi.api.model.StockCache;

public class GetOrderBookQotListenerImpl implements GetOrderBookQotListener
{

	@Override
	public void doReply(GetOrderBookReplyDto message)
	{
		StockCache.putOrderBook(message);
	}
}
