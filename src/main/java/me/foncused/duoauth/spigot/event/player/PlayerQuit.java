package me.foncused.duoauth.spigot.event.player;

import me.foncused.duoauth.spigot.DuoAuth;
import me.foncused.duoauth.spigot.lib.wstrange.GoogleAuth;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerQuit implements Listener {

	private final DuoAuth plugin;
	private final GoogleAuth ga;

	public PlayerQuit(final DuoAuth plugin) {
		this.plugin = plugin;
		this.ga = this.plugin.getGoogleAuth();
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		final UUID uuid = event.getPlayer().getUniqueId();
		this.plugin.removePlayer(uuid);
		this.ga.removeCreds(uuid);
	}

}
