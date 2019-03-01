package me.foncused.duoauth.cache;

public class AuthCache {

	private String password;
	private String pin;
	private boolean authed;
	private int attempts;
	private String ip;

	public AuthCache(final String password, final String pin, final boolean authed, final int attempts, final String ip) {
		this.password = password;
		this.pin = pin;
		this.authed = authed;
		this.attempts = attempts;
		this.ip = ip;
	}

	public synchronized String getPassword() {
		return this.password;
	}

	public synchronized void setPassword(final String password) {
		this.password = password;
	}

	public synchronized String getPIN() {
		return this.pin;
	}

	public synchronized void setPIN(final String pin) {
		this.pin = pin;
	}



}
