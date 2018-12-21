package me.foncused.duoauth.utility;

import me.foncused.duoauth.lib.jeremyh.Bcrypt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class DuoAuthUtilities {

	private static int costFactor = 14;
	private static final String PREFIX = "[DuoAuth] ";

	public DuoAuthUtilities(final int costFactor) {
		DuoAuthUtilities.costFactor = costFactor;
	}

	public static void alert(final String message) {
		Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "ALERT " + ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + message);
	}

	public static void alertOne(final Player player, final String message) {
		if(player.isOnline()) {
			player.sendMessage(ChatColor.DARK_AQUA + "ALERT " + ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + message);
		}
	}

	public static void notify(String message) {
		final String m = ChatColor.RED + "ALERT " + ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + ChatColor.ITALIC + message;
		Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("duoauth.notify")).forEach(player -> player.sendMessage(m));
		console(m);
	}

	public static void notifyOne(final Player player, final String message) {
		if(player.isOnline()) {
			player.sendMessage(ChatColor.RED + "ALERT " + ChatColor.DARK_GRAY + "» " + ChatColor.GRAY + ChatColor.ITALIC + message);
		}
	}

	public static void console(final String message) {
		Bukkit.getLogger().info(ChatColor.stripColor(PREFIX + message));
	}

	public static void consoleWarning(final String message) {
		Bukkit.getLogger().warning(ChatColor.stripColor(PREFIX + message));
	}

	public static void consoleSevere(final String message) {
		Bukkit.getLogger().severe(ChatColor.stripColor(PREFIX + message));
	}

	public static String getSecureBCryptHash(final String password) {
		return Bcrypt.hashpw(password, Bcrypt.gensalt(costFactor));
	}

	public static String getPlayerAddress(final Player player) {
		return player.getAddress().toString().replaceAll("/", "").split(":")[0];
	}

}
