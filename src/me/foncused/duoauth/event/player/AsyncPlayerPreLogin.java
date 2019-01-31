package me.foncused.duoauth.event.player;

import me.foncused.duoauth.DuoAuth;
import me.foncused.duoauth.config.ConfigManager;
import me.foncused.duoauth.database.AuthDatabase;
import me.foncused.duoauth.enumerable.AuthMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_OTHER;

public class AsyncPlayerPreLogin implements Listener {

	private final ConfigManager cm;
	private final AuthDatabase db;

	public AsyncPlayerPreLogin(final DuoAuth plugin) {
		this.cm = plugin.getConfigManager();
		this.db = plugin.getDatabase();
	}

	@EventHandler
	public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent event) {
		final UUID uuid = event.getUniqueId();
		if(this.db.contains(uuid)) {
			final int commandAttempts = this.cm.getCommandAttempts();
			if(commandAttempts != 0 && this.db.readAttempts(uuid) >= commandAttempts) {
				event.disallow(KICK_OTHER, AuthMessage.LOCKED.toString());
				return;
			}
			if(this.cm.isDeauthAddressChanges() && (!(this.db.readAddress(uuid).equals(event.getAddress().getHostAddress())))) {
				this.db.writeAuthed(uuid, false);
			}
		}
	}

}
