package com.buildmode.models;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Represents a player's build mode session.
 */
public class BuildSession {
    
    private final UUID playerUUID;
    private final long startTime;
    private long endTime;
    private ItemStack[] savedInventory;
    private ItemStack[] savedArmor;
    private ItemStack savedOffhand;
    private GameMode previousGameMode;
    
    /**
     * Creates a new build session for a player.
     * 
     * @param player The player
     * @param durationMinutes The duration of the session in minutes
     */
    public BuildSession(Player player, int durationMinutes) {
        this.playerUUID = player.getUniqueId();
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + (durationMinutes * 60 * 1000);
        this.previousGameMode = player.getGameMode();
        
        // Save player's inventory
        saveInventory(player);
    }
    
    /**
     * Creates a build session from saved data.
     * 
     * @param playerUUID The player's UUID
     * @param startTime The start time in milliseconds
     * @param endTime The end time in milliseconds
     * @param savedInventory The saved inventory
     * @param savedArmor The saved armor
     * @param savedOffhand The saved offhand item
     * @param previousGameMode The previous game mode
     */
    public BuildSession(UUID playerUUID, long startTime, long endTime, 
                        ItemStack[] savedInventory, ItemStack[] savedArmor, 
                        ItemStack savedOffhand, GameMode previousGameMode) {
        this.playerUUID = playerUUID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.savedInventory = savedInventory;
        this.savedArmor = savedArmor;
        this.savedOffhand = savedOffhand;
        this.previousGameMode = previousGameMode;
    }
    
    /**
     * Saves the player's inventory.
     * 
     * @param player The player
     */
    private void saveInventory(Player player) {
        this.savedInventory = player.getInventory().getStorageContents().clone();
        this.savedArmor = player.getInventory().getArmorContents().clone();
        this.savedOffhand = player.getInventory().getItemInOffHand().clone();
    }
    
    /**
     * Gets the player's UUID.
     * 
     * @return The player's UUID
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    
    /**
     * Gets the start time of the session.
     * 
     * @return The start time in milliseconds
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Gets the end time of the session.
     * 
     * @return The end time in milliseconds
     */
    public long getEndTime() {
        return endTime;
    }
    
    /**
     * Sets the end time of the session.
     * 
     * @param endTime The end time in milliseconds
     */
    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
    
    /**
     * Gets the saved inventory.
     * 
     * @return The saved inventory
     */
    public ItemStack[] getSavedInventory() {
        return savedInventory;
    }
    
    /**
     * Gets the saved armor.
     * 
     * @return The saved armor
     */
    public ItemStack[] getSavedArmor() {
        return savedArmor;
    }
    
    /**
     * Gets the saved offhand item.
     * 
     * @return The saved offhand item
     */
    public ItemStack getSavedOffhand() {
        return savedOffhand;
    }
    
    /**
     * Gets the previous game mode.
     * 
     * @return The previous game mode
     */
    public GameMode getPreviousGameMode() {
        return previousGameMode;
    }
    
    /**
     * Checks if the session has expired.
     * 
     * @return True if the session has expired, false otherwise
     */
    public boolean hasExpired() {
        return System.currentTimeMillis() >= endTime;
    }
    
    /**
     * Gets the remaining time of the session.
     * 
     * @return The remaining time in milliseconds
     */
    public long getRemainingTime() {
        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    /**
     * Gets the remaining time of the session in seconds.
     * 
     * @return The remaining time in seconds
     */
    public int getRemainingSeconds() {
        return (int) (getRemainingTime() / 1000);
    }
}
