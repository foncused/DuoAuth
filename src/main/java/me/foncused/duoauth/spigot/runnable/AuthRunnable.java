package me.foncused.duoauth.spigot.runnable;

import me.foncused.duoauth.spigot.DuoAuth;
import me.foncused.duoauth.spigot.config.ConfigManager;
import me.foncused.duoauth.spigot.config.LangManager;
import me.foncused.duoauth.spigot.database.AuthDatabase;
import me.foncused.duoauth.spigot.enumerable.DatabaseProperty;
import me.foncused.duoauth.spigot.lib.aikar.TaskChainManager;
import me.foncused.duoauth.spigot.util.AuthUtil;
import me.foncused.duoauth.spigot.util.BungeeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public class AuthRunnable {

	private final DuoAuth plugin;
	private final ConfigManager cm;
	private final LangManager lm;
	private final AuthDatabase db;

	public AuthRunnable(final DuoAuth plugin) {
		this.plugin = plugin;
		this.cm = this.plugin.getConfigManager();
		this.lm = this.plugin.getLangManager();
		this.db = this.plugin.getDatabase();
	}

	public void runTimeoutTask() {
		new BukkitRunnable() {
			@Override
			public void run() {
				final Set<UUID> uuids = db.readAll();
				if(uuids != null && (!(uuids.isEmpty()))) {
					uuids.forEach(uuid -> {
						final String timestamp = db.readProperty(uuid, DatabaseProperty.TIMESTAMP).getAsString();
						if(timestamp != null
								&& getTimeDifference(timestamp, AuthUtil.getDateFormat()) >= (cm.getDeauthTimeout() / 24.0)
								&& db.readProperty(uuid, DatabaseProperty.AUTHED).getAsBoolean()) {
							final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
							final String name = player.getName();
							db.writeProperty(uuid, DatabaseProperty.AUTHED, false);
							TaskChainManager.newChain()
									.sync(() -> {
										final String notify = "Authentication for user " + uuid + " (" + name + ") has expired";
										AuthUtil.console(notify);
										if(cm.isDeauthTimeoutOnline() && plugin.containsPlayer(uuid) && player.isOnline()) {
											plugin.getAuthCache(uuid).setAuthed(false);
											final Player online = (Player) player;
											if(cm.isBungee()) {
												BungeeUtil.sendMessage(online, "Add");
											}
											AuthUtil.alertOne(online, lm.getSessionExpired());
											AuthUtil.notify(notify);
										}
									})
									.execute();
						}
					});
				}
			}
		}.runTaskTimerAsynchronously(this.plugin, 0, this.cm.getDeauthTimeoutCheckHeartbeat() * 60 * 20);
	}

	public void runUnlockTask() {
		final int unlockTimeout = this.cm.getUnlockTimeout();
		if(!(unlockTimeout <= 0)) {
			new BukkitRunnable() {
				@Override
				public void run() {
					final Set<UUID> uuids = db.readAll();
					if(uuids != null && (!(uuids.isEmpty()))) {
						uuids.forEach(uuid -> {
							final String timestamp = db.readProperty(uuid, DatabaseProperty.TIMESTAMP).getAsString();
							if(timestamp != null
									&& getTimeDifference(timestamp, AuthUtil.getDateFormat()) >= (unlockTimeout / 24.0)
									&& db.readProperty(uuid, DatabaseProperty.ATTEMPTS).getAsInt() >= cm.getCommandAttempts()) {
								final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
								final String name = player.getName();
								db.writeProperty(uuid, DatabaseProperty.ATTEMPTS, 0);
								AuthUtil.notify("Authentication for user " + uuid + " (" + name + ") is now unlocked");
							}
						});
					}
				}
			}.runTaskTimerAsynchronously(this.plugin, 0, this.cm.getUnlockTimeoutCheckHeartbeat() * 60 * 20);
		}
	}

	private double getTimeDifference(final String date, final String format) {
		final DateFormat formatter = new SimpleDateFormat(format);
		try {
			return ((formatter.parse(formatter.format(new Date())).getTime() - formatter.parse(date).getTime()) / 86400000.0);
		} catch(final ParseException e) {
			return 0;
		}
	}

}
