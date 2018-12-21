package me.foncused.duoauth.event.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;

public class PlayerQuit implements Listener {

	private Map<String, Boolean> players;

	public PlayerQuit(final Map<String, Boolean> players) {
		this.players = players;
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		this.players.remove(event.getPlayer().getUniqueId().toString());
	}

}
