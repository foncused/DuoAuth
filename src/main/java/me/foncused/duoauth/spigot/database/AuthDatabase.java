package me.foncused.duoauth.spigot.database;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.foncused.duoauth.spigot.DuoAuth;
import me.foncused.duoauth.spigot.enumerable.DatabaseProperty;
import me.foncused.duoauth.spigot.util.AuthUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuthDatabase {

	private final DuoAuth plugin;

	public AuthDatabase(final DuoAuth plugin) {
		this.plugin = plugin;
	}

	public JsonElement readProperty(final UUID uuid, final DatabaseProperty property) {
		final JsonObject object = this.read(uuid);
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p);
		}
		this.readError(uuid, property);
		return null;
	}

	private JsonObject read(final UUID uuid) {
		try {
			final FileReader reader = new FileReader(new File(this.getJsonPath(uuid)));
			final JsonObject object = new JsonParser().parse(reader).getAsJsonObject();
			reader.close();
			return object;
		} catch(final IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Set<UUID> readAll() {
		final File[] files = new File(this.getDataFolder()).listFiles();
		if(files != null) {
			final Set<UUID> uuids = new HashSet<>();
			for(final File file : files) {
				final String name = file.getName();
				if(name.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.json$")) {
					uuids.add(UUID.fromString(name.split("\\.")[0]));
				}
			}
			return Collections.unmodifiableSet(uuids);
		}
		return null;
	}

	private void readError(final UUID uuid, final DatabaseProperty property) {
		AuthUtil.consoleSevere(this.getJsonFile(uuid) + ": Unable to read property '" + property.toString() + "' from file");
	}

	public <O> boolean writeProperty(final UUID uuid, final DatabaseProperty property, final O data) {
		final JsonObject object = this.read(uuid);
		if(object != null) {
			final String p = property.toString();
			object.add(p, new Gson().toJsonTree(data));
			final boolean written = this.write(uuid, object);
			if(written) {
				AuthUtil.console(this.getJsonFile(uuid) + ": " + p + " -> " + data);
				return true;
			} else {
				this.writeError(uuid, property);
			}
		}
		this.writeError(uuid, property);
		return false;
	}

	public boolean write(
		final UUID uuid,
		final String password,
		final String secret,
		final boolean authed,
		final int attempts,
		final InetAddress ip
	) {
		final JsonObject object = new JsonObject();
		object.addProperty(DatabaseProperty.PASSWORD.toString(), password);
		object.addProperty(DatabaseProperty.SECRET.toString(), secret);
		object.addProperty(DatabaseProperty.AUTHED.toString(), authed);
		object.addProperty(DatabaseProperty.ATTEMPTS.toString(), attempts);
		object.addProperty(DatabaseProperty.IP.toString(), ip.getHostAddress());
		object.addProperty(DatabaseProperty.TIMESTAMP.toString(), AuthUtil.getFormattedTime(AuthUtil.getDateFormat()));
		return this.write(uuid, object);
	}

	private boolean write(final UUID uuid, final JsonObject object) {
		try {
			final String dataPath = this.getDataFolder();
			final File data = new File(dataPath);
			if(!(data.exists()) && (!(data.mkdirs()))) {
				AuthUtil.consoleSevere("Unable to create directory " + dataPath);
				return false;
			}
			if(data.exists()) {
				final String jsonPath = this.getJsonPath(uuid);
				final File json = new File(jsonPath);
				if(!(json.exists()) && (!(json.createNewFile()))) {
					AuthUtil.consoleSevere("Unable to create file " + jsonPath);
					return false;
				}
				final FileWriter writer = new FileWriter(json);
				writer.write(object.toString());
				writer.close();
				return true;
			}
			return false;
		} catch(final IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private void writeError(final UUID uuid, final DatabaseProperty property) {
		AuthUtil.consoleSevere(this.getJsonFile(uuid) + ": Unable to write property '" + property.toString() + "' to file");
	}

	public boolean contains(final UUID uuid) {
		return new File(this.getJsonPath(uuid)).exists();
	}

	public boolean delete(final UUID uuid) {
		return new File(this.getJsonPath(uuid)).delete();
	}

	private String getJsonPath(final UUID uuid) {
		return this.getDataFolder() + this.getJsonFile(uuid);
	}

	private String getJsonFile(final UUID uuid) {
		return uuid.toString() + ".json";
	}

	private String getDataFolder() {
		return this.plugin.getDataFolder().getPath() + "/data/";
	}

}
