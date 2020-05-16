package com.futu.openapi.api.listener.impl;

import com.futu.openapi.api.dto.GetBasicQotReplyDto;
import com.futu.openapi.api.listener.GetBasicQotListener;
import com.futu.openapi.api.model.StockCache;

public class GetBasicQotListenerImpl implements GetBasicQotListener
{

	@Override
	public void doReply(GetBasicQotReplyDto message)
	{
		StockCache.putBasicQot(message);
	}
}
