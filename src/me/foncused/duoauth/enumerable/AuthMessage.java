package me.foncused.duoauth.enumerable;

public enum AuthMessage {

	PREFIX_ALERT("&3ALERT &8» &7"),
	PREFIX_NOTIFY("&cALERT &8» &7&o"),

	PLAYER_NOT_AUTHED("&cYou need to authenticate with /auth <password> <pin> before you can chat, move, or play."),
	PLAYER_NOT_DB("&cYou have not set up authentication. To enable authentication, please use the /auth command."),
	NO_CONSOLE("&cYou cannot do this from the console."),
	MUST_WAIT("&cYou must wait before doing that again."),
	NO_PERMISSION("&cYou do not have permission to do this!"),
	LOCKED("&cYour account has been locked. You will need to wait for your account to be unlocked, or you may contact the server administrators for assistance."),
	AUTH_IN_PROGRESS("&7Authentication in progress - please be patient..."),
	AUTH_IN_PROGRESS_ADMIN("&cThat user is currently performing an authentication attempt. Please try again in a moment."),
	SESSION_EXPIRED("&cYour session has expired. Please use the /auth command to continue playing. Thank you!"),
	DEAUTH_SUCCESS("&aYour have deauthenticated successfully. To continue playing, please use the &c/auth &acommand."),
	DEAUTH_FAILED("&cDeauthentication failed. Please contact the server administrators if you are receiving this message."),
	DEAUTH_ADMIN_SUCCESS("&cYou have been deauthenticated by an administrator. Please use the /auth command to continue playing. Thank you!"),
	RESET_SUCCESS("&aYour credentials have been reset! To re-enable authentication, please use the &c/auth &acommand."),
	RESET_FAILED("&cFailed to reset authentication. Please contact the server administrators if you are receiving this message."),
	RESET_ADMIN_SUCCESS("&aYour credentials have been reset by an administrator."),
	SETTING_UP("&6Setting up authentication..."),
	SETTING_UP_SUCCESS("&aYour credentials have been set!"),
	SETTING_UP_FAILED("&cFailed to set up authentication. Please contact the server administrators if you are receiving this message."),
	AUTHENTICATING("&6Authenticating..."),
	AUTHENTICATING_SUCCESS("&aAuthentication successful. Have fun!"),
	AUTHENTICATING_FAILED("&cAuthentication failed. Please ensure your password and PIN are correct. Please contact the server administrators if you believe that this is in error."),
	ENFORCED("&cThe server administrator has required you to set up authentication. Please enter the command '/auth <password> <pin>' using the credentials given to you, and then use '/auth reset' to set your own credentials. Thank you!"),
	LOADING("&cDuoAuth is still loading. Please try again in a moment.");

	private final String message;

	AuthMessage(final String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return this.message;
	}

}
