package me.foncused.duoauth.spigot.cache;

import java.net.InetAddress;

public class AuthCache {

	private String password;
	private String secret;
	private boolean authed;
	private int attempts;
	private InetAddress ip;

	public AuthCache(final String password, final String secret, final boolean authed, final int attempts, final InetAddress ip) {
		this.password = password;
		this.secret = secret;
		this.authed = authed;
		this.attempts = attempts;
		this.ip = ip;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getSecret() {
		return this.secret;
	}

	public void setSecret(final String code) {
		this.secret = code;
	}

	public boolean isAuthed() {
		return this.authed;
	}

	public void setAuthed(final boolean authed) {
		this.authed = authed;
	}

	public int getAttempts() {
		return this.attempts;
	}

	public void setAttempts(final int attempts) {
		this.attempts = attempts;
	}

	public InetAddress getIp() {
		return this.ip;
	}

	public void setIp(final InetAddress ip) {
		this.ip = ip;
	}

}
