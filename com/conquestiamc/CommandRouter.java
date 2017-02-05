package com.conquestiamc;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class CommandRouter
        extends JavaPlugin
        implements Listener
{
    private HashMap<String, String> redirects = new HashMap();
    private HashMap<String, String> replacements = new HashMap<>();


    public void onDisable()
    {
        Logger log = Logger.getLogger("Minecraft");
        log.info("[CommandRouter] is disabled!");
    }

    public void onEnable()
    {
        this.getCommand("cqcr").setExecutor(new ReloadCommand());
        getServer().getPluginManager().registerEvents(this, this);
        File file = new File(getDataFolder(), "config.yml");
        if (!file.exists()) {
            saveDefaultConfig();
        }
        getCommandRedirects();
        Logger log = Logger.getLogger("Minecraft");
        log.info("[CommandRouter] is enabled!");
    }

    private void getCommandRedirects()
    {
        FileConfiguration config = getConfig();
        if (config.getConfigurationSection("commands") != null) {
            //Iterate through the commands in the config
            for (String command : config.getConfigurationSection("commands").getKeys(false)) {
                //Put the commmand in the hashmap as the Key with the commands it redirects to as the value
                this.redirects.put(command, config.getString("commands." + command));
            }
        }

        if (config.getConfigurationSection("replacements") != null) {
            //Iterate through the commands in the replacements section
            for (String command : config.getConfigurationSection("replacements").getKeys(false)) {
                //put the command in the hashmap as the key with the command it is replaced by as the value
                this.replacements.put(command, config.getString("replacements." + command));
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onCommandPreProcess(PlayerCommandPreprocessEvent event)
    {
        String command = event.getMessage().substring(1); //Remove the "/" before the command
        Player player = event.getPlayer();
        String[] brokenCommand = command.split(" ");

        if (this.redirects.containsKey(command)) {
            player.performCommand(redirects.get(command));
            getLogger().info(player.getName() + ": /" + redirects.get(command));
            event.setCancelled(true);
        } else if (brokenCommand[0] != null && this.replacements.containsKey(brokenCommand[0])) {
            String newCommand = this.replacements.get(brokenCommand[0]);
            for (int i = 1; i < brokenCommand.length; i++) {
                newCommand = newCommand + " " + brokenCommand[i];
            }
            player.performCommand(newCommand);
            getLogger().info(player.getName() + ": /" + newCommand);
            event.setCancelled(true);
        }

    }

    public class ReloadCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
            if (args.length != 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                getCommandRedirects();
                getLogger().info("[CommandRouter] reloaded configs");

                if (commandSender instanceof Player) {
                    commandSender.sendMessage(ChatColor.AQUA + "[CommandRouter]" + ChatColor.GRAY + " reloaded configs");
                }
                return true;
            }
            return false;
        }
    }
}
