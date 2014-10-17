package io.github.secondflight.Parkour.Main;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

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
			if (command.getName().equalsIgnoreCase("parkour") && args.length == 3 && args[0].equalsIgnoreCase("new")) {
				boolean error = false;
				
				try {
					int i = Integer.parseInt(args[2]);
				} catch (Exception ex) {
					error = true;
				}
				if (!error) {
					if ((!(getConfig().get("locations") == null) && !(getConfig().getConfigurationSection("locations").getKeys(false).contains(args[1]))) || getConfig().get("locations") == null) {
						getConfig().set("locations." + args[1] + ".x", player.getLocation().getBlockX());
						getConfig().set("locations." + args[1] + ".y", player.getLocation().getBlockY());
						getConfig().set("locations." + args[1] + ".z", player.getLocation().getBlockZ());
						getConfig().set("locations." + args[1] + ".world", player.getWorld().getName());
						getConfig().set("locations." + args[1] + ".points", Integer.parseInt(args[2]));
						
						//saveConfig();
						
						player.sendMessage("A new end point has been created at your location. Don't forget to place down a block there, such as a sign, or the end point will be inaccessable.");
					} else {
						player.sendMessage(ChatColor.RED + "There is already a parkour end sign with that name!"); 
						player.sendMessage("");
						player.sendMessage(" If you would like to make a new course with this name, please remove the old one first by using " + ChatColor.RED + "/parkour remove [name] " + ChatColor.WHITE  + "or" + ChatColor.RED + " /parkour remove [index]" + ChatColor.WHITE + ". Note: You can also edit an existing parkour course by using " + ChatColor.RED + "/parkour edit [name] " + ChatColor.WHITE  + "or" + ChatColor.RED + " /parkour edit [index]" + ChatColor.WHITE + ".");
					}
				} else {
					player.sendMessage(ChatColor.RED + "Invalid arguments - [points] must be a number.");
					player.sendMessage("");
					player.sendMessage(ChatColor.RED + "Usage: /parkour new [name] [points]");
					player.sendMessage("See " + ChatColor.RED + "/parkour help" + ChatColor.WHITE + " for more info.");
				}
				
			} else if (command.getName().equalsIgnoreCase("parkour") && args[0].equalsIgnoreCase("new") && !(args.length == 3)) {
				player.sendMessage(ChatColor.RED + "Usage: /parkour new [name] [points]");
				player.sendMessage("See " + ChatColor.RED + "/parkour help" + ChatColor.WHITE + " for more info.");
			}
			
			
			if (command.getName().equalsIgnoreCase("parkour") && args.length == 1 && args[0].equalsIgnoreCase("list")) {
				if (!(getConfig().get("locations") == null)) {
					if (getConfig().getConfigurationSection("locations").getKeys(false).size() > 0) {
						player.sendMessage("Here are the active parkour end points:");
						player.sendMessage("");
					
						for (String s : getConfig().getConfigurationSection("locations").getKeys(false)) {
							player.sendMessage("Name: " + s);
							player.sendMessage("X: " + getConfig().get("locations." + s + ".x"));
							player.sendMessage("Y: " + getConfig().get("locations." + s + ".y"));
							player.sendMessage("Z: " + getConfig().get("locations." + s + ".z"));
							player.sendMessage("World: " + getConfig().get("locations." + s + ".world"));
							player.sendMessage("Point value: " + getConfig().get("locations." + s + ".points"));
							
							if (Bukkit.getServer().getWorld(getConfig().getString("locations." + s + ".world")).getBlockAt(getConfig().getInt("locations." + s + ".x"), getConfig().getInt("locations." + s + ".y"), getConfig().getInt("locations." + s + ".z")).getType() == Material.AIR) {
								player.sendMessage(ChatColor.RED + "WARNING: There is no block at this location, making it inaccessable."); 
								player.sendMessage("This can be fixed by placing a block, such as a sign, at the location. You can teleport to this location by using " + ChatColor.RED + "/parkour tp " + s + ChatColor.WHITE + ".");
							}
							
							player.sendMessage("");
						}
					}else {
						player.sendMessage("There are no parkour end points in the config right now.");
						player.sendMessage("See " + ChatColor.RED + "/parkour new" + ChatColor.WHITE + " to add one.");
					}
				} else {
					player.sendMessage("There are no parkour end points in the config right now.");
					player.sendMessage("See " + ChatColor.RED + "/parkour new" + ChatColor.WHITE + " to add one.");
				}
			} else if (command.getName().equalsIgnoreCase("parkour") && args[0].equalsIgnoreCase("list") && !(args.length == 1)) {
				player.sendMessage(ChatColor.RED + "Usage: /parkour list");
				player.sendMessage("Use " + ChatColor.RED + "/parkour help" + ChatColor.WHITE + " for more info.");
			}
			
			if (command.getName().equalsIgnoreCase("parkour") && args.length == 2 && args[0].equalsIgnoreCase("tp")) {
				if (!(getConfig().get("locations") == null)) {
					if (getConfig().getConfigurationSection("locations").getKeys(false).size() > 0) {
						boolean error = true;
						
						for (String s : getConfig().getConfigurationSection("locations").getKeys(false)) {
							if (s.equals(args[1])) {
								error = false;
								break;
							}
						}
						
						if (!error) {
							player.teleport (new Location(Bukkit.getServer().getWorld(getConfig().getString("locations." + args[1] + ".world")), (double) getConfig().getInt("locations." + args[1] + ".x") + 0.5, (double) getConfig().getInt("locations." + args[1] + ".y"), (double) getConfig().getInt("locations." + args[1] + ".z") + 0.5));
						} else {
							player.sendMessage(ChatColor.RED + "Invalid course name.");
							player.sendMessage("You can use " + ChatColor.RED + "/parkour list" + ChatColor.WHITE + " to get all the active courses and their names.");
						}
					} else {
						player.sendMessage(ChatColor.RED + "There are no active parkour courses to teleport to.");
						player.sendMessage("You can use " + ChatColor.RED + "/parkour new [name] [point value]" + ChatColor.WHITE + " to make a new one.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "There are no active parkour courses to teleport to.");
					player.sendMessage("You can use " + ChatColor.RED + "/parkour new [name] [point value]" + ChatColor.WHITE + " to make a new one.");
				}
			} else if (command.getName().equalsIgnoreCase("parkour") && args[0].equalsIgnoreCase("tp") && !(args.length == 2)) {
				player.sendMessage(ChatColor.RED + "Usage: /parkour tp [course name]");
				player.sendMessage("You can use " + ChatColor.RED + "/parkour list" + ChatColor.WHITE + " to get all the active courses and their names.");
				player.sendMessage("Use " + ChatColor.RED + "/parkour help" + ChatColor.WHITE + " for more info.");
			}
			
			if (command.getName().equalsIgnoreCase("parkour") && args.length == 2 && args[0].equalsIgnoreCase("remove")) {
				if (!(getConfig().get("locations") == null)) {
					if (getConfig().getConfigurationSection("locations").getKeys(false).size() > 0) {
						boolean error = true;
						
						for (String s : getConfig().getConfigurationSection("locations").getKeys(false)) {
							if (s.equals(args[1])) {
								error = false;
								break;
							}
						}
						
						if (!error) {
							getConfig().getConfigurationSection("locations").set(args[1], null);
							//saveConfig();
							
							player.sendMessage("Course '" + args[1] + "' has successfully been removed.");
						} else {
							player.sendMessage(ChatColor.RED + "Invalid course name.");
							player.sendMessage("You can use " + ChatColor.RED + "/parkour list" + ChatColor.WHITE + " to get all the active courses and their names.");
						}
					} else {
						player.sendMessage(ChatColor.RED + "There are no active parkour courses to remove.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "There are no active parkour courses to remove.");
				}
			} else if (command.getName().equalsIgnoreCase("parkour") && args[0].equalsIgnoreCase("remove") && !(args.length == 2)) {
				player.sendMessage(ChatColor.RED + "Usage: /parkour remove [course name]");
				player.sendMessage("Use " + ChatColor.RED + "/parkour help" + ChatColor.WHITE + " for more info.");
			}
			
			if (command.getName().equalsIgnoreCase("test")) {
				// test goes here lel
			}
		}
		
		return false;
	}
}