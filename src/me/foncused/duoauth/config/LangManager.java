package me.foncused.duoauth.config;

public class LangManager {

	private final String prefixAlert;
	private final String prefixNotify;
	private final String playerNotAuthed;
	private final String playerNotDb;
	private final String noConsole;
	private final String mustWait;
	private final String noPermission;
	private final String locked;
	private final String authInProgress;
	private final String authInProgressAdmin;
	private final String sessionExpired;
	private final String deauthSuccess;
	private final String deauthFailed;
	private final String deauthAdminSuccess;
	private final String resetSuccess;
	private final String resetFailed;
	private final String resetAdminSuccess;
	private final String settingUp;
	private final String settingUpSuccess;
	private final String settingUpFailed;
	private final String authenticating;
	private final String authenticatingSuccess;
	private final String authenticatingFailed;
	private final String enforced;
	private final String loading;

	public LangManager(
		final String prefixAlert,
		final String prefixNotify,
		final String playerNotAuthed,
		final String playerNotDb,
		final String noConsole,
		final String mustWait,
		final String noPermission,
		final String locked,
		final String authInProgress,
		final String authInProgressAdmin,
		final String sessionExpired,
		final String deauthSuccess,
		final String deauthFailed,
		final String deauthAdminSuccess,
		final String resetSuccess,
		final String resetFailed,
		final String resetAdminSuccess,
		final String settingUp,
		final String settingUpSuccess,
		final String settingUpFailed,
		final String authenticating,
		final String authenticatingSuccess,
		final String authenticatingFailed,
		final String enforced,
		final String loading
	) {
		this.prefixAlert = prefixAlert;
		this.prefixNotify = prefixNotify;
		this.playerNotAuthed = playerNotAuthed;
		this.playerNotDb = playerNotDb;
		this.noConsole = noConsole;
		this.mustWait = mustWait;
		this.noPermission = noPermission;
		this.locked = locked;
		this.authInProgress = authInProgress;
		this.authInProgressAdmin = authInProgressAdmin;
		this.sessionExpired = sessionExpired;
		this.deauthSuccess = deauthSuccess;
		this.deauthFailed = deauthFailed;
		this.deauthAdminSuccess = deauthAdminSuccess;
		this.resetSuccess = resetSuccess;
		this.resetFailed = resetFailed;
		this.resetAdminSuccess = resetAdminSuccess;
		this.settingUp = settingUp;
		this.settingUpSuccess = settingUpSuccess;
		this.settingUpFailed = settingUpFailed;
		this.authenticating = authenticating;
		this.authenticatingSuccess = authenticatingSuccess;
		this.authenticatingFailed = authenticatingFailed;
		this.enforced = enforced;
		this.loading = loading;
	}

	public synchronized String getPrefixAlert() {
		return this.prefixAlert;
	}

	public synchronized String getPrefixNotify() {
		return this.prefixNotify;
	}

	public synchronized String getPlayerNotAuthed() {
		return this.playerNotAuthed;
	}

	public synchronized String getPlayerNotDb() {
		return this.playerNotDb;
	}

	public synchronized String getNoConsole() {
		return this.noConsole;
	}

	public synchronized String getMustWait() {
		return this.mustWait;
	}

	public synchronized String getNoPermission() {
		return this.noPermission;
	}

	public synchronized String getLocked() {
		return this.locked;
	}

	public synchronized String getAuthInProgress() {
		return this.authInProgress;
	}

	public synchronized String getAuthInProgressAdmin() {
		return this.authInProgressAdmin;
	}

	public synchronized String getSessionExpired() {
		return this.sessionExpired;
	}

	public synchronized String getDeauthSuccess() {
		return this.deauthSuccess;
	}

	public synchronized String getDeauthFailed() {
		return this.deauthFailed;
	}

	public synchronized String getDeauthAdminSuccess() {
		return this.deauthAdminSuccess;
	}

	public synchronized String getResetSuccess() {
		return this.resetSuccess;
	}

	public synchronized String getResetFailed() {
		return this.resetFailed;
	}

	public synchronized String getResetAdminSuccess() {
		return this.resetAdminSuccess;
	}

	public synchronized String getSettingUp() {
		return this.settingUp;
	}

	public synchronized String getSettingUpSuccess() {
		return this.settingUpSuccess;
	}

	public synchronized String getSettingUpFailed() {
		return this.settingUpFailed;
	}

	public synchronized String getAuthenticating() {
		return this.authenticating;
	}

	public synchronized String getAuthenticatingSuccess() {
		return this.authenticatingSuccess;
	}

	public synchronized String getAuthenticatingFailed() {
		return this.authenticatingFailed;
	}

	public synchronized String getEnforced() {
		return this.enforced;
	}

	public synchronized String getLoading() {
		return this.loading;
	}

}
