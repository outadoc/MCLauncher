package com.kokakiwi.mclauncher.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import com.kokakiwi.mclauncher.LauncherFrame;

public class LocalString
{
    private final LauncherFrame                    launcherFrame;
    private final Map<String, Map<String, Object>> strings = new HashMap<String, Map<String, Object>>();
    private String                                 lang;

    public LocalString(LauncherFrame launcherFrame, List<String> langs)
    {
        this.launcherFrame = launcherFrame;

        for (String lang : langs)
        {
            strings.put(lang,
                    load(Utils.getResourceAsStream("lang/" + lang + ".yml")));
        }

        lang = Locale.getDefault().toString();

        if (strings.get(lang) == null)
        {
            lang = "en_US";
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> load(InputStream input)
    {
        Yaml parser = new Yaml();
        return (Map<String, Object>) parser.load(input);
    }

    public String getString(String node)
    {
        String nodeValue = (String) get(node);

        Map<String, String> params = new HashMap<String, String>();
        params.put("GAMENAME",
                launcherFrame.config.getString("gameLauncher.gameName"));

        nodeValue = StringFormatter.format(nodeValue, params);

        return nodeValue;
    }

    @SuppressWarnings("unchecked")
    private Object get(String nodeName)
    {
        Map<String, Object> cStrings = strings.get(lang);

        if (nodeName.contains("."))
        {
            String[] nodes = nodeName.split("\\.");
            Object currentNode = null;

            for (String node : nodes)
            {
                if (currentNode == null)
                {
                    currentNode = cStrings.get(node);
                }
                else
                {
                    if (currentNode instanceof Map)
                    {
                        currentNode = ((Map<String, Object>) currentNode)
                                .get(node);
                    }
                    else
                    {
                        break;
                    }
                }
            }

            return currentNode;
        }
        else
        {
            return cStrings.get(nodeName.toLowerCase());
        }
    }
}
