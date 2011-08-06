package com.kokakiwi.mclauncher.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.kokakiwi.mclauncher.utils.java.Utils;

public class ProfileManager
{
    private final Map<String, Profile> profiles       = new HashMap<String, Profile>();
    private String                     currentProfile = "default";
    
    public ProfileManager()
    {
        final File defaultProfileDir = new File("profiles/default");
        if (!defaultProfileDir.exists())
        {
            defaultProfileDir.mkdirs();
            createProfile("Default");
        }
        
        final File profilesDir = new File("profiles");
        final String[] profDir = profilesDir.list();
        for (final String dir : profDir)
        {
            final File profileDir = new File(profilesDir, dir);
            if (profileDir.isDirectory())
            {
                final File descriptorFile = new File(profileDir, "profile.yml");
                if (descriptorFile.exists())
                {
                    final Configuration descriptor = new Configuration();
                    descriptor.load(descriptorFile);
                    final Profile profile = new Profile(descriptor);
                    profiles.put(profile.getID(), profile);
                }
            }
        }
    }
    
    public void createProfile(String name)
    {
        final Profile profile = new Profile(name);
        profiles.put(profile.getID(), profile);
        try
        {
            profile.save();
        }
        catch (final Exception e)
        {
            MCLogger.error(e.getLocalizedMessage());
        }
    }
    
    public void deleteProfile(Profile profile)
    {
        deleteProfile(profile.getID());
    }
    
    public void deleteProfile(String id)
    {
        File profileDir = new File("profiles/" + id);
        deleteDirectory(profileDir);
        profiles.remove(id);
    }
    
    public void deleteDirectory(File dir)
    {
        if(dir.isDirectory())
        {
            for(String sub : dir.list())
            {
                File subElement = new File(dir, sub);
                if(subElement.isDirectory())
                {
                    deleteDirectory(subElement);
                }
                subElement.delete();
            }
        }
        dir.delete();
    }
    
    public Profile getCurrentProfile()
    {
        return getProfile(currentProfile);
    }
    
    public void setCurrentProfile(Profile profile)
    {
        setCurrentProfile(profile.getID());
    }
    
    public void setCurrentProfile(String id)
    {
        currentProfile = id;
    }
    
    public Profile getProfile(String id)
    {
        return profiles.get(id);
    }
    
    public Map<String, Profile> getProfiles()
    {
        return profiles;
    }
    
    public class Profile
    {
        private String              name;
        private String              id;
        private Configuration       descriptor = new Configuration();
        private final Configuration config     = new Configuration();
        
        /**
         * Load a profile from a descriptor file
         * 
         * @param descriptor
         *            Descriptor file
         */
        public Profile(Configuration descriptor)
        {
            name = descriptor.getString("name");
            id = descriptor.getString("id");
            this.descriptor = descriptor;
            loadConfig();
        }
        
        /**
         * Create a new profile
         * 
         * @param name
         *            Profile name
         */
        public Profile(String name)
        {
            this.name = name;
            id = name.toLowerCase().trim();
            loadConfig();
            final File descFile = new File("profiles/" + id + "/profile.yml");
            if (!descFile.exists())
            {
                try
                {
                    descFile.getParentFile().mkdirs();
                    descFile.createNewFile();
                    initDescriptor();
                    descriptor.save(descFile);
                }
                catch (final Exception e)
                {
                    MCLogger.info(e.getLocalizedMessage());
                }
            }
        }
        
        public void loadConfig()
        {
            config.load(Utils.getResourceAsStream("config/config.yml"), "yaml");
            final File confFile = new File("profiles/" + id + "/config.yml");
            if (!confFile.exists())
            {
                try
                {
                    confFile.getParentFile().mkdirs();
                    confFile.createNewFile();
                }
                catch (final IOException e)
                {
                    MCLogger.info(e.getLocalizedMessage());
                }
            }
            config.load(confFile);
        }
        
        public void initDescriptor()
        {
            descriptor.set("name", name);
            descriptor.set("id", id);
        }
        
        public void save() throws Exception
        {
            final File confFile = new File("profiles/" + id + "/config.yml");
            config.save(confFile);
            saveDescriptor();
        }
        
        public void saveDescriptor() throws Exception
        {
            final File descFile = new File("profiles/" + id + "/profile.yml");
            descriptor.save(descFile);
        }
        
        public String getName()
        {
            return name;
        }
        
        public String getID()
        {
            return id;
        }
        
        public void setName(String name)
        {
            this.name = name;
            descriptor.set("name", name);
        }
        
        public void setID(String id)
        {
            this.id = id;
            descriptor.set("id", id);
        }
        
        public Configuration getDescriptor()
        {
            return descriptor;
        }
        
        public Configuration getConfig()
        {
            return config;
        }
        
        @Override
        public String toString()
        {
            return name;
        }
    }
}
