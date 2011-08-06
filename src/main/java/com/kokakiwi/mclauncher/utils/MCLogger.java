package com.kokakiwi.mclauncher.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.kokakiwi.mclauncher.utils.java.SystemUtils;

public class MCLogger
{
    private static Configuration config;
    
    // private static Logger global = Logger.getGlobal();
    private static Logger        logger = Logger.getLogger("MCLauncher");
    
    static
    {
        try
        {
            final FileHandler fh = new FileHandler("mclauncher.log");
            fh.setFormatter(new MCFormatter());
            final ConsoleHandler ch = new ConsoleHandler();
            ch.setFormatter(new MCFormatter());
            
            logger.setUseParentHandlers(false);
            logger.addHandler(ch);
            logger.addHandler(fh);
        }
        catch (final Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public static void info(String message)
    {
        logger.info(message);
    }
    
    public static void warning(String message)
    {
        logger.warning(message);
    }
    
    public static void error(String message)
    {
        logger.severe(message);
    }
    
    public static void debug(String message)
    {
        if (System.getenv("debugMode") != null
                || config.getBoolean("launcher.debugMode"))
        {
            logger.log(new Debug(), message);
        }
    }
    
    public static void printSystemInfos()
    {
        Map<String, String> infos = new HashMap<String, String>();
        infos.put("OS Name", System.getProperty("os.name") + " (" + SystemUtils.getSystemOS().getName() + ")");
        infos.put("OS Arch", System.getProperty("os.arch") + " (" + SystemUtils.getSystemArch() + ")");
        infos.put("Java version", System.getProperty("java.version"));
        infos.put("Java API Version", System.getProperty("java.class.version"));
        infos.put("Launcher path", SystemUtils.getExecDirectoryPath());
        
        final StringBuffer sb = new StringBuffer();
        sb.append("System informations:");
        sb.append(System.lineSeparator());
        for(String key : infos.keySet())
        {
            String value = infos.get(key);
            sb.append("\t");
            sb.append(key);
            sb.append(" : ");
            sb.append(value);
            sb.append(System.lineSeparator());
        }
        
        debug(sb.toString());
    }
    
    public static void setConfig(Configuration config)
    {
        MCLogger.config = config;
    }
    
    public static class MCFormatter extends Formatter
    {
        
        @Override
        public String format(LogRecord log)
        {
            final StringBuffer sb = new StringBuffer();
            sb.append("[MCLauncher] ");
            sb.append(log.getLevel().getName());
            sb.append(" : ");
            sb.append(log.getMessage());
            sb.append("\r\n");
            return sb.toString();
        }
        
    }
    
    private static class Debug extends Level
    {
        private static final long serialVersionUID = 606354531090515777L;
        
        protected Debug()
        {
            super("DEBUG", Level.INFO.intValue());
        }
    }
}
