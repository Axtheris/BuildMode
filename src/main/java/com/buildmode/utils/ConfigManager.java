package com.buildmode.utils;

import com.buildmode.BuildMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages the plugin configuration.
 */
public class ConfigManager {
    
    private final BuildMode plugin;
    private Set<Material> blacklistedMaterials;
    private Set<Material> whitelistedMaterials;
    private boolean useBlacklist;
    private File sessionsFile;
    
    /**
     * Creates a new config manager.
     * 
     * @param plugin The plugin instance
     */
    public ConfigManager(BuildMode plugin) {
        this.plugin = plugin;
        this.sessionsFile = new File(plugin.getDataFolder(), "sessions.yml");
        reload();
    }
    
    /**
     * Reloads the configuration.
     */
    public void reload() {
        plugin.reloadConfig();
        loadMaterialLists();
    }
    
    /**
     * Loads the blacklist and whitelist of materials from the configuration.
     */
    private void loadMaterialLists() {
        FileConfiguration config = plugin.getConfig();
        
        // Determine if we're using blacklist or whitelist
        useBlacklist = config.getString("restriction-mode", "blacklist").equalsIgnoreCase("blacklist");
        
        // Load blacklist
        blacklistedMaterials = new HashSet<>();
        List<String> blacklist = config.getStringList("blacklist");
        for (String materialName : blacklist) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                blacklistedMaterials.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in blacklist: " + materialName);
            }
        }
        
        // Load whitelist
        whitelistedMaterials = new HashSet<>();
        List<String> whitelist = config.getStringList("whitelist");
        for (String materialName : whitelist) {
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                whitelistedMaterials.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in whitelist: " + materialName);
            }
        }
    }
    
    /**
     * Checks if a material is allowed in build mode.
     * 
     * @param material The material to check
     * @return True if the material is allowed, false otherwise
     */
    public boolean isMaterialAllowed(Material material) {
        if (useBlacklist) {
            return !blacklistedMaterials.contains(material);
        } else {
            return whitelistedMaterials.contains(material);
        }
    }
    
    /**
     * Gets the build mode duration in minutes.
     * 
     * @return The build mode duration in minutes
     */
    public int getBuildDurationMinutes() {
        return plugin.getConfig().getInt("build-duration-minutes", 60);
    }
    
    /**
     * Gets the cooldown between sessions in minutes.
     * 
     * @return The cooldown in minutes
     */
    public int getCooldownMinutes() {
        return plugin.getConfig().getInt("cooldown-minutes", 1);
    }
    
    /**
     * Checks if the boss bar is enabled.
     * 
     * @return True if the boss bar is enabled, false otherwise
     */
    public boolean isBossBarEnabled() {
        return plugin.getConfig().getBoolean("bossbar", true);
    }
    
    /**
     * Checks if the scoreboard is enabled.
     * 
     * @return True if the scoreboard is enabled, false otherwise
     */
    public boolean isScoreboardEnabled() {
        return plugin.getConfig().getBoolean("scoreboard", false);
    }
    
    /**
     * Checks if redstone components are allowed.
     * 
     * @return True if redstone components are allowed, false otherwise
     */
    public boolean isRedstoneAllowed() {
        return plugin.getConfig().getBoolean("allowed-redstone", true);
    }
    
    /**
     * Gets the blacklisted materials.
     * 
     * @return The blacklisted materials
     */
    public Set<Material> getBlacklistedMaterials() {
        return new HashSet<>(blacklistedMaterials);
    }
    
    /**
     * Gets the whitelisted materials.
     * 
     * @return The whitelisted materials
     */
    public Set<Material> getWhitelistedMaterials() {
        return new HashSet<>(whitelistedMaterials);
    }
    
    /**
     * Checks if the plugin is using a blacklist.
     * 
     * @return True if using a blacklist, false if using a whitelist
     */
    public boolean isUsingBlacklist() {
        return useBlacklist;
    }
    
    /**
     * Gets the sessions file.
     * 
     * @return The sessions file
     */
    public File getSessionsFile() {
        return sessionsFile;
    }
    
    /**
     * Saves the sessions configuration.
     * 
     * @param config The configuration to save
     */
    public void saveSessionsConfig(YamlConfiguration config) {
        try {
            config.save(sessionsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save sessions: " + e.getMessage());
        }
    }
    
    /**
     * Loads the sessions configuration.
     * 
     * @return The sessions configuration
     */
    public YamlConfiguration loadSessionsConfig() {
        if (!sessionsFile.exists()) {
            return new YamlConfiguration();
        }
        
        return YamlConfiguration.loadConfiguration(sessionsFile);
    }
}
