package me.foncused.duoauth.event.player;

import co.aikar.taskchain.TaskChain;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import me.foncused.duoauth.DuoAuth;
import me.foncused.duoauth.cache.AuthCache;
import me.foncused.duoauth.config.ConfigManager;
import me.foncused.duoauth.config.LangManager;
import me.foncused.duoauth.database.AuthDatabase;
import me.foncused.duoauth.enumerable.AuthMessage;
import me.foncused.duoauth.enumerable.DatabaseProperty;
import me.foncused.duoauth.lib.aikar.TaskChainManager;
import me.foncused.duoauth.lib.wstrange.GoogleAuth;
import me.foncused.duoauth.util.AuthUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class PlayerJoin implements Listener {

	private final DuoAuth plugin;
	private final ConfigManager cm;
	private final GoogleAuth ga;
	private final LangManager lm;
	private final AuthDatabase db;

	public PlayerJoin(final DuoAuth plugin) {
		this.plugin = plugin;
		this.cm = this.plugin.getConfigManager();
		this.ga = this.plugin.getGoogleAuth();
		this.lm = this.plugin.getLangManager();
		this.db = this.plugin.getDatabase();
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		final String name = player.getName();
		TaskChainManager.newChain()
				.asyncFirst(() -> this.db.contains(uuid))
				.syncLast(contained -> {
					if(contained) {
						final TaskChain chain = TaskChainManager.newChain();
						chain
								.async(() -> {
									chain.setTaskData("password", db.readProperty(uuid, DatabaseProperty.PASSWORD).getAsString());
									chain.setTaskData("secret", db.readProperty(uuid, DatabaseProperty.SECRET).getAsString());
									chain.setTaskData("authed", db.readProperty(uuid, DatabaseProperty.AUTHED).getAsBoolean());
									chain.setTaskData("attempts", db.readProperty(uuid, DatabaseProperty.ATTEMPTS).getAsInt());
									try {
										chain.setTaskData("ip", InetAddress.getByName(db.readProperty(uuid, DatabaseProperty.IP).getAsString()));
									} catch(final UnknownHostException e) {
										e.printStackTrace();
									}
								})
								.sync(() -> {
									final AuthCache cache =
											new AuthCache(
													(String) chain.getTaskData("password"),
													(String) chain.getTaskData("secret"),
													(boolean) chain.getTaskData("authed"),
													(int) chain.getTaskData("attempts"),
													(InetAddress) chain.getTaskData("ip")
									);
									this.log(name, cache);
									this.plugin.setAuthCache(uuid, cache);
								})
								.execute();
					} else if(player.hasPermission("duoauth.enforced")) {
						AuthUtil.alertOne(player, this.lm.getEnforced());
						final InetAddress ip = AuthUtil.getPlayerAddress(player);
						final String digest = this.cm.getPasswordDefault();
						AuthUtil.alertOne(player, this.lm.getGenerating());
						final GoogleAuthenticatorKey key = this.ga.generateRfc6238Credentials(uuid);
						final String secret = key.getKey();
						TaskChainManager.newChain()
								.asyncFirst(() -> this.db.write(
										uuid,
										digest,
										secret,
										false,
										0,
										ip
								))
								.syncLast(written -> {
									final AuthCache cache = new AuthCache(
											digest,
											secret,
											false,
											0,
											ip
									);
									this.plugin.setAuthCache(uuid, cache);
									final String u = uuid.toString();
									if(written) {
										this.log(name, cache);
										AuthUtil.notify("User " + u + " (" + name + ") has 'duoauth.enforced' and setup of default authentication was successful");
										AuthUtil.alertOne(player, AuthMessage.SECRET_KEY.toString() + key.getKey());
										AuthUtil.alertOne(player, AuthMessage.QR.toString() + this.ga.getAuthUrl(this.cm.getCodeIssuer(), name, key));
									} else {
										AuthUtil.notify("User " + u + " (" + name + ") has 'duoauth.enforced' but setup of default authentication has failed");
										player.kickPlayer(this.lm.getKicked());
									}
								})
								.execute();
					}
				})
				.execute();
	}

	private void log(final String name, final AuthCache cache) {
		AuthUtil.console(
				ChatColor.GOLD + "Player: " + ChatColor.GRAY + name + ChatColor.GOLD + ", " +
						"Authed: " + ChatColor.GRAY + cache.isAuthed() + ChatColor.GOLD + ", " +
						"Attempts: " + ChatColor.GRAY + cache.getAttempts() + ChatColor.GOLD + ", " +
						"IP: " + ChatColor.GRAY + cache.getIp().getHostAddress()
		);
	}

}
