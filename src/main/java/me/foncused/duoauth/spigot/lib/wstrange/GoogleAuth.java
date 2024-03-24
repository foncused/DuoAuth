package me.foncused.duoauth.spigot.lib.wstrange;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/*
Copyright (c) 2013 Warren Strange
Copyright (c) 2014-2017 Enrico M. Crisostomo
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.

  * Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

  * Neither the name of the author nor the names of its
    contributors may be used to endorse or promote products derived from
    this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

public class GoogleAuth {

	private final GoogleAuthenticator ga;
	private final Map<UUID, GoogleAuthenticatorKey> creds;

	public GoogleAuth() {
		this.ga = new GoogleAuthenticator();
		this.creds = new ConcurrentHashMap<>();
	}

	public GoogleAuthenticatorKey generateRfc6238Credentials(final UUID uuid) {
		this.creds.put(uuid, this.ga.createCredentials());
		return this.creds.get(uuid);
	}

	public GoogleAuthenticatorKey getCreds(final UUID uuid) {
		return this.creds.get(uuid);
	}

	public boolean containsCreds(final UUID uuid) {
		return this.creds.containsKey(uuid);
	}

	public void removeCreds(final UUID uuid) {
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
