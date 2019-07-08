package me.foncused.duoauth.cache;

import java.net.InetAddress;

public class AuthCache {

	private String password;
	private String pin;
	private boolean authed;
	private int attempts;
	private InetAddress ip;

	public AuthCache(final String password, final String pin, final boolean authed, final int attempts, final InetAddress ip) {
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

	public synchronized String getPin() {
		return this.pin;
	}

	public synchronized void setPin(final String pin) {
		this.pin = pin;
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
