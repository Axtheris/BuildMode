package com.buildmode.listeners;

import com.buildmode.BuildMode;
import com.buildmode.models.BuildSession;
import org.bukkit.GameMode;
import org.bukkit.Material;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

/**
 * Listener for BuildMode events.
 */
public class BuildModeListener implements Listener {
    
    private final BuildMode plugin;
    
    /**
     * Creates a new listener.
     * 
     * @param plugin The plugin instance
     */
    public BuildModeListener(BuildMode plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Checks if an item is illegal for build mode.
     * 
     * @param item The item to check
     * @return True if the item is illegal, false otherwise
     */
    private boolean isIllegal(ItemStack item) {
        if (item == null) {
            return false;
        }
        
        Material material = item.getType();
        
        // Check if material is allowed based on config
        boolean allowed = plugin.getConfigManager().isMaterialAllowed(material);
        
        // If allowed, check for NBT data that might indicate a special item
        if (allowed) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // Check for BlockEntityTag, Enchantments, Potion NBT
                // This is a simplified check, in a real plugin you would need to use NMS or reflection
                // to check for these NBT tags
                if (meta.hasEnchants() || meta.hasDisplayName() || meta.hasLore()) {
                    return true;
                }
            }
        }
        
        return !allowed;
    }
    
    /**
     * Sends an illegal item message to a player.
     * 
     * @param player The player
     */
    private void sendIllegalItemMessage(Player player) {
        player.sendActionBar(Component.text("✗ Not a Build-Mode item", NamedTextColor.RED));
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        
        // Check if player has an active session
        if (plugin.getSessionManager().isInBuildMode(uuid)) {
            BuildSession session = plugin.getSessionManager().getSession(uuid);
            
            // Check if session has expired
            if (session.hasExpired()) {
                // End session
                plugin.getSessionManager().endSession(player);
                player.sendMessage(Component.text("Your build mode session expired while you were offline.", NamedTextColor.RED));
            } else {
                // Set up player for build mode again
                player.setGameMode(GameMode.CREATIVE);
                player.sendMessage(Component.text("You are in build mode.", NamedTextColor.GREEN));
            }
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Session will be saved by SessionManager when the plugin is disabled
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getSessionManager().isInBuildMode(player.getUniqueId())) {
            return;
        }
        
        // Check if the player is interacting with a block using an item
        if (event.hasItem() && isIllegal(event.getItem())) {
            event.setCancelled(true);
            sendIllegalItemMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getSessionManager().isInBuildMode(player.getUniqueId())) {
            return;
        }
        
        // Check if the player is placing an illegal block
        if (isIllegal(event.getItemInHand())) {
            event.setCancelled(true);
            sendIllegalItemMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryCreative(InventoryCreativeEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (!plugin.getSessionManager().isInBuildMode(player.getUniqueId())) {
            return;
        }
        
        // Check if the player is trying to get an illegal item
        if (isIllegal(event.getCursor())) {
            event.setCancelled(true);
            sendIllegalItemMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (!plugin.getSessionManager().isInBuildMode(player.getUniqueId())) {
            return;
        }
        
        // Check if the player is trying to move an illegal item
        if (event.getCurrentItem() != null && isIllegal(event.getCurrentItem())) {
            // Allow if the player is in their own inventory and not transferring to a container
            if (event.getClickedInventory() != null && 
                event.getClickedInventory().getType() != InventoryType.PLAYER &&
                event.getClickedInventory().getType() != InventoryType.CREATIVE) {
                event.setCancelled(true);
                sendIllegalItemMessage(player);
                return;
            }
            
            // Check for shift-click into container
            if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
                if (event.getClickedInventory() != null && 
                    event.getClickedInventory().getType() == InventoryType.PLAYER &&
                    event.getView().getTopInventory().getType() != InventoryType.CREATIVE) {
                    event.setCancelled(true);
                    sendIllegalItemMessage(player);
                    return;
                }
            }
        }
        
        // Prevent interaction with storage containers
        if (event.getClickedInventory() != null) {
            InventoryType type = event.getClickedInventory().getType();
            if (type == InventoryType.CHEST || 
                type == InventoryType.BARREL || 
                type == InventoryType.HOPPER || 
                type == InventoryType.SHULKER_BOX) {
                event.setCancelled(true);
                player.sendActionBar(Component.text("✗ Cannot interact with containers in Build Mode", NamedTextColor.RED));
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        if (!plugin.getSessionManager().isInBuildMode(player.getUniqueId())) {
            return;
        }
        
        // Check if the player is trying to drag an illegal item
        if (isIllegal(event.getOldCursor())) {
            // Check if any of the slots are in a container inventory
            boolean inContainer = false;
            for (int slot : event.getRawSlots()) {
                if (slot < event.getView().getTopInventory().getSize() && 
                    event.getView().getTopInventory().getType() != InventoryType.CREATIVE) {
                    inContainer = true;
                    break;
                }
            }
            
            if (inContainer) {
                event.setCancelled(true);
                sendIllegalItemMessage(player);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        // This event is fired for hoppers and other automated item movement
        // We need to check if the source or destination inventory belongs to a player in build mode
        
        // This is a simplified check, in a real plugin you would need to check if the inventory
        // belongs to a player in build mode
        if (event.getSource().getHolder() instanceof Player) {
            Player player = (Player) event.getSource().getHolder();
            
            if (plugin.getSessionManager().isInBuildMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
        
        if (event.getDestination().getHolder() instanceof Player) {
            Player player = (Player) event.getDestination().getHolder();
            
            if (plugin.getSessionManager().isInBuildMode(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        
        if (!plugin.getSessionManager().isInBuildMode(player.getUniqueId())) {
            return;
        }
        
        // Prevent dropping items in build mode
        event.setCancelled(true);
        player.sendActionBar(Component.text("✗ Cannot drop items in Build Mode", NamedTextColor.RED));
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (!plugin.getSessionManager().isInBuildMode(player.getUniqueId())) {
            return;
        }
        
        // Check if the item is illegal
        if (isIllegal(event.getItem().getItemStack())) {
            event.setCancelled(true);
            sendIllegalItemMessage(player);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        if (!plugin.getSessionManager().isInBuildMode(player.getUniqueId())) {
            return;
        }
        
        // Prevent opening storage containers
        InventoryType type = event.getInventory().getType();
        if (type == InventoryType.CHEST || 
            type == InventoryType.BARREL || 
            type == InventoryType.HOPPER || 
            type == InventoryType.SHULKER_BOX) {
            event.setCancelled(true);
            player.sendActionBar(Component.text("✗ Cannot open containers in Build Mode", NamedTextColor.RED));
        }
    }
}
