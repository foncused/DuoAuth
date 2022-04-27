package me.foncused.duoauth.spigot.config;

import me.foncused.duoauth.spigot.DuoAuth;
import me.foncused.duoauth.spigot.enumerable.AuthMessage;
import org.bukkit.configuration.file.YamlConfiguration;

public class LangManager {

	private final DuoAuth plugin;
	private final YamlConfiguration yaml;
	private String allowAdminSuccess;
	private String authenticating;
	private String authenticatingFailed;
	private String authenticatingSuccess;
	private String authInProgress;
	private String authInProgressAdmin;
	private String bug;
	private String codeInvalid;
	private String deauthAdminSuccess;
	private String deauthFailed;
	private String deauthSuccess;
	private String enforced;
	private String generate;
	private String generating;
	private String kicked;
	private String loading;
	private String locked;
	private String mustWait;
	private String noConsole;
	private String noGenerate;
	private String noPermission;
	private String playerNotAuthed;
	private String playerNotDb;
	private String pleaseSaveQr;
	private String prefixAlert;
	private String prefixNotify;
	private String resetAdminSuccess;
	private String resetFailed;
	private String resetSuccess;
	private String sessionExpired;
	private String settingUp;
	private String settingUpFailed;
	private String settingUpSuccess;

	public LangManager(final DuoAuth plugin) {
		this(plugin, null);
	}

	public LangManager(final DuoAuth plugin, final YamlConfiguration yaml) {
		this.plugin = plugin;
		this.yaml = yaml;
	}

	public void validate() {
		this.allowAdminSuccess = this.plugin.translate(this.yaml.getString("allow_admin_success", AuthMessage.ALLOW_ADMIN_SUCCESS.toString()));
		this.authenticating = this.plugin.translate(this.yaml.getString("authenticating", AuthMessage.AUTHENTICATING.toString()));
		this.authenticatingFailed = this.plugin.translate(this.yaml.getString("authenticating_failed", AuthMessage.AUTHENTICATING_FAILED.toString()));
		this.authenticatingSuccess = this.plugin.translate(this.yaml.getString("authenticating_success", AuthMessage.AUTHENTICATING_SUCCESS.toString()));
		this.authInProgress = this.plugin.translate(this.yaml.getString("auth_in_progress", AuthMessage.AUTH_IN_PROGRESS.toString()));
		this.authInProgressAdmin = this.plugin.translate(this.yaml.getString("auth_in_progress_admin", AuthMessage.AUTH_IN_PROGRESS_ADMIN.toString()));
		this.bug = this.plugin.translate(this.yaml.getString("bug", AuthMessage.BUG.toString()));
		this.codeInvalid = this.plugin.translate(this.yaml.getString("code_invalid", AuthMessage.CODE_INVALID.toString()));
		this.deauthAdminSuccess = this.plugin.translate(this.yaml.getString("deauth_admin_success", AuthMessage.DEAUTH_ADMIN_SUCCESS.toString()));
		this.deauthFailed = this.plugin.translate(this.yaml.getString("deauth_failed", AuthMessage.DEAUTH_FAILED.toString()));
		this.deauthSuccess = this.plugin.translate(this.yaml.getString("deauth_success", AuthMessage.DEAUTH_SUCCESS.toString()));
		this.enforced = this.plugin.translate(this.yaml.getString("enforced", AuthMessage.ENFORCED.toString()));
		this.generate = this.plugin.translate(this.yaml.getString("generate", AuthMessage.GENERATE.toString()));
		this.generating = this.plugin.translate(this.yaml.getString("generating", AuthMessage.GENERATING.toString()));
		this.kicked = this.plugin.translate(this.yaml.getString("kicked", AuthMessage.KICKED.toString()));
		this.loading = this.plugin.translate(this.yaml.getString("loading", AuthMessage.LOADING.toString()));
		this.locked = this.plugin.translate(this.yaml.getString("locked", AuthMessage.LOCKED.toString()));
		this.mustWait = this.plugin.translate(this.yaml.getString("must_wait", AuthMessage.MUST_WAIT.toString()));
		this.noConsole = this.plugin.translate(this.yaml.getString("no_console", AuthMessage.NO_CONSOLE.toString()));
		this.noGenerate = this.plugin.translate(this.yaml.getString("no_generate", AuthMessage.NO_GENERATE.toString()));
		this.noPermission = this.plugin.translate(this.yaml.getString("no_permission", AuthMessage.NO_PERMISSION.toString()));
		this.playerNotAuthed = this.plugin.translate(this.yaml.getString("player_not_authed", AuthMessage.PLAYER_NOT_AUTHED.toString()));
		this.playerNotDb = this.plugin.translate(this.yaml.getString("player_not_db", AuthMessage.PLAYER_NOT_DB.toString()));
		this.pleaseSaveQr = this.plugin.translate(this.yaml.getString("please_save_qr", AuthMessage.PLEASE_SAVE_QR.toString()));
		this.prefixAlert = this.plugin.translate(this.yaml.getString("prefix_alert", AuthMessage.PREFIX_ALERT.toString()));
		this.prefixNotify = this.plugin.translate(this.yaml.getString("prefix_notify", AuthMessage.PREFIX_NOTIFY.toString()));
		this.resetAdminSuccess = this.plugin.translate(this.yaml.getString("reset_admin_success", AuthMessage.RESET_ADMIN_SUCCESS.toString()));
		this.resetFailed = this.plugin.translate(this.yaml.getString("reset_failed", AuthMessage.RESET_FAILED.toString()));
		this.resetSuccess = this.plugin.translate(this.yaml.getString("reset_success", AuthMessage.RESET_SUCCESS.toString()));
		this.sessionExpired = this.plugin.translate(this.yaml.getString("session_expired", AuthMessage.SESSION_EXPIRED.toString()));
		this.settingUp = this.plugin.translate(this.yaml.getString("setting_up", AuthMessage.SETTING_UP.toString()));
		this.settingUpFailed = this.plugin.translate(this.yaml.getString("setting_up_failed", AuthMessage.SETTING_UP_FAILED.toString()));
		this.settingUpSuccess = this.plugin.translate(this.yaml.getString("setting_up_success", AuthMessage.SETTING_UP_SUCCESS.toString()));
	}

