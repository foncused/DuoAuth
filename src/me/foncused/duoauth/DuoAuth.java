package me.foncused.duoauth;

import co.aikar.taskchain.TaskChain;
import me.foncused.duoauth.command.AuthCommand;
import me.foncused.duoauth.database.Database;
import me.foncused.duoauth.enumerable.DatabaseOption;
import me.foncused.duoauth.event.auth.Auth;
import me.foncused.duoauth.event.player.AsyncPlayerPreLogin;
import me.foncused.duoauth.event.player.PlayerJoin;
import me.foncused.duoauth.event.player.PlayerLogin;
import me.foncused.duoauth.event.player.PlayerQuit;
import me.foncused.duoauth.lib.aikar.TaskChainManager;
import me.foncused.duoauth.runnable.Runnable;
import me.foncused.duoauth.utility.DuoAuthUtilities;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DuoAuth extends JavaPlugin {

	private Map<String, Boolean> players = Collections.synchronizedMap(new HashMap<>());
	private FileConfiguration config;
	private Database db;

	@Override
	public void onEnable() {
		this.initialize();
		this.register();
	}

	// Config, Database, and Libraries
	private void initialize() {
		this.registerConfig();
		this.registerDatabase();
		this.registerLibraries();
	}

	private void registerConfig() {
		this.saveDefaultConfig();
		this.config = this.getConfig();
	}

	private void registerDatabase() {
		final String database = this.config.getString("database");
		DatabaseOption option;
		try {
			option = DatabaseOption.valueOf(database.toUpperCase());
		} catch(final IllegalArgumentException e) {
			DuoAuthUtilities.consoleWarning("Database option set to " + database + " is not safe, reverting...");
			option = DatabaseOption.JSON;
		}
		DuoAuthUtilities.console("Database option set to " + option.name());
		this.db = new Database(this, this.players, option);
	}

	private void registerLibraries() {
		new TaskChainManager(this);
	}

	// Utilities, Runnables, Events, and Commands
	private void register() {
		// Utilities
		int costFactor = this.config.getInt("cost-factor");
		if(costFactor < 12) {
			DuoAuthUtilities.consoleWarning("Bcrypt cost factor set to " + costFactor + " is too low, setting to minimum...");
			costFactor = 12;
		} else if(costFactor > 30) {
			DuoAuthUtilities.consoleWarning("Bycrypt cost factor set to " + costFactor + " is too high, setting to maximum...");
			costFactor = 30;
		}
		DuoAuthUtilities.console("Bcrypt cost factor set to " + costFactor);
		new DuoAuthUtilities(costFactor);
		// Runnables
		int deauthTimeout = this.config.getInt("deauth.timeout");
		if(deauthTimeout <= 0) {
			DuoAuthUtilities.consoleWarning("Deauth timeout set to " + deauthTimeout + " hours is not safe, reverting to default...");
			deauthTimeout = 48;
		}
		DuoAuthUtilities.console("Deauth timeout set to " + deauthTimeout + " hours");
		int deauthTimeoutCheckInterval = this.config.getInt("deauth.timeout-check-interval");
		if(deauthTimeoutCheckInterval <= 0) {
			DuoAuthUtilities.consoleWarning("Deauth timeout check interval set to " + deauthTimeoutCheckInterval + " minutes is not safe, reverting to default...");
			deauthTimeoutCheckInterval = 5;
		}
		DuoAuthUtilities.console("Deauth timeout check interval set to " + deauthTimeoutCheckInterval + " minutes");
		final boolean timeoutOnline = this.config.getBoolean("deauth.timeout-online");
		DuoAuthUtilities.console(timeoutOnline ? "Deauth timeout online mode activated" : "Deauth timeout online mode deactivated");
		new Runnable(
				this.players,
				this,
				this.db,
				deauthTimeout,
				deauthTimeoutCheckInterval,
				timeoutOnline
		).runTimeoutTask();
		// Events
		final PluginManager pm = Bukkit.getPluginManager();
		final PlayerLogin pl = new PlayerLogin(true);
		pm.registerEvents(pl, this);
		int commandAttempts = this.config.getInt("command.attempts");
		if(commandAttempts <= 0) {
			DuoAuthUtilities.consoleWarning("Maximum authentication attempts set to " + commandAttempts + " is not safe, reverting to default...");
			commandAttempts = 5;
		}
		DuoAuthUtilities.console("Maximum authentication attempts set to " + commandAttempts);
		final boolean deauthAddressChanges = this.config.getBoolean("deauth.ip-changes");
		DuoAuthUtilities.console("Changing IP address deauth check set to " + deauthAddressChanges);
		final AsyncPlayerPreLogin appl = new AsyncPlayerPreLogin(this.db, commandAttempts, deauthAddressChanges);
		pm.registerEvents(appl, this);
		pm.registerEvents(new Auth(this.players), this);
		final TaskChain chain = TaskChainManager.newChain();
		chain
				.sync(() -> {
					chain.setTaskData("password", this.config.getString("password.default"));
					String pin = this.config.getString("pin.default");
					if(!(pin.matches("^[0-9]+$"))) {
						pin = "1234";
					}
					chain.setTaskData("pin", pin);
				})
				.async(() -> {
					final String pwhash = DuoAuthUtilities.getSecureBCryptHash((String) chain.getTaskData("password"));
					DuoAuthUtilities.console("Default password hash for 'duoauth.enforced' is " + pwhash);
					chain.setTaskData("pwhash", pwhash);
					final String pinhash = DuoAuthUtilities.getSecureBCryptHash((String) chain.getTaskData("pin"));
					DuoAuthUtilities.console("Default PIN hash for 'duoauth.enforced' is " + pinhash);
					chain.setTaskData("pinhash", pinhash);
				})
				.sync(() -> {
					pm.registerEvents(
							new PlayerJoin(
									this.players,
									this.db,
									(String) chain.getTaskData("pwhash"),
									(String) chain.getTaskData("pinhash")
							),
							this
					);
					pm.registerEvents(new PlayerQuit(this.players), this);
					// Commands
					int commandCooldown = this.config.getInt("command.cooldown");
					if(commandCooldown <= 0) {
						DuoAuthUtilities.consoleWarning("Command cooldown time set to " + commandCooldown + " seconds is not safe, reverting to default...");
						commandCooldown = 20;
					}
					DuoAuthUtilities.console("Command cooldown time set to " + commandCooldown + " seconds");
					int passwordMinLength = this.config.getInt("password.min-length");
					if(passwordMinLength <= 0) {
						DuoAuthUtilities.consoleWarning("Minimum password length set to " + passwordMinLength + " is not safe, reverting to default...");
						passwordMinLength = 8;
					}
					DuoAuthUtilities.console("Minimum password length set to " + passwordMinLength);
					final boolean passwordBothCases = this.config.getBoolean("password.both-cases");
					DuoAuthUtilities.console(passwordBothCases ? "Both cases required" : "Both cases not required");
					final boolean passwordNumbers = this.config.getBoolean("password.numbers");
					DuoAuthUtilities.console(passwordNumbers ? "Numbers required" : "Numbers not required");
					final boolean passwordSpecialChars = this.config.getBoolean("password.special-chars");
					DuoAuthUtilities.console(passwordSpecialChars ? "Special characters required" : "Special characters not required");
					int pinMinLength = this.config.getInt("pin.min-length");
					if(pinMinLength <= 0) {
						DuoAuthUtilities.consoleWarning("Minimum PIN length set to " + pinMinLength + " is not safe, reverting to default...");
						pinMinLength = 4;
					}
					DuoAuthUtilities.console("Minimum PIN length set to " + pinMinLength);
					this.getCommand("auth").setExecutor(
							new AuthCommand(
									this,
									this.players,
									this.db,
									commandCooldown,
									appl.getCommandAttempts(),
									passwordMinLength,
									passwordBothCases,
									passwordNumbers,
									passwordSpecialChars,
									pinMinLength
							)
					);
					pl.setLoading(false);
				})
				.execute();
	}

}
