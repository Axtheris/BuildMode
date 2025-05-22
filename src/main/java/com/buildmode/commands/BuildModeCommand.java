package com.buildmode.commands;

import com.buildmode.BuildMode;
import com.buildmode.models.BuildSession;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Command handler for the BuildMode plugin.
 */
public class BuildModeCommand implements CommandExecutor, TabCompleter {
    
    private final BuildMode plugin;
    
    /**
     * Creates a new command handler.
     * 
     * @param plugin The plugin instance
     */
    public BuildModeCommand(BuildMode plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "start":
                return handleStart(sender);
            case "end":
                return handleEnd(sender);
            case "reload":
                return handleReload(sender);
            case "list":
                return handleList(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    /**
     * Handles the start subcommand.
     * 
     * @param sender The command sender
     * @return True if the command was handled, false otherwise
     */
    private boolean handleStart(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Start build mode session
        plugin.getSessionManager().startSession(player);
        
        return true;
    }
    
    /**
     * Handles the end subcommand.
     * 
     * @param sender The command sender
     * @return True if the command was handled, false otherwise
     */
    private boolean handleEnd(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        // End build mode session
        plugin.getSessionManager().endSession(player);
        
        return true;
    }
    
    /**
     * Handles the reload subcommand.
     * 
     * @param sender The command sender
     * @return True if the command was handled, false otherwise
     */
    private boolean handleReload(CommandSender sender) {
        if (!sender.hasPermission("buildmode.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        // Reload plugin
        plugin.reload();
        
        sender.sendMessage(ChatColor.GREEN + "BuildMode configuration reloaded.");
        
        return true;
    }
    
    /**
     * Handles the list subcommand.
     * 
     * @param sender The command sender
     * @return True if the command was handled, false otherwise
     */
    private boolean handleList(CommandSender sender) {
        if (!sender.hasPermission("buildmode.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }
        
        Map<UUID, BuildSession> sessions = plugin.getSessionManager().getActiveSessions();
        
        if (sessions.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "There are no active build mode sessions.");
            return true;
        }
        
        sender.sendMessage(ChatColor.GREEN + "Active build mode sessions:");
        
        for (Map.Entry<UUID, BuildSession> entry : sessions.entrySet()) {
            UUID uuid = entry.getKey();
            BuildSession session = entry.getValue();
            
            String playerName = Bukkit.getOfflinePlayer(uuid).getName();
            int remainingSeconds = session.getRemainingSeconds();
            int minutes = remainingSeconds / 60;
            int seconds = remainingSeconds % 60;
            
            sender.sendMessage(ChatColor.YELLOW + playerName + ": " + 
                    ChatColor.WHITE + String.format("%02d:%02d", minutes, seconds) + " remaining");
        }
        
        return true;
    }
    
    /**
     * Sends the help message to a command sender.
     * 
     * @param sender The command sender
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GREEN + "BuildMode Commands:");
        sender.sendMessage(ChatColor.YELLOW + "/buildmode start" + ChatColor.WHITE + " - Start a build mode session");
        sender.sendMessage(ChatColor.YELLOW + "/buildmode end" + ChatColor.WHITE + " - End your build mode session");
        
        if (sender.hasPermission("buildmode.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/buildmode reload" + ChatColor.WHITE + " - Reload the plugin configuration");
            sender.sendMessage(ChatColor.YELLOW + "/buildmode list" + ChatColor.WHITE + " - List active build mode sessions");
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("start", "end"));
            
            if (sender.hasPermission("buildmode.admin")) {
                completions.add("reload");
                completions.add("list");
            }
            
            return completions;
        }
        
        return new ArrayList<>();
    }
}
