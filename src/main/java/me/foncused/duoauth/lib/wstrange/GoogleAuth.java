package me.foncused.duoauth.lib.wstrange;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GoogleAuth {

	private final GoogleAuthenticator ga;
	private final Map<UUID, GoogleAuthenticatorKey> creds;

	public GoogleAuth() {
		this.ga = new GoogleAuthenticator();
		this.creds = new ConcurrentHashMap<>();
	}

	public synchronized GoogleAuthenticatorKey generateRFC6238Credentials(final UUID uuid) {
		this.creds.put(uuid, this.ga.createCredentials());
		return this.creds.get(uuid);
	}

	public synchronized GoogleAuthenticatorKey getCreds(final UUID uuid) {
		return this.creds.get(uuid);
	}

	public synchronized boolean containsCreds(final UUID uuid) {
		return this.creds.containsKey(uuid);
	}

	public synchronized void removeCreds(final UUID uuid) {
		this.creds.remove(uuid);
	}

	public boolean authorize(final String secret, final int code) {
		return this.ga.authorize(secret, code);
	}

	public String getAuthUrl(final String issuer, final String account, final GoogleAuthenticatorKey key) {
		return GoogleAuthenticatorQRGenerator.getOtpAuthURL(issuer, account, key);
	}

	public String getAuthTotpUrl(final String issuer, final String account, final GoogleAuthenticatorKey key) {
		return GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(issuer, account, key);
	}

}
