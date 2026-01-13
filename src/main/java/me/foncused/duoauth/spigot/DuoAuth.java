package me.foncused.duoauth.spigot;

import com.google.common.io.ByteStreams;
import me.foncused.duoauth.spigot.cache.AuthCache;
import me.foncused.duoauth.spigot.command.AuthCommand;
import me.foncused.duoauth.spigot.config.ConfigManager;
import me.foncused.duoauth.spigot.config.LangManager;
import me.foncused.duoauth.spigot.database.AuthDatabase;
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

	public static final String PREFIX = "[" + ChatColor.DARK_GRAY + "Duo" + ChatColor.GREEN + "Auth" + ChatColor.RESET + "] ";

	private Map<UUID, AuthCache> players;
	private ConfigManager cm;
	private LangManager lm;
	private GoogleAuth ga;
	private AuthDatabase db;

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
		this.cm = new ConfigManager(this.getConfig());
		this.cm.validate();
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
			this.lm = new LangManager(this, yaml);
			this.lm.validate();
			return;
		} catch(final IOException e) {
			AuthUtil.consoleSevere("Unable to create file " + path);
			e.printStackTrace();
		} catch(final InvalidConfigurationException e) {
			AuthUtil.consoleWarning("Unable to parse " + path + " due to invalid YAML");
			e.printStackTrace();
		}
		this.lm = new LangManager(this);
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
		Objects.requireNonNull(this.getCommand("auth")).setExecutor(new AuthCommand(this));
	}

	private void registerEvents() {
		final PluginManager pm = this.getServer().getPluginManager();
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

	public String translate(final String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

}
