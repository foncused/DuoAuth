package me.foncused.duoauth.event.player;

import me.foncused.duoauth.DuoAuth;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuit implements Listener {

	private final DuoAuth plugin;

	public PlayerQuit(final DuoAuth plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		this.plugin.removePlayer(event.getPlayer().getUniqueId());
	}

}
