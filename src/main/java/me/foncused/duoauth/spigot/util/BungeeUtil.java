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
			}
		} catch(final Exception e) {
			e.printStackTrace();
		}
	}

}
