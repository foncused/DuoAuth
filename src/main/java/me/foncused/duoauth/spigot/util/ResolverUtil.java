package me.foncused.duoauth.spigot.util;

import me.foncused.duoauth.spigot.lib.foncused.Resolver;
import org.bukkit.Bukkit;

import java.util.Objects;
import java.util.UUID;

public class ResolverUtil {

    public static String getName(final UUID uuid) {
        try {
            return Resolver.getName(uuid);
        } catch(final NullPointerException e) {
            return uuid.toString();
        }
    }

    public static UUID getUniqueId(final String name) {
        try {
            return Objects.requireNonNull(Bukkit.getPlayer(name)).getUniqueId();
        } catch(final Exception e) {
            return Resolver.getUUID(name);
        }
    }

}
