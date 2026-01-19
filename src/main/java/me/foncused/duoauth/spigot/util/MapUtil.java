package me.foncused.duoauth.spigot.util;

import me.foncused.duoauth.spigot.DuoAuth;
import me.foncused.duoauth.spigot.lib.wstrange.GoogleAuth;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.image.BufferedImage;
import java.util.*;

public class MapUtil {

    private static DuoAuth plugin;
    private static GoogleAuth ga;
    private static Map<UUID, Set<UUID>> rendered;

    public MapUtil(final DuoAuth plugin) {
        MapUtil.plugin = plugin;
        MapUtil.ga = MapUtil.plugin.getGoogleAuth();
        MapUtil.rendered = new HashMap<>();
    }

    public static void showQr(final Player player) {
        final UUID uuid = player.getUniqueId();
        if(rendered.containsKey(uuid)) {
            return;
        }
        rendered.put(uuid, new HashSet<>());
        final BufferedImage qr = ga.generateQrImage(uuid);
        final MapView view = Bukkit.createMap(player.getWorld());
        view.getRenderers().forEach(view::removeRenderer);
        view.addRenderer(new MapRenderer() {
            @Override
            public void render(final MapView mv, final MapCanvas mc, final Player p) {
                final Set<UUID> uuids = rendered.get(uuid);
                final UUID u = p.getUniqueId();
                if(uuids.contains(u)) {
                    return;
                }
                if(qr != null) {
                    mc.drawImage(0, 0, qr);
                    uuids.add(u);
                }
            }
        });
        final ItemStack map = new ItemStack(Material.FILLED_MAP, 1);
        final MapMeta meta = (MapMeta) map.getItemMeta();
        assert meta != null;
        meta.setMapView(view);
        meta.setDisplayName(DuoAuth.PREFIX);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "duoauth"), PersistentDataType.STRING, DuoAuth.PREFIX);
        map.setItemMeta(meta);
        new BukkitRunnable() {
            int slotPrevious = 0;
            @Override
            public void run() {
                final PlayerInventory inventory = player.getInventory();
                ItemStack stack = inventory.getItemInOffHand();
                if(!(stack.equals(map))) {
                    stack = inventory.getItem(slotPrevious);
                }
                if(stack != null && stack.equals(map)) {
                    if(!(ga.containsCreds(uuid))) {
                        inventory.setItem(slotPrevious, null);
                        rendered.remove(uuid);
                        this.cancel();
                        return;
                    }
                    if(slotPrevious < 9) {
                        inventory.setHeldItemSlot(slotPrevious);
                        final Location location = player.getLocation();
                        if(inventory.getItemInOffHand().getType() == Material.AIR && location.getPitch() != 90) {
                            location.setPitch(90);
                            player.teleport(location);
                        }
                    }
                    return;
                }
                int slot = 0;
                if(inventory.getItemInOffHand().getType() == Material.AIR) {
                    inventory.setItemInOffHand(map);
                    slot = 40;
                } else {
                    if(!(inventory.addItem(map).isEmpty())) {
                        return;
                    }
                    for(int i = 0; i < inventory.getSize(); i++) {
                        final ItemStack s = inventory.getItem(i);
                        if(s != null && s.equals(map)) {
                            slot = i;
                        }
                    }
                }
                slotPrevious = slot;
            }
        }.runTaskTimer(plugin, 0, 5);
    }

}
