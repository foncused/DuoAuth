package me.foncused.duoauth.spigot.util;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.foncused.duoauth.spigot.DuoAuth;
import org.bukkit.entity.Player;

public class BungeeUtil {

	private static DuoAuth plugin;

	public BungeeUtil(final DuoAuth plugin) {
		BungeeUtil.plugin = plugin;
	}

	public static void sendMessage(final Player player, final String action) {
		try {
			if(player != null && player.isOnline()) {
				final ByteArrayDataOutput bado = ByteStreams.newDataOutput();
				bado.writeUTF(action);
				bado.writeUTF(player.getUniqueId().toString());
				player.sendPluginMessage(plugin, "duoauth:filter", bado.toByteArray());
				return;
			}
			failed(player);
		} catch(final Exception e) {
			failed(player);
			e.printStackTrace();
		}
	}

	private static void failed(final Player player) {
		AuthUtil.consoleWarning(
				"Failed to send plugin message to BungeeCord. This means that certain proxy-level commands may " +
						"not be blocked for " + player.getName() + " (" + player.getUniqueId() + ") " +
						"if they have been granted permission to execute them in your BungeeCord config.yml file (" +
						"e.g. /server <server>, /send <player> <server>) which may be used to bypass authentication."
		);
	}

}
