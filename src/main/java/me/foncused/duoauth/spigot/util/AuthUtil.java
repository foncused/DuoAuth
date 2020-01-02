package me.foncused.duoauth.spigot.util;

import me.foncused.duoauth.spigot.DuoAuth;
import me.foncused.duoauth.spigot.cache.AuthCache;
import me.foncused.duoauth.spigot.config.LangManager;
import me.foncused.duoauth.spigot.lib.jeremyh.Bcrypt;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class AuthUtil {

	private static DuoAuth plugin;
	private static LangManager lm;
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

	public static void alertOneTextComponent(final Player player, final TextComponent component) {
		if(player.isOnline()) {
			component.setText(getAlert(component.getText()));
			player.spigot().sendMessage(component);
		}
	}

	private static String getAlert(final String message) {
		return lm.getPrefixAlert() + message;
	}

	public static void notify(final String message) {
		final String m = getNotification(message);
		Bukkit.getOnlinePlayers().stream()
				.filter(player -> player.hasPermission("duoauth.notify"))
				.forEach(player -> player.sendMessage(m));
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
		Bukkit.getConsoleSender().sendMessage(DuoAuth.PREFIX + message);
	}

	public static void consoleWarning(final String message) {
		Bukkit.getConsoleSender().sendMessage(DuoAuth.PREFIX + ChatColor.RED + "WARNING: " + message);
	}

	public static void consoleSevere(final String message) {
		Bukkit.getConsoleSender().sendMessage(DuoAuth.PREFIX + ChatColor.DARK_RED + "SEVERE: " + message);
	}

	public static String logCache(final String name, final AuthCache cache) {
		final String msg = ChatColor.GOLD + "Player: " + ChatColor.GRAY + name + ChatColor.GOLD + ", " +
				"Authed: " + ChatColor.GRAY + cache.isAuthed() + ChatColor.GOLD + ", " +
				"Attempts: " + ChatColor.GRAY + cache.getAttempts() + ChatColor.GOLD + ", " +
				"IP: " + ChatColor.GRAY + cache.getIp().getHostAddress();
		console(msg);
		return msg;
	}

	public static String getDateFormat() {
		return DATE_FORMAT;
	}

	public static String getFormattedTime(final String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

	public static InetAddress getPlayerAddress(final Player player) {
		return player.getAddress().getAddress();
	}

	public static String getSecureBCryptHash(final String password, final int costFactor) {
		return Bcrypt.hashpw(password, Bcrypt.gensalt(costFactor));
	}

	public static String getSecureSHA512Hash(final String password) {
		String hash = null;
		try {
			final MessageDigest md = MessageDigest.getInstance("SHA-512");
			md.reset();
			md.update(password.getBytes("UTF-8"));
			hash = String.format("%0128x", new BigInteger(1, md.digest()));
		} catch(final Exception e) {
			e.printStackTrace();
		}
		return hash;
	}

	public static String removeDuplicateChars(final String s) {
		final Set<Character> charset = new LinkedHashSet<>();
		for(final char c : s.toCharArray()) {
			charset.add(c);
		}
		final StringBuilder sb = new StringBuilder();
		for(final Character c : charset) {
			sb.append(c);
		}
		return sb.toString();
	}

}
