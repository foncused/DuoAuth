package me.foncused.duoauth.runnable;

import me.foncused.duoauth.DuoAuth;
import me.foncused.duoauth.database.AuthDatabase;
import me.foncused.duoauth.utility.AuthUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AuthRunnable {

	private Map<String, Boolean> players;
	private DuoAuth plugin;
	private AuthDatabase db;
	private int deauthTimeout;
	private int deauthTimeoutCheckInterval;
	private boolean deauthTimeoutOnline;

	public AuthRunnable(final Map<String, Boolean> players, final DuoAuth plugin, final AuthDatabase db, final int deauthTimeout, final int deauthTimeoutCheckInterval, final boolean deauthTimeoutOnline) {
		this.players = players;
		this.plugin = plugin;
		this.db = db;
		this.deauthTimeout = deauthTimeout;
		this.deauthTimeoutCheckInterval = deauthTimeoutCheckInterval;
		this.deauthTimeoutOnline = deauthTimeoutOnline;
	}

	public void runTimeoutTask() {
		new BukkitRunnable() {
			public void run() {
				final Set<String> uuids = db.readAll();
				if(uuids != null && (!(uuids.isEmpty()))) {
					uuids.forEach(uuid -> {
						final String timestamp = db.readTimestamp(uuid);
						if(timestamp != null) {
							final double days = deauthTimeout / 24.0;
							if(db.readAuthed(uuid) && getTimeDifference(timestamp, db.getDateFormat(), days * 2073600000) >= days) {
								final OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
								final String name = player.getName();
								final String notify = "Authentication for user " + uuid + " (" + name + ") has expired";
								AuthUtilities.console(notify);
								db.writeAuthed(uuid, false);
								if(deauthTimeoutOnline && players.containsKey(uuid) && player.isOnline()) {
									AuthUtilities.alertOne(
											(Player) player,
											ChatColor.RED + "Authentication has expired. Please use the /auth command to reauthenticate. Thank you!"
									);
									AuthUtilities.notify(notify);
									players.put(uuid, false);
								}
							}
						}
					});
				}
			}
		}.runTaskTimerAsynchronously(this.plugin, 0,  this.deauthTimeoutCheckInterval * 60 * 20);
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
