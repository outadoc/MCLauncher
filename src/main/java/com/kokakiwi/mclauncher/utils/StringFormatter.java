package com.kokakiwi.mclauncher.utils;

import java.util.Map;

public class StringFormatter {
	public static String format(String from, Map<String, String> keys)
	{
		for(String key : keys.keySet())
		{
			String value = keys.get(key);
			from = from.replaceAll("\\{" + key.toUpperCase() + "\\}", value);
		}
		return from;
	}
}
