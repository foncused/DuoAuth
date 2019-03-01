package me.foncused.duoauth.command;

import co.aikar.taskchain.TaskChain;
import me.foncused.duoauth.DuoAuth;
import me.foncused.duoauth.cache.AuthCache;
import me.foncused.duoauth.config.ConfigManager;
import me.foncused.duoauth.database.AuthDatabase;
import me.foncused.duoauth.enumerable.AuthMessage;
import me.foncused.duoauth.enumerable.DatabaseProperty;
import me.foncused.duoauth.lib.aikar.TaskChainManager;
import me.foncused.duoauth.lib.jeremyh.Bcrypt;
import me.foncused.duoauth.util.AuthUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AuthCommand implements CommandExecutor {

	private final DuoAuth plugin;
	private final ConfigManager cm;
	private final AuthDatabase db;
	private final Set<UUID> auths;
	private final Set<UUID> cooldowns;

	public AuthCommand(final DuoAuth plugin) {
		this.plugin = plugin;
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
					final AuthCache cache = this.plugin.getAuthCache(uuid);
					final String u = uuid.toString();
					final String name = player.getName();
					final int commandCooldown = this.cm.getCommandCooldown();
					switch(args.length) {
						case 1:
							TaskChainManager.newChain()
									.asyncFirst(() -> this.db.contains(uuid))
									.syncLast(contained -> {
										if(contained) {
											if(cache != null) {
												if(cache.isAuthed()) {
													if(player.hasPermission("duoauth.bypass") || (!(this.cooldowns.contains(uuid)))) {
														if(!(this.auths.contains(uuid))) {
															this.cooldowns.add(uuid);
															new BukkitRunnable() {
																@Override
																public void run() {
																	cooldowns.remove(uuid);
																}
															}.runTaskLater(this.plugin, commandCooldown * 20);
															switch(args[0].toLowerCase()) {
																case "deauth":
																	TaskChainManager.newChain()
																			.asyncFirst(() -> this.db.writeProperty(uuid, DatabaseProperty.AUTHED, false))
																			.syncLast(deauthed -> {
																				if(deauthed) {
																					if(player.isOnline()) {
																						cache.setAuthed(false);
																					}
																					AuthUtil.alertOne(player, ChatColor.GREEN + "Your have deauthenticated successfully. To continue playing, please use the " + ChatColor.RED + "/auth " + ChatColor.GREEN + "command.");
																					AuthUtil.notify("Deauthenticated user " + u + " (" + name + ")");
																				} else {
																					AuthUtil.alertOne(player, ChatColor.RED + "Deauthentication failed. Please contact the server administrators if you are receiving this message.");
																					AuthUtil.notify("Failed to deauthenticate user " + u + " (" + name + ")");
																				}
																			})
																			.execute();
																	break;
																case "reset":
																	TaskChainManager.newChain()
																			.asyncFirst(() -> this.db.delete(uuid))
																			.syncLast(deleted -> {
																				if(deleted) {
																					if(player.isOnline()) {
																						cache.setAuthed(true);
																					}
																					AuthUtil.alertOne(player, ChatColor.GREEN + "Your credentials have been reset! To re-enable authentication, please use the " + ChatColor.RED + "/auth " + ChatColor.GREEN + "command.");
																					AuthUtil.notify("Reset authentication for user " + u + " (" + name + ")");
																				} else {
																					AuthUtil.alertOne(player, ChatColor.RED + "Failed to reset authentication. Please contact the server administrators if you are receiving this message.");
																					AuthUtil.notify("Failed to reset authentication for user " + u + " (" + name + ")");
																				}
																			})
																			.execute();
																	break;
																default:
																	this.printUsage(player);
																	break;
															}
														} else {
															AuthUtil.alertOne(player, AuthMessage.AUTH_IN_PROGRESS.toString());
														}
													} else {
														player.sendMessage(AuthMessage.MUST_WAIT.toString());
													}
												} else {
													AuthUtil.alertOne(player, AuthMessage.PLAYER_NOT_AUTHED.toString());
												}
											} else {
												AuthUtil.alertOne(player, AuthMessage.PLAYER_NOT_DATABASED.toString());
											}
										} else {
											AuthUtil.alertOne(player, AuthMessage.PLAYER_NOT_DATABASED.toString());
										}
									})
									.execute();
							break;
						case 2:
							switch(args[0].toLowerCase()) {
								case "deauth":
									if(player.hasPermission("duoauth.admin")) {
										final OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(args[1]);
										final String target = targetOffline.getName();
										final UUID targetId = targetOffline.getUniqueId();
										if(!(this.auths.contains(targetId))) {
											TaskChainManager.newChain()
													.asyncFirst(() -> this.db.contains(targetId) && this.db.readProperty(targetId, DatabaseProperty.AUTHED).getAsBoolean() && this.db.writeProperty(targetId, DatabaseProperty.AUTHED, false))
													.syncLast(deauthed -> {
														final String id = targetId.toString();
														if(deauthed) {
															final AuthCache c = this.plugin.getAuthCache(targetId);
															if(c != null && targetOffline.isOnline()) {
																c.setAuthed(false);
																AuthUtil.alertOne((Player) targetOffline, ChatColor.RED + "You have been deauthenticated by an administrator. Please use the /auth command to continue playing. Thank you!");
															}
															AuthUtil.alertOne(player, ChatColor.GREEN + "Deauthentication of user " + target + " was successful.");
															AuthUtil.notify("Deauthentication of user " + id + " (" + target + ") was successful");
														} else {
															AuthUtil.alertOne(player, ChatColor.RED + "Failed to deauthenticate user " + target + ". Has this player set up authentication?");
															AuthUtil.notify("Failed to deauthenticate user " + id + " (" + target + ")");
														}
													})
													.execute();
										} else {
											AuthUtil.alertOne(player, AuthMessage.AUTH_IN_PROGRESS_ADMIN.toString());
										}
									} else {
										player.sendMessage(AuthMessage.NO_PERMISSION.toString());
									}
									break;
								case "reset":
									this.reset(player, args[1]);
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
														@Override
														public void run() {
															cooldowns.remove(uuid);
														}
													}.runTaskLater(this.plugin, commandCooldown * 20);
													this.auths.add(uuid);
													final InetAddress ip = AuthUtil.getPlayerAddress(player);
													final int commandAttempts = this.cm.getCommandAttempts();
													final int costFactor = this.cm.getCostFactor();
													final TaskChain chain = TaskChainManager.newChain();
													chain
															.sync(() -> {
																if(cache != null && player.isOnline()) {
																	chain.setTaskData("password", cache.getPassword());
																	chain.setTaskData("pin", cache.getPin());
																	chain.setTaskData("attempts", cache.getAttempts());
																}
															})
															.async(() -> {
																if(!(this.db.contains(uuid))) {
																	TaskChainManager.newChain()
																			.sync(() -> {
																				AuthUtil.alertOne(player, ChatColor.GOLD + "Setting up authentication...");
																				AuthUtil.notify("Setting up authentication for user " + u + " (" + name + ")...");
																			})
																			.execute();
																	final String pwhash = AuthUtil.getSecureBCryptHash(password, costFactor);
																	final String pinhash = AuthUtil.getSecureBCryptHash(pin, costFactor);
																	final boolean written = this.db.write(uuid, pwhash, pinhash, true, 0, ip);
																	TaskChainManager.newChain()
																			.sync(() -> {
																				if(written) {
																					if(player.isOnline()) {
																						this.plugin.setAuthCache(
																								uuid,
																								new AuthCache(
																										pwhash,
																										pinhash,
																										true,
																										0,
																										ip
																								)
																						);
																					}
																					AuthUtil.alertOne(player, ChatColor.GREEN + "Your credentials have been set!");
																					AuthUtil.notify("User " + u + " (" + name + ") successfully set up authentication");
																				} else {
																					AuthUtil.alertOne(player, ChatColor.RED + "Failed to set up authentication. Please contact the server administrators if you are receiving this message.");
																					AuthUtil.notify("User " + u + " (" + name + ") failed to set up authentication");
																				}
																			})
																			.execute();
																	chain.setTaskData("result", null);
																} else {
																	int attempts = (chain.hasTaskData("attempts"))
																			? (int) chain.getTaskData("attempts")
																			: this.db.readProperty(uuid, DatabaseProperty.ATTEMPTS).getAsInt();
																	if(commandAttempts != 0 && attempts >= commandAttempts) {
																		AuthUtil.alertOne(player, ChatColor.RED + "You have failed to authenticate " + attempts + " times in a row. You will need to wait for your account to be unlocked, or you may contact the server administrators for assistance.");
																		AuthUtil.notify("User " + u + " (" + name + ") has failed authentication " + attempts + " times");
																		TaskChainManager.newChain()
																				.delay(5, TimeUnit.SECONDS)
																				.sync(() -> player.kickPlayer(AuthMessage.LOCKED.toString()))
																				.execute();
																		chain.setTaskData("result", "");
																	} else {
																		TaskChainManager.newChain()
																				.sync(() -> {
																					AuthUtil.alertOne(player, ChatColor.GOLD + "Authenticating...");
																					AuthUtil.notify("Authenticating user " + u + " (" + name + ")...");
																				})
																				.execute();
																		final String pw = (chain.hasTaskData("password")
																				? (String) chain.getTaskData("password")
																				: this.db.readProperty(uuid, DatabaseProperty.PASSWORD).getAsString()
																		);
																		final String pi = (chain.hasTaskData("pin")
																				? (String) chain.getTaskData("pin")
																				: this.db.readProperty(uuid, DatabaseProperty.PIN).getAsString()
																		);
																		chain.setTaskData(
																				"result",
																				(Bcrypt.checkpw(password, pw) && Bcrypt.checkpw(pin, pi))
																						? ChatColor.GREEN + "Authentication successful. Have fun!"
																						: ChatColor.RED + "Authentication failed. Please ensure your password and PIN are correct. Please contact the server administrators if you believe that this is in error."
																		);
																	}
																}
															})
															.sync(() -> {
																final String result = (String) chain.getTaskData("result");
																if(result != null) {
																	if(!(result.isEmpty())) {
																		AuthUtil.alertOne(player, result);
																	}
																	if(result.contains("successful")) {
																		TaskChainManager.newChain()
																				.async(() -> {
																					this.db.writeProperty(uuid, DatabaseProperty.AUTHED, true);
																					this.db.writeProperty(uuid, DatabaseProperty.ATTEMPTS, 0);
																					this.db.writeProperty(uuid, DatabaseProperty.IP, ip);
																					this.db.writeProperty(uuid, DatabaseProperty.TIMESTAMP, AuthUtil.getFormattedTime(AuthUtil.getDateFormat()));
																				})
																				.execute();
																		if(cache != null && player.isOnline()) {
																			cache.setAuthed(true);
																		}
																		AuthUtil.notify("User " + u + " (" + name + ") authenticated successfully");
																	} else {
																		TaskChainManager.newChain()
																				.syncFirst(() -> {
																					if(cache != null && player.isOnline()) {
																						cache.setAuthed(false);
																						return cache.getAttempts();
																					}
																					if(chain.hasTaskData("attempts")) {
																						return (int) chain.getTaskData("attempts");
																					}
																					return -1;
																				})
																				.asyncLast(attempts -> {
																					this.db.writeProperty(uuid, DatabaseProperty.AUTHED, false);
																					if(attempts == -1) {
																						attempts = this.db.readProperty(uuid, DatabaseProperty.ATTEMPTS).getAsInt();
																					}
																					if(attempts < commandAttempts) {
																						attempts++;
																						if(cache != null) {
																							cache.setAttempts(attempts);
																						}
																						this.db.writeProperty(uuid, DatabaseProperty.ATTEMPTS, attempts);
																					}
																				})
																				.execute();
																		AuthUtil.notify("User " + u + " (" + name + ") failed authentication");
																	}
																}
																this.auths.remove(uuid);
															})
															.execute();
												} else {
													AuthUtil.alertOne(player, AuthMessage.AUTH_IN_PROGRESS.toString());
												}
											} else {
												player.sendMessage(AuthMessage.MUST_WAIT.toString());
											}
										} else {
											AuthUtil.alertOne(
													player,
													ChatColor.RED + "The PIN you entered is invalid. Your PIN must contain at least " + pinMinLength + " digits and must be numeric."
											);
										}
									} else {
										AuthUtil.alertOne(
												player,
												ChatColor.RED + "The password you entered is invalid. Your password must contain at least " + passwordMinLength + " characters" +
														(passwordBothCases
																? ", 1 uppercase letter (A-Z), 1 lowercase letter (a-z)"
																: "") +
														(passwordNumbers
																? ", 1 number (0-9)"
																: "") +
														(passwordSpecialChars
																? ", 1 special character (@#$%^&+=)."
																: ".")
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
			} else if(this.cm.isConsoleReset() && args.length == 2 && args[0].toLowerCase().equals("reset")) {
				this.reset(sender, args[1]);
			} else {
				sender.sendMessage(ChatColor.RED + "You cannot do this from the console!");
			}
		}
		return true;
	}

	private void reset(final CommandSender sender, final String args1) {
		final boolean console = (!(sender instanceof Player));
		if(console || sender.hasPermission("duoauth.admin")) {
			final OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(args1);
			final String target = targetOffline.getName();
			final UUID targetId = targetOffline.getUniqueId();
			if(!(this.auths.contains(targetId))) {
				TaskChainManager.newChain()
						.asyncFirst(() -> this.db.delete(targetId))
						.syncLast(deleted -> {
							final String id = targetId.toString();
							String msg;
							if(deleted) {
								final AuthCache c = this.plugin.getAuthCache(targetId);
								if(c != null && targetOffline.isOnline()) {
									c.setAuthed(true);
									AuthUtil.alertOne((Player) targetOffline, ChatColor.GREEN + "Your credentials have been reset by an administrator.");
								}
								msg = ChatColor.GREEN + "Authentication for user " + target + " has been reset.";
								AuthUtil.notify("Reset authentication for user " + id + " (" + target + ")");
							} else {
								msg = ChatColor.RED + "Failed to reset authentication for user " + target + ". Has this player set up authentication?";
								AuthUtil.notify("Failed to reset authentication for user " + id + " (" + target + ")");
							}
							if(console) {
								AuthUtil.console(msg);
							} else {
								AuthUtil.alertOne((Player) sender, msg);
							}
						})
						.execute();
			} else {
				if(console) {
					AuthUtil.console(AuthMessage.AUTH_IN_PROGRESS_ADMIN.toString());
				} else {
					AuthUtil.alertOne((Player) sender, AuthMessage.AUTH_IN_PROGRESS_ADMIN.toString());
				}
			}
		} else {
			sender.sendMessage(AuthMessage.NO_PERMISSION.toString());
		}
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
