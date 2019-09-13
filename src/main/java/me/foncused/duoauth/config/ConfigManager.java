package me.foncused.duoauth.config;

import me.foncused.duoauth.enumerable.DatabaseOption;
import me.foncused.duoauth.lib.aikar.TaskChainManager;
import me.foncused.duoauth.util.AuthUtil;

public class ConfigManager {

	private final int costFactor;
	private final int commandCooldown;
	private final int commandAttempts;
	private String passwordDefault;
	private final int passwordMinLength;
	private final boolean passwordBothCases;
	private final boolean passwordNumbers;
	private final boolean passwordSpecialChars;
	private final String codeIssuer;
	private final boolean deauthAddressChanges;
	private final int deauthTimeout;
	private final boolean deauthTimeoutOnline;
	private final int deauthTimeoutCheckHeartbeat;
	private final int unlockTimeout;
	private final int unlockTimeoutCheckHeartbeat;
	private final DatabaseOption databaseOption;
	private final boolean consoleReset;
	private final boolean chat;
	private final boolean restrictMovement;
	private boolean loading;

	public ConfigManager(
		final int costFactor,
		final int commandCooldown,
		final int commandAttempts,
		final String passwordDefault,
		final int passwordMinLength,
		final boolean passwordBothCases,
		final boolean passwordNumbers,
		final boolean passwordSpecialChars,
		final String codeIssuer,
		final boolean deauthAddressChanges,
		final int deauthTimeout,
		final boolean deauthTimeoutOnline,
		final int deauthTimeoutCheckHeartbeat,
		final int unlockTimeout,
		final int unlockTimeoutCheckHeartbeat,
		final String database,
		final boolean consoleReset,
		final boolean chat,
		final boolean restrictMovement
	) {
		this.loading = true;
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
		if(commandCooldown <= 0) {
			this.commandCooldown = 20;
			AuthUtil.consoleWarning("Command cooldown time set to " + commandCooldown + " seconds is not safe, reverting to default...");
		} else {
			this.commandCooldown = commandCooldown;
		}
		AuthUtil.console("Command cooldown time set to " + this.commandCooldown + " seconds");
		if(commandAttempts < 0) {
			this.commandAttempts = 10;
			AuthUtil.consoleWarning("Maximum authentication attempts set to " + commandAttempts + " is not safe, reverting to default...");
		} else {
			this.commandAttempts = commandAttempts;
		}
		AuthUtil.console(this.commandAttempts != 0 ? "Maximum authentication attempts set to " + this.commandAttempts + " attempts" : "Maximum authentication attempts check disabled");
		if(passwordMinLength <= 0) {
			this.passwordMinLength = 8;
			AuthUtil.consoleWarning("Minimum password length set to " + passwordMinLength + " is not safe, reverting to default...");
		} else {
			this.passwordMinLength = passwordMinLength;
		}
		AuthUtil.console("Minimum password length set to " + this.passwordMinLength);
		this.passwordBothCases = passwordBothCases;
		AuthUtil.console(this.passwordBothCases ? "Both cases required" : "Both cases not required");
		this.passwordNumbers = passwordNumbers;
		AuthUtil.console(this.passwordNumbers ? "Numbers required" : "Numbers not required");
		this.passwordSpecialChars = passwordSpecialChars;
		AuthUtil.console(this.passwordSpecialChars ? "Special characters required" : "Special characters not required");
		this.codeIssuer = codeIssuer;
		AuthUtil.console("Code issuer set to " + this.codeIssuer);
		this.deauthAddressChanges = deauthAddressChanges;
		AuthUtil.console(this.deauthAddressChanges ? "IP address check enabled" : "IP address check disabled");
		if(deauthTimeout <= 0) {
			this.deauthTimeout = 48;
			AuthUtil.consoleWarning("Deauth timeout set to " + deauthTimeout + " hours is not safe, reverting to default...");
		} else {
			this.deauthTimeout = deauthTimeout;
		}
		AuthUtil.console("Deauth timeout set to " + this.deauthTimeout + " hours");
		this.deauthTimeoutOnline = deauthTimeoutOnline;
		AuthUtil.console(this.deauthTimeoutOnline ? "Deauth timeout online mode enabled" : "Deauth timeout online mode disabled");
		if(deauthTimeoutCheckHeartbeat <= 0) {
			this.deauthTimeoutCheckHeartbeat = 10;
			AuthUtil.consoleWarning("Deauth timeout check heartbeat set to " + deauthTimeoutCheckHeartbeat + " minutes is not safe, reverting to default...");
		} else {
			this.deauthTimeoutCheckHeartbeat = deauthTimeoutCheckHeartbeat;
		}
		AuthUtil.console("Deauth timeout check heartbeat set to " + this.deauthTimeoutCheckHeartbeat + " minutes");
		if(unlockTimeout < 0) {
			this.unlockTimeout = 120;
			AuthUtil.consoleWarning("Unlock timeout set to " + unlockTimeout + " hours is not safe, reverting to default...");
		} else {
			this.unlockTimeout = unlockTimeout;
		}
		AuthUtil.console("Unlock timeout set to " + this.unlockTimeout + " hours");
		if(unlockTimeoutCheckHeartbeat <= 0) {
			this.unlockTimeoutCheckHeartbeat = 15;
			AuthUtil.consoleWarning("Unlock timeout check heartbeat set to " + unlockTimeoutCheckHeartbeat + " minutes is not safe, reverting to default...");
		} else {
			this.unlockTimeoutCheckHeartbeat = unlockTimeoutCheckHeartbeat;
		}
		AuthUtil.console("Unlock timeout check heartbeat set to " + this.unlockTimeoutCheckHeartbeat + " minutes");
		DatabaseOption databaseOption;
		try {
			databaseOption = DatabaseOption.valueOf(database.toUpperCase());
		} catch(final IllegalArgumentException e) {
			databaseOption = DatabaseOption.JSON;
			AuthUtil.consoleWarning("Database option set to " + database + " is not safe, reverting...");
		}
		this.databaseOption = databaseOption;
		AuthUtil.console("Database option set to " + this.databaseOption.toString());
		this.consoleReset = consoleReset;
		AuthUtil.console(this.consoleReset ? "Console access is enabled" : "Console access is disabled");
		this.chat = chat;
		AuthUtil.console(this.chat ? "Chat is enabled" : "Chat is disabled");
		this.restrictMovement = restrictMovement;
		AuthUtil.console(this.restrictMovement ? "Movement is restricted" : "Movement is not restricted");
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

	public synchronized int getCostFactor() {
		return this.costFactor;
	}

	public synchronized int getCommandCooldown() {
		return this.commandCooldown;
	}

	public synchronized int getCommandAttempts() {
		return this.commandAttempts;
	}

	public synchronized String getPasswordDefault() {
		return this.passwordDefault;
	}

	public synchronized int getPasswordMinLength() {
		return this.passwordMinLength;
	}

	public synchronized boolean isPasswordBothCases() {
		return this.passwordBothCases;
	}

	public synchronized boolean isPasswordNumbers() {
		return this.passwordNumbers;
	}

	public synchronized boolean isPasswordSpecialChars() {
		return this.passwordSpecialChars;
	}

	public synchronized String getCodeIssuer() {
		return this.codeIssuer;
	}

	public synchronized boolean isDeauthAddressChanges() {
		return this.deauthAddressChanges;
	}

	public synchronized int getDeauthTimeout() {
		return this.deauthTimeout;
	}

	public synchronized boolean isDeauthTimeoutOnline() {
		return this.deauthTimeoutOnline;
	}

	public synchronized int getDeauthTimeoutCheckHeartbeat() {
		return this.deauthTimeoutCheckHeartbeat;
	}

	public synchronized int getUnlockTimeout() {
		return this.unlockTimeout;
	}

	public synchronized int getUnlockTimeoutCheckHeartbeat() {
		return this.unlockTimeoutCheckHeartbeat;
	}

	public synchronized DatabaseOption getDatabaseOption() {
		return this.databaseOption;
	}

	public synchronized boolean isConsoleReset() {
		return this.consoleReset;
	}

	public synchronized boolean isChat() {
		return this.chat;
	}

	public synchronized boolean isRestrictMovement() {
		return this.restrictMovement;
	}

	public synchronized boolean isLoading() {
		return this.loading;
	}

}
