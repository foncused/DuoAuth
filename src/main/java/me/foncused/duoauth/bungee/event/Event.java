package me.foncused.duoauth.bungee.event;

import me.foncused.duoauth.bungee.DuoAuth;
import me.foncused.duoauth.spigot.enumerable.AuthMessage;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Event implements Listener {

	private final DuoAuth plugin;
	private final ProxyServer server;
	private final Set<UUID> auths;

	public Event(final DuoAuth plugin) {
		this.plugin = plugin;
		this.server = this.plugin.getProxy();
		this.auths = new HashSet<>();
	}

	@EventHandler
	public void onChat(final ChatEvent event) {
		if(event.isCommand() && (!(event.getMessage().toLowerCase().matches("^/(auth|2fa).*$")))) {
			final Connection sender = event.getSender();
			if(sender instanceof ProxiedPlayer) {
				final ProxiedPlayer player = (ProxiedPlayer) sender;
				if(this.auths.contains(player.getUniqueId())) {
					player.sendMessage(
							(AuthMessage.PREFIX_ALERT.toString() + AuthMessage.PLAYER_NOT_AUTHED)
									.replaceAll("&", "§")
					);
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onPluginMessage(final PluginMessageEvent event) {
		if(!(event.getTag().equals("duoauth:filter"))) {
			return;
		}
		final ByteArrayInputStream bais = new ByteArrayInputStream(event.getData());
		final DataInputStream dis = new DataInputStream(bais);
		try {
			final String action = dis.readUTF();
			final Logger logger = this.server.getLogger();
			final String prefix = "[DuoAuth] ";
			switch(action) {
				case "Add": {
					final String u = dis.readUTF();
					logger.log(Level.INFO, prefix + "Adding filter to " + u);
					this.auths.add(UUID.fromString(u));
					break;
				}
				case "Remove": {
					final String u = dis.readUTF();
					logger.log(Level.INFO, prefix + "Removing filter from " + u);
					this.auths.remove(UUID.fromString(u));
					break;
				}
				default:
					logger.log(
							Level.INFO,
							prefix + "Proxy received plugin message " +
									"with unknown action '" + action + "' - this will be ignored!"
					);
					break;
			}
			dis.close();
			try {
				bais.close();
			} catch(final IOException e) {
				e.printStackTrace();
			}
		} catch(final Exception e) {
			e.printStackTrace();
		}
	}

}
