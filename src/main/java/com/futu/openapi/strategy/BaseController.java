package com.futu.openapi.strategy;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.Collection;

public abstract class BaseController
{

	Gson gson = new GsonBuilder().setPrettyPrinting().addSerializationExclusionStrategy(new ExclusionStrategy()
	{

		@Override
		public boolean shouldSkipField(FieldAttributes fieldAttributes)
		{
			return fieldAttributes.getAnnotation(Expose.class) != null;
		}

		@Override
		public boolean shouldSkipClass(Class<?> aClass)
		{
			return false;
		}
	}).create();

	public String htmlRsp(Object... objs)
	{
		StringBuilder rsp = new StringBuilder();
		rsp.append("<html>");
		for (Object obj : objs)
		{
			if (obj instanceof Collection)
			{
				for (Object item : (Collection) obj)
				{
					rsp.append(gson.toJson(item)).append("<br><br>");
				}
			} else
			{
				rsp.append(gson.toJson(obj)).append("<br><br>");
			}
		}
		rsp.append("</html>");
		return rsp.toString();
	}
}
