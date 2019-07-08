package me.foncused.duoauth.util;

import me.foncused.duoauth.DuoAuth;
import me.foncused.duoauth.config.LangManager;
import me.foncused.duoauth.lib.jeremyh.Bcrypt;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AuthUtil {

	private static DuoAuth plugin;
	private static LangManager lm;
	private static final String PREFIX = "[" + ChatColor.DARK_GRAY + "Duo" + ChatColor.GREEN + "Auth" + ChatColor.RESET + "] ";
	private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss:SSS";

	public AuthUtil(final DuoAuth plugin) {
		AuthUtil.plugin = plugin;
		AuthUtil.lm = AuthUtil.plugin.getLangManager();
	}

	public static void alert(final String message) {
		Bukkit.broadcastMessage(getAlert(message));
	}

	public static void alertOne(final Player player, final String message) {
		if(player.isOnline()) {
			player.sendMessage(getAlert(message));
		}
	}

	private static String getAlert(final String message) {
		return lm.getPrefixAlert() + message;
	}

	public static void notify(final String message) {
		final String m = getNotification(message);
		Bukkit.getOnlinePlayers().stream().filter(player -> player.hasPermission("duoauth.notify")).forEach(player -> player.sendMessage(m));
		console(m);
	}

	public static void notifyOne(final Player player, final String message) {
		if(player.isOnline()) {
			player.sendMessage(getNotification(message));
		}
	}

	private static String getNotification(final String message) {
		return lm.getPrefixNotify() + message;
	}

	public static void console(final String message) {
		Bukkit.getConsoleSender().sendMessage(PREFIX + message);
	}

	public static void consoleWarning(final String message) {
		Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.RED + "WARNING: " + message);
	}

	public static void consoleSevere(final String message) {
		Bukkit.getConsoleSender().sendMessage(PREFIX + ChatColor.DARK_RED + "SEVERE: " + message);
	}

	public static String getDateFormat() {
		return DATE_FORMAT;
	}

	public static String getFormattedTime(final String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	public static String getSecureBCryptHash(final String password, final int costFactor) {
		return Bcrypt.hashpw(password, Bcrypt.gensalt(costFactor));
	}

	public static InetAddress getPlayerAddress(final Player player) {
		return player.getAddress().getAddress();
	}

}
