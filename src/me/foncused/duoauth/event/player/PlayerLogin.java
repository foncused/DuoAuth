package me.foncused.duoauth.event.player;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER;

public class PlayerLogin implements Listener {

	private boolean loading = true;

	public PlayerLogin(final boolean loading) {
		this.loading = loading;
	}

	public void setLoading(final boolean loading) {
		this.loading = loading;
	}

	@EventHandler
	public void onPlayerLogin(final PlayerLoginEvent event) {
		if(this.loading) {
			event.disallow(KICK_OTHER, ChatColor.RED + "DuoAuth is still loading. Please try again in a moment.");
		}

	}
}
