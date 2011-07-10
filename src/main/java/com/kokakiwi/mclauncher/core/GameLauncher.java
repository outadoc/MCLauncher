package com.kokakiwi.mclauncher.core;

import java.applet.Applet;
import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Vector;

import com.kokakiwi.mclauncher.LauncherFrame;
import com.kokakiwi.mclauncher.core.wrapper.Wrapper;
import com.kokakiwi.mclauncher.utils.State;
import com.kokakiwi.mclauncher.utils.Utils;

public class GameLauncher implements Runnable
{
    private final LauncherFrame launcherFrame;
    private final Launcher      launcher;
    private boolean             natives_loaded;
    public ClassLoader          classLoader;
    public Wrapper              wrapper;

    public GameLauncher(LauncherFrame launcherFrame)
    {
        this.launcherFrame = launcherFrame;
        launcher = launcherFrame.launcher;
    }

    public void run()
    {
        try
        {
            if (!launcherFrame.launcher.updater.fatalError)
            {
                String path = (String) AccessController
                        .doPrivileged(new PrivilegedExceptionAction<Object>() {
                            public Object run() throws Exception
                            {
                                return Utils.getWorkingDirectory(launcherFrame)
                                        + File.separator + "bin"
                                        + File.separator;
                            }
                        });
                File dir = new File(path);
                updateClassPath(dir);
                runGame();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void updateClassPath(File dir)
    {
        launcher.setState(State.UPDATING_CLASSPATH);
        launcher.setPercentage(95);

        int urlNumber = launcher.updater.urlList.length;
        URL[] urls = new URL[urlNumber];
        for (int i = 0; i < urlNumber; i++)
        {
            try
            {
                String fileName = launcher.updater
                        .getJarName(launcher.updater.urlList[i]);
                urls[i] = new File(dir, fileName).toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
        }

        if (classLoader == null)
        {
            classLoader = new URLClassLoader(urls);
        }

        String path = dir.getAbsolutePath();
        if (!path.endsWith(File.separator))
        {
            path = path + File.separator;
        }

        unloadNatives(path);

        System.setProperty("org.lwjgl.librarypath", path + "natives");
        System.setProperty("net.java.games.input.librarypath", path + "natives");
        natives_loaded = true;
    }

    private void unloadNatives(String nativePath)
    {
        if (!natives_loaded)
        {
            return;
        }
        try
        {
            Field field = ClassLoader.class
                    .getDeclaredField("loadedLibraryNames");
            field.setAccessible(true);
            Vector<?> libs = (Vector<?>) field.get(getClass().getClassLoader());
            String path = new File(nativePath).getCanonicalPath();

            for (int i = 0; i < libs.size(); i++)
            {
                String s = (String) libs.get(i);

                if (s.startsWith(path))
                {
                    libs.remove(i);
                    i--;
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void runGame()
    {
        launcher.setState(State.DONE);
        launcher.setPercentage(100);

        wrapper = new Wrapper(launcherFrame);
        wrapper.init();
        if (wrapper.createApplet())
        {
            launcher.replace(wrapper.getApplet());
        }
    }

    public Applet createApplet() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException
    {
        Class<?> appletClass = classLoader
                .loadClass("net.minecraft.client.MinecraftApplet");
        return (Applet) appletClass.newInstance();
    }

}
