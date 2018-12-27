package me.foncused.duoauth.command;

import me.foncused.duoauth.DuoAuth;
import me.foncused.duoauth.config.ConfigManager;
import me.foncused.duoauth.database.AuthDatabase;
import me.foncused.duoauth.enumerable.AuthMessage;
import me.foncused.duoauth.lib.aikar.TaskChainManager;
import me.foncused.duoauth.lib.jeremyh.Bcrypt;
import me.foncused.duoauth.utility.AuthUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AuthCommand implements CommandExecutor {

	private final DuoAuth plugin;
	private final Map<UUID, Boolean> players;
	private final ConfigManager cm;
	private final AuthDatabase db;
	private final Set<UUID> auths;
	private final Set<UUID> cooldowns;

	public AuthCommand(final DuoAuth plugin) {
		this.plugin = plugin;
		this.players = this.plugin.getPlayers();
		this.cm = this.plugin.getConfigManager();
		this.db = this.plugin.getDatabase();
		this.auths = new HashSet<>();
		this.cooldowns = new HashSet<>();
	}

	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
		if(cmd.getName().equalsIgnoreCase("auth")) {
			if(sender instanceof Player) {
				final Player player = (Player) sender;
				if(player.hasPermission("duoauth.auth")) {
					final UUID uuid = player.getUniqueId();
					final String u = uuid.toString();
					final String name = player.getName();
					final int commandCooldown = this.cm.getCommandCooldown();
					switch(args.length) {
						case 1:
							switch(args[0]) {
								case "deauth":
									TaskChainManager.newChain()
											.asyncFirst(() -> this.db.contains(uuid))
											.syncLast(contained -> {
												if(contained) {
													if(this.players.get(uuid)) {
														if(!(this.cooldowns.contains(uuid))) {
															this.cooldowns.add(uuid);
															new BukkitRunnable() {
																public void run() {
																	cooldowns.remove(uuid);
																}
															}.runTaskLater(this.plugin, commandCooldown * 20);
															TaskChainManager.newChain()
																	.asyncFirst(() -> this.db.writeAuthed(uuid, false))
																	.syncLast(deauthed -> {
																		if(deauthed) {
																			if(player.isOnline()) {
																				this.players.put(uuid, false);
																			}
																			AuthUtilities.alertOne(player, ChatColor.GREEN + "Your have deauthenticated successfully. To continue playing, please use the " + ChatColor.RED + "/auth " + ChatColor.GREEN + "command.");
																			AuthUtilities.notify("Deauthenticated user " + u + " (" + name + ")");
																		} else {
																			AuthUtilities.alertOne(player, ChatColor.RED + "Deauthentication failed. Please contact the server administrators if you are receiving this message.");
																			AuthUtilities.notify("Failed to deauthenticate user " + u + " (" + name + ")");
																		}
																	})
																	.execute();
														} else {
															player.sendMessage(AuthMessage.MUST_WAIT.toString());
														}
													} else {
														AuthUtilities.alertOne(player, AuthMessage.PLAYER_NOT_AUTHED.toString());
													}
												} else {
													AuthUtilities.alertOne(player, AuthMessage.PLAYER_NOT_DATABASED.toString());
												}
											})
											.execute();
									break;
								case "reset":
									TaskChainManager.newChain()
											.asyncFirst(() -> this.db.contains(uuid))
											.syncLast(contained -> {
												if(contained) {
													if(this.players.get(uuid)) {
														if(!(this.cooldowns.contains(uuid))) {
															this.cooldowns.add(uuid);
															new BukkitRunnable() {
																public void run() {
																	cooldowns.remove(uuid);
																}
															}.runTaskLater(this.plugin, commandCooldown * 20);
															TaskChainManager.newChain()
																	.asyncFirst(() -> this.db.delete(uuid))
																	.syncLast(deleted -> {
																		if(deleted) {
																			if(player.isOnline()) {
																				this.players.put(uuid, true);
																			}
																			AuthUtilities.alertOne(player, ChatColor.GREEN + "Your credentials have been reset! To re-enable authentication, please use the " + ChatColor.RED + "/auth " + ChatColor.GREEN + "command.");
																			AuthUtilities.notify("Reset authentication for user " + u + " (" + name + ")");
																		} else {
																			AuthUtilities.alertOne(player, ChatColor.RED + "Failed to reset authentication. Please contact the server administrators if you are receiving this message.");
																			AuthUtilities.notify("Failed to reset authentication for user " + u + " (" + name + ")");
																		}
																	})
																	.execute();
														} else {
															player.sendMessage(AuthMessage.MUST_WAIT.toString());
														}
													} else {
														AuthUtilities.alertOne(player, AuthMessage.PLAYER_NOT_AUTHED.toString());
													}
												} else {
													AuthUtilities.alertOne(player, AuthMessage.PLAYER_NOT_DATABASED.toString());
												}
											})
											.execute();
									break;
								default:
									this.printUsage(player);
									break;
							}
							break;
						case 2:
							switch(args[0]) {
								case "deauth":
									if(player.hasPermission("duoauth.admin")) {
										final OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(args[1]);
										final String target = targetOffline.getName();
										final UUID targetId = targetOffline.getUniqueId();
										TaskChainManager.newChain()
												.asyncFirst(() -> this.db.writeAuthed(uuid, false))
												.syncLast(deauthed -> {
													final String id = targetId.toString();
													if(deauthed) {
														if(targetOffline.isOnline()) {
															this.players.put(targetId, false);
															AuthUtilities.alertOne((Player) targetOffline, ChatColor.RED + "You have been deauthenticated by an administrator. Please use the /auth command to continue playing. Thank you!");
														}
														AuthUtilities.alertOne(player, ChatColor.GREEN + "Deauthentication of user " + target + " was successful.");
														AuthUtilities.notify("Deauthentication of user " + id + " (" + target + ") was successful");
													} else {
														AuthUtilities.alertOne(player, ChatColor.RED + "Failed to deauthenticate user " + target + ". Has this player set up authentication?");
														AuthUtilities.notify("Failed to deauthenticate user " + id + " (" + target + ")");
													}
												})
												.execute();
									} else {
										player.sendMessage(AuthMessage.NO_PERMISSION.toString());
									}
									break;
								case "reset":
									if(player.hasPermission("duoauth.admin")) {
										final OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(args[1]);
										final String target = targetOffline.getName();
										final UUID targetId = targetOffline.getUniqueId();
										TaskChainManager.newChain()
												.asyncFirst(() -> this.db.delete(targetId))
												.syncLast(deleted -> {
													final String id = targetId.toString();
													if(deleted) {
														if(targetOffline.isOnline()) {
															this.players.put(targetId, true);
															AuthUtilities.alertOne((Player) targetOffline, ChatColor.GREEN + "Your credentials have been reset by an administrator.");
														}
														AuthUtilities.alertOne(player, ChatColor.GREEN + "Authentication for user " + target + " has been reset.");
														AuthUtilities.notify("Reset authentication for user " + id + " (" + target + ")");
													} else {
														AuthUtilities.alertOne(player, ChatColor.RED + "Failed to reset authentication for user " + target + ". Has this player set up authentication?");
														AuthUtilities.notify("Failed to reset authentication for user " + id + " (" + target + ")");
													}
												})
												.execute();
									} else {
										player.sendMessage(AuthMessage.NO_PERMISSION.toString());
									}
									break;
								default:
									final String password = args[0];
									final int passwordMinLength = this.cm.getPasswordMinLength();
									final boolean passwordBothCases = this.cm.isPasswordBothCases();
									final boolean passwordNumbers = this.cm.isPasswordNumbers();
									final boolean passwordSpecialChars = this.cm.isPasswordSpecialChars();
									if(password.length() >= passwordMinLength && password.matches((passwordBothCases ? "(?=.*[A-Z])(?=.*[a-z])" : "(?=.*[A-Za-z])") + (passwordNumbers ? "(?=.*[0-9])" : "") + (passwordSpecialChars ? "(?=.*[@#$%^&+=])" : "") + "(?=\\S+$).*$")) {
										final String pin = args[1];
										final int pinMinLength = this.cm.getPinMinLength();
										if(pin.length() >= pinMinLength && pin.matches("^[0-9]+$")) {
											if(player.hasPermission("duoauth.bypass") || (!(this.cooldowns.contains(uuid)))) {
												if(!(this.auths.contains(uuid))) {
													this.cooldowns.add(uuid);
													new BukkitRunnable() {
														public void run() {
															cooldowns.remove(uuid);
														}
													}.runTaskLater(this.plugin, commandCooldown * 20);
													this.auths.add(uuid);
													final String address = AuthUtilities.getPlayerAddress(player);
													final int commandAttempts = this.cm.getCommandAttempts();
													final int costFactor = this.cm.getCostFactor();
													TaskChainManager.newChain()
															.asyncFirst(() -> {
																if(!(this.db.contains(uuid))) {
																	TaskChainManager.newChain()
																			.sync(() -> {
																				AuthUtilities.alertOne(player, ChatColor.GOLD + "Setting up authentication...");
																				AuthUtilities.notify("Setting up authentication for user " + u + " (" + name + ")...");
																			})
																			.execute();
																	final String pwhash = AuthUtilities.getSecureBCryptHash(password, costFactor);
																	final String pinhash = AuthUtilities.getSecureBCryptHash(pin, costFactor);
																	final boolean written = this.db.write(uuid, pwhash, pinhash, 0, address);
																	TaskChainManager.newChain()
																			.sync(() -> {
																				if(written) {
																					if(player.isOnline()) {
																						this.players.put(uuid, true);
																					}
																					AuthUtilities.alertOne(player, ChatColor.GREEN + "Your credentials have been set!");
																					AuthUtilities.notify("User " + u + " (" + name + ") successfully set up authentication");
																				} else {
																					AuthUtilities.alertOne(player, ChatColor.RED + "Failed to set up authentication. Please contact the server administrators if you are receiving this message.");
																					AuthUtilities.notify("User " + u + " (" + name + ") failed to set up authentication");
																				}
																			})
																			.execute();
																	return null;
																} else {
																	final int attempts = this.db.readAttempts(uuid);
																	if(commandAttempts != 0 && attempts >= commandAttempts) {
																		AuthUtilities.alertOne(player, ChatColor.RED + "You have failed to authenticate " + attempts + " times in a row. You will need to wait for your account to be unlocked, or you may contact the server administrators for assistance.");
																		AuthUtilities.notify("User " + u + " (" + name + ") has failed authentication " + attempts + " times");
																		TaskChainManager.newChain()
																				.delay(5, TimeUnit.SECONDS)
																				.sync(() -> player.kickPlayer(AuthMessage.LOCKED.toString()))
																				.execute();
																		return "";
																	} else {
																		TaskChainManager.newChain()
																				.sync(() -> {
																					AuthUtilities.alertOne(player, ChatColor.GOLD + "Authenticating...");
																					AuthUtilities.notify("Authenticating user " + u + " (" + name + ")...");
																				})
																				.execute();
																		final String dbpassword = this.db.readPassword(uuid);
																		final String dbpin = this.db.readPIN(uuid);
																		return (Bcrypt.checkpw(password, dbpassword) && (Bcrypt.checkpw(pin, dbpin))) ? ChatColor.GREEN + "Authentication successful. Have fun!" : ChatColor.RED + "Authentication failed. Please ensure your password and PIN are correct. Please contact the server administrators if you believe that this is in error.";
																	}
																}
															})
															.syncLast(result -> {
																if(result != null) {
																	if(!(result.isEmpty())) {
																		AuthUtilities.alertOne(player, result);
																	}
																	if(result.contains("successful")) {
																		TaskChainManager.newChain()
																				.async(() -> {
																					this.db.writeAuthed(uuid, true);
																					this.db.writeTimestamp(uuid);
																					this.db.writeAttempts(uuid, 0);
																				})
																				.execute();
																		if(player.isOnline()) {
																			this.players.put(uuid, true);
																		}
																		AuthUtilities.notify("User " + u + " (" + name + ") authenticated successfully");
																	} else {
																		TaskChainManager.newChain()
																				.async(() -> {
																					this.db.writeAuthed(uuid, false);
																					final int attempts = this.db.readAttempts(uuid);
																					if(attempts < commandAttempts) {
																						this.db.writeAttempts(uuid, attempts + 1);
																					}
																				})
																				.execute();
																		if(player.isOnline()) {
																			this.players.put(uuid, false);
																		}
																		AuthUtilities.notify("User " + u + " (" + name + ") failed authentication");
																	}
																}
																this.auths.remove(uuid);
															})
															.execute();
												} else {
													AuthUtilities.alertOne(player, ChatColor.GOLD + "Authentication in progress - please be patient...");
												}
											} else {
												player.sendMessage(AuthMessage.MUST_WAIT.toString());
											}
										} else {
											AuthUtilities.alertOne(
													player,
													ChatColor.RED + "The PIN you entered is invalid. Your PIN must contain at least " + pinMinLength + " digits and must be numeric."
											);
										}
									} else {
										AuthUtilities.alertOne(
												player,
												ChatColor.RED + "The password you entered is invalid. Your password must contain at least " + passwordMinLength + " characters" +
														(passwordBothCases ? ", 1 uppercase letter (A-Z), 1 lowercase letter (a-z)" : "") +
														(passwordNumbers ? ", 1 number (0-9)" : "") +
														(passwordSpecialChars ? ", 1 special character (@#$%^&+=)." : ".")
										);
									}
									break;
							}
							break;
						default:
							this.printUsage(player);
							break;
					}
				} else {
					sender.sendMessage(AuthMessage.NO_PERMISSION.toString());
				}
			} else {
				sender.sendMessage(ChatColor.RED + "You cannot do this from the console!");
			}
		}
		return true;
	}

	private void printUsage(final Player player) {
		player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "---------------------------------------------");
		player.sendMessage(ChatColor.DARK_GRAY + "    DuoAuth v" + this.plugin.getDescription().getVersion() + " by foncused");
		player.sendMessage(ChatColor.RED + "    /auth help" + ChatColor.GRAY + " - view this message");
		player.sendMessage(ChatColor.RED + "    /auth deauth" + ChatColor.GRAY + " - deauthenticate yourself");
		final boolean admin = player.hasPermission("duoauth.admin");
		if(admin) {
			player.sendMessage(ChatColor.RED + "    /auth deauth <player>" + ChatColor.GRAY + " - deauthenticate a player");
		}
		player.sendMessage(ChatColor.RED + "    /auth reset" + ChatColor.GRAY + " - reset your own credentials");
		if(admin) {
			player.sendMessage(ChatColor.RED + "    /auth reset <player>" + ChatColor.GRAY + " - reset a player's credentials");
		}
		player.sendMessage(ChatColor.RED + "    /auth <password> <pin>" + ChatColor.GRAY + " - set up or attempt authentication");
		player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "---------------------------------------------");
	}

}
