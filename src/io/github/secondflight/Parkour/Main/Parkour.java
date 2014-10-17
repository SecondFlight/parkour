package io.github.secondflight.Parkour.Main;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Parkour extends JavaPlugin{
	public final Logger logger = Logger.getLogger("Minecraft");
	public Parkour plugin;
		
	public void onEnable() {
		plugin = this;
			
			
			
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " has been Enabled.");
			
			
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
		
	public void onDisable() {
			
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if (command.getName().equalsIgnoreCase("parkour") && args.length == 2 && args[0].equalsIgnoreCase("new")) {
				if ((!(getConfig().get("locations") == null) && !(getConfig().getConfigurationSection("locations").getKeys(false).contains(args[1]))) || getConfig().get("locations") == null) {
					getConfig().set("locations." + args[1] + ".x", player.getLocation().getBlockX());
					getConfig().set("locations." + args[1] + ".y", player.getLocation().getBlockY());
					getConfig().set("locations." + args[1] + ".z", player.getLocation().getBlockZ());
				} else {
					player.sendMessage(ChatColor.RED + "There is already a parkour end sign with that name!"); 
					player.sendMessage("");
					player.sendMessage(" If you would like to make a new course with this name, please remove the old one first by using " + ChatColor.RED + "/parkour remove [name] " + ChatColor.WHITE  + "or" + ChatColor.RED + " /parkour remove [index]" + ChatColor.WHITE + ". Note: You can also edit an existing parkour course by using " + ChatColor.RED + "/parkour edit [name] " + ChatColor.WHITE  + "or" + ChatColor.RED + " /parkour edit [index]" + ChatColor.WHITE + ".");
				}
				player.getWorld().getBlockAt(getConfig().getInt("locations." + args[1] + ".x"), getConfig().getInt("locations." + args[1] + ".y"), getConfig().getInt("locations." + args[1] + ".z")).setType(Material.BEDROCK);;
			}
			
			if (command.getName().equalsIgnoreCase("parkour") && args.length == 1 && args[0].equalsIgnoreCase("list")) {
				if (!(getConfig().get("locations") == null)) {
					if (getConfig().getConfigurationSection("locations").getKeys(false).size() > 0) {
						player.sendMessage("Here are the active parkour end signs:");
						player.sendMessage("");
					
						int i = 1;
					
						for (String s : getConfig().getConfigurationSection("locations").getKeys(false)) {
							player.sendMessage("Index: " + Integer.toString(i));
							player.sendMessage("Name: " + s);
							player.sendMessage("X: " + getConfig().get("locations." + s + ".x"));
							player.sendMessage("Y: " + getConfig().get("locations." + s + ".y"));
							player.sendMessage("Z: " + getConfig().get("locations." + s + ".z"));
							
							player.sendMessage("");
						}
					}
				} else {
					player.sendMessage("There are no parkour end points in the config right now.");
					player.sendMessage("Use " + ChatColor.RED + "/parkour new" + ChatColor.WHITE + " to add one.");
				}
			}
			
			if (command.getName().equalsIgnoreCase("test")) {
				// test goes here lel
			}
		}
		
		return false;
	}
}

