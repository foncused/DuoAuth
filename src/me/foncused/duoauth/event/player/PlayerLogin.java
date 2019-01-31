package me.foncused.duoauth.event.player;

import me.foncused.duoauth.DuoAuth;
import me.foncused.duoauth.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER;

public class PlayerLogin implements Listener {

	private final ConfigManager cm;

	public PlayerLogin(final DuoAuth plugin) {
		this.cm = plugin.getConfigManager();
	}

	@EventHandler
	public void onPlayerLogin(final PlayerLoginEvent event) {
		if((this.cm == null || this.cm.isLoading()) && event.getPlayer().hasPermission("duoauth.enforced")) {
			event.disallow(KICK_OTHER, ChatColor.RED + "DuoAuth is still loading. Please try again in a moment.");
		}

	}
}
