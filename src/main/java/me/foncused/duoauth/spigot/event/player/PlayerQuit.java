package me.foncused.duoauth.spigot.event.player;

import me.foncused.duoauth.spigot.DuoAuth;
import me.foncused.duoauth.spigot.lib.wstrange.GoogleAuth;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class PlayerQuit implements Listener {

	private final DuoAuth plugin;
	private final GoogleAuth ga;

	public PlayerQuit(final DuoAuth plugin) {
		this.plugin = plugin;
		this.ga = this.plugin.getGoogleAuth();
	}

	@EventHandler
	public void onPlayerQuit(final PlayerQuitEvent event) {
		final Player player = event.getPlayer();
		final UUID uuid = player.getUniqueId();
		this.plugin.removePlayer(uuid);
		this.ga.removeCreds(uuid);
		final PlayerInventory inventory = player.getInventory();
		for(int i = 0; i < inventory.getSize(); i++) {
			final ItemStack stack = inventory.getItem(i);
			if(stack != null && stack.getType() == Material.FILLED_MAP && stack.hasItemMeta()) {
				final MapMeta meta = (MapMeta) stack.getItemMeta();
				if(meta != null && meta.hasMapView()) {
					final MapView view = meta.getMapView();
					if(view != null) {
						view.getRenderers().forEach(view::removeRenderer);
					}
					if(meta.getPersistentDataContainer().has(new NamespacedKey(this.plugin, "duoauth"), PersistentDataType.STRING)) {
						inventory.setItem(i, null);
					}
				}
			}
		}
	}

}