	public String getAllowAdminSuccess() {
		return this.yaml != null ? this.allowAdminSuccess : this.plugin.translate(AuthMessage.ALLOW_ADMIN_SUCCESS.toString());
	}

	public String getAuthenticating() {
		return this.yaml != null ? this.authenticating : this.plugin.translate(AuthMessage.AUTHENTICATING.toString());
	}

	public String getAuthenticatingFailed() {
		return this.yaml != null ? this.authenticatingFailed : this.plugin.translate(AuthMessage.AUTHENTICATING_FAILED.toString());
	}

	public String getAuthenticatingSuccess() {
		return this.yaml != null ? this.authenticatingSuccess : this.plugin.translate(AuthMessage.AUTHENTICATING_SUCCESS.toString());
	}

	public String getAuthInProgress() {
		return this.yaml != null ? this.authInProgress : this.plugin.translate(AuthMessage.AUTH_IN_PROGRESS.toString());
	}

	public String getAuthInProgressAdmin() {
		return this.yaml != null ? this.authInProgressAdmin : this.plugin.translate(AuthMessage.AUTH_IN_PROGRESS_ADMIN.toString());
	}

	public String getBug() {
		return this.yaml != null ? this.bug : this.plugin.translate(AuthMessage.BUG.toString());
	}

	public String getCodeInvalid() {
		return this.yaml != null ? this.codeInvalid : this.plugin.translate(AuthMessage.CODE_INVALID.toString());
	}

	public String getDeauthAdminSuccess() {
		return this.yaml != null ? this.deauthAdminSuccess : this.plugin.translate(AuthMessage.DEAUTH_ADMIN_SUCCESS.toString());
	}

	public String getDeauthFailed() {
		return this.yaml != null ? this.deauthFailed : this.plugin.translate(AuthMessage.DEAUTH_FAILED.toString());
	}

	public String getDeauthSuccess() {
		return this.yaml != null ? this.deauthSuccess : this.plugin.translate(AuthMessage.DEAUTH_SUCCESS.toString());
	}

	public String getEnforced() {
		return this.yaml != null ? this.enforced : this.plugin.translate(AuthMessage.ENFORCED.toString());
	}

	public String getGenerate() {
		return this.yaml != null ? this.generate : this.plugin.translate(AuthMessage.GENERATE.toString());
	}

	public String getGenerating() {
		return this.yaml != null ? this.generating : this.plugin.translate(AuthMessage.GENERATING.toString());
	}

	public String getKicked() {
		return this.yaml != null ? this.kicked : this.plugin.translate(AuthMessage.KICKED.toString());
	}

	public String getLoading() {
		return this.yaml != null ? this.loading : this.plugin.translate(AuthMessage.LOADING.toString());
	}

	public String getLocked() {
		return this.yaml != null ? this.locked : this.plugin.translate(AuthMessage.LOCKED.toString());
	}

	public String getMustWait() {
		return this.yaml != null ? this.mustWait : this.plugin.translate(AuthMessage.MUST_WAIT.toString());
	}

	public String getNoConsole() {
		return this.yaml != null ? this.noConsole : this.plugin.translate(AuthMessage.NO_CONSOLE.toString());
	}

	public String getNoGenerate() {
		return this.yaml != null ? this.noGenerate : this.plugin.translate(AuthMessage.NO_GENERATE.toString());
	}

	public String getNoPermission() {
		return this.yaml != null ? this.noPermission : this.plugin.translate(AuthMessage.NO_PERMISSION.toString());
	}

	public String getPlayerNotAuthed() {
		return this.yaml != null ? this.playerNotAuthed : this.plugin.translate(AuthMessage.PLAYER_NOT_AUTHED.toString());
	}

	public String getPlayerNotDb() {
		return this.yaml != null ? this.playerNotDb : this.plugin.translate(AuthMessage.PLAYER_NOT_DB.toString());
	}

	public String getPleaseSaveQr() {
		return this.yaml != null ? this.pleaseSaveQr : this.plugin.translate(AuthMessage.PLEASE_SAVE_QR.toString());
	}

	public String getPrefixAlert() {
		return this.yaml != null ? this.prefixAlert : this.plugin.translate(AuthMessage.PREFIX_ALERT.toString());
	}

	public String getPrefixNotify() {
		return this.yaml != null ? this.prefixNotify : this.plugin.translate(AuthMessage.PREFIX_NOTIFY.toString());
	}

	public String getResetAdminSuccess() {
		return this.yaml != null ? this.resetAdminSuccess : this.plugin.translate(AuthMessage.RESET_ADMIN_SUCCESS.toString());
	}

	public String getResetFailed() {
		return this.yaml != null ? this.resetFailed : this.plugin.translate(AuthMessage.RESET_FAILED.toString());
	}

	public String getResetSuccess() {
		return this.yaml != null ? this.resetSuccess : this.plugin.translate(AuthMessage.RESET_SUCCESS.toString());
	}

	public String getSessionExpired() {
		return this.yaml != null ? this.sessionExpired : this.plugin.translate(AuthMessage.SESSION_EXPIRED.toString());
	}

	public String getSettingUp() {
		return this.yaml != null ? this.settingUp : this.plugin.translate(AuthMessage.SETTING_UP.toString());
	}

	public String getSettingUpFailed() {
		return this.yaml != null ? this.settingUpFailed : this.plugin.translate(AuthMessage.SETTING_UP_FAILED.toString());
	}

	public String getSettingUpSuccess() {
		return this.yaml != null ? this.settingUpSuccess : this.plugin.translate(AuthMessage.SETTING_UP_SUCCESS.toString());
	}

}
