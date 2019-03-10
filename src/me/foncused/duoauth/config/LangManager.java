package me.foncused.duoauth.config;

public class LangManager {

	private final String prefixAlert;
	private final String prefixNotify;
	private final String playerNotAuthed;
	private final String playerNotDb;
	private final String mustWait;
	private final String noPermission;
	private final String locked;
	private final String authInProgress;
	private final String authInProgressAdmin;
	private final String sessionExpired;
	private final String deauthSuccess;
	private final String deauthFailed;

	public LangManager(
		final String prefixAlert,
		final String prefixNotify,
		final String playerNotAuthed,
		final String playerNotDb,
		final String mustWait,
		final String noPermission,
		final String locked,
		final String authInProgress,
		final String authInProgressAdmin,
		final String sessionExpired,
		final String deauthSuccess,
		final String deauthFailed
	) {
		this.prefixAlert = prefixAlert;
		this.prefixNotify = prefixNotify;
		this.playerNotAuthed = playerNotAuthed;
		this.playerNotDb = playerNotDb;
		this.mustWait = mustWait;
		this.noPermission = noPermission;
		this.locked = locked;
		this.authInProgress = authInProgress;
		this.authInProgressAdmin = authInProgressAdmin;
		this.sessionExpired = sessionExpired;
		this.deauthSuccess = deauthSuccess;
		this.deauthFailed = deauthFailed;
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

}
