package me.foncused.duoauth.spigot.event.player;

import me.foncused.duoauth.spigot.DuoAuth;
import me.foncused.duoauth.spigot.config.ConfigManager;
import me.foncused.duoauth.spigot.config.LangManager;
import me.foncused.duoauth.spigot.database.AuthDatabase;
import me.foncused.duoauth.spigot.enumerable.DatabaseProperty;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_OTHER;

public class AsyncPlayerPreLogin implements Listener {

	private final ConfigManager cm;
	private final LangManager lm;
	private final AuthDatabase db;

	public AsyncPlayerPreLogin(final DuoAuth plugin) {
		this.cm = plugin.getConfigManager();
		this.lm = plugin.getLangManager();
		this.db = plugin.getDatabase();
	}

	@EventHandler
	public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent event) {
		final UUID uuid = event.getUniqueId();
		if(this.db.contains(uuid)) {
			final int commandAttempts = this.cm.getCommandAttempts();
			if(commandAttempts != 0 && this.db.readProperty(uuid, DatabaseProperty.ATTEMPTS).getAsInt() >= commandAttempts) {
				event.disallow(KICK_OTHER, this.lm.getLocked());
				return;
			}
			if(this.cm.isDeauthAddressChanges()
					&& (!(this.db.readProperty(uuid, DatabaseProperty.IP).getAsString().equals(event.getAddress().getHostAddress())))) {
				this.db.writeProperty(uuid, DatabaseProperty.AUTHED, false);
			}
		}
	}

}
