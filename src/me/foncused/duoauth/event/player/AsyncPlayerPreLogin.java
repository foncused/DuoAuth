package me.foncused.duoauth.event.player;

import me.foncused.duoauth.config.ConfigManager;
import me.foncused.duoauth.database.AuthDatabase;
import me.foncused.duoauth.enumerable.AuthMessage;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import static org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result.KICK_OTHER;

public class AsyncPlayerPreLogin implements Listener {

	private ConfigManager cm;
	private AuthDatabase db;

	public AsyncPlayerPreLogin(final ConfigManager cm, final AuthDatabase db) {
		this.cm = cm;
		this.db = db;
	}

	@EventHandler
	public void onAsyncPlayerPreLogin(final AsyncPlayerPreLoginEvent event) {
		final String uuid = event.getUniqueId().toString();
		if(this.db.contains(uuid)) {
			final int commandAttempts = this.cm.getCommandAttempts();
			if(commandAttempts != 0 && this.db.readAttempts(uuid) >= commandAttempts) {
				event.disallow(KICK_OTHER, AuthMessage.LOCKED.toString());
				return;
			}
			final String ip = event.getAddress().getHostAddress();
			if(this.cm.isDeauthAddressChanges() && (!(this.db.readAddress(uuid).equals(ip)))) {
				this.db.writeAuthed(uuid, false);
			}
		}
	}

}
