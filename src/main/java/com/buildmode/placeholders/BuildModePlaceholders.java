package com.buildmode.placeholders;

import com.buildmode.BuildMode;
import com.buildmode.models.BuildSession;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI expansion for the BuildMode plugin.
 */
public class BuildModePlaceholders extends PlaceholderExpansion {
    
    private final BuildMode plugin;
    
    /**
     * Creates a new placeholder expansion.
     * 
     * @param plugin The plugin instance
     */
    public BuildModePlaceholders(BuildMode plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "buildmode";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "Axther";
    }
    
    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true; // This is an internal expansion, so we want it to persist through reloads
    }
    
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }
        
        // %buildmode_active%
        if (identifier.equals("active")) {
            return plugin.getSessionManager().isInBuildMode(player.getUniqueId()) ? "true" : "false";
        }
        
        // %buildmode_timeleft%
        if (identifier.equals("timeleft")) {
            BuildSession session = plugin.getSessionManager().getSession(player.getUniqueId());
            
            if (session == null) {
                return "0:00";
            }
            
            int remainingSeconds = session.getRemainingSeconds();
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            
            return String.format("%d:%02d", minutes, seconds);
        }
        
        // %buildmode_timeleft_seconds%
        if (identifier.equals("timeleft_seconds")) {
            BuildSession session = plugin.getSessionManager().getSession(player.getUniqueId());
            
            if (session == null) {
                return "0";
            }
            
            return String.valueOf(session.getRemainingSeconds());
        }
        
        // %buildmode_cooldown%
        if (identifier.equals("cooldown")) {
            if (!plugin.getSessionManager().isOnCooldown(player.getUniqueId())) {
                return "0";
            }
            
            // This is a simplified implementation, in a real plugin you would need to
            // track the exact cooldown time remaining
            return String.valueOf(plugin.getConfigManager().getCooldownMinutes());
        }
        
        return null; // Placeholder is not recognized
    }
}
