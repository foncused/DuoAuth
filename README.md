# DuoAuth
![DuoAuth](https://i.imgur.com/4tvxwSd.png)

### Description:
"DuoAuth" is an adaptive authentication plugin designed to safeguard your Spigot server against impersonation by requiring players to enter a password and 6-digit code (TOTP) combo at the command line (/auth <password\> <code\>) before they can chat, move, interact, or play on your server. The 6-digit code is provided from any [RFC6238-compliant](https://tools.ietf.org/html/rfc6238) mobile authentication app (Google Authenticator is recommended).

The plugin uses the unbroken [bcrypt](https://en.wikipedia.org/wiki/Bcrypt) password hashing algorithm to store credentials in 184-bit digests. As a result, the authentication process can be made **arbitrarily slow** by adjusting the algorithm cost factor, permitting flexibility based on administrative preference and individual server performance. Several other configurable items, including minimum password requirements and deauthentication settings, are supported as well.

### Links:
- Spigot: https://www.spigotmc.org/resources/duoauth.63418/
- Donate: https://paypal.me/foncused
