package com.futu.openapi.api.spi;

import com.futu.openapi.FTAPI_Conn;
import com.futu.openapi.FTSPI_Qot;
import com.futu.openapi.api.dto.GetBasicQotReplyDto;
import com.futu.openapi.api.dto.GetOptionChainReplyDto;
import com.futu.openapi.api.dto.GetOrderBookReplyDto;
import com.futu.openapi.api.dto.GetWarrantReplyDto;
import com.futu.openapi.api.dto.OptionChainItemDto;
import com.futu.openapi.api.listener.GetBasicQotListener;
import com.futu.openapi.api.listener.GetOrderBookQotListener;
import com.futu.openapi.api.listener.GetWarrantQotListener;
import com.futu.openapi.api.listener.QotListener;
import com.futu.openapi.api.listener.impl.GetOptionChainQotListenerImpl;
import com.futu.openapi.pb.QotCommon;
import com.futu.openapi.pb.QotGetBasicQot;
import com.futu.openapi.pb.QotGetOptionChain;
import com.futu.openapi.pb.QotGetOrderBook;
import com.futu.openapi.pb.QotGetSubInfo;
import com.futu.openapi.pb.QotGetWarrant;
import com.futu.openapi.pb.QotRegQotPush;
import com.futu.openapi.pb.QotSub;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class QotSpi implements FTSPI_Qot
{

	private List<QotListener> qotListeners;

	public QotSpi(List<QotListener> qotListeners)
	{
		this.qotListeners = qotListeners;
	}

	@Override
	public void onReply_GetBasicQot(FTAPI_Conn client, int nSerialNo, QotGetBasicQot.Response rsp)
	{
		log.info(rsp.toString());
		for (QotListener qotListener : qotListeners)
		{
			if (!(qotListener instanceof GetBasicQotListener))
			{
				continue;
			}
			List<QotCommon.BasicQot> basicQotListList = rsp.getS2C().getBasicQotListList();
			for (QotCommon.BasicQot basicQot : basicQotListList)
			{
				double curPrice = basicQot.getCurPrice();
				String code = basicQot.getSecurity().getCode();

				GetBasicQotReplyDto getBasicQotReplyDto = new GetBasicQotReplyDto();
				getBasicQotReplyDto.setCurPrice(new BigDecimal(String.valueOf(curPrice)));
				getBasicQotReplyDto.setSecurityCode(code);
				qotListener.doReply(getBasicQotReplyDto);
			}
		}
	}

	@Override
	public void onReply_Sub(FTAPI_Conn client, int nSerialNo, QotSub.Response rsp)
	{
		log.info(rsp.toString());
	}

	@Override
	public void onReply_GetOptionChain(FTAPI_Conn client, int nSerialNo, QotGetOptionChain.Response rsp)
	{
		log.info(rsp.toString());
		if(rsp.getRetType() != 0)
		{
			log.error("请求期权失败");
			return;
		}
		GetOptionChainReplyDto getOptionChainReplyDto = new GetOptionChainReplyDto();
		List<OptionChainItemDto> optionChainItemDtos = new ArrayList<>();
		getOptionChainReplyDto.setOptionChainItemDtoList(optionChainItemDtos);
		List<QotGetOptionChain.OptionItem> optionList = rsp.getS2C().getOptionChainList().get(0).getOptionList();
		for (QotGetOptionChain.OptionItem optionItem : optionList)
		{
			String code = optionItem.getCall().getBasic().getSecurity().getCode();
			BigDecimal strikePrice = new BigDecimal(String.valueOf(optionItem.getCall().getOptionExData().getStrikePrice()));
			String ownerCode = optionItem.getCall().getOptionExData().getOwner().getCode();
			String strikeTime = optionItem.getCall().getOptionExData().getStrikeTime();
			OptionChainItemDto optionChainItemDto = new OptionChainItemDto();
			optionChainItemDto.setSecurityCode(code);
			optionChainItemDto.setStrikePrice(new BigDecimal(String.valueOf(strikePrice)));
			optionChainItemDto.setOwnerSecurityCode(ownerCode);
			optionChainItemDto.setStrikeTime(strikeTime);
			optionChainItemDtos.add(optionChainItemDto);
			getOptionChainReplyDto.setSecurityCode(ownerCode);
		}
		for (QotListener qotListener : qotListeners)
		{
			if (!(qotListener instanceof GetOptionChainQotListenerImpl))
			{
				continue;
			}
			qotListener.doReply(getOptionChainReplyDto);
		}
	}

	@Override
	public void onReply_GetOrderBook(FTAPI_Conn client, int nSerialNo, QotGetOrderBook.Response rsp)
	{
		log.info(rsp.toString());
		if(rsp.getS2C() == null)
		{
			log.error("响应orderBook为空");
			return;
		}
		if(rsp.getS2C().getOrderBookAskListCount() == 0)
		{
			log.info("请求交易表没有值");
			return;
		}
		String code = rsp.getS2C().getSecurity().getCode();
		QotCommon.OrderBook orderBookAskList = rsp.getS2C().getOrderBookAskList(0);
		double sellPrice = orderBookAskList.getPrice();
		QotCommon.OrderBook orderBookBidList = rsp.getS2C().getOrderBookBidList(0);
		double buyPrice = orderBookBidList.getPrice();

		GetOrderBookReplyDto getOptionChainReplyDto = new GetOrderBookReplyDto();
		getOptionChainReplyDto.setSecurityCode(code);
		getOptionChainReplyDto.setFirstSellPrice(new BigDecimal(String.valueOf(sellPrice)));
		getOptionChainReplyDto.setFirstBuyPrice(new BigDecimal(String.valueOf(buyPrice)));

		for (QotListener qotListener : qotListeners)
		{
			if (!(qotListener instanceof GetOrderBookQotListener))
			{
				continue;
			}
			qotListener.doReply(getOptionChainReplyDto);
		}

	}

	@Override
	public void onReply_GetWarrant(FTAPI_Conn client, int nSerialNo, QotGetWarrant.Response rsp)
	{
		log.info(rsp.toString());
		List<QotGetWarrant.WarrantData> warrantDataListList = rsp.getS2C().getWarrantDataListList();
		GetWarrantReplyDto warrantReplyDto = new GetWarrantReplyDto();
		warrantReplyDto.setWarrantDataList(warrantDataListList);
		for(QotGetWarrant.WarrantData data : warrantDataListList)
		{
			warrantReplyDto.setSecurityCode(data.getOwner().getCode());
		}
		for (QotListener qotListener : qotListeners)
		{
			if (!(qotListener instanceof GetWarrantQotListener))
			{
				continue;
			}
			qotListener.doReply(warrantReplyDto);
		}
	}

	@Override
	public void onReply_RegQotPush(FTAPI_Conn client, int nSerialNo, QotRegQotPush.Response rsp)
	{
		if(rsp.getRetType()==0)
		{
			log.info("注册push成功");
		}
	}
}
