package me.foncused.duoauth.event.player;

import co.aikar.taskchain.TaskChain;
import me.foncused.duoauth.DuoAuth;
import me.foncused.duoauth.cache.AuthCache;
import me.foncused.duoauth.config.ConfigManager;
import me.foncused.duoauth.database.AuthDatabase;
import me.foncused.duoauth.enumerable.DatabaseProperty;
import me.foncused.duoauth.lib.aikar.TaskChainManager;
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
	private final AuthDatabase db;

	public PlayerJoin(final DuoAuth plugin) {
		this.plugin = plugin;
		this.cm = this.plugin.getConfigManager();
		this.db = this.plugin.getDatabase();
	}

	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		TaskChainManager.newChain()
				.asyncFirst(() -> this.db.contains(uuid))
				.syncLast(contained -> {
					if(contained) {
						final TaskChain chain = TaskChainManager.newChain();
						chain
								.async(() -> {
									chain.setTaskData("password", db.readProperty(uuid, DatabaseProperty.PASSWORD).getAsString());
									chain.setTaskData("pin", db.readProperty(uuid, DatabaseProperty.PIN).getAsString());
									chain.setTaskData("authed", db.readProperty(uuid, DatabaseProperty.AUTHED).getAsBoolean());
									chain.setTaskData("attempts", db.readProperty(uuid, DatabaseProperty.ATTEMPTS).getAsInt());
									try {
										chain.setTaskData("ip", InetAddress.getByName(db.readProperty(uuid, DatabaseProperty.IP).getAsString()));
									} catch(final UnknownHostException e) {
										e.printStackTrace();
									}
								})
								.sync(() -> {
									plugin.setAuthCache(
											uuid,
											new AuthCache(
													(String) chain.getTaskData("password"),
													(String) chain.getTaskData("pin"),
													(boolean) chain.getTaskData("authed"),
													(int) chain.getTaskData("attempts"),
													(InetAddress) chain.getTaskData("ip")
											)
									);
								})
								.execute();
					} else if(player.hasPermission("duoauth.enforced")) {
						final InetAddress ip = AuthUtil.getPlayerAddress(player);
						TaskChainManager.newChain()
								.asyncFirst(() -> this.db.write(
										uuid,
										this.cm.getPasswordDefault(),
										this.cm.getPinDefault(),
										false,
										0,
										ip
								))
								.syncLast(written -> {
									AuthUtil.alertOne(player, ChatColor.RED + "The server administrator has required you to set up authentication. Please enter the command '/auth <password> <pin>' using the credentials given to you, and then use '/auth reset' to set your own credentials. Thank you!");
									final String name = player.getName();
									final String u = uuid.toString();
									AuthUtil.notify(
											written
													? "User " + u + "(" + name + ") has 'duoauth.enforced' and setup of default authentication was successful"
													: "User " + u + "(" + name + ") has 'duoauth.enforced' but setup of default authentication has failed"
									);
								})
								.execute();
					}
				})
				.execute();
	}

}
