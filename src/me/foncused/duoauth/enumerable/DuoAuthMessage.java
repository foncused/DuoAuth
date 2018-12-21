package me.foncused.duoauth.enumerable;

import org.bukkit.ChatColor;

public enum DuoAuthMessage {

	PLAYER_NOT_AUTHED(ChatColor.RED + "You need to authenticate with /auth <password> <pin> before you can chat, move, or play."),
	PLAYER_NOT_DATABASED(ChatColor.RED + "You have not set up authentication. To enable authentication, please use the /auth command."),
	MUST_WAIT(ChatColor.RED + "You must wait before doing that again."),
	NO_PERMISSION(ChatColor.RED + "You do not have permission to do this!"),
	LOCKED(ChatColor.RED + "Your account has been locked. You will need to wait for your account to be unlocked, or you may contact the server administrators for assistance.");

	private final String message;

	DuoAuthMessage(final String message) { this.message = message; }

	@Override
	public String toString() { return this.message; }

}
