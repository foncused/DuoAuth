# DuoAuth
![DuoAuth](https://i.imgur.com/nnYcoeV.png)

### Description:
"DuoAuth" is an adaptive authentication plugin designed to safeguard your Spigot server against impersonation by requiring a password and PIN combo before players can chat, move, interact, or play on the server.

What do I mean by *adaptive*? DuoAuth uses the unbroken [bcrypt](https://en.wikipedia.org/wiki/Bcrypt) cryptographic hashing algorithm to store credentials in 184-bit digests, allowing a configurable "cost factor" parameter to make the authentication process **arbitrarily slow**.

In short, because this plugin uses the bcrypt algorithm, it can stay resistant against brute-force attacks even with increasing processing power.

### Links:
- Spigot: https://www.spigotmc.org/resources/duoauth.63418/
- Donate: https://paypal.me/foncused
