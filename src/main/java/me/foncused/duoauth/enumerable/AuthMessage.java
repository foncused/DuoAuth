package me.foncused.duoauth.enumerable;

public enum AuthMessage {

	AUTHENTICATING("&6Authenticating..."),
	AUTHENTICATING_FAILED("&cAuthentication failed. Please ensure your password and code are correct. Please contact the server administrators if you believe that this is in error."),
	AUTHENTICATING_SUCCESS("&aAuthentication successful. Have fun!"),
	AUTH_IN_PROGRESS("&7Authentication in progress - please be patient..."),
	AUTH_IN_PROGRESS_ADMIN("&cThat user is currently performing an authentication attempt. Please try again in a moment."),
	CODE_INVALID("&cThe code you entered is invalid. Your code must be a 6-digits and numeric."),
	DEAUTH_ADMIN_SUCCESS("&cYou have been deauthenticated by an administrator. Please use the /auth command to continue playing. Thank you!"),
	DEAUTH_FAILED("&cDeauthentication failed. Please contact the server administrators if you are receiving this message."),
	DEAUTH_SUCCESS("&aYour have deauthenticated successfully. To continue playing, please use the &c/auth &acommand."),
	ENFORCED("&cThe server administrator has required you to set up authentication. Please scan the QR shown or enter the secret key into your mobile authentication app. The 6-digit code shown in the app will then be used for performing authentication with '/auth <password> <code>'. The default password must be given to you before you can proceed with these instructions. Once you are authenticated, you may use '/auth reset' and '/auth generate' to set your own credentials. Thank you!"),
	GENERATE("&cYou need to enter '/auth generate' to generate an authentication secret for your code. Once generated, scan the QR shown or enter the secret key into your mobile authentication app. The 6-digit code shown in the app will then be used for performing authentication with '/auth <password> <code>'."),
	GENERATING("&6Generating authentication secret..."),
	LOADING("&8Duo&aAuth &cis still loading. Please try again in a moment."),
	LOCKED("&cYour account has been locked. You will need to wait for your account to be unlocked, or you may contact the server administrators for assistance."),
	MUST_WAIT("&cYou must wait before doing that again."),
	NO_CONSOLE("&cYou cannot do this from the console."),
	NO_GENERATE("&cYou are already authenticated, so there is no need to generate a shared secret at this time."),
	NO_PERMISSION("&cYou do not have permission to do this!"),
	PLAYER_NOT_AUTHED("&cYou need to authenticate with /auth <password> <code> before you can chat, move, or play."),
	PLAYER_NOT_DB("&cYou have not set up authentication. To enable authentication, please use the /auth command."),
	PREFIX_ALERT("&3ALERT &8» &7"),
	PREFIX_NOTIFY("&cALERT &8» &7&o"),
	RESET_ADMIN_SUCCESS("&aYour credentials have been reset by an administrator."),
	RESET_FAILED("&cFailed to reset authentication. Please contact the server administrators if you are receiving this message."),
	RESET_SUCCESS("&aYour credentials have been reset! To re-enable authentication, please use the &c/auth &acommand."),
	SESSION_EXPIRED("&cYour session has expired. Please use the /auth command to continue playing. Thank you!"),
	SETTING_UP("&6Setting up authentication..."),
	SETTING_UP_FAILED("&cFailed to set up authentication. Please verify your code is correct, or contact the server administrators if you are receiving this message."),
	SETTING_UP_SUCCESS("&aYour credentials have been set!");

	private final String message;

	AuthMessage(final String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return this.message;
	}

}
