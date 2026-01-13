# DuoAuth
![DuoAuth](https://i.imgur.com/4tvxwSd.png)

### Description:
"DuoAuth" is an adaptive authentication plugin designed to safeguard your Spigot server against impersonation by requiring players to enter a password and 6-digit code (TOTP) combo at the command line (/auth <password\> <code\>) before they can chat, move, interact, or play on your server. The 6-digit code is provided from any [RFC6238-compliant](https://tools.ietf.org/html/rfc6238) mobile authentication app (Google Authenticator is recommended).

The plugin uses the unbroken [bcrypt](https://en.wikipedia.org/wiki/Bcrypt) password hashing algorithm to store credentials in 184-bit digests. As a result, the authentication process can be made **arbitrarily slow** by adjusting the cost factor parameter, permitting flexibility based on administrative preference and individual server performance. Several other configurable items, including minimum password requirements and deauthentication settings, are supported as well.

### Installation:
1. Copy DuoAuth.jar into the /plugins directory of your fallback server
2. If you are using BungeeCord, copy DuoAuth.jar into your BungeeCord /plugins directory
3. Assign permissions
4. Start your server
5. Edit your config.yml

### Permissions:
- duoauth.auth - grants access to the /auth command
- duoauth.bypass - bypass the cooldown time for the /auth command (see config "command.cooldown")
- duoauth.admin - grants access to /auth deauth <player\>, /auth reset <player\> and /auth check <player\>
- duoauth.notify - receive alerts and results when a player uses the /auth command
- duoauth.enforced - forces the player to set up authentication
- duoauth.unlimited - bypass the maximum number of authentication attempts (see config "command.attempts")

**None of these permissions are granted by default; they must be explicitly granted to your users and groups.**

### Configuration:
- bungeecord - **true** to enable BungeeCord support, **false** if not (this is only useful for blocking proxy-level commands e.g. /server <server\>, /send <player\> <server\> when players are not authenticated)
- cost-factor - the cost factor of the bcrypt algorithm
  - Min: **12**
  - Max: **30**
  - Recommended: Set this value in respect to the maximum amount of time you are willing to have your players wait to be authenticated (normally **12-17**)
- command
  - cooldown - the cooldown time in seconds for using the /auth command (bypassed by 'duoauth.bypass')
    - Min: **1**
    - Recommended: **20**
  - attempts - the maximum number of incorrect authentication attempts before players are locked out (bypassed by 'duoauth.unlimited')
    - Recommended: **5+**
    - Disable: **0**
  - password
    - default - the default password to be used when players join with permission 'duoauth.enforced'
    - min-length - the minimum length of a provided password
      - Min: **1**
      - Recommended: **8+**
    - both-cases - **true** to enforce at least one uppercase (A-Z) and one lowercase letter (a-z), **false** if not
      - Recommended: **true**
    - numbers - **true** to enforce at least one number (0-9), **false** if not
      - Recommended: **true**
    - special-chars - **true** to enforce at least one special character in the provided 'special-charset', **false** if not
      - Recommended: **true**
    - special-charset - the set of characters players must include (at least one) in their passwords
- code
  - issuer - the issuer of the OTP URL
- deauth
  - ip-changes - **true** to immediately deauthenticate players whenever their IP addresses change, **false** if not
  Recommended: **true**
  - timeout - the time in hours to deauthenticate players after the last successful authentication
    - Min: **1**
    - Recommended: **72+**
  - timeout-online - **true** to deauthenticate players even if they are online (can be annoying), **false** if not
    - Recommended: **false**
  - timeout-check-heartbeat - the heartbeat in minutes to check if players should be deauthenticated
    - Min: **1**
    - Recommended: **10+**
- unlock
  - timeout - the time in hours to automatically unlock locked players
    - Recommended: **72+**
    - Disable: **0**
  - timeout-check-heartbeat - the heartbeat in minutes to check if players should be unlocked
    - Min: **1**
    - Recommended: **10+**
- database - the method used for storing the digests
  - This is not currently configurable. For now, DuoAuth will only store data in .json files in the /data directory.
- chat - **true** to allow chat prior to authentication, **false** if not
- restrict-movement - **true** to restrict yaw and pitch prior to authentication, **false** if not

https://github.com/foncused/DuoAuth/blob/master/src/main/resources/config.yml
```yaml
# @plugin DuoAuth
# @version 1.1.16
# @author foncused

# Are you using BungeeCord?
# If set to true, DuoAuth.jar should also be installed as a BungeeCord plugin
# This is only useful for blocking proxy-level commands (/server <server>, /send <player> <server>)
bungeecord: false

# The cost factor of the bcrypt algorithm
# For best security measures, it is recommended to set this value in respect to the maximum amount of
# time you are willing to have your players wait to be authenticated (normally 12-17)
cost-factor: 15

# Command options for /auth command
command:

  # The cooldown time in seconds for using the /auth command (bypassed by 'duoauth.bypass')
  cooldown: 20

  # The maximum number of incorrect authentication attempts before players are locked out (bypassed by 'duoauth.unlimited')
  # Disabled by setting to 0
  attempts: 5

# Password options
# For best security measures, set a min-length of at least 8, both-cases to true,
# numbers to true, special-chars to true, and change default (Password1234#) as
# this default config.yml is public on GitHub; be advised the default password
# WILL NOT WORK if it does not meet the other requirements
password:

  # The default password to be used when players join with permission 'duoauth.enforced'
  default: "Password1234#"

  # The minimum length of a provided password
  min-length: 8

  # true to enforce at least one uppercase and one lowercase letter, false if not
  both-cases: true

  # true to enforce at least one number, false if not
  numbers: true

  # true to enforce at least one special character in the provided 'special-charset', false if not
  special-chars: true
  special-charset: "@#$%^&+="

# Code options
code:

  # The issuer of the OTP URL
  # This will be the title displayed next to the 2FA code in the mobile authentication app
  issuer: "DuoAuth"

# Deauthentication options
# For best security measures, set ip-changes to true
deauth:

  # true to immediately deauthenticate players whenever their IP addresses change, false if not
  ip-changes: true

  # The time in hours to deauthenticate players after the last successful authentication
  timeout: 72

  # true to deauthenticate players even if they are online (can be annoying), false if not
  timeout-online: false

  # The heartbeat in minutes to check if players should be deauthenticated
  timeout-check-heartbeat: 10

# Unlock options
unlock:

  # The time in hours to automatically unlock locked players
  # Disabled by setting to 0
  timeout: 120

  # The heartbeat in minutes to check if players should be unlocked
  timeout-check-heartbeat: 15

# Database
# The method used for storing the digests
# This is not currently configurable; for now, DuoAuth will only store data in .json files in the /data directory
database: "json"

# true to allow chat prior to authentication, false if not
chat: false

# true to restrict yaw and pitch prior to authentication, false if not
restrict-movement: false
```

DuoAuth also generates a second configuration file (lang.yml) to provide customization for messages printed to the chat.
https://github.com/foncused/DuoAuth/blob/master/src/main/resources/lang.yml
```yaml
allow_admin_success: "&aYou have been allowed by an administrator. Have fun!"
authenticating: "&6Authenticating ..."
authenticating_failed: "&cAuthentication failed. Please ensure your password and code are correct. Please contact the server administrators if you believe that this is in error."
authenticating_success: "&aAuthentication successful. Have fun!"
auth_in_progress: "&6Authentication in progress - please be patient ..."
auth_in_progress_admin: "&cThat user is currently performing an authentication attempt. Please try again in a moment."
bug: "&cAn unexpected error has occurred."
code_invalid: "&cThe code you entered is invalid. Your code must be a 6-digit number provided by your mobile authentication app."
deauth_admin_success: "&cYou have been deauthenticated by an administrator. Please use the /auth command to continue playing. Thank you!"
deauth_failed: "&cDeauthentication failed. Please contact the server administrators if you are receiving this message."
deauth_success: "&aYou have deauthenticated successfully. To continue playing, please use the &c/auth &acommand."
enforced: "&cThe server administrator has required you to set up authentication. Please scan the QR shown or enter the secret key into your mobile authentication app. The 6-digit code shown in the app will then be used for performing authentication with '/auth <password> <code>'. The default password must be given to you before you can proceed with these instructions. Once you are authenticated, you may use '/auth reset' and '/auth generate' to set your own credentials. Thank you!"
generate: "&cYou need to enter '/auth generate' to generate an authentication secret for your code. Once generated, scan the QR shown or enter the secret key into your mobile authentication app. The 6-digit code shown in the app will then be used for performing authentication with '/auth <password> <code>'."
generating: "&6Generating authentication secret ..."
kicked: "&cSorry, default authentication setup has failed. Please contact the server administrator if you are reading this message."
loading: "&8Duo&aAuth &cis still loading. Please try again in a moment."
locked: "&cYour account has been locked. You will need to wait for your account to be unlocked, or you may contact the server administrators for assistance."
must_wait: "&cYou must wait before doing that again."
no_console: "&cYou cannot do this from the console."
no_generate: "&cYou are already authenticated, so there is no need to generate a shared secret at this time."
no_permission: "&cYou do not have permission to do this!"
player_not_authed: "&cYou need to authenticate with /auth <password> <code> before you can chat, move, or play."
player_not_db: "&cYou have not set up authentication. To enable authentication, please use the /auth command."
please_save_qr: "&aPlease save the QR .png image somewhere safe."
prefix_alert: "&r[&8Duo&aAuth&r] &3ALERT &8» &7"
prefix_notify: "&r[&8Duo&aAuth&r] &cALERT &8» &7&o"
reset_admin_success: "&aYour credentials have been reset by an administrator."
reset_failed: "&cFailed to reset authentication. Please contact the server administrators if you are receiving this message."
reset_success: "&aYour credentials have been reset! To re-enable authentication, please use the &c/auth &acommand."
session_expired: "&cYour session has expired. Please use the /auth command to continue playing. Thank you!"
setting_up: "&6Setting up authentication ..."
setting_up_failed: "&cFailed to set up authentication. Please verify your code is correct, or contact the server administrators if you are receiving this message."
setting_up_success: "&aYour credentials have been set!"
```

### Commands:
- /auth (alias /2fa)
  - help - prints /auth command usage
  - generate - generate authentication secret
  - allow <player\> - allow a player to bypass authentication ('duoauth.admin' required)
  - deauth - deauthenticates the player
  - deauth <player\> - deauthenticates a player ('duoauth.admin' required)
  - reset - resets the player's own authentication credentials
  - reset <player\> - resets a player's authentication credentials ('duoauth.admin' required)
  - check <player\> - check a player's authentication status, logs status to the console ('duoauth.admin' required)
  - <password\> <code\> - sets up or attempts authentication with the provided password and 6-digit code

The enforced setup ('duoauth.enforced') exists primarily as a way to prevent staff impersonation, including prior to when they would manually set up authentication, by using the default password in the config.yml file. Once they are authenticated and setup with your default password, they will be able to reset themselves and set their own password if they wish (or they can keep the default password - not recommended). The process flow is as follows:
1. Scan the generated QR code or enter the secret key into your mobile authentication app
2. Retrieve the TOTP code from your app
3. /auth <default password\> <code\>
4. /auth reset
5. /auth generate
6. Repeat steps 1-2, and 3 but with your custom password

Once a player has set up authentication, their credentials will be stored in the plugin's /data directory in <UUID>.json format.
- feb991c8-41e9-4e14-bb11-673ae0ccce8a.json
```json
{
"Password":"$2a$14$hQ7cCEvjnZ.30haqtUKOGOAyGao3MerifrDPHXLTG5wRCmNd22vgW",
"Secret":"P5PFU7XMFR65SBNO",
"Authed":false,
"Attempts":1,
"IP":"127.0.0.1",
"Timestamp":"08/17/2019 17:44:26:571"
}
```

### Ideas:
- ~~Add config option to enable console access to the /auth reset <player> command~~
- ~~Add subcommand to deauthenticate~~
- ~~Add lang.yml file to customize plugin messages~~
- ~~Add support for mobile authentication apps~~
- Add support for SQLite and MySQL
Support:

To avoid any lag issues or TPS drops, DuoAuth calculates and compares digests in asynchronous tasks. If you run into any server performance problems, or if the plugin is not working as advertised (console errors, bugs, etc.), please do not hesitate to contact me, post in the discussion thread, or open an issue on GitHub. In doing so, it would be beneficial to provide your config.yml, server timings report, and system specs.

### Links:
- Spigot: https://www.spigotmc.org/resources/duoauth.80609/
- Donate: https://paypal.me/foncused
