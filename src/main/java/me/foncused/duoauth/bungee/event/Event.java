package me.foncused.duoauth.bungee.event;

import me.foncused.duoauth.bungee.DuoAuth;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.LoginEvent;
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
	public void onLogin(final LoginEvent event) {
		final PendingConnection connection = event.getConnection();
		final UUID uuid = event.getConnection().getUniqueId();
		this.server.getLogger().log(Level.INFO, DuoAuth.PREFIX + "Adding filter to " + connection.getName() + " (" + uuid.toString() + ")");
		this.auths.add(uuid);
	}

	@EventHandler
	public void onChat(final ChatEvent event) {
		if(event.isCommand() && (!(event.getMessage().toLowerCase().matches("^/(auth|2fa).*$")))) {
			final Connection sender = event.getSender();
			if(sender instanceof final ProxiedPlayer player) {
				if(this.auths.contains(player.getUniqueId())) {
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
			switch(action) {
				case "Add" -> {
					final String u = dis.readUTF();
					if(this.auths.add(UUID.fromString(u))) {
						logger.log(Level.INFO, DuoAuth.PREFIX + "Adding filter to " + u);
					}
				}
				case "Remove" -> {
					final String u = dis.readUTF();
					if(this.auths.remove(UUID.fromString(u))) {
						logger.log(Level.INFO, DuoAuth.PREFIX + "Removing filter from " + u);
					}
				}
				default -> logger.log(
						Level.INFO,
						DuoAuth.PREFIX + "Proxy received plugin message " +
								"with unknown action '" + action + "' - this will be ignored!"
				);
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
