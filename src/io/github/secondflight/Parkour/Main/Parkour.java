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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class Parkour extends JavaPlugin implements Listener{
	public final Logger logger = Logger.getLogger("Minecraft");
	public Parkour plugin;
		
	public static Map<String, Course> courses = new HashMap<String, Course>();
	public static Map<Player, Integer> startTime = new HashMap<Player, Integer>();
	public static Map<Player, Course> currentCourse = new HashMap<Player, Course>();
	
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
	
	@EventHandler
	public void onJoin (PlayerJoinEvent event) {
		Player p = event.getPlayer();
		if (p.isOp()) {
			if (getConfig().getString("admin." + event.getPlayer().getUniqueId().toString() + ".hasAdmin") == null || !(getConfig().getString("admin." + event.getPlayer().getUniqueId().toString() + ".hasAdmin").equals("true"))) {
				getConfig().set("admin." + event.getPlayer().getUniqueId().toString() + ".hasAdmin", "true");
				getConfig().set("admin." + event.getPlayer().getUniqueId().toString() + ".name", p.getDisplayName());
				saveConfig();
			}
		}
	}
	
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
						currentCourse.put(p, course);
						p.sendMessage(ChatColor.GREEN + "Go!");
					}
				}
				
				if (!(course.end == null)) {
					if (b.equals(course.end)) {
						if (startTime.containsKey(p)) {
							if (currentCourse.get(p).name.equalsIgnoreCase(s)) {
								Date d = new Date();
								int total = (int) d.getTime() - startTime.get(p);
								
								p.sendMessage(ChatColor.GREEN + "Your time was " + msToString(total, ChatColor.GREEN));
								
								if (getConfig().get("highscores." + event.getPlayer().getUniqueId() + "." + course.name) != null) {
									if (getConfig().getInt("highscores." + event.getPlayer().getUniqueId() + "." + course.name) >= total) {
										p.sendMessage(ChatColor.GREEN + "You beat your previous best of " + msToString(getConfig().getInt("highscores." + event.getPlayer().getUniqueId() + "." + course.name), ChatColor.GREEN) + ChatColor.GREEN + "!");
										getConfig().set("highscores." + event.getPlayer().getUniqueId() + "." + course.name, total);
										saveConfig();
									} else {
										p.sendMessage(ChatColor.GRAY + "You did not beat your previous best time (" + msToString(getConfig().getInt("highscores." + event.getPlayer().getUniqueId() + "." + course.name), ChatColor.GRAY) + ChatColor.GRAY + "). Better luck next time.");
									}
								} else {
									getConfig().set("highscores." + event.getPlayer().getUniqueId() + "." + course.name, total);
									saveConfig();
								}
								
								
							} else {
								p.sendMessage(ChatColor.RED + "You are no longer in parkour mode.");
							}
							
							clearTime(p);
							currentCourse.remove(p);
						}
					}
				}
			}
		}
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		
		if(sender instanceof Player) {
			Player player = (Player) sender;
			
			if (command.getName().equalsIgnoreCase("parkour") && !(args[0].equalsIgnoreCase("list"))) {
				if (!(hasAdmin(player))) {
					return false;
				}
			}
			
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
						saveConfig();
						
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
			
			
			if (command.getName().equalsIgnoreCase("parkour") && (args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("list")) {
				if (!(getConfig().get("courses") == null)) {
					if (getConfig().getConfigurationSection("courses").getKeys(false).size() > 0) {
						player.sendMessage("Here are the active parkour courses:");
						player.sendMessage("");
						
						if (args.length == 2 && args[1].equalsIgnoreCase("debug")) {
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
						} else if (args.length == 1) {
							for (String s : getConfig().getConfigurationSection("courses").getKeys(false)) {
								player.sendMessage(s);
							}
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
							if (startTime.containsKey(player)) {
								clearTime(player);
								currentCourse.remove(player);
								player.sendMessage(ChatColor.RED + "You are no longer in parkour mode.");
							}
							player.teleport (new Location(Bukkit.getServer().getWorld(getConfig().getString("courses." + args[1] + ".world")), (double) getConfig().getInt("courses." + args[1] + ".start.x") + 0.5, (double) getConfig().getInt("courses." + args[1] + ".start.y"), (double) getConfig().getInt("courses." + args[1] + ".start.z") + 0.5));
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
							saveConfig();
							
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
								saveConfig();
								
								player.sendMessage(ChatColor.GREEN + "The start point for '" + args[1] + "' has been set to your current location.");
							
							} else if (args[2].equalsIgnoreCase("setend")) {
								getConfig().set("courses." + args[1] + ".end.x", player.getLocation().getBlockX());
								getConfig().set("courses." + args[1] + ".end.y", player.getLocation().getBlockY());
								getConfig().set("courses." + args[1] + ".end.z", player.getLocation().getBlockZ());
								
								configToMap();
								saveConfig();
								
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
			} else if (command.getName().equalsIgnoreCase("parkour") && args[0].equalsIgnoreCase("admin") && !(args.length == 3)) {
				if (args[1].equalsIgnoreCase("add")) {
					boolean showError = true;
					
					for (Player p : Bukkit.getServer().getOnlinePlayers()) {
						
						if (args[2].equals(p.getDisplayName())) {
							getConfig().set("admin." + p.getUniqueId().toString(), "true");
							showError = false;
							saveConfig();
							break;
						}
					}
					
					if (showError) {
						player.sendMessage(ChatColor.GRAY + "Player does not exist or is not online.");
					} else {
						player.sendMessage(ChatColor.GREEN + "Player " + args[2] + " has been added to the admin list for the parkour plugin.");
					}
				} else if (args[1].equalsIgnoreCase("remove")) {
					if (!(getConfig().get("admin") == null)) {
						if (getConfig().getConfigurationSection("admin").getKeys(false).size() > 0) {
							boolean showError = true;
							
							for (String s : getConfig().getConfigurationSection("admin").getKeys(false)) {
								if (getConfig().get("admin." + s + ".name").equals(args[2])) {
									getConfig().set("admin." + s, "");
									getConfig().set("admin." + s + ".name", "");
									getConfig().set("admin." + s + ".hasAdmin", "");
									showError = false;
									saveConfig();
									break;
								}
							}
							
							if (showError) {
								player.sendMessage(ChatColor.GRAY + "This player does not seem to be listed as an admin.");
							} else {
								player.sendMessage(ChatColor.GREEN + "Successfully removed " + args[2] + " from the admin list. Please note that they will be automatically re-added next time they join if they are opped on the server.");
							}
						}
					}
				}
			}
			
			if (command.getName().equalsIgnoreCase("parkour") && args.length == 1 && args[0].equalsIgnoreCase("help")) {
				player.sendMessage("Use " + ChatColor.RED + "/parkour help [command]" + ChatColor.WHITE + " to get detailed help for a sepcific command.");
				player.sendMessage("");
				player.sendMessage("/parkour new [name] [point value]");
				player.sendMessage("    Creates a new parkour course with the given name and point value.");
				player.sendMessage("");
				player.sendMessage("/parkour edit [name] setstart");
				player.sendMessage("    Sets the start location for the given parkour course to whatever block your feet are at.");
				player.sendMessage("");
				player.sendMessage("/parkour edit [name] setend");
				player.sendMessage("    Sets the end location for the given parkour course to whatever block your feet are at.");
				player.sendMessage("");
				player.sendMessage("/parkour edit [name] setname [new name]");
				player.sendMessage("    Sets the name of the given parkour course to the given new name.");
				player.sendMessage("    " + ChatColor.RED + ChatColor.BOLD + "WARNING! This does not transfer player high scores to the newly named course. This means that the high scores will still be stored under the old name, making them inaccesable. Tread with caution.");
				player.sendMessage("");
				player.sendMessage("/parkour remove [name]");
				player.sendMessage("    Removes the given parkour course.");
				player.sendMessage("    " + ChatColor.YELLOW + ChatColor.ITALIC + "Note: This does not remove player high scores for the given course name.");
				player.sendMessage("");
				player.sendMessage("/parkour tp [name]");
				player.sendMessage("    Teleports you to the given course's start.");
				player.sendMessage("");
				player.sendMessage("/parkour list");
				player.sendMessage("    Lists the registered parkour courses, along with some useful info about each one.");
				player.sendMessage("    " + ChatColor.ITALIC + "Note: You can add the 'debug' argument at the end of the command to have it display extra information.");
				player.sendMessage("");
				player.sendMessage("/parkour admin [add/remove] [player]");
				player.sendMessage("    Adds or removes admin permissions for the given player.");
			}
			
			if (command.getName().equalsIgnoreCase("test")) {
				if (getConfig().get("admin." + player.getUniqueId().toString()) != null) {
					player.sendMessage("u has admin tho");
				} else {
					player.sendMessage("try again tho, tha forec be wiff u tho");
				}
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
	
	public boolean hasAdmin (Player player) {
		if (getConfig().getString("admin." + player.getUniqueId().toString() + ".hasAdmin").equals("true")) {
			return true;
		} else {
			player.sendMessage(ChatColor.RED + "You do not have permission to do this.");
			return false;
		}
	}
}