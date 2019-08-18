package me.foncused.duoauth;

import com.google.common.io.ByteStreams;
import me.foncused.duoauth.cache.AuthCache;
import me.foncused.duoauth.command.AuthCommand;
import me.foncused.duoauth.config.ConfigManager;
import me.foncused.duoauth.config.LangManager;
import me.foncused.duoauth.database.AuthDatabase;
import me.foncused.duoauth.enumerable.AuthMessage;
import me.foncused.duoauth.event.auth.Auth;
import me.foncused.duoauth.event.player.AsyncPlayerPreLogin;
import me.foncused.duoauth.event.player.PlayerJoin;
import me.foncused.duoauth.event.player.PlayerLogin;
import me.foncused.duoauth.event.player.PlayerQuit;
import me.foncused.duoauth.lib.aikar.TaskChainManager;
import me.foncused.duoauth.lib.foncused.AuthFilter;
import me.foncused.duoauth.lib.wstrange.GoogleAuth;
import me.foncused.duoauth.runnable.AuthRunnable;
import me.foncused.duoauth.util.AuthUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class DuoAuth extends JavaPlugin {

	private Map<UUID, AuthCache> players;
	private ConfigManager cm;
	private GoogleAuth ga;
	private LangManager lm;
	private AuthDatabase db;

	@Override
	public void onEnable() {
		this.registerPlayers();
		this.loadDependencies();
		this.registerLogFilter();
		this.registerConfig();
		this.registerGoogleAuth();
		this.registerLang();
		this.registerUtils();
		this.registerDatabase();
		this.registerCommands();
		this.registerEvents();
		this.registerRunnables();
	}

	private void registerPlayers() {
		this.players = Collections.synchronizedMap(new HashMap<>());
	}

	private void loadDependencies() {
		new TaskChainManager(this);
	}

	private void registerLogFilter() {
		((Logger) LogManager.getRootLogger()).addFilter(new AuthFilter());
	}

	private void registerConfig() {
		this.saveDefaultConfig();
		final FileConfiguration config = this.getConfig();
		this.cm = new ConfigManager(
				config.getInt("cost-factor", 14),
				config.getInt("command.cooldown", 20),
				config.getInt("command.attempts", 5),
				config.getString("password.default"),
				config.getInt("password.min-length", 8),
				config.getBoolean("password.both-cases", true),
				config.getBoolean("password.numbers", true),
				config.getBoolean("password.special-chars", true),
				//config.getString("database", "json"),
				"json",
				config.getBoolean("deauth.ip-changes", true),
				config.getInt("deauth.timeout", 72),
				config.getBoolean("deauth.timeout-online", false),
				config.getInt("deauth.timeout-check-heartbeat", 10),
				config.getBoolean("console-reset", false),
				config.getBoolean("chat", false),
				config.getBoolean("restrict-movement", false)
		);
	}

	private void registerGoogleAuth() {
		this.ga = new GoogleAuth();
	}

	private void registerLang() {
		final String name = "lang.yml";
		final String path = this.getDataFolder().getPath() + "/" + name;
		try {
			final File lang = new File(path);
			if(!(lang.exists())) {
				ByteStreams.copy(
						Objects.requireNonNull(this.getResource(name)),
						new FileOutputStream(lang)
				);
			}
			final YamlConfiguration yaml = new YamlConfiguration();
			yaml.load(lang);
			this.lm = new LangManager(
					this.translate(yaml.getString("authenticating", AuthMessage.AUTHENTICATING.toString())),
					this.translate(yaml.getString("authenticating_failed", AuthMessage.AUTHENTICATING_FAILED.toString())),
					this.translate(yaml.getString("authenticating_success", AuthMessage.AUTHENTICATING_SUCCESS.toString())),
					this.translate(yaml.getString("auth_in_progress", AuthMessage.AUTH_IN_PROGRESS.toString())),
					this.translate(yaml.getString("auth_in_progress_admin", AuthMessage.AUTH_IN_PROGRESS_ADMIN.toString())),
					this.translate(yaml.getString("code_invalid", AuthMessage.CODE_INVALID.toString())),
					this.translate(yaml.getString("deauth_admin_success", AuthMessage.DEAUTH_ADMIN_SUCCESS.toString())),
					this.translate(yaml.getString("deauth_failed", AuthMessage.DEAUTH_FAILED.toString())),
					this.translate(yaml.getString("deauth_success", AuthMessage.DEAUTH_SUCCESS.toString())),
					this.translate(yaml.getString("enforced", AuthMessage.ENFORCED.toString())),
					this.translate(yaml.getString("generate", AuthMessage.GENERATE.toString())),
					this.translate(yaml.getString("generating", AuthMessage.GENERATING.toString())),
					this.translate(yaml.getString("loading", AuthMessage.LOADING.toString())),
					this.translate(yaml.getString("locked", AuthMessage.LOCKED.toString())),
					this.translate(yaml.getString("must_wait", AuthMessage.MUST_WAIT.toString())),
					this.translate(yaml.getString("no_console", AuthMessage.NO_CONSOLE.toString())),
					this.translate(yaml.getString("no_generate", AuthMessage.NO_GENERATE.toString())),
					this.translate(yaml.getString("no_permission", AuthMessage.NO_PERMISSION.toString())),
					this.translate(yaml.getString("player_not_authed", AuthMessage.PLAYER_NOT_AUTHED.toString())),
					this.translate(yaml.getString("player_not_db", AuthMessage.PLAYER_NOT_DB.toString())),
					this.translate(yaml.getString("prefix_alert", AuthMessage.PREFIX_ALERT.toString())),
					this.translate(yaml.getString("prefix_notify", AuthMessage.PREFIX_NOTIFY.toString())),
					this.translate(yaml.getString("reset_admin_success", AuthMessage.RESET_ADMIN_SUCCESS.toString())),
					this.translate(yaml.getString("reset_failed", AuthMessage.RESET_FAILED.toString())),
					this.translate(yaml.getString("reset_success", AuthMessage.RESET_SUCCESS.toString())),
					this.translate(yaml.getString("session_expired", AuthMessage.SESSION_EXPIRED.toString())),
					this.translate(yaml.getString("setting_up", AuthMessage.SETTING_UP.toString())),
					this.translate(yaml.getString("setting_up_failed", AuthMessage.SETTING_UP_FAILED.toString())),
					this.translate(yaml.getString("setting_up_success", AuthMessage.SETTING_UP_SUCCESS.toString()))
			);
			return;
		} catch(final IOException e) {
			AuthUtil.consoleSevere("Unable to create file " + path);
			e.printStackTrace();
		} catch(final InvalidConfigurationException e) {
			AuthUtil.consoleWarning("Unable to parse " + path + " due to invalid YAML");
			e.printStackTrace();
		}
		this.lm = new LangManager(
				this.translate(AuthMessage.AUTHENTICATING.toString()),
				this.translate(AuthMessage.AUTHENTICATING_FAILED.toString()),
				this.translate(AuthMessage.AUTHENTICATING_SUCCESS.toString()),
				this.translate(AuthMessage.AUTH_IN_PROGRESS.toString()),
				this.translate(AuthMessage.AUTH_IN_PROGRESS_ADMIN.toString()),
				this.translate(AuthMessage.CODE_INVALID.toString()),
				this.translate(AuthMessage.DEAUTH_ADMIN_SUCCESS.toString()),
				this.translate(AuthMessage.DEAUTH_FAILED.toString()),
				this.translate(AuthMessage.DEAUTH_SUCCESS.toString()),
				this.translate(AuthMessage.ENFORCED.toString()),
				this.translate(AuthMessage.GENERATE.toString()),
				this.translate(AuthMessage.GENERATING.toString()),
				this.translate(AuthMessage.LOADING.toString()),
				this.translate(AuthMessage.LOCKED.toString()),
				this.translate(AuthMessage.MUST_WAIT.toString()),
				this.translate(AuthMessage.NO_CONSOLE.toString()),
				this.translate(AuthMessage.NO_GENERATE.toString()),
				this.translate(AuthMessage.NO_PERMISSION.toString()),
				this.translate(AuthMessage.PLAYER_NOT_AUTHED.toString()),
				this.translate(AuthMessage.PLAYER_NOT_DB.toString()),
				this.translate(AuthMessage.PREFIX_ALERT.toString()),
				this.translate(AuthMessage.PREFIX_NOTIFY.toString()),
				this.translate(AuthMessage.RESET_ADMIN_SUCCESS.toString()),
				this.translate(AuthMessage.RESET_FAILED.toString()),
				this.translate(AuthMessage.RESET_SUCCESS.toString()),
				this.translate(AuthMessage.SESSION_EXPIRED.toString()),
				this.translate(AuthMessage.SETTING_UP.toString()),
				this.translate(AuthMessage.SETTING_UP_FAILED.toString()),
				this.translate(AuthMessage.SETTING_UP_SUCCESS.toString())
		);
	}

	private void registerUtils() {
		new AuthUtil(this);
	}

	private void registerDatabase() {
		this.db = new AuthDatabase(this);
	}

	private void registerCommands() {
		this.getCommand("auth").setExecutor(new AuthCommand(this));
	}

	private void registerEvents() {
		final PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new AsyncPlayerPreLogin(this), this);
		pm.registerEvents(new Auth(this), this);
		pm.registerEvents(new PlayerJoin(this), this);
		pm.registerEvents(new PlayerLogin(this), this);
		pm.registerEvents(new PlayerQuit(this), this);
	}

	private void registerRunnables() {
		new AuthRunnable(this).runTimeoutTask();
	}

	public AuthCache getAuthCache(final UUID uuid) {
		return this.players.get(uuid);
	}

	public void setAuthCache(final UUID uuid, final AuthCache cache) {
		this.players.put(uuid, cache);
	}

	public boolean containsPlayer(final UUID uuid) {
		return this.players.containsKey(uuid);
	}

	public void removePlayer(final UUID uuid) {
		this.players.remove(uuid);
	}

	public ConfigManager getConfigManager() {
		return this.cm;
	}

	public GoogleAuth getGoogleAuth() {
		return this.ga;
	}

	public LangManager getLangManager() {
		return this.lm;
	}

	public AuthDatabase getDatabase() {
		return this.db;
	}

	private String translate(final String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

}
