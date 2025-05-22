package com.buildmode.api;

import com.buildmode.BuildMode;
import com.buildmode.models.BuildSession;

import java.util.UUID;

/**
 * API for the BuildMode plugin.
 */
public class BuildModeAPI {
    
    private static BuildMode plugin;
    
    /**
     * Sets the plugin instance.
     * 
     * @param plugin The plugin instance
     */
    public static void setPlugin(BuildMode plugin) {
        BuildModeAPI.plugin = plugin;
    }
    
    /**
     * Checks if a player is in build mode.
     * 
     * @param uuid The player's UUID
     * @return True if the player is in build mode, false otherwise
     */
    public static boolean isInBuildMode(UUID uuid) {
        if (plugin == null) {
            return false;
        }
        
        return plugin.getSessionManager().isInBuildMode(uuid);
    }
    
    /**
     * Gets a player's build session.
     * 
     * @param uuid The player's UUID
     * @return The build session, or null if the player is not in build mode
     */
    public static BuildSession getSession(UUID uuid) {
        if (plugin == null) {
            return null;
        }
        
        return plugin.getSessionManager().getSession(uuid);
    }
    
    /**
     * Gets the remaining time of a player's build session in seconds.
     * 
     * @param uuid The player's UUID
     * @return The remaining time in seconds, or 0 if the player is not in build mode
     */
    public static int getRemainingSeconds(UUID uuid) {
        BuildSession session = getSession(uuid);
        
        if (session == null) {
            return 0;
        }
        
        return session.getRemainingSeconds();
    }
}
