package com.futu.openapi.api.listener;

import com.futu.openapi.api.dto.BaseReplyDto;

public interface QotListener<T extends BaseReplyDto>
{
	public void doReply(T message);
}
