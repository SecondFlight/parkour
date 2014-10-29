package io.github.secondflight.Parkour.Main;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Parkour extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public Parkour plugin;
		
	public static Map<String, Course> courses = new HashMap<String, Course>();
	public static Map<Player, Integer> startTime = new HashMap<Player, Integer>();
	
	public void onEnable() {
		
		plugin = this;	
			
		PluginDescriptionFile pdfFile = this.getDescription();
		this.logger.info(pdfFile.getName() + " has been Enabled.");
		
		getServer().getPluginManager().registerEvents(this, this);
			
		getConfig().options().copyDefaults(true);
		saveConfig();
		
		configToMap();
	}
		
	public void onDisable() {
			
	}
	
	/*
	@EventHandler
	public void joinEvent (PlayerJoinEvent event) {
		if (!(getConfig().get("highscores." + event.getPlayer().getUniqueId()) == null)) {
			getConfig().set("highscores." + event.getPlayer().getUniqueId(), "hi");
		}
	}
	*/
	
	@EventHandler
	public void clickEvent (PlayerInteractEvent event) {
		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			Block b = event.getClickedBlock();
			Player p = event.getPlayer();
			for (String s : courses.keySet()) {
				Course course = courses.get(s);
				
				if (!(course.start == null)) {
					if (b.equals(course.start)) {
						storeTime(p);
						p.sendMessage(ChatColor.GREEN + "Go!");
					}
				}
				
				if (!(course.end == null)) {
					if (b.equals(course.end)) {
						if (startTime.containsKey(p)) {
							Date d = new Date();
							int total = (int) d.getTime() - startTime.get(p);
							
							p.sendMessage(ChatColor.GREEN + "Your time was " + msToString(total, ChatColor.GREEN));
							
							if (getConfig().get("highscores." + event.getPlayer().getUniqueId() + "." + course.name) != null) {
								if (getConfig().getInt("highscores." + event.getPlayer().getUniqueId() + "." + course.name) >= total) {
									p.sendMessage(ChatColor.GREEN + "You beat your previous best of " + msToString(getConfig().getInt("highscores." + event.getPlayer().getUniqueId() + "." + course.name), ChatColor.GREEN) + ChatColor.GREEN + "!");
									getConfig().set("highscores." + event.getPlayer().getUniqueId() + "." + course.name, total);
								} else {
									p.sendMessage(ChatColor.RED + "You did not beat your previous best. Your best time on this course is " + msToString(getConfig().getInt("highscores." + event.getPlayer().getUniqueId() + "." + course.name), ChatColor.RED) + ChatColor.RED + ".");
								}
							} else {
								getConfig().set("highscores." + event.getPlayer().getUniqueId() + "." + course.name, total);
							}
							
							clearTime(p);
						}
					}
				}
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(sender instanceof Player) {
			Player player = (Player) sender;
			if (command.getName().equalsIgnoreCase("parkour") && args.length == 3 && args[0].equalsIgnoreCase("new")) {
				boolean error = false;
				
				try {
					Integer.parseInt(args[2]);
				} catch (Exception ex) {
					error = true;
				}
				if (!error) {
					if ((!(getConfig().get("courses") == null) && !(getConfig().getConfigurationSection("courses").getKeys(false).contains(args[1]))) || getConfig().get("courses") == null) {
						getConfig().set("courses." + args[1] + ".world", player.getWorld().getName());
						getConfig().set("courses." + args[1] + ".points", Integer.parseInt(args[2]));
						
						configToMap();
						//saveConfig();
						
						player.sendMessage("A new course has been created. Do " + ChatColor.RED + "/parkour edit " + args[1] + ChatColor.WHITE + " to continue.");
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
				if (!(getConfig().get("courses") == null)) {
					if (getConfig().getConfigurationSection("courses").getKeys(false).size() > 0) {
						player.sendMessage("Here are the active parkour courses:");
						player.sendMessage("");
					
						for (String s : getConfig().getConfigurationSection("courses").getKeys(false)) {
							player.sendMessage(ChatColor.YELLOW + "Name: " + ChatColor.WHITE + s);
							player.sendMessage("Start X: " + getConfig().get("courses." + s + ".start.x"));
							player.sendMessage("Start Y: " + getConfig().get("courses." + s + ".start.y"));
							player.sendMessage("Start Z: " + getConfig().get("courses." + s + ".start.z"));
							player.sendMessage("End X: " + getConfig().get("courses." + s + ".end.x"));
							player.sendMessage("End Y: " + getConfig().get("courses." + s + ".end.y"));
							player.sendMessage("End Z: " + getConfig().get("courses." + s + ".end.z"));
							player.sendMessage("World: " + getConfig().get("courses." + s + ".world"));
							player.sendMessage("Point value: " + getConfig().get("courses." + s + ".points"));
							
							if (Bukkit.getServer().getWorld(getConfig().getString("courses." + s + ".world")).getBlockAt(getConfig().getInt("courses." + s + ".start.x"), getConfig().getInt("courses." + s + ".start.y"), getConfig().getInt("courses." + s + ".start.z")).getType() == Material.AIR) {
								player.sendMessage(ChatColor.RED + "WARNING: There is no block at the start location, making it inaccessable."); 
								player.sendMessage("This can be fixed by placing a block, such as a sign, at the location. You can teleport to this location by using " + ChatColor.RED + "/parkour tp " + s + ChatColor.WHITE + ".");
							}
							
							if (Bukkit.getServer().getWorld(getConfig().getString("courses." + s + ".world")).getBlockAt(getConfig().getInt("courses." + s + ".end.x"), getConfig().getInt("courses." + s + ".end.y"), getConfig().getInt("courses." + s + ".end.z")).getType() == Material.AIR) {
								player.sendMessage(ChatColor.RED + "WARNING: There is no block at the end location, making it inaccessable."); 
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
				if (!(getConfig().get("courses") == null)) {
					if (getConfig().getConfigurationSection("courses").getKeys(false).size() > 0) {
						boolean error = true;
						
						for (String s : getConfig().getConfigurationSection("courses").getKeys(false)) {
							if (s.equals(args[1])) {
								error = false;
								break;
							}
						}
						
						if (!error) {
							player.teleport (new Location(Bukkit.getServer().getWorld(getConfig().getString("courses." + args[1] + ".world")), (double) getConfig().getInt("courses." + args[1] + ".end.x") + 0.5, (double) getConfig().getInt("courses." + args[1] + ".end.y"), (double) getConfig().getInt("courses." + args[1] + ".end.z") + 0.5));
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
				if (!(getConfig().get("courses") == null)) {
					if (getConfig().getConfigurationSection("courses").getKeys(false).size() > 0) {
						boolean error = true;
						
						for (String s : getConfig().getConfigurationSection("courses").getKeys(false)) {
							if (s.equals(args[1])) {
								error = false;
								break;
							}
						}
						
						if (!error) {
							getConfig().getConfigurationSection("courses").set(args[1], null);
							courses.remove(args[1]);
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
			
			if (command.getName().equalsIgnoreCase("parkour") && args.length > 2 && args[0].equalsIgnoreCase("edit")) {
				if (!(getConfig().get("courses") == null)) {
					if (getConfig().getConfigurationSection("courses").getKeys(false).size() > 0) {
						boolean error = true;
						
						for (String s : getConfig().getConfigurationSection("courses").getKeys(false)) {
							if (s.equals(args[1])) {
								error = false;
								break;
							}
						}
						
						if (!error) {
							if (args[2].equalsIgnoreCase("setstart")) {
								getConfig().set("courses." + args[1] + ".start.x", player.getLocation().getBlockX());
								getConfig().set("courses." + args[1] + ".start.y", player.getLocation().getBlockY());
								getConfig().set("courses." + args[1] + ".start.z", player.getLocation().getBlockZ());
								
								configToMap();
								//saveConfig();
								
								player.sendMessage(ChatColor.GREEN + "The start point for '" + args[1] + "' has been set to your current location.");
							
							} else if (args[2].equalsIgnoreCase("setend")) {
								getConfig().set("courses." + args[1] + ".end.x", player.getLocation().getBlockX());
								getConfig().set("courses." + args[1] + ".end.y", player.getLocation().getBlockY());
								getConfig().set("courses." + args[1] + ".end.z", player.getLocation().getBlockZ());
								
								configToMap();
								//saveConfig();
								
								player.sendMessage(ChatColor.GREEN + "The end point for '" + args[1] + "' has been set to your current location.");
							
							} else if (args.length == 4 && args[2].equalsIgnoreCase("setname")) {
								getConfig().set("courses." + args[1] + ".end.x", args[3]);
							}
						} else {
							player.sendMessage(ChatColor.RED + "Invalid course name.");
							player.sendMessage("You can use " + ChatColor.RED + "/parkour list" + ChatColor.WHITE + " to get all the active courses and their names.");
						}
					} else {
						player.sendMessage(ChatColor.RED + "There are no active parkour courses to edit.");
					}
				} else {
					player.sendMessage(ChatColor.RED + "There are no active parkour courses to edit.");
				}
			} else if (command.getName().equalsIgnoreCase("parkour") && args[0].equalsIgnoreCase("edit") && !(args.length > 2)) {
				//TODO: usage
				player.sendMessage(ChatColor.RED + "Usage: /parkour edit [course name] [action]");
				player.sendMessage("Use " + ChatColor.RED + "/parkour help" + ChatColor.WHITE + " for more info.");
			}
			
			if (command.getName().equalsIgnoreCase("test")) {
				// test goes here lel
			}
		}
		
		return false;
	}
	
	public void configToMap () {
		if (!(getConfig().get("courses") == null)) {
			for (String s : getConfig().getConfigurationSection("courses").getKeys(false)) {
				Course course = new Course (s, getConfig().getInt("courses." + s + ".points"));
			
				if (!(getConfig().get("courses." + s + ".start.x") == null)) {
					course.start = Bukkit.getServer().getWorld(getConfig().getString("courses." + s + ".world")).getBlockAt(getConfig().getInt("courses." + s + ".start.x"), getConfig().getInt("courses." + s + ".start.y"), getConfig().getInt("courses." + s + ".start.z"));
				}
			
				if (!(getConfig().get("courses." + s + ".end.x") == null)) {
					course.end = Bukkit.getServer().getWorld(getConfig().getString("courses." + s + ".world")).getBlockAt(getConfig().getInt("courses." + s + ".end.x"), getConfig().getInt("courses." + s + ".end.y"), getConfig().getInt("courses." + s + ".end.z"));
				}
			
				courses.put(course.name, course);
			}
		}
	}
	
	public void storeTime (Player player) {
		startTime.put(player, (int) new Date().getTime());
	}
	
	public void clearTime (Player player) {
		startTime.remove(player);
	}
	
	public String msToString (int total, ChatColor color) {
		int hours = (int) Math.floor(total / 3600000);
		int minutes = (int) Math.floor((total - (hours * 3600000)) / 60000);
		int seconds = (int) Math.floor((total - (hours * 3600000) - (minutes * 60000)) / 1000);
		int ms = (int) Math.floor((total - (hours * 3600000) - (minutes * 60000) - (seconds * 1000)));
		
		String gc = color + ":" + ChatColor.WHITE;
		
		String time;
		
		if (!(hours == 0)) {
			time = (ChatColor.WHITE + Integer.toString(hours) + gc + String.format("%02d", minutes) + gc + String.format("%02d", seconds)  + gc + String.format("%03d", ms));
		} else {
			time = (ChatColor.WHITE + String.format("%02d", minutes) + gc + String.format("%02d", seconds)  + gc + String.format("%03d", ms));
		}
		
		return time;
	}
}