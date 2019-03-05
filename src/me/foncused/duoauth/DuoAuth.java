package me.foncused.duoauth;

import me.foncused.duoauth.cache.AuthCache;
import me.foncused.duoauth.command.AuthCommand;
import me.foncused.duoauth.config.ConfigManager;
import me.foncused.duoauth.database.AuthDatabase;
import me.foncused.duoauth.event.auth.Auth;
import me.foncused.duoauth.event.player.AsyncPlayerPreLogin;
import me.foncused.duoauth.event.player.PlayerJoin;
import me.foncused.duoauth.event.player.PlayerLogin;
import me.foncused.duoauth.event.player.PlayerQuit;
import me.foncused.duoauth.lib.aikar.TaskChainManager;
import me.foncused.duoauth.runnable.AuthRunnable;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuoAuth extends JavaPlugin {

	private Map<UUID, AuthCache> players;
	private ConfigManager cm;
	private AuthDatabase db;

	@Override
	public void onEnable() {
		this.registerPlayers();
		this.loadDependencies();
		this.registerConfig();
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
				config.getString("pin.default"),
				config.getInt("pin.min-length", 4),
				//config.getString("database", "json"),
				"json",
				config.getBoolean("deauth.ip-changes", true),
				config.getInt("deauth.timeout", 72),
				config.getBoolean("deauth.timeout-online", false),
				config.getInt("deauth.timeout-check-heartbeat", 10),
				config.getBoolean("console-reset", false),
				config.getBoolean("chat", false)
		);
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

	public AuthDatabase getDatabase() {
		return this.db;
	}
}
