package me.foncused.duoauth.spigot.event.auth;

import me.foncused.duoauth.spigot.DuoAuth;
import me.foncused.duoauth.spigot.cache.AuthCache;
import me.foncused.duoauth.spigot.config.ConfigManager;
import me.foncused.duoauth.spigot.config.LangManager;
import me.foncused.duoauth.spigot.enumerable.AuthMessage;
import me.foncused.duoauth.spigot.lib.wstrange.GoogleAuth;
import me.foncused.duoauth.spigot.util.AuthUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class Auth implements Listener {

	private final DuoAuth plugin;
	private final ConfigManager cm;
	private final LangManager lm;
	private final GoogleAuth ga;

	public Auth(final DuoAuth plugin) {
		this.plugin = plugin;
		this.cm = this.plugin.getConfigManager();
		this.lm = this.plugin.getLangManager();
		this.ga = this.plugin.getGoogleAuth();
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onAsyncPlayerChat(final AsyncPlayerChatEvent event) {
		if(!(this.cm.isChat())) {
			final Player player = event.getPlayer();
			final UUID uuid = player.getUniqueId();
			final AuthCache cache = this.plugin.getAuthCache(uuid);
			if(cache == null && player.hasPermission("duoauth.enforced")) {
				player.sendMessage(AuthMessage.BUG.toString());
				event.setCancelled(true);
				return;
			}
			if(cache != null && (!(cache.isAuthed()))) {
				this.message(player);
				event.setCancelled(true);
				return;
			}
			if(this.ga.containsCreds(uuid)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if(event.isAsynchronous()) {
            event.setCancelled(true);
            return;
        }
		if(event.getDamage() >= 0.0 && event.getEntity() instanceof Player player) {
			final UUID uuid = player.getUniqueId();
			final AuthCache cache = this.plugin.getAuthCache(uuid);
			if(cache == null && player.hasPermission("duoauth.enforced")) {
				player.sendMessage(AuthMessage.BUG.toString());
				event.setCancelled(true);
				return;
			}
			if(cache != null && (!(cache.isAuthed()))) {
				this.message(player);
				event.setCancelled(true);
				return;
			}
			if(this.ga.containsCreds(uuid)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onEntityPickupItem(final EntityPickupItemEvent event) {
        if(event.isAsynchronous()) {
            event.setCancelled(true);
            return;
        }
		final Entity entity = event.getEntity();
		if(entity instanceof Player player) {
			final AuthCache cache = this.plugin.getAuthCache(player.getUniqueId());
			if(cache == null && player.hasPermission("duoauth.enforced")) {
				player.sendMessage(AuthMessage.BUG.toString());
				event.setCancelled(true);
				return;
			}
			if(cache != null && (!(cache.isAuthed()))) {
				this.message(player);
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onInventoryClick(final InventoryClickEvent event) {
        if(event.isAsynchronous()) {
            event.setCancelled(true);
            return;
        }
		final Entity entity = event.getWhoClicked();
		if(entity instanceof Player player) {
			final UUID uuid = player.getUniqueId();
            final AuthCache cache = this.plugin.getAuthCache(uuid);
			if(cache == null && player.hasPermission("duoauth.enforced")) {
				player.sendMessage(AuthMessage.BUG.toString());
				event.setCancelled(true);
				return;
			}
			if(cache != null && (!(cache.isAuthed()))) {
				this.message(player);
				event.setCancelled(true);
				return;
			}
			if(this.ga.containsCreds(uuid)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        if(event.isAsynchronous()) {
            event.setCancelled(true);
            return;
        }
		final Player player = event.getPlayer();
		final AuthCache cache = this.plugin.getAuthCache(player.getUniqueId());
		if(cache == null && player.hasPermission("duoauth.enforced")) {
			player.sendMessage(AuthMessage.BUG.toString());
			event.setCancelled(true);
			return;
		}
		if(cache != null && (!(cache.isAuthed())) && (!(event.getMessage().toLowerCase().matches("^/(auth|2fa).*$")))) {
			this.message(player);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerDropItem(final PlayerDropItemEvent event) {
        if(event.isAsynchronous()) {
            event.setCancelled(true);
            return;
        }
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		final AuthCache cache = this.plugin.getAuthCache(uuid);
		if(cache == null && player.hasPermission("duoauth.enforced")) {
			player.sendMessage(AuthMessage.BUG.toString());
			event.setCancelled(true);
			return;
		}
		if(cache != null && (!(cache.isAuthed()))) {
			this.message(player);
			event.setCancelled(true);
			if(!(event.isCancelled())) {
				event.getItemDrop().setItemStack(new ItemStack(Material.AIR));
			}
			return;
		}
		if(this.ga.containsCreds(uuid)) {
			final ItemStack stack = event.getItemDrop().getItemStack();
			if(stack.getType() == Material.FILLED_MAP && stack.hasItemMeta()) {
				final MapMeta meta = (MapMeta) stack.getItemMeta();
				if(meta != null && meta.hasMapView()
						&& meta.getPersistentDataContainer().has(new NamespacedKey(this.plugin, "duoauth"), PersistentDataType.STRING)) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteract(final PlayerInteractEvent event) {
        if(event.isAsynchronous()) {
            event.setCancelled(true);
            return;
        }
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		final AuthCache cache = this.plugin.getAuthCache(uuid);
		if(cache == null && player.hasPermission("duoauth.enforced")) {
			player.sendMessage(AuthMessage.BUG.toString());
			event.setCancelled(true);
			return;
		}
		if(cache != null && (!(cache.isAuthed()))) {
			this.message(player);
			event.setCancelled(true);
			return;
		}
		if(this.ga.containsCreds(uuid)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
		if(event.isAsynchronous()) {
			event.setCancelled(true);
			return;
		}
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		final AuthCache cache = this.plugin.getAuthCache(uuid);
		if(cache == null && player.hasPermission("duoauth.enforced")) {
			player.sendMessage(AuthMessage.BUG.toString());
			event.setCancelled(true);
			return;
		}
		if(cache != null && (!(cache.isAuthed()))) {
			this.message(player);
			event.setCancelled(true);
			return;
		}
		if(this.ga.containsCreds(uuid)) {
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onPlayerMove(final PlayerMoveEvent event) {
		final Location from = event.getFrom();
        if(event.isAsynchronous()) {
            event.setTo(from);
            event.setCancelled(true);
            return;
        }
		final Player player = event.getPlayer();
		final AuthCache cache = this.plugin.getAuthCache(player.getUniqueId());
		if(cache == null && player.hasPermission("duoauth.enforced")) {
			player.sendMessage(AuthMessage.BUG.toString());
			event.setCancelled(true);
			return;
		}
		if(cache != null && (!(cache.isAuthed()))) {
			try {
				final Location to = event.getTo();
				if(this.cm.isRestrictMovement()) {
					this.message(player);
					event.setTo(from);
				} else {
                    assert to != null;
                    if((from.getBlockX() != to.getBlockX())
                            || (from.getBlockY() != to.getBlockY())
                            || (from.getBlockZ() != to.getBlockZ())) {
						this.message(player);
                        player.teleport(from);
                    }
                }
			} catch(final NullPointerException e) {
				this.message(player);
				player.teleport(from);
			}
		}
	}

	@EventHandler
	public void onPlayerSwapHandItems(final PlayerSwapHandItemsEvent event) {
		if(event.isAsynchronous()) {
			event.setCancelled(true);
			return;
		}
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		final AuthCache cache = this.plugin.getAuthCache(uuid);
		if(cache == null && player.hasPermission("duoauth.enforced")) {
			player.sendMessage(AuthMessage.BUG.toString());
			event.setCancelled(true);
			return;
		}
		if(cache != null && (!(cache.isAuthed()))) {
			this.message(player);
			event.setCancelled(true);
			return;
		}
		if(this.ga.containsCreds(uuid)) {
			event.setCancelled(true);
		}
	}

	private void message(final Player player) {
		AuthUtil.alertOne(player, this.lm.getPlayerNotAuthed());
	}

}
