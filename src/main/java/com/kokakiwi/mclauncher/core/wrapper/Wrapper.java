package com.kokakiwi.mclauncher.core.wrapper;

import java.applet.Applet;
import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.utils.Utils;

public class Wrapper {
	private LauncherFrame launcherFrame;
	private ClassLoader classLoader;
	
	private Applet applet;

	public Wrapper(LauncherFrame launcherFrame) {
		this.launcherFrame = launcherFrame;
		this.classLoader = launcherFrame.launcher.launcher.classLoader;
	}
	
	public void init()
	{
		//Make Minecraft portable ! :D
		try {
			List<Field> fields = JavaUtils.getFieldsWithType(classLoader.loadClass("net.minecraft.client.Minecraft"), File.class);
			for(Field field : fields)
			{
				field.setAccessible(true);
				try {
					field.get(classLoader.loadClass("net.minecraft.client.Minecraft"));
					field.set(null, Utils.getWorkingDirectory(launcherFrame));
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {}
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean createApplet()
	{
		try {
			this.applet = launcherFrame.launcher.launcher.createApplet();
			return true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public Applet getApplet()
	{
		return this.applet;
	}
}
