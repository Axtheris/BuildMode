package com.buildmode;

import com.buildmode.commands.BuildModeCommand;
import com.buildmode.listeners.BuildModeListener;
import com.buildmode.managers.SessionManager;
import com.buildmode.placeholders.BuildModePlaceholders;
import com.buildmode.utils.ConfigManager;
import com.buildmode.utils.UIManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BuildMode extends JavaPlugin {
    
    private static BuildMode instance;
    private ConfigManager configManager;
    private SessionManager sessionManager;
    private UIManager uiManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize config
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        
        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        // Initialize UI manager
        uiManager = new UIManager(this);
        
        // Register commands
        getCommand("buildmode").setExecutor(new BuildModeCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new BuildModeListener(this), this);
        
        // Register PlaceholderAPI expansion if available
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            if (new BuildModePlaceholders(this).register()) {
                getLogger().info("Registered PlaceholderAPI expansion!");
            }
        }
        
        // Display ASCII art and credit
        getLogger().info("\n" +
                "  ____  __  __ \n" +
                " |  _ \\|  \\/  |\n" +
                " | |_) | |\\/| |\n" +
                " |  _ <| |  | |\n" +
                " |_| \\_\\_|  |_|\n" +
                "                \n" +
                "BuildMode v" + getPluginMeta().getVersion() + " - With love from Axther\n");
        
        getLogger().info("BuildMode has been enabled!");
    }
    
    @Override
    public void onDisable() {
        // Save active sessions
        if (sessionManager != null) {
            sessionManager.saveAllSessions();
        }
        
        // Clean up UI elements
        if (uiManager != null) {
            uiManager.cleanup();
        }
        
        getLogger().info("BuildMode has been disabled!");
    }
    
    /**
     * Gets the instance of the plugin.
     * 
     * @return The plugin instance
     */
    public static BuildMode getInstance() {
        return instance;
    }
    
    /**
     * Gets the config manager.
     * 
     * @return The config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * Gets the session manager.
     * 
     * @return The session manager
     */
    public SessionManager getSessionManager() {
        return sessionManager;
    }
    
    /**
     * Gets the UI manager.
     * 
     * @return The UI manager
     */
    public UIManager getUIManager() {
        return uiManager;
    }
    
    /**
     * Reloads the plugin configuration.
     */
    public void reload() {
        reloadConfig();
        configManager.reload();
        sessionManager.reload();
        uiManager.reload();
        getLogger().info("BuildMode configuration reloaded!");
    }
}
