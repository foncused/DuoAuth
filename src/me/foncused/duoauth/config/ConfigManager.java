package me.foncused.duoauth.config;

import co.aikar.taskchain.TaskChain;
import me.foncused.duoauth.enumerable.DatabaseOption;
import me.foncused.duoauth.lib.aikar.TaskChainManager;
import me.foncused.duoauth.util.AuthUtil;

public class ConfigManager {

	private int costFactor;
	private int commandCooldown;
	private int commandAttempts;
	private String passwordDefault;
	private int passwordMinLength;
	private boolean passwordBothCases;
	private boolean passwordNumbers;
	private boolean passwordSpecialChars;
	private String pinDefault;
	private int pinMinLength;
	private DatabaseOption databaseOption;
	private boolean deauthAddressChanges;
	private int deauthTimeout;
	private boolean deauthTimeoutOnline;
	private int deauthTimeoutCheckHeartbeat;
	private boolean consoleReset;
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
			final String pinDefault,
			final int pinMinLength,
			final String database,
			final boolean deauthAddressChanges,
			final int deauthTimeout,
			final boolean deauthTimeoutOnline,
			final int deauthTimeoutCheckHeartbeat,
			final boolean consoleReset
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
		if(pinMinLength <= 0) {
			this.pinMinLength = 4;
			AuthUtil.consoleWarning("Minimum PIN length set to " + pinMinLength + " is not safe, reverting to default...");
		} else {
			this.pinMinLength = pinMinLength;
		}
		AuthUtil.console("Minimum PIN length set to " + this.pinMinLength);
		DatabaseOption databaseOption;
		try {
			databaseOption = DatabaseOption.valueOf(database.toUpperCase());
		} catch(final IllegalArgumentException e) {
			databaseOption = DatabaseOption.JSON;
			AuthUtil.consoleWarning("Database option set to " + database + " is not safe, reverting...");
		}
		this.databaseOption = databaseOption;
		AuthUtil.console("Database option set to " + this.databaseOption.toString());
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
			this.deauthTimeoutCheckHeartbeat = 5;
			AuthUtil.consoleWarning("Deauth timeout check heartbeat set to " + deauthTimeoutCheckHeartbeat + " minutes is not safe, reverting to default...");
		} else {
			this.deauthTimeoutCheckHeartbeat = deauthTimeoutCheckHeartbeat;
		}
		AuthUtil.console("Deauth timeout check heartbeat set to " + this.deauthTimeoutCheckHeartbeat + " minutes");
		this.consoleReset = consoleReset;
		AuthUtil.console(this.consoleReset ? "Console access is enabled" : "Console access is disabled");
		final TaskChain chain = TaskChainManager.newChain();
		chain
				.sync(() -> {
					if(pinDefault.matches("^[0-9]+$")) {
						chain.setTaskData("pin-default", pinDefault);

					} else {
						chain.setTaskData("pin-default", "1234");
						AuthUtil.consoleWarning("Default PIN set to " + pinDefault + " is not numeric, reverting to default...");
					}
				})
				.async(() -> {
					chain.setTaskData("password", AuthUtil.getSecureBCryptHash(passwordDefault, this.costFactor));
					chain.setTaskData("pin", AuthUtil.getSecureBCryptHash((String) chain.getTaskData("pin-default"), this.costFactor));
				})
				.sync(() -> {
					this.passwordDefault = (String) chain.getTaskData("password");
					AuthUtil.console("Default password hash for 'duoauth.enforced' is " + this.passwordDefault);
					this.pinDefault = (String) chain.getTaskData("pin");
					AuthUtil.console("Default PIN hash for 'duoauth.enforced' is " + this.pinDefault);
					this.loading = false;
				})
				.execute();
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

	public String getPinDefault() {
		return this.pinDefault;
	}

	public int getPinMinLength() {
		return this.pinMinLength;
	}

	public DatabaseOption getDatabaseOption() {
		return this.databaseOption;
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

	public boolean isConsoleReset() {
		return this.consoleReset;
	}

	public boolean isLoading() {
		return this.loading;
	}

}
