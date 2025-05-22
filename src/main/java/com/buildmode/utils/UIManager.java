package com.buildmode.utils;

import com.buildmode.BuildMode;
import com.buildmode.models.BuildSession;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages UI elements for the BuildMode plugin.
 */
public class UIManager {
    
    private final BuildMode plugin;
    private final Map<UUID, BossBar> bossBars;
    private BukkitTask updateTask;
    
    /**
     * Creates a new UI manager.
     * 
     * @param plugin The plugin instance
     */
    public UIManager(BuildMode plugin) {
        this.plugin = plugin;
        this.bossBars = new HashMap<>();
        startUpdateTask();
    }
    
    /**
     * Starts the update task for UI elements.
     */
    private void startUpdateTask() {
        // Cancel existing task if it exists
        if (updateTask != null) {
            updateTask.cancel();
        }
        
        // Start new task that runs every second
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateUI, 20L, 20L);
    }
    
    /**
     * Updates all UI elements.
     */
    private void updateUI() {
        Map<UUID, BuildSession> sessions = plugin.getSessionManager().getActiveSessions();
        
        // Update boss bars for active sessions
        for (Map.Entry<UUID, BuildSession> entry : sessions.entrySet()) {
            UUID uuid = entry.getKey();
            BuildSession session = entry.getValue();
            Player player = Bukkit.getPlayer(uuid);
            
            if (player != null && player.isOnline()) {
                updateBossBar(player, session);
            }
        }
        
        // Remove boss bars for inactive sessions
        for (UUID uuid : bossBars.keySet().toArray(new UUID[0])) {
            if (!sessions.containsKey(uuid)) {
                removeBossBar(uuid);
            }
        }
    }
    
    /**
     * Updates the boss bar for a player.
     * 
     * @param player The player
     * @param session The build session
     */
    private void updateBossBar(Player player, BuildSession session) {
        // Check if boss bars are enabled
        if (!plugin.getConfigManager().isBossBarEnabled()) {
            removeBossBar(player.getUniqueId());
            return;
        }
        
        UUID uuid = player.getUniqueId();
        BossBar bossBar = bossBars.get(uuid);
        
        // Create boss bar if it doesn't exist
        if (bossBar == null) {
            bossBar = Bukkit.createBossBar(
                    "Build Mode",
                    BarColor.GREEN,
                    BarStyle.SOLID
            );
            bossBar.addPlayer(player);
            bossBars.put(uuid, bossBar);
        }
        
        // Update boss bar
        int remainingSeconds = session.getRemainingSeconds();
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        
        // Format time as mm:ss
        String timeString = String.format("%02d:%02d", minutes, seconds);
        
        // Update title
        bossBar.setTitle(Component.translatable("Build Mode: %s", Component.text(timeString)).color(NamedTextColor.GOLD).toString());
        
        // Update progress
        long totalDuration = session.getEndTime() - session.getStartTime();
        long remaining = session.getRemainingTime();
        double progress = (double) remaining / totalDuration;
        bossBar.setProgress(Math.max(0, Math.min(1, progress)));
        
        // Update color based on remaining time
        if (remainingSeconds <= 60) {
            bossBar.setColor(BarColor.RED);
        } else if (remainingSeconds <= 300) {
            bossBar.setColor(BarColor.YELLOW);
        } else {
            bossBar.setColor(BarColor.GREEN);
        }
    }
    
    /**
     * Removes the boss bar for a player.
     * 
     * @param uuid The player's UUID
     */
    private void removeBossBar(UUID uuid) {
        BossBar bossBar = bossBars.remove(uuid);
        
        if (bossBar != null) {
            bossBar.removeAll();
        }
    }
    
    /**
     * Reloads the UI manager.
     */
    public void reload() {
        // Restart update task
        startUpdateTask();
    }
    
    /**
     * Cleans up UI elements.
     */
    public void cleanup() {
        // Cancel update task
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }
        
        // Remove all boss bars
        for (BossBar bossBar : bossBars.values()) {
            bossBar.removeAll();
        }
        
        bossBars.clear();
    }
}
