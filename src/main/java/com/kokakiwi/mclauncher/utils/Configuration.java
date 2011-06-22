package com.kokakiwi.mclauncher.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Configuration {
	private Map<String, String> config = new HashMap<String, String>();
	
	public boolean load(InputStream inputFile)
	{
		Properties props = new Properties();
		
		try {
			props.load(inputFile);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		for(Object key : props.stringPropertyNames())
		{
			String name  = key.toString();
			String value = props.getProperty(name);
			
			config.put(name, value);
		}
		
		return true;
	}
	
	public void set(String name, String value)
	{
		config.put(name, value);
	}
	
	public String get(String name)
	{
		return config.get(name);
	}
}
