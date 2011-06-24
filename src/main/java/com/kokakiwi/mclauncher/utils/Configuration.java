package com.kokakiwi.mclauncher.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.yaml.snakeyaml.Yaml;

public class Configuration {
	private Map<String, Object> config = new HashMap<String, Object>();
	
	public boolean load(InputStream inputStream)
	{
		return load(inputStream, "");
	}
	
	@SuppressWarnings("unchecked")
	public boolean load(InputStream inputFile, String type)
	{
		if(type.equalsIgnoreCase("yaml")) {
			Yaml yamlParser = new Yaml();
			config.putAll((Map<? extends String, ? extends Object>) yamlParser.load(inputFile));
		}else {
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
		}
		
		return true;
	}
	
	public void set(String name, Object value)
	{
		config.put(name.toLowerCase(), value);
	}
	
	public String getString(String name)
	{
		return (String) get(name);
	}

	@SuppressWarnings("unchecked")
	public List<Object> getList(String name)
	{
		return (List<Object>) get(name);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getStringList(String name)
	{
		return (List<String>) get(name);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, Object> getNode(String name)
	{
		return (Map<String, Object>) get(name);
	}
	
	public boolean getBoolean(String name)
	{
		return (Boolean) (get(name) == null ? false : get(name));
	}
	
	public int getInteger(String name)
	{
		return (Integer) get(name);
	}
	
	@SuppressWarnings("unchecked")
	public Object get(String nodeName)
	{
		if(nodeName.contains("."))
		{
			String[] nodes = nodeName.split("\\.");
			Object currentNode = null;
			
			for(String node : nodes)
			{
				if(currentNode == null)
				{
					currentNode = config.get(node);
				}else {
					if(currentNode instanceof Map)
					{
						currentNode = ((Map<String, Object>) currentNode).get(node);
					}else
						break;
				}
			}
			
			return currentNode;
		}else {
			return config.get(nodeName.toLowerCase());
		}
	}
}
