package me.foncused.duoauth.enumerable;

public enum DatabaseProperty {

	PASSWORD("Password"),
	PIN("PIN"),
	AUTHED("Authed"),
	ATTEMPTS("Attempts"),
	IP("IP"),
	TIMESTAMP("Timestamp");

	private final String property;

	DatabaseProperty(final String property) {
		this.property = property;
	}

	@Override
	public String toString() {
		return this.property;
	}

}
