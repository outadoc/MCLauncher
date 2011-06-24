package com.kokakiwi.mclauncher.core.wrapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class JavaUtils {
	public static List<Field> getFieldsWithType(Class<?> parentClass, Class<?> fieldType)
	{
		Field[] fields = parentClass.getDeclaredFields();
		List<Field> fieldList = new ArrayList<Field>();
		for(Field field : fields)
		{
			if(field.getType() == fieldType)
				fieldList.add(field);
		}
		
		return fieldList;
	}
	
	public static List<Method> getMethodsWithType(Class<?> parentClass, Class<?> methodType)
	{
		Method[] methods = parentClass.getDeclaredMethods();
		List<Method> methodList = new ArrayList<Method>();
		
		for(Method method : methods)
		{
			if(method.getReturnType() == methodType)
				methodList.add(method);
		}
		
		return methodList;
	}
	
	public static List<Method> getMethodsWithReturnTypeAndValuesType(Class<?> parentClass, Class<?> returnType, Class<?> ...classes)
	{
		Method[] methods = parentClass.getDeclaredMethods();
		List<Method> methodList = new ArrayList<Method>();
		
		for(Method method : methods)
		{
			if(method.getReturnType() == returnType)
			{
				String methodName = method.getName();
				try {
					parentClass.getDeclaredMethod(methodName, classes);
					methodList.add(method);
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					
				}
			}
		}
		
		return methodList;
	}
}
