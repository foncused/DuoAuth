# DuoAuth
![DuoAuth](https://i.imgur.com/nnYcoeV.png)

"DuoAuth" is an adaptive authentication plugin designed to safeguard your Spigot server against impersonation, by requiring a password and PIN combo, before players can chat, move, interact, or play on the server.

What do I mean by *adaptive*? DuoAuth uses the unbroken [bcrypt](https://en.wikipedia.org/wiki/Bcrypt) cryptographic hashing function to store credentials in 184-bit digests, allowing a configurable "cost factor" parameter to make the function **arbitrarily slow**.

In short, because this plugin uses the bcrypt algorithm, it can stay resistant against brute-force attacks even with increasing processing power.

Resource page: https://www.spigotmc.org/resources/duoauth.63418/
Donation link: https://paypal.me/foncused
