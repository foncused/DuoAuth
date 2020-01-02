package me.foncused.duoauth.spigot.command;

import co.aikar.taskchain.TaskChain;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import me.foncused.duoauth.spigot.DuoAuth;
import me.foncused.duoauth.spigot.cache.AuthCache;
import me.foncused.duoauth.spigot.config.ConfigManager;
import me.foncused.duoauth.spigot.config.LangManager;
import me.foncused.duoauth.spigot.database.AuthDatabase;
import me.foncused.duoauth.spigot.enumerable.AuthMessage;
import me.foncused.duoauth.spigot.enumerable.DatabaseProperty;
import me.foncused.duoauth.spigot.lib.aikar.TaskChainManager;
import me.foncused.duoauth.spigot.lib.jeremyh.Bcrypt;
import me.foncused.duoauth.spigot.lib.wstrange.GoogleAuth;
import me.foncused.duoauth.spigot.util.AuthUtil;
import me.foncused.duoauth.spigot.util.BungeeUtil;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AuthCommand implements CommandExecutor {

	private final DuoAuth plugin;
	private final ConfigManager cm;
	private final GoogleAuth ga;
	private final LangManager lm;
	private final AuthDatabase db;
	private final Set<UUID> auths;
	private final Set<UUID> cooldowns;

	public AuthCommand(final DuoAuth plugin) {
		this.plugin = plugin;
		this.cm = this.plugin.getConfigManager();
		this.ga = this.plugin.getGoogleAuth();
		this.lm = this.plugin.getLangManager();
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
							final String args0 = args[0].toLowerCase();
							if(args0.equals("help")) {
								this.printUsage(player);
								break;
							}
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
															switch(args0) {
																case "generate":
																	AuthUtil.alertOne(player, this.lm.getNoGenerate());
																	break;
																case "deauth":
																	TaskChainManager.newChain()
																			.asyncFirst(() -> this.db.writeProperty(uuid, DatabaseProperty.AUTHED, false))
																			.syncLast(deauthed -> {
																				if(deauthed) {
																					if(player.isOnline()) {
																						cache.setAuthed(false);
																						if(this.cm.isBungee()) {
																							BungeeUtil.sendMessage(player, "Add");
																						}
																					}
																					AuthUtil.alertOne(player, this.lm.getDeauthSuccess());
																					AuthUtil.notify("Deauthenticated user " + u + " (" + name + ")");
																				} else {
																					AuthUtil.alertOne(player, this.lm.getDeauthFailed());
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
																						if(this.cm.isBungee()) {
																							BungeeUtil.sendMessage(player, "Remove");
																						}
																					}
																					AuthUtil.alertOne(player, this.lm.getResetSuccess());
																					AuthUtil.notify("Reset authentication for user " + u + " (" + name + ")");
																				} else {
																					AuthUtil.alertOne(player, this.lm.getResetFailed());
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
															AuthUtil.alertOne(player, this.lm.getAuthInProgress());
														}
													} else {
														player.sendMessage(this.lm.getMustWait());
													}
												} else {
													AuthUtil.alertOne(player, this.lm.getPlayerNotAuthed());
												}
											} else {
												AuthUtil.alertOne(player, this.lm.getPlayerNotDb());
											}
										} else if(args0.equals("generate")) {
											GoogleAuthenticatorKey key = this.ga.getCreds(uuid);
											if(key == null) {
												AuthUtil.alertOne(player, this.lm.getGenerating());
												AuthUtil.notify("Generating authentication secret for user " + u + " (" + name + ")...");
												key = this.ga.generateRfc6238Credentials(uuid);
											}
											AuthUtil.alertOne(player, AuthMessage.SECRET_KEY.toString() + key.getKey());
											final TextComponent tc = new TextComponent(AuthMessage.QR.toString() + "Click me!");
											tc.setClickEvent(
													new ClickEvent(
															ClickEvent.Action.OPEN_URL,
															this.ga.getAuthUrl(this.cm.getCodeIssuer(), name, key)
													)
											);
											tc.setHoverEvent(
													new HoverEvent(
															HoverEvent.Action.SHOW_TEXT,
															new ComponentBuilder(this.lm.getPleaseSaveQr()).create()
													)
											);
											AuthUtil.alertOneTextComponent(player, tc);
										} else {
											AuthUtil.alertOne(player, this.lm.getPlayerNotDb());
										}
									})
									.execute();
							break;
						case 2:
							final OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(args[1]);
							final String target = targetOffline.getName();
							final UUID targetId = targetOffline.getUniqueId();
							final String id = targetId.toString();
							switch(args[0].toLowerCase()) {
								case "deauth":
									if(player.hasPermission("duoauth.admin")) {
										if(!(this.auths.contains(targetId))) {
											TaskChainManager.newChain()
													.asyncFirst(() -> this.db.contains(targetId)
															&& this.db.readProperty(targetId, DatabaseProperty.AUTHED).getAsBoolean()
															&& this.db.writeProperty(targetId, DatabaseProperty.AUTHED, false))
													.syncLast(deauthed -> {
														if(deauthed) {
															final AuthCache c = this.plugin.getAuthCache(targetId);
															if(c != null && targetOffline.isOnline()) {
																c.setAuthed(false);
																final Player targetOnline = (Player) targetOffline;
																if(this.cm.isBungee()) {
																	BungeeUtil.sendMessage(targetOnline, "Add");
																}
																AuthUtil.alertOne(targetOnline, this.lm.getDeauthAdminSuccess());
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
											AuthUtil.alertOne(player, this.lm.getAuthInProgressAdmin());
										}
									} else {
										player.sendMessage(this.lm.getNoPermission());
									}
									break;
								case "reset":
									this.reset(player, args[1]);
									break;
								case "check":
									if(player.hasPermission("duoauth.admin")) {
										if(!(this.auths.contains(targetId))) {
											final AuthCache c = this.plugin.getAuthCache(targetId);
											if(c != null && targetOffline.isOnline()) {
												AuthUtil.alertOne(player, AuthUtil.logCache(target, c));
											} else {
												final TaskChain chain = TaskChainManager.newChain();
												chain
														.async(() -> {
															if(this.db.contains(targetId)) {
																chain.setTaskData("password", this.db.readProperty(targetId, DatabaseProperty.PASSWORD).getAsString());
																chain.setTaskData("secret", this.db.readProperty(targetId, DatabaseProperty.SECRET).getAsString());
																chain.setTaskData("authed", this.db.readProperty(targetId, DatabaseProperty.AUTHED).getAsBoolean());
																chain.setTaskData("attempts", this.db.readProperty(targetId, DatabaseProperty.ATTEMPTS).getAsInt());
																try {
																	chain.setTaskData("ip", InetAddress.getByName(this.db.readProperty(targetId, DatabaseProperty.IP).getAsString()));
																} catch(final UnknownHostException e) {
																	e.printStackTrace();
																}
															}
														})
														.sync(() -> {
															final String password = (String) chain.getTaskData("password");
															if(password != null && (!(password.isEmpty()))) {
																AuthUtil.alertOne(
																		player,
																		AuthUtil.logCache(
																				target,
																				new AuthCache(
																						password,
																						(String) chain.getTaskData("secret"),
																						(boolean) chain.getTaskData("authed"),
																						(int) chain.getTaskData("attempts"),
																						(InetAddress) chain.getTaskData("ip")
																				)
																		)
																);
															} else {
																AuthUtil.alertOne(player, ChatColor.RED + "Failed to check user " + target + ". Has this player set up authentication?");
																AuthUtil.notify("Failed to check user " + id + " (" + target + ")");
															}
														})
														.execute();
											}
										} else {
											AuthUtil.alertOne(player, this.lm.getAuthInProgressAdmin());
										}
									} else {
										player.sendMessage(this.lm.getNoPermission());
									}
									break;
								default:
									final String password = args[0];
									final int passwordMinLength = this.cm.getPasswordMinLength();
									final boolean passwordBothCases = this.cm.isPasswordBothCases();
									final boolean passwordNumbers = this.cm.isPasswordNumbers();
									final boolean passwordSpecialChars = this.cm.isPasswordSpecialChars();
									final String passwordSpecialCharset = this.cm.getPasswordSpecialCharset();
									if(password.length() >= passwordMinLength
											&& password.matches(
													(passwordBothCases ? "(?=.*[A-Z])(?=.*[a-z])" : "(?=.*[A-Za-z])") +
															(passwordNumbers ? "(?=.*[0-9])" : "") +
															(passwordSpecialChars ? "(?=.*[" + passwordSpecialCharset + "])" : "") +
															"(?=\\S+$).*$"
									)) {
										final String code = args[1];
										if(code.length() == 6 && code.matches("^[0-9]+$")) {
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
																	chain.setTaskData("secret", cache.getSecret());
																	chain.setTaskData("attempts", cache.getAttempts());
																}
															})
															.async(() -> {
																if(!(this.db.contains(uuid))) {
																	final GoogleAuthenticatorKey key = this.ga.getCreds(uuid);
																	if(key != null) {
																		final String secret = key.getKey();
																		if(!(this.ga.authorize(secret, Integer.parseInt(code)))) {
																			chain.setTaskData("written", false);
																			return;
																		}
																		TaskChainManager.newChain()
																				.sync(() -> {
																					AuthUtil.alertOne(player, this.lm.getSettingUp());
																					AuthUtil.notify("Setting up authentication for user " + u + " (" + name + ")...");
																				})
																				.execute();
																		final String digest = AuthUtil.getSecureBCryptHash(
																				AuthUtil.getSecureSHA512Hash(password),
																				costFactor
																		);
																		chain.setTaskData("digest", digest);
																		chain.setTaskData("secret", secret);
																		chain.setTaskData("written", this.db.write(uuid, digest, secret, true, 0, ip));
																	} else {
																		TaskChainManager.newChain().sync(() -> AuthUtil.alertOne(player, this.lm.getGenerate())).execute();
																	}
																} else {
																	int attempts = (chain.hasTaskData("attempts"))
																			? (int) chain.getTaskData("attempts")
																			: this.db.readProperty(uuid, DatabaseProperty.ATTEMPTS).getAsInt();
																	if(commandAttempts != 0 && attempts >= commandAttempts) {
																		TaskChainManager.newChain()
																				.sync(() -> {
																					AuthUtil.alertOne(player, this.lm.getLocked());
																					AuthUtil.notify("User " + u + " (" + name + ") has failed authentication " + attempts + " times");
																				})
																				.delay(7, TimeUnit.SECONDS)
																				.sync(() -> player.kickPlayer(this.lm.getLocked()))
																				.execute();
																	} else {
																		TaskChainManager.newChain()
																				.sync(() -> {
																					AuthUtil.alertOne(player, this.lm.getAuthenticating());
																					AuthUtil.notify("Authenticating user " + u + " (" + name + ")...");
																				})
																				.execute();
																		final String digest = (chain.hasTaskData("password")
																				? (String) chain.getTaskData("password")
																				: this.db.readProperty(uuid, DatabaseProperty.PASSWORD).getAsString()
																		);
																		final String secret = (chain.hasTaskData("secret")
																				? (String) chain.getTaskData("secret")
																				: this.db.readProperty(uuid, DatabaseProperty.SECRET).getAsString()
																		);
																		final boolean bcrypt = Bcrypt.checkpw(AuthUtil.getSecureSHA512Hash(password), digest);
																		chain.setTaskData("bcrypt", bcrypt);
																		final boolean rfc6238 = this.ga.authorize(secret, Integer.parseInt(code));
																		chain.setTaskData("rfc6238", rfc6238);
																		chain.setTaskData("result", bcrypt && rfc6238);
																	}
																}
															})
															.sync(() -> {
																if(chain.hasTaskData("written")) {
																	if((boolean) chain.getTaskData("written")) {
																		if(player.isOnline()) {
																			this.plugin.setAuthCache(
																					uuid,
																					new AuthCache(
																							(String) chain.getTaskData("digest"),
																							(String) chain.getTaskData("secret"),
																							true,
																							0,
																							ip
																					)
																			);
																		}
																		AuthUtil.alertOne(player, this.lm.getSettingUpSuccess());
																		AuthUtil.notify("User " + u + " (" + name + ") successfully set up authentication");
																		this.ga.removeCreds(uuid);
																	} else {
																		AuthUtil.alertOne(player, this.lm.getSettingUpFailed());
																		AuthUtil.notify("User " + u + " (" + name + ") failed to set up authentication");
																	}
																} else if(chain.hasTaskData("result")) {
																	if((boolean) chain.getTaskData("result")) {
																		AuthUtil.alertOne(player, this.lm.getAuthenticatingSuccess());
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
																			cache.setAttempts(0);
																		}
																		this.ga.removeCreds(uuid);
																		if(this.cm.isBungee()) {
																			BungeeUtil.sendMessage(player, "Remove");
																		}
																		AuthUtil.notify("User " + u + " (" + name + ") authenticated successfully");
																	} else {
																		AuthUtil.alertOne(player, this.lm.getAuthenticatingFailed());
																		final String error = this.getError(chain);
																		if(!(player.hasPermission("duoauth.unlimited"))) {
																			TaskChainManager.newChain()
																					.syncFirst(() -> {
																						if(cache != null && player.isOnline()) {
																							cache.setAuthed(false);
																							if(this.cm.isBungee()) {
																								BungeeUtil.sendMessage(player, "Add");
																							}
																							return cache.getAttempts();
																						}
																						if(chain.hasTaskData("attempts")) {
																							return (int) chain.getTaskData("attempts");
																						}
																						return -1;
																					})
																					.async(attempts -> {
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
																						return attempts;
																					})
																					.syncLast(attempts -> AuthUtil.notify("User " + u + " (" + name + ") failed authentication (" + attempts + " attempts)" + error))
																					.execute();
																		} else {
																			AuthUtil.notify("User " + u + " (" + name + ") failed authentication" + error);
																		}
																	}
																}
																this.auths.remove(uuid);
															})
															.execute();
												} else {
													AuthUtil.alertOne(player, this.lm.getAuthInProgress());
												}
											} else {
												player.sendMessage(this.lm.getMustWait());
											}
										} else {
											AuthUtil.alertOne(player, this.lm.getCodeInvalid());
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
																? ", 1 special character (" + passwordSpecialCharset + ")."
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
					sender.sendMessage(this.lm.getNoPermission());
				}
			} else if(this.cm.isConsoleReset() && args.length == 2 && args[0].toLowerCase().equals("reset")) {
				this.reset(sender, args[1]);
			} else {
				sender.sendMessage(this.lm.getNoConsole());
			}
		}
		return true;
	}

	private String getError(final TaskChain chain) {
		if(chain.hasTaskData("bcrypt") && chain.hasTaskData("rfc6238")) {
			final boolean bcrypt = (boolean) chain.getTaskData("bcrypt");
			final boolean rfc6238 = (boolean) chain.getTaskData("rfc6238");
			if((!(bcrypt)) && (!(rfc6238))) {
				return " (bad password and code)";
			} else if(!(bcrypt)) {
				return " (bad password)";
			} else if(!(rfc6238)) {
				return " (bad code)";
			} else {
				return " (?)";
			}
		}
		return "";
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
									final Player targetOnline = (Player) targetOffline;
									if(this.cm.isBungee()) {
										BungeeUtil.sendMessage(targetOnline, "Remove");
									}
									AuthUtil.alertOne(targetOnline, this.lm.getResetAdminSuccess());
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
				final String msg = this.lm.getAuthInProgressAdmin();
				if(console) {
					AuthUtil.console(msg);
				} else {
					AuthUtil.alertOne((Player) sender, msg);
				}
			}
		} else {
			sender.sendMessage(this.lm.getNoPermission());
		}
	}

	private void printUsage(final Player player) {
		player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "---------------------------------------------");
		player.sendMessage("    " + DuoAuth.PREFIX + ChatColor.DARK_GRAY + "v" + this.plugin.getDescription().getVersion() + " by foncused");
		player.sendMessage(ChatColor.RED + "    /auth help" + ChatColor.GRAY + " - view this message");
		player.sendMessage(ChatColor.RED + "    /auth generate" + ChatColor.GRAY + " - generate authentication secret");
		player.sendMessage(ChatColor.RED + "    /auth deauth" + ChatColor.GRAY + " - deauthenticate yourself");
		final boolean admin = player.hasPermission("duoauth.admin");
		if(admin) {
			player.sendMessage(ChatColor.RED + "    /auth deauth <player>" + ChatColor.GRAY + " - deauthenticate a player");
		}
		player.sendMessage(ChatColor.RED + "    /auth reset" + ChatColor.GRAY + " - reset your own credentials");
		if(admin) {
			player.sendMessage(ChatColor.RED + "    /auth reset <player>" + ChatColor.GRAY + " - reset a player's credentials");
			player.sendMessage(ChatColor.RED + "    /auth check <player>" + ChatColor.GRAY + " - check a player's auth status");
		}
		player.sendMessage(ChatColor.RED + "    /auth <password> <code>" + ChatColor.GRAY + " - set up or attempt authentication");
		player.sendMessage(ChatColor.DARK_GRAY + "" + ChatColor.STRIKETHROUGH + "---------------------------------------------");
	}

}
