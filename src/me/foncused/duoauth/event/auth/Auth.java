package me.foncused.duoauth.event.auth;

import me.foncused.duoauth.enumerable.AuthMessage;
import me.foncused.duoauth.utility.AuthUtilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;

public class Auth implements Listener {

	private final Map<UUID, Boolean> players;

	public Auth(final Map<UUID, Boolean> players) {
		this.players = players;
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
		final Player player = event.getPlayer();
		if(!(this.isAuthed(player.getUniqueId()))) {
			this.message(player);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
		final Player player = event.getPlayer();
		if(!(this.isAuthed(player.getUniqueId())) && (!(event.getMessage().toLowerCase().matches("^/(auth|2fa).*")))) {
			this.message(player);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDropItem(final PlayerDropItemEvent event) {
		final Player player = event.getPlayer();
		if(!(this.isAuthed(player.getUniqueId()))) {
			this.message(player);
			event.setCancelled(true);
			if(!(event.isCancelled())) {
				event.getItemDrop().setItemStack(new ItemStack(Material.AIR));
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
		final Player player = event.getPlayer();
		if(!(this.isAuthed(player.getUniqueId()))) {
			this.message(player);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event) {
		final Entity entity = event.getWhoClicked();
		if(entity instanceof Player) {
			final Player player = (Player) entity;
			if(!(this.isAuthed(player.getUniqueId()))) {
				this.message(player);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent event) {
		final Player player = event.getPlayer();
		if(!(this.isAuthed(player.getUniqueId()))) {
			final Location loc1 = event.getFrom();
			final Location loc2 = event.getTo();
			if((loc1.getBlockX() != loc2.getBlockX()) || (loc1.getBlockY() != loc2.getBlockY()) || (loc1.getBlockZ() != loc2.getBlockZ())) {
				player.teleport(loc1);
			}
		}
	}

	private boolean isAuthed(final UUID uuid) {
		return this.players.get(uuid);
	}

	private void message(final Player player) {
		AuthUtilities.alertOne(player, AuthMessage.PLAYER_NOT_AUTHED.toString());
	}

}
