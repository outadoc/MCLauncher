package com.kokakiwi.mclauncher.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class MCLogger
{
    private static Configuration config;
    
    // private static Logger global = Logger.getGlobal();
    private static Logger logger = Logger.getLogger("MCLauncher");
    
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
    
    public static void debug(String message)
    {
        if(System.getenv("debugMode") != null || config.getBoolean("launcher.debugMode"))
        {
            logger.log(new Debug(), message);
        }
    }
    
    public static void printSystemInfos()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("System informations:\r\n");
        sb.append("\tOS Name: ");
        sb.append(System.getProperty("os.name"));
        sb.append(" (");
        sb.append(SystemUtils.getSystemOS().getName());
        sb.append(") ");
        sb.append(System.getProperty("os.version"));
        sb.append("\r\n");
        sb.append("\tOS Arch: ");
        sb.append(System.getProperty("os.arch"));
        sb.append(" (");
        sb.append(SystemUtils.getSystemArch());
        sb.append(")\r\n");
        sb.append("\tJava Version: ");
        sb.append(System.getProperty("java.version"));
        sb.append("\r\n");
        sb.append("\tJava API Version: ");
        sb.append(System.getProperty("java.class.version"));
        sb.append("\r\n");
        sb.append("\tLauncher path: ");
        sb.append(SystemUtils.getExecDirectoryPath());
        sb.append("\r\n");
        
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
