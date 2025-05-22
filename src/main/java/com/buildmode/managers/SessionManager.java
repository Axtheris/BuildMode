package com.buildmode.managers;

import com.buildmode.BuildMode;
import com.buildmode.api.BuildModeAPI;
import com.buildmode.models.BuildSession;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages build mode sessions for players.
 */
public class SessionManager {
    
    private final BuildMode plugin;
    private final Map<UUID, BuildSession> activeSessions;
    private final Map<UUID, Long> lastSessionEndTime;
    private BukkitTask checkTask;
    
    /**
     * Creates a new session manager.
     * 
     * @param plugin The plugin instance
     */
    public SessionManager(BuildMode plugin) {
        this.plugin = plugin;
        this.activeSessions = new HashMap<>();
        this.lastSessionEndTime = new HashMap<>();
        
        // Set plugin instance in API
        BuildModeAPI.setPlugin(plugin);
        
        // Load saved sessions
        loadSessions();
        
        // Start session check task
        startCheckTask();
    }
    
    /**
     * Starts the session check task.
     */
    private void startCheckTask() {
        // Cancel existing task if it exists
        if (checkTask != null) {
            checkTask.cancel();
        }
        
        // Start new task that runs every 30 seconds
        checkTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkSessions, 20L, 30 * 20L);
    }
    
    /**
     * Checks all active sessions and ends expired ones.
     */
    private void checkSessions() {
        for (UUID uuid : activeSessions.keySet().toArray(new UUID[0])) {
            BuildSession session = activeSessions.get(uuid);
            
            if (session.hasExpired()) {
                Player player = Bukkit.getPlayer(uuid);
                
                if (player != null && player.isOnline()) {
                    endSession(player);
                    player.sendMessage("§cYour build mode session has expired.");
                } else {
                    // Player is offline, just remove the session
                    activeSessions.remove(uuid);
                    lastSessionEndTime.put(uuid, System.currentTimeMillis());
                }
            }
        }
    }
    
    /**
     * Starts a build mode session for a player.
     * 
     * @param player The player
     * @return True if the session was started, false otherwise
     */
    public boolean startSession(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Check if player already has an active session
        if (activeSessions.containsKey(uuid)) {
            player.sendMessage("§cYou already have an active build mode session.");
            return false;
        }
        
        // Check cooldown
        if (isOnCooldown(uuid)) {
            int cooldownMinutes = plugin.getConfigManager().getCooldownMinutes();
            player.sendMessage("§cYou must wait " + cooldownMinutes + " minute(s) between build mode sessions.");
            return false;
        }
        
        // Get session duration from config
        int durationMinutes = plugin.getConfigManager().getBuildDurationMinutes();
        
        // Check if player has admin permission to bypass time limit
        if (player.hasPermission("buildmode.admin")) {
            durationMinutes = Integer.MAX_VALUE / (60 * 1000); // Very long duration
        }
        
        // Create new session
        BuildSession session = new BuildSession(player, durationMinutes);
        activeSessions.put(uuid, session);
        
        // Set up player for build mode
        setupBuildMode(player);
        
        // Notify player
        player.sendMessage("§aBuild mode activated for " + durationMinutes + " minutes.");
        
        return true;
    }
    
    /**
     * Ends a build mode session for a player.
     * 
     * @param player The player
     * @return True if the session was ended, false otherwise
     */
    public boolean endSession(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Check if player has an active session
        if (!activeSessions.containsKey(uuid)) {
            player.sendMessage("§cYou don't have an active build mode session.");
            return false;
        }
        
        // Get session
        BuildSession session = activeSessions.get(uuid);
        
        // Restore player's state
        restorePlayerState(player, session);
        
        // Remove session
        activeSessions.remove(uuid);
        lastSessionEndTime.put(uuid, System.currentTimeMillis());
        
        // Notify player
        player.sendMessage("§aBuild mode deactivated.");
        
        return true;
    }
    
    /**
     * Sets up a player for build mode.
     * 
     * @param player The player
     */
    private void setupBuildMode(Player player) {
        // Clear inventory
        player.getInventory().clear();
        
        // Set game mode to creative
        player.setGameMode(GameMode.CREATIVE);
        
        // Give wooden axe (potentially for WorldEdit)
        ItemStack woodenAxe = new ItemStack(Material.WOODEN_AXE);
        player.getInventory().addItem(woodenAxe);
    }
    
    /**
     * Restores a player's state after build mode.
     * 
     * @param player The player
     * @param session The build session
     */
    private void restorePlayerState(Player player, BuildSession session) {
        // Clear inventory
        player.getInventory().clear();
        
        // Restore inventory
        player.getInventory().setStorageContents(session.getSavedInventory());
        player.getInventory().setArmorContents(session.getSavedArmor());
        player.getInventory().setItemInOffHand(session.getSavedOffhand());
        
        // Restore game mode
        GameMode previousMode = session.getPreviousGameMode();
        if (previousMode != GameMode.CREATIVE) {
            player.setGameMode(previousMode);
        } else {
            player.setGameMode(GameMode.SURVIVAL);
        }
    }
    
    /**
     * Checks if a player is in build mode.
     * 
     * @param uuid The player's UUID
     * @return True if the player is in build mode, false otherwise
     */
    public boolean isInBuildMode(UUID uuid) {
        return activeSessions.containsKey(uuid);
    }
    
    /**
     * Gets a player's build session.
     * 
     * @param uuid The player's UUID
     * @return The build session, or null if the player is not in build mode
     */
    public BuildSession getSession(UUID uuid) {
        return activeSessions.get(uuid);
    }
    
    /**
     * Checks if a player is on cooldown.
     * 
     * @param uuid The player's UUID
     * @return True if the player is on cooldown, false otherwise
     */
    public boolean isOnCooldown(UUID uuid) {
        if (!lastSessionEndTime.containsKey(uuid)) {
            return false;
        }
        
        long lastEnd = lastSessionEndTime.get(uuid);
        long cooldownMillis = plugin.getConfigManager().getCooldownMinutes() * 60 * 1000;
        
        return System.currentTimeMillis() - lastEnd < cooldownMillis;
    }
    
    /**
     * Saves all active sessions to file.
     */
    public void saveAllSessions() {
        YamlConfiguration config = new YamlConfiguration();
        
        // Save active sessions
        for (Map.Entry<UUID, BuildSession> entry : activeSessions.entrySet()) {
            UUID uuid = entry.getKey();
            BuildSession session = entry.getValue();
            
            String path = "sessions." + uuid.toString();
            config.set(path + ".startTime", session.getStartTime());
            config.set(path + ".endTime", session.getEndTime());
            config.set(path + ".previousGameMode", session.getPreviousGameMode().toString());
            
            // Save inventory (this is simplified, actual implementation would need to serialize ItemStacks)
            // For a real plugin, you would need to use ConfigurationSerializable or a custom serialization method
            // This is just a placeholder
            config.set(path + ".inventory", session.getSavedInventory());
            config.set(path + ".armor", session.getSavedArmor());
            config.set(path + ".offhand", session.getSavedOffhand());
        }
        
        // Save last session end times
        for (Map.Entry<UUID, Long> entry : lastSessionEndTime.entrySet()) {
            UUID uuid = entry.getKey();
            Long time = entry.getValue();
            
            config.set("lastSessionEnd." + uuid.toString(), time);
        }
        
        // Save to file
        plugin.getConfigManager().saveSessionsConfig(config);
    }
    
    /**
     * Loads saved sessions from file.
     */
    private void loadSessions() {
        YamlConfiguration config = plugin.getConfigManager().loadSessionsConfig();
        
        // Load active sessions
        if (config.contains("sessions")) {
            for (String uuidString : config.getConfigurationSection("sessions").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                String path = "sessions." + uuidString;
                
                long startTime = config.getLong(path + ".startTime");
                long endTime = config.getLong(path + ".endTime");
                GameMode previousGameMode = GameMode.valueOf(config.getString(path + ".previousGameMode"));
                
                // Load inventory (this is simplified, actual implementation would need to deserialize ItemStacks)
                // For a real plugin, you would need to use ConfigurationSerializable or a custom deserialization method
                // This is just a placeholder
                ItemStack[] savedInventory = (ItemStack[]) config.get(path + ".inventory");
                ItemStack[] savedArmor = (ItemStack[]) config.get(path + ".armor");
                ItemStack savedOffhand = (ItemStack) config.get(path + ".offhand");
                
                // Create session
                BuildSession session = new BuildSession(uuid, startTime, endTime, savedInventory, savedArmor, savedOffhand, previousGameMode);
                
                // Only add session if it hasn't expired
                if (!session.hasExpired()) {
                    activeSessions.put(uuid, session);
                }
            }
        }
        
        // Load last session end times
        if (config.contains("lastSessionEnd")) {
            for (String uuidString : config.getConfigurationSection("lastSessionEnd").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidString);
                long time = config.getLong("lastSessionEnd." + uuidString);
                
                lastSessionEndTime.put(uuid, time);
            }
        }
    }
    
    /**
     * Gets all active sessions.
     * 
     * @return The active sessions
     */
    public Map<UUID, BuildSession> getActiveSessions() {
        return new HashMap<>(activeSessions);
    }
    
    /**
     * Reloads the session manager.
     */
    public void reload() {
        // Restart check task
        startCheckTask();
    }
}
