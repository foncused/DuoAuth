package me.foncused.duoauth.enumerable;

public enum AuthMessage {

	PREFIX_ALERT("&3ALERT &8» &7"),
	PREFIX_NOTIFY("&cALERT &8» &7&o"),

	PLAYER_NOT_AUTHED("&cYou need to authenticate with /auth <password> <pin> before you can chat, move, or play."),
	PLAYER_NOT_DB("&cYou have not set up authentication. To enable authentication, please use the /auth command."),
	MUST_WAIT("&cYou must wait before doing that again."),
	NO_PERMISSION("&cYou do not have permission to do this!"),
	LOCKED("&cYour account has been locked. You will need to wait for your account to be unlocked, or you may contact the server administrators for assistance."),
	AUTH_IN_PROGRESS("&7Authentication in progress - please be patient..."),
	AUTH_IN_PROGRESS_ADMIN("&cThat user is currently performing an authentication attempt. Please try again in a moment."),
	SESSION_EXPIRED("&cYour session has expired. Please use the /auth command to continue playing. Thank you!"),
	DEAUTH_SUCCESS("&aYour have deauthenticated successfully. To continue playing, please use the &c/auth &acommand."),
	DEAUTH_FAILED("&cDeauthentication failed. Please contact the server administrators if you are receiving this message.");

	private final String message;

	AuthMessage(final String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return this.message;
	}

}
