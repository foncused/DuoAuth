package me.foncused.duoauth;

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

public class DuoAuth extends JavaPlugin {

	private Map<String, Boolean> players = Collections.synchronizedMap(new HashMap<>());
	private ConfigManager cm;
	private AuthDatabase db;

	@Override
	public void onEnable() {
		this.loadDependencies();
		this.registerConfig();
		this.registerDatabase();
		this.registerCommands();
		this.registerEvents();
		this.registerRunnables();
	}

	private void loadDependencies() {
		new TaskChainManager(this);
	}

	private void registerConfig() {
		this.saveDefaultConfig();
		final FileConfiguration config = this.getConfig();
		this.cm = new ConfigManager(
				config.getInt("cost-factor"),
				config.getInt("command.cooldown"),
				config.getInt("command.attempts"),
				config.getString("password.default"),
				config.getInt("password.min-length"),
				config.getBoolean("password.both-cases"),
				config.getBoolean("password.numbers"),
				config.getBoolean("password.special-chars"),
				config.getString("pin.default"),
				config.getInt("pin.min-length"),
				config.getString("database"),
				config.getBoolean("deauth.ip-changes"),
				config.getInt("deauth.timeout"),
				config.getBoolean("deauth.timeout-online"),
				config.getInt("deauth.timeout-check-heartbeat")
		);
	}

	private void registerCommands() {
		this.getCommand("auth").setExecutor(new AuthCommand(this));
	}

	private void registerDatabase() {
		this.db = new AuthDatabase(this);
	}

	private void registerEvents() {
		final PluginManager pm = Bukkit.getPluginManager();
		pm.registerEvents(new AsyncPlayerPreLogin(this.getConfigManager(), this.getDatabase()), this);
		pm.registerEvents(new Auth(this.getPlayers()), this);
		pm.registerEvents(new PlayerJoin(this.getPlayers(), this.getConfigManager(), this.getDatabase()), this);
		pm.registerEvents(new PlayerLogin(this.getConfigManager()), this);
		pm.registerEvents(new PlayerQuit(this.getPlayers()), this);
	}

	private void registerRunnables() {
		new AuthRunnable(this).runTimeoutTask();
	}

	public Map<String, Boolean> getPlayers() {
		return this.players;
	}

	public ConfigManager getConfigManager() {
		return this.cm;
	}

	public AuthDatabase getDatabase() {
		return this.db;
	}
}
