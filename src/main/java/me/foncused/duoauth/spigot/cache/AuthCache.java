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

	public synchronized String getPassword() {
		return this.password;
	}

	public synchronized void setPassword(final String password) {
		this.password = password;
	}

	public synchronized String getSecret() {
		return this.secret;
	}

	public synchronized void setSecret(final String code) {
		this.secret = code;
	}

	public synchronized boolean isAuthed() {
		return this.authed;
	}

	public synchronized void setAuthed(final boolean authed) {
		this.authed = authed;
	}

	public synchronized int getAttempts() {
		return this.attempts;
	}

	public synchronized void setAttempts(final int attempts) {
		this.attempts = attempts;
	}

	public synchronized InetAddress getIp() {
		return this.ip;
	}

	public synchronized void setIp(final InetAddress ip) {
		this.ip = ip;
	}

}
