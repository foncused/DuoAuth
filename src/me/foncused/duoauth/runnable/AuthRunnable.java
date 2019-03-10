package me.foncused.duoauth.runnable;

import me.foncused.duoauth.DuoAuth;
import me.foncused.duoauth.config.ConfigManager;
import me.foncused.duoauth.config.LangManager;
import me.foncused.duoauth.database.AuthDatabase;
import me.foncused.duoauth.enumerable.DatabaseProperty;
import me.foncused.duoauth.lib.aikar.TaskChainManager;
import me.foncused.duoauth.util.AuthUtil;
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
						if(timestamp != null) {
							final double days = cm.getDeauthTimeout() / 24.0;
							if(db.readProperty(uuid, DatabaseProperty.AUTHED).getAsBoolean() && getTimeDifference(timestamp, AuthUtil.getDateFormat(), 86400000) >= days) {
								final OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
								final String name = player.getName();
								final String notify = "Authentication for user " + uuid + " (" + name + ") has expired";
								db.writeProperty(uuid, DatabaseProperty.AUTHED, false);
								TaskChainManager.newChain()
										.sync(() -> {
											AuthUtil.console(notify);
											if(cm.isDeauthTimeoutOnline() && plugin.containsPlayer(uuid) && player.isOnline()) {
												plugin.getAuthCache(uuid).setAuthed(false);
												AuthUtil.alertOne(
														(Player) player,
														lm.getSessionExpired()
												);
												AuthUtil.notify(notify);
											}
										})
										.execute();
							}
						}
					});
				}
			}
		}.runTaskTimerAsynchronously(this.plugin, 0,  this.cm.getDeauthTimeoutCheckHeartbeat() * 60 * 20);
	}

	private double getTimeDifference(final String date, final String format, final double divide) {
		final DateFormat formatter = new SimpleDateFormat(format);
		try {
			return ((formatter.parse(formatter.format(new Date())).getTime() - formatter.parse(date).getTime()) / divide);
		} catch(final ParseException e) {
			return 0;
		}
	}

}
