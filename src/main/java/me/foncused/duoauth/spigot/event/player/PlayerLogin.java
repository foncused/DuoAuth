package me.foncused.duoauth.spigot.event.player;

import me.foncused.duoauth.spigot.DuoAuth;
import me.foncused.duoauth.spigot.config.ConfigManager;
import me.foncused.duoauth.spigot.config.LangManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import static org.bukkit.event.player.PlayerLoginEvent.Result.KICK_OTHER;

public class PlayerLogin implements Listener {

	private final DuoAuth plugin;
	private final ConfigManager cm;
	private final LangManager lm;

	public PlayerLogin(final DuoAuth plugin) {
		this.plugin = plugin;
		this.cm = this.plugin.getConfigManager();
		this.lm = this.plugin.getLangManager();
	}

	@EventHandler
	public void onPlayerLogin(final PlayerLoginEvent event) {
		if((this.cm == null || this.cm.isLoading()) && event.getPlayer().hasPermission("duoauth.enforced")) {
			event.disallow(KICK_OTHER, this.lm.getLoading());
		}
	}
}
