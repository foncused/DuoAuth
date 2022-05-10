package me.foncused.duoauth.spigot.config;

import me.foncused.duoauth.spigot.enumerable.DatabaseOption;
import me.foncused.duoauth.spigot.lib.aikar.TaskChainManager;
import me.foncused.duoauth.spigot.util.AuthUtil;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {

	private final FileConfiguration config;
	private boolean bungeecord;
	private int costFactor;
	private int commandCooldown;
	private int commandAttempts;
	private String passwordDefault;
	private int passwordMinLength;
	private boolean passwordBothCases;
	private boolean passwordNumbers;
	private boolean passwordSpecialChars;
	private String passwordSpecialCharset;
	private String codeIssuer;
	private boolean deauthAddressChanges;
	private int deauthTimeout;
	private boolean deauthTimeoutOnline;
	private int deauthTimeoutCheckHeartbeat;
	private int unlockTimeout;
	private int unlockTimeoutCheckHeartbeat;
	private DatabaseOption databaseOption;
	private boolean chat;
	private boolean restrictMovement;
	private boolean loading;

	public ConfigManager(final FileConfiguration config) {
		this.config = config;
	}

	public void validate() {

		this.loading = true;

		// bungeecord
		this.bungeecord = this.config.getBoolean("bungeecord", false);
		AuthUtil.console(this.bungeecord ? "BungeeCord mode is enabled" : "BungeeCord mode is disabled");

		// cost-factor
		final int costFactor = this.config.getInt("cost-factor", 15);
		if(costFactor < 12) {
			this.costFactor = 12;
			AuthUtil.consoleWarning("Bcrypt cost factor set to " + costFactor + " is too low, reverting to minimum...");
		} else if(costFactor > 30) {
			this.costFactor = 30;
			AuthUtil.consoleWarning("Bycrypt cost factor set to " + costFactor + " is too high, reverting to maximum...");
		} else {
			this.costFactor = costFactor;
		}
		AuthUtil.console("Bcrypt cost factor set to " + this.costFactor);

		// command.cooldown
		final int commandCooldown = this.config.getInt("command.cooldown", 20);
		if(commandCooldown <= 0) {
			this.commandCooldown = 20;
			AuthUtil.consoleWarning("Command cooldown time set to " + commandCooldown + " seconds is not safe, reverting to default...");
		} else {
			this.commandCooldown = commandCooldown;
		}
		AuthUtil.console("Command cooldown time set to " + this.commandCooldown + " seconds");

		// command.attempts
		final int commandAttempts = this.config.getInt("command.attempts", 5);
		if(commandAttempts < 0) {
			this.commandAttempts = 10;
			AuthUtil.consoleWarning("Maximum authentication attempts set to " + commandAttempts + " is not safe, reverting to default...");
		} else {
			this.commandAttempts = commandAttempts;
		}
		AuthUtil.console(
				this.commandAttempts != 0
						? "Maximum authentication attempts set to " + this.commandAttempts + " attempts"
						: "Maximum authentication attempts check disabled"
		);

		// password.min-length
		final int passwordMinLength = this.config.getInt("password.min-length", 8);
		if(passwordMinLength <= 0) {
			this.passwordMinLength = 8;
			AuthUtil.consoleWarning("Minimum password length set to " + passwordMinLength + " is not safe, reverting to default...");
		} else {
			this.passwordMinLength = passwordMinLength;
		}
		AuthUtil.console("Minimum password length set to " + this.passwordMinLength);

		// password.both-cases
		this.passwordBothCases = this.config.getBoolean("password.both-cases", true);
		AuthUtil.console(this.passwordBothCases ? "Both cases required" : "Both cases not required");

		// password.numbers
		this.passwordNumbers = this.config.getBoolean("password.numbers", true);
		AuthUtil.console(this.passwordNumbers ? "Numbers required" : "Numbers not required");

		// password.special-chars
		this.passwordSpecialChars = this.config.getBoolean("password.special-chars", true);
		AuthUtil.console(this.passwordSpecialChars ? "Special characters required" : "Special characters not required");

		// password.special-charset
		final String passwordSpecialCharset = this.config.getString("password.special-charset", "@#$%^&+=");
		this.passwordSpecialCharset = passwordSpecialCharset.isEmpty() ? "@#$%^&+=" : AuthUtil.removeDuplicateChars(passwordSpecialCharset);
		AuthUtil.console("Special charset set to " + this.passwordSpecialCharset);

		// code.issuer
		final String codeIssuer = this.config.getString("code.issuer", "DuoAuth");
		this.codeIssuer = codeIssuer.isEmpty() ? "DuoAuth" : codeIssuer;
		AuthUtil.console("Code issuer set to " + this.codeIssuer);

		// deauth.ip-changes
		this.deauthAddressChanges = this.config.getBoolean("deauth.ip-changes", true);
		AuthUtil.console(this.deauthAddressChanges ? "IP address check enabled" : "IP address check disabled");

		// deauth.timeout
		final int deauthTimeout = this.config.getInt("deauth.timeout", 72);
		if(deauthTimeout <= 0) {
			this.deauthTimeout = 48;
			AuthUtil.consoleWarning("Deauth timeout set to " + deauthTimeout + " hours is not safe, reverting to default...");
		} else {
			this.deauthTimeout = deauthTimeout;
		}
		AuthUtil.console("Deauth timeout set to " + this.deauthTimeout + " hours");

		// deauth.timeout-online
		this.deauthTimeoutOnline = this.config.getBoolean("deauth.timeout-online", false);
		AuthUtil.console(this.deauthTimeoutOnline ? "Deauth timeout online mode enabled" : "Deauth timeout online mode disabled");

		// deauth.timeout-check-heartbeat
		final int deauthTimeoutCheckHeartbeat = this.config.getInt("deauth.timeout-check-heartbeat", 10);
		if(deauthTimeoutCheckHeartbeat <= 0) {
			this.deauthTimeoutCheckHeartbeat = 10;
			AuthUtil.consoleWarning("Deauth timeout check heartbeat set to " + deauthTimeoutCheckHeartbeat + " minutes is not safe, reverting to default...");
		} else {
			this.deauthTimeoutCheckHeartbeat = deauthTimeoutCheckHeartbeat;
		}
		AuthUtil.console("Deauth timeout check heartbeat set to " + this.deauthTimeoutCheckHeartbeat + " minutes");

		// unlock.timeout
		final int unlockTimeout = this.config.getInt("unlock.timeout", 120);
		if(unlockTimeout < 0) {
			this.unlockTimeout = 120;
			AuthUtil.consoleWarning("Unlock timeout set to " + unlockTimeout + " hours is not safe, reverting to default...");
		} else {
			this.unlockTimeout = unlockTimeout;
		}
		AuthUtil.console("Unlock timeout set to " + this.unlockTimeout + " hours");

		// unlock.timeout-check-heartbeat
		final int unlockTimeoutCheckHeartbeat = this.config.getInt("unlock.timeout-check-heartbeat", 15);
		if(unlockTimeoutCheckHeartbeat <= 0) {
			this.unlockTimeoutCheckHeartbeat = 15;
			AuthUtil.consoleWarning("Unlock timeout check heartbeat set to " + unlockTimeoutCheckHeartbeat + " minutes is not safe, reverting to default...");
		} else {
			this.unlockTimeoutCheckHeartbeat = unlockTimeoutCheckHeartbeat;
		}
		AuthUtil.console("Unlock timeout check heartbeat set to " + this.unlockTimeoutCheckHeartbeat + " minutes");

		// database
		//final String database = this.config.getString("database", "json");
		final String database = "json";
		DatabaseOption databaseOption;
		try {
			databaseOption = DatabaseOption.valueOf(database.toUpperCase());
		} catch(final IllegalArgumentException e) {
			databaseOption = DatabaseOption.JSON;
			AuthUtil.consoleWarning("Database option set to " + database + " is not safe, reverting...");
		}
		this.databaseOption = databaseOption;
		AuthUtil.console("Database option set to " + this.databaseOption);

		// chat
		this.chat = this.config.getBoolean("chat", false);
		AuthUtil.console(this.chat ? "Chat is enabled" : "Chat is disabled");

		// restrict-movement
		this.restrictMovement = this.config.getBoolean("restrict-movement", false);
		AuthUtil.console(this.restrictMovement ? "Movement is restricted" : "Movement is not restricted");

		// password.default
		final String passwordDefault = this.config.getString("password.default", "Password1234#");
		AuthUtil.console("Calculating default password digest at cost factor " + this.costFactor + " (this may take a moment)...");
		TaskChainManager.newChain()
				.asyncFirst(() -> AuthUtil.getSecureBCryptHash(AuthUtil.getSecureSHA512Hash(passwordDefault), this.costFactor))
				.syncLast(passwordDigest -> {
					this.passwordDefault = passwordDigest;
					AuthUtil.console("Default password digest for 'duoauth.enforced' is " + this.passwordDefault);
					this.loading = false;
				})
				.execute();

	}

	public boolean isBungee() {
		return this.bungeecord;
	}

	public int getCostFactor() {
		return this.costFactor;
	}

	public int getCommandCooldown() {
		return this.commandCooldown;
	}

	public int getCommandAttempts() {
		return this.commandAttempts;
	}

	public String getPasswordDefault() {
		return this.passwordDefault;
	}

	public int getPasswordMinLength() {
		return this.passwordMinLength;
	}

	public boolean isPasswordBothCases() {
		return this.passwordBothCases;
	}

	public boolean isPasswordNumbers() {
		return this.passwordNumbers;
	}

	public boolean isPasswordSpecialChars() {
		return this.passwordSpecialChars;
	}

	public String getPasswordSpecialCharset() {
		return this.passwordSpecialCharset;
	}

	public String getCodeIssuer() {
		return this.codeIssuer;
	}

	public boolean isDeauthAddressChanges() {
		return this.deauthAddressChanges;
	}

	public int getDeauthTimeout() {
		return this.deauthTimeout;
	}

	public boolean isDeauthTimeoutOnline() {
		return this.deauthTimeoutOnline;
	}

	public int getDeauthTimeoutCheckHeartbeat() {
		return this.deauthTimeoutCheckHeartbeat;
	}

	public int getUnlockTimeout() {
		return this.unlockTimeout;
	}

	public int getUnlockTimeoutCheckHeartbeat() {
		return this.unlockTimeoutCheckHeartbeat;
	}

	DatabaseOption getDatabaseOption() {
		return this.databaseOption;
	}

	public boolean isChat() {
		return this.chat;
	}

	public boolean isRestrictMovement() {
		return this.restrictMovement;
	}

	public boolean isLoading() {
		return this.loading;
	}

}
