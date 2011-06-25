package com.kokakiwi.mclauncher.utils;

import java.util.Map;

public class StringFormatter {
	public static String format(String from, Map<String, String> keys)
	{
		String finalString = from;
		for(String key : keys.keySet())
		{
			String value = keys.get(key);
			finalString = finalString.replaceAll("\\{" + key.toUpperCase() + "\\}", value);
		}
		return finalString;
	}
}
