package me.foncused.duoauth.config;

import co.aikar.taskchain.TaskChain;
import me.foncused.duoauth.enumerable.DatabaseOption;
import me.foncused.duoauth.lib.aikar.TaskChainManager;
import me.foncused.duoauth.utility.AuthUtilities;

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
	private boolean loading;

	public ConfigManager(final int costFactor, final int commandCooldown, final int commandAttempts, final String passwordDefault, final int passwordMinLength, final boolean passwordBothCases, final boolean passwordNumbers, final boolean passwordSpecialChars, final String pinDefault, final int pinMinLength, final String database, final boolean deauthAddressChanges, final int deauthTimeout, final boolean deauthTimeoutOnline, final int deauthTimeoutCheckHeartbeat) {
		this.loading = true;
		if(costFactor < 12) {
			this.costFactor = 12;
			AuthUtilities.consoleWarning("Bcrypt cost factor set to " + costFactor + " is too low, reverting to minimum...");
		} else if(costFactor > 30) {
			this.costFactor = 30;
			AuthUtilities.consoleWarning("Bycrypt cost factor set to " + costFactor + " is too high, reverting to maximum...");
		} else {
			this.costFactor = costFactor;
		}
		AuthUtilities.console("Bcrypt cost factor set to " + this.costFactor);
		if(commandCooldown <= 0) {
			this.commandCooldown = 20;
			AuthUtilities.consoleWarning("Command cooldown time set to " + commandCooldown + " seconds is not safe, reverting to default...");
		} else {
			this.commandCooldown = commandCooldown;
		}
		AuthUtilities.console("Command cooldown time set to " + this.commandCooldown + " seconds");
		if(commandAttempts < 0) {
			this.commandAttempts = 10;
			AuthUtilities.consoleWarning("Maximum authentication attempts set to " + commandAttempts + " is not safe, reverting to default...");
		} else {
			this.commandAttempts = commandAttempts;
		}
		AuthUtilities.console(this.commandAttempts != 0 ? "Maximum authentication attempts set to " + this.commandAttempts + " attempts" : "Maximum authentication attempts check disabled");
		if(passwordMinLength <= 0) {
			this.passwordMinLength = 8;
			AuthUtilities.consoleWarning("Minimum password length set to " + passwordMinLength + " is not safe, reverting to default...");
		} else {
			this.passwordMinLength = passwordMinLength;
		}
		AuthUtilities.console("Minimum password length set to " + this.passwordMinLength);
		this.passwordBothCases = passwordBothCases;
		AuthUtilities.console(this.passwordBothCases ? "Both cases required" : "Both cases not required");
		this.passwordNumbers = passwordNumbers;
		AuthUtilities.console(this.passwordNumbers ? "Numbers required" : "Numbers not required");
		this.passwordSpecialChars = passwordSpecialChars;
		AuthUtilities.console(this.passwordSpecialChars ? "Special characters required" : "Special characters not required");
		if(pinMinLength <= 0) {
			this.pinMinLength = 4;
			AuthUtilities.consoleWarning("Minimum PIN length set to " + pinMinLength + " is not safe, reverting to default...");
		} else {
			this.pinMinLength = pinMinLength;
		}
		AuthUtilities.console("Minimum PIN length set to " + this.pinMinLength);
		DatabaseOption databaseOption;
		try {
			databaseOption = DatabaseOption.valueOf(database.toUpperCase());
		} catch(final IllegalArgumentException e) {
			databaseOption = DatabaseOption.JSON;
			AuthUtilities.consoleWarning("Database option set to " + database + " is not safe, reverting...");
		}
		this.databaseOption = databaseOption;
		AuthUtilities.console("Database option set to " + this.databaseOption.toString());
		this.deauthAddressChanges = deauthAddressChanges;
		AuthUtilities.console(this.deauthAddressChanges ? "IP address check enabled" : "IP address check disabled");
		if(deauthTimeout <= 0) {
			this.deauthTimeout = 48;
			AuthUtilities.consoleWarning("Deauth timeout set to " + deauthTimeout + " hours is not safe, reverting to default...");
		} else {
			this.deauthTimeout = deauthTimeout;
		}
		AuthUtilities.console("Deauth timeout set to " + this.deauthTimeout + " hours");
		this.deauthTimeoutOnline = deauthTimeoutOnline;
		AuthUtilities.console(this.deauthTimeoutOnline ? "Deauth timeout online mode enabled" : "Deauth timeout online mode disabled");
		if(deauthTimeoutCheckHeartbeat <= 0) {
			this.deauthTimeoutCheckHeartbeat = 5;
			AuthUtilities.consoleWarning("Deauth timeout check heartbeat set to " + deauthTimeoutCheckHeartbeat + " minutes is not safe, reverting to default...");
		} else {
			this.deauthTimeoutCheckHeartbeat = deauthTimeoutCheckHeartbeat;
		}
		AuthUtilities.console("Deauth timeout check heartbeat set to " + this.deauthTimeoutCheckHeartbeat + " minutes");
		final TaskChain chain = TaskChainManager.newChain();
		chain
				.sync(() -> {
					if(pinDefault.matches("^[0-9]+$")) {
						chain.setTaskData("pin-default", pinDefault);

					} else {
						chain.setTaskData("pin-default", "1234");
						AuthUtilities.consoleWarning("Default PIN set to " + pinDefault + " is not numeric, reverting to default...");
					}
				})
				.async(() -> {
					chain.setTaskData("password", AuthUtilities.getSecureBCryptHash(passwordDefault, this.costFactor));
					chain.setTaskData("pin", AuthUtilities.getSecureBCryptHash((String) chain.getTaskData("pin-default"), this.costFactor));
				})
				.sync(() -> {
					this.passwordDefault = (String) chain.getTaskData("password");
					AuthUtilities.console("Default password hash for 'duoauth.enforced' is " + this.passwordDefault);
					this.pinDefault = (String) chain.getTaskData("pin");
					AuthUtilities.console("Default PIN hash for 'duoauth.enforced' is " + this.pinDefault);
					this.loading = false;
				})
				.execute();
	}

	public int getCostFactor() {
		return this.costFactor;
	}

	public void setCostFactor(final int costFactor) {
		this.costFactor = costFactor;
	}

	public int getCommandCooldown() {
		return this.commandCooldown;
	}

	public void setCommandCooldown(final int commandCooldown) {
		this.commandCooldown = commandCooldown;
	}

	public int getCommandAttempts() {
		return this.commandAttempts;
	}

	public void setCommandAttempts(final int commandAttempts) {
		this.commandAttempts = commandAttempts;
	}

	public String getPasswordDefault() {
		return this.passwordDefault;
	}

	public void setPasswordDefault(final String passwordDefault) {
		this.passwordDefault = passwordDefault;
	}

	public int getPasswordMinLength() {
		return this.passwordMinLength;
	}

	public void setPasswordMinLength(final int passwordMinLength) {
		this.passwordMinLength = passwordMinLength;
	}

	public boolean isPasswordBothCases() {
		return this.passwordBothCases;
	}

	public void setPasswordBothCases(final boolean passwordBothCases) {
		this.passwordBothCases = passwordBothCases;
	}

	public boolean isPasswordNumbers() {
		return this.passwordNumbers;
	}

	public void setPasswordNumbers(final boolean passwordNumbers) {
		this.passwordNumbers = passwordNumbers;
	}

	public boolean isPasswordSpecialChars() {
		return this.passwordSpecialChars;
	}

	public void setPasswordSpecialChars(final boolean passwordSpecialChars) {
		this.passwordSpecialChars = passwordSpecialChars;
	}

	public String getPinDefault() {
		return this.pinDefault;
	}

	public void setPinDefault(final String pinDefault) {
		this.pinDefault = pinDefault;
	}

	public int getPinMinLength() {
		return this.pinMinLength;
	}

	public void setPinMinLength(final int pinMinLength) {
		this.pinMinLength = pinMinLength;
	}

	public DatabaseOption getDatabaseOption() {
		return this.databaseOption;
	}

	public void setDatabaseOption(final DatabaseOption databaseOption) {
		this.databaseOption = databaseOption;
	}

	public boolean isDeauthAddressChanges() {
		return this.deauthAddressChanges;
	}

	public void setDeauthAddressChanges(final boolean deauthAddressChanges) {
		this.deauthAddressChanges = deauthAddressChanges;
	}

	public int getDeauthTimeout() {
		return this.deauthTimeout;
	}

	public void setDeauthTimeout(final int deauthTimeout) {
		this.deauthTimeout = deauthTimeout;
	}

	public boolean isDeauthTimeoutOnline() {
		return this.deauthTimeoutOnline;
	}

	public void setDeauthTimeoutOnline(final boolean deauthTimeoutOnline) {
		this.deauthTimeoutOnline = deauthTimeoutOnline;
	}

	public int getDeauthTimeoutCheckHeartbeat() {
		return this.deauthTimeoutCheckHeartbeat;
	}

	public void setDeauthTimeoutCheckHeartbeat(final int deauthTimeoutCheckHeartbeat) {
		this.deauthTimeoutCheckHeartbeat = deauthTimeoutCheckHeartbeat;
	}

	public boolean isLoading() {
		return this.loading;
	}

}
