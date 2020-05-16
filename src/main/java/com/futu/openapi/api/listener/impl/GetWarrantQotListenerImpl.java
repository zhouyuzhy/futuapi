package com.futu.openapi.api.listener.impl;

import com.futu.openapi.api.dto.GetWarrantReplyDto;
import com.futu.openapi.api.listener.GetWarrantQotListener;
import com.futu.openapi.api.model.StockCache;

public class GetWarrantQotListenerImpl implements GetWarrantQotListener
{

	@Override
	public void doReply(GetWarrantReplyDto message)
	{
		StockCache.putWarrantData(message);
	}
}
