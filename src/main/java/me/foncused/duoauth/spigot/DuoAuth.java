package me.foncused.duoauth.spigot;

import com.google.common.io.ByteStreams;
import me.foncused.duoauth.spigot.cache.AuthCache;
import me.foncused.duoauth.spigot.command.AuthCommand;
import me.foncused.duoauth.spigot.config.ConfigManager;
import me.foncused.duoauth.spigot.config.LangManager;
import me.foncused.duoauth.spigot.database.AuthDatabase;
import me.foncused.duoauth.spigot.enumerable.AuthMessage;
import me.foncused.duoauth.spigot.event.auth.Auth;
import me.foncused.duoauth.spigot.event.player.AsyncPlayerPreLogin;
import me.foncused.duoauth.spigot.event.player.PlayerJoin;
import me.foncused.duoauth.spigot.event.player.PlayerLogin;
import me.foncused.duoauth.spigot.event.player.PlayerQuit;
import me.foncused.duoauth.spigot.lib.aikar.TaskChainManager;
import me.foncused.duoauth.spigot.lib.foncused.AuthFilter;
import me.foncused.duoauth.spigot.lib.wstrange.GoogleAuth;
import me.foncused.duoauth.spigot.runnable.AuthRunnable;
import me.foncused.duoauth.spigot.util.AuthUtil;
import me.foncused.duoauth.spigot.util.BungeeUtil;
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
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DuoAuth extends JavaPlugin {

	private Map<UUID, AuthCache> players;
	private ConfigManager cm;
	private LangManager lm;
	private GoogleAuth ga;
	private AuthDatabase db;
	public static final String PREFIX = "[" + ChatColor.DARK_GRAY + "Duo" + ChatColor.GREEN + "Auth" + ChatColor.RESET + "] ";

	@Override
	public void onEnable() {
		this.registerPlayers();
		this.loadDependencies();
		this.registerLogFilter();
		this.registerConfig();
		this.registerLang();
		this.registerGoogleAuth();
		this.registerUtils();
		this.registerBungeeCord();
		this.registerDatabase();
		this.registerCommands();
		this.registerEvents();
		this.registerRunnables();
	}

	private void registerPlayers() {
		this.players = new ConcurrentHashMap<>();
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
				config.getBoolean("bungeecord", false),
				config.getInt("cost-factor", 15),
				config.getInt("command.cooldown", 20),
				config.getInt("command.attempts", 5),
				config.getString("password.default"),
				config.getInt("password.min-length", 8),
				config.getBoolean("password.both-cases", true),
				config.getBoolean("password.numbers", true),
				config.getBoolean("password.special-chars", true),
				config.getString("password.special-charset", "@#$%^&+="),
				config.getString("code.issuer", "DuoAuth"),
				config.getBoolean("deauth.ip-changes", true),
				config.getInt("deauth.timeout", 72),
				config.getBoolean("deauth.timeout-online", false),
				config.getInt("deauth.timeout-check-heartbeat", 10),
				config.getInt("unlock.timeout", 120),
				config.getInt("unlock.timeout-check-heartbeat", 15),
				//config.getString("database", "json"),
				"json",
				config.getBoolean("chat", false),
				config.getBoolean("restrict-movement", false)
		);
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
					this.translate(yaml.getString("allow_admin_success", AuthMessage.ALLOW_ADMIN_SUCCESS.toString())),
					this.translate(yaml.getString("authenticating", AuthMessage.AUTHENTICATING.toString())),
					this.translate(yaml.getString("authenticating_failed", AuthMessage.AUTHENTICATING_FAILED.toString())),
					this.translate(yaml.getString("authenticating_success", AuthMessage.AUTHENTICATING_SUCCESS.toString())),
					this.translate(yaml.getString("auth_in_progress", AuthMessage.AUTH_IN_PROGRESS.toString())),
					this.translate(yaml.getString("auth_in_progress_admin", AuthMessage.AUTH_IN_PROGRESS_ADMIN.toString())),
					this.translate(yaml.getString("bug", AuthMessage.BUG.toString())),
					this.translate(yaml.getString("code_invalid", AuthMessage.CODE_INVALID.toString())),
					this.translate(yaml.getString("deauth_admin_success", AuthMessage.DEAUTH_ADMIN_SUCCESS.toString())),
					this.translate(yaml.getString("deauth_failed", AuthMessage.DEAUTH_FAILED.toString())),
					this.translate(yaml.getString("deauth_success", AuthMessage.DEAUTH_SUCCESS.toString())),
					this.translate(yaml.getString("enforced", AuthMessage.ENFORCED.toString())),
					this.translate(yaml.getString("generate", AuthMessage.GENERATE.toString())),
					this.translate(yaml.getString("generating", AuthMessage.GENERATING.toString())),
					this.translate(yaml.getString("kicked", AuthMessage.KICKED.toString())),
					this.translate(yaml.getString("loading", AuthMessage.LOADING.toString())),
					this.translate(yaml.getString("locked", AuthMessage.LOCKED.toString())),
					this.translate(yaml.getString("must_wait", AuthMessage.MUST_WAIT.toString())),
					this.translate(yaml.getString("no_console", AuthMessage.NO_CONSOLE.toString())),
					this.translate(yaml.getString("no_generate", AuthMessage.NO_GENERATE.toString())),
					this.translate(yaml.getString("no_permission", AuthMessage.NO_PERMISSION.toString())),
					this.translate(yaml.getString("player_not_authed", AuthMessage.PLAYER_NOT_AUTHED.toString())),
					this.translate(yaml.getString("player_not_db", AuthMessage.PLAYER_NOT_DB.toString())),
					this.translate(yaml.getString("please_save_qr", AuthMessage.PLEASE_SAVE_QR.toString())),
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
				this.translate(AuthMessage.ALLOW_ADMIN_SUCCESS.toString()),
				this.translate(AuthMessage.AUTHENTICATING.toString()),
				this.translate(AuthMessage.AUTHENTICATING_FAILED.toString()),
				this.translate(AuthMessage.AUTHENTICATING_SUCCESS.toString()),
				this.translate(AuthMessage.AUTH_IN_PROGRESS.toString()),
				this.translate(AuthMessage.AUTH_IN_PROGRESS_ADMIN.toString()),
				this.translate(AuthMessage.BUG.toString()),
				this.translate(AuthMessage.CODE_INVALID.toString()),
				this.translate(AuthMessage.DEAUTH_ADMIN_SUCCESS.toString()),
				this.translate(AuthMessage.DEAUTH_FAILED.toString()),
				this.translate(AuthMessage.DEAUTH_SUCCESS.toString()),
				this.translate(AuthMessage.ENFORCED.toString()),
				this.translate(AuthMessage.GENERATE.toString()),
				this.translate(AuthMessage.GENERATING.toString()),
				this.translate(AuthMessage.KICKED.toString()),
				this.translate(AuthMessage.LOADING.toString()),
				this.translate(AuthMessage.LOCKED.toString()),
				this.translate(AuthMessage.MUST_WAIT.toString()),
				this.translate(AuthMessage.NO_CONSOLE.toString()),
				this.translate(AuthMessage.NO_GENERATE.toString()),
				this.translate(AuthMessage.NO_PERMISSION.toString()),
				this.translate(AuthMessage.PLAYER_NOT_AUTHED.toString()),
				this.translate(AuthMessage.PLAYER_NOT_DB.toString()),
				this.translate(AuthMessage.PLEASE_SAVE_QR.toString()),
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

	private void registerGoogleAuth() {
		this.ga = new GoogleAuth();
	}

	private void registerUtils() {
		new AuthUtil(this);
		new BungeeUtil(this);
	}

	private void registerBungeeCord() {
		if(this.cm.isBungee()) {
			Bukkit.getMessenger().registerOutgoingPluginChannel(this, "duoauth:filter");
		}
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
		final AuthRunnable ar = new AuthRunnable(this);
		ar.runTimeoutTask();
		ar.runUnlockTask();
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

	public LangManager getLangManager() {
		return this.lm;
	}

	public GoogleAuth getGoogleAuth() {
		return this.ga;
	}

	public AuthDatabase getDatabase() {
		return this.db;
	}

	private String translate(final String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

}
