package me.foncused.duoauth.spigot.event.player;

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
import me.foncused.duoauth.spigot.lib.wstrange.GoogleAuth;
import me.foncused.duoauth.spigot.util.AuthUtil;
import me.foncused.duoauth.spigot.util.BungeeUtil;
import me.foncused.duoauth.spigot.util.MapUtil;
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
	private final LangManager lm;
	private final GoogleAuth ga;
	private final AuthDatabase db;

	public PlayerJoin(final DuoAuth plugin) {
		this.plugin = plugin;
		this.cm = this.plugin.getConfigManager();
		this.lm = this.plugin.getLangManager();
		this.ga = this.plugin.getGoogleAuth();
		this.db = this.plugin.getDatabase();
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        if(event.isAsynchronous()) {
            player.kickPlayer(this.lm.getBug());
            return;
        }
		final UUID uuid = player.getUniqueId();
		final String name = player.getName();
		TaskChainManager.newChain()
				.asyncFirst(() -> this.db.contains(uuid))
				.syncLast(contained -> {
					if(contained) {
						final TaskChain chain = TaskChainManager.newChain();
						chain
								.async(() -> {
									try {
										chain.setTaskData("password", this.db.readProperty(uuid, DatabaseProperty.PASSWORD).getAsString());
									} catch(final UnsupportedOperationException e) {
										this.db.delete(uuid);
										TaskChainManager.newChain().sync(() -> player.kickPlayer(this.lm.getBug())).execute();
										chain.abortChain();
										return;
									}
									chain.setTaskData("secret", this.db.readProperty(uuid, DatabaseProperty.SECRET).getAsString());
									chain.setTaskData("authed", this.db.readProperty(uuid, DatabaseProperty.AUTHED).getAsBoolean());
									chain.setTaskData("attempts", this.db.readProperty(uuid, DatabaseProperty.ATTEMPTS).getAsInt());
									try {
										chain.setTaskData("ip", InetAddress.getByName(this.db.readProperty(uuid, DatabaseProperty.IP).getAsString()));
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
									AuthUtil.logCache(name, cache);
									this.plugin.setAuthCache(uuid, cache);
									if(this.cm.isBungee()) {
										BungeeUtil.sendMessage(player, cache.isAuthed() ? "Remove" : "Add");
									}
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
									if(this.cm.isBungee()) {
										BungeeUtil.sendMessage(player, "Add");
									}
									final String u = uuid.toString();
									if(written) {
										AuthUtil.logCache(name, cache);
										AuthUtil.notify("User " + u + " (" + name + ") has 'duoauth.enforced' and setup of default authentication was successful");
										AuthUtil.alertOne(player, AuthMessage.SECRET_KEY + key.getKey());
										AuthUtil.alertOne(player, this.lm.getScanQr());
										MapUtil.showQr(player);
									} else {
										AuthUtil.notify("User " + u + " (" + name + ") has 'duoauth.enforced' but setup of default authentication has failed");
										player.kickPlayer(this.lm.getKicked());
									}
								})
								.execute();
					} else if(this.cm.isBungee()) {
						BungeeUtil.sendMessage(player, "Remove");
					}
				})
				.execute();
	}

}
