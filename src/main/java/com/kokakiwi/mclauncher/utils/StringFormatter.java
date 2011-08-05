package com.kokakiwi.mclauncher.utils;

import java.util.Map;

public class StringFormatter
{
    public static String format(String from, Map<String, String> keys)
    {
        String finalString = from;
        for (final String key : keys.keySet())
        {
            final String value = keys.get(key);
            finalString = finalString.replaceAll("\\{" + key.toUpperCase()
                    + "\\}", value);
        }
        return finalString;
    }
}
