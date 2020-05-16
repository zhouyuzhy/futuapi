package com.futu.openapi.api.dto;

import com.futu.openapi.pb.QotGetWarrant;
import lombok.Data;

import java.util.List;

@Data
public class GetWarrantReplyDto extends BaseReplyDto
{
	List<QotGetWarrant.WarrantData> warrantDataList;
}
