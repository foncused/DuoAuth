package me.foncused.duoauth.database;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.foncused.duoauth.DuoAuth;
import me.foncused.duoauth.config.ConfigManager;
import me.foncused.duoauth.util.AuthUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AuthDatabase {

	private final DuoAuth plugin;
	private final Map<UUID, Boolean> players;
	private final ConfigManager cm;
	private final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss:SSS";

	private enum Property {

		PASSWORD("Password"),
		PIN("PIN"),
		AUTHED("Authed"),
		ATTEMPTS("Attempts"),
		IP("IP"),
		TIMESTAMP("Timestamp");

		private final String property;

		Property(final String property) {
			this.property = property;
		}

		@Override
		public String toString() {
			return this.property;
		}

	}

	public AuthDatabase(final DuoAuth plugin) {
		this.plugin = plugin;
		this.players = this.plugin.getPlayers();
		this.cm = this.plugin.getConfigManager();
	}

	public synchronized String readPassword(final UUID uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.PASSWORD;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsString();
		}
		this.readError(uuid, property);
		return null;
	}

	public synchronized boolean writePassword(final UUID uuid, final String password) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.PASSWORD;
		if(object != null) {
			object.addProperty(property.toString(), password);
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}

	public synchronized String readPIN(final UUID uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.PIN;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsString();
		}
		this.readError(uuid, property);
		return null;
	}

	public synchronized boolean writePIN(final UUID uuid, final String pin) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.PIN;
		if(object != null) {
			object.addProperty(property.toString(), pin);
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}

	public synchronized boolean readAuthed(final UUID uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.AUTHED;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsBoolean();
		}
		this.readError(uuid, property);
		return true;
	}

	public synchronized boolean writeAuthed(final UUID uuid, final boolean authed) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.AUTHED;
		if(object != null) {
			object.addProperty(property.toString(), authed);
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}

	public synchronized int readAttempts(final UUID uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.ATTEMPTS;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsInt();
		}
		this.readError(uuid, property);
		return -1;
	}

	public synchronized boolean writeAttempts(final UUID uuid, final int attempts) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.ATTEMPTS;
		if(object != null) {
			object.addProperty(property.toString(), attempts);
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}

	public synchronized String readAddress(final UUID uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.IP;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsString();
		}
		this.readError(uuid, property);
		return null;
	}

	public synchronized boolean writeAddress(final UUID uuid, final String ip) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.IP;
		if(object != null) {
			object.addProperty(property.toString(), ip);
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}

	public synchronized String readTimestamp(final UUID uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.TIMESTAMP;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsString();
		}
		this.readError(uuid, property);
		return null;
	}

	public synchronized boolean writeTimestamp(final UUID uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.TIMESTAMP;
		if(object != null) {
			object.addProperty(property.toString(), this.getFormattedTime(this.DATE_FORMAT));
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}

	/*public synchronized <O> O readProperty(final String uuid, final Property property) {
		final JsonObject object = this.read(uuid);
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return (O) object.get(p).getAsJsonObject();
		}
		this.readError(uuid, property);
		return null;
	}

	public synchronized <O> boolean writeProperty(final String uuid, final Property property, final O data) {
		final JsonObject object = this.read(uuid);
		if(object != null) {
			object.add(property.toString(), new Gson().toJsonTree(data));
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}*/

	private void readError(final UUID uuid, final Property property) {
		AuthUtil.consoleSevere("Unable to read property '" + property.toString() + "' from file " + this.getJsonPath(uuid));
	}

	private void writeError(final UUID uuid, final Property property) {
		AuthUtil.consoleSevere("Unable to write property '" + property.toString() + "' from file " + this.getJsonPath(uuid));
	}

	public synchronized boolean contains(final UUID uuid) {
		return new File(this.getJsonPath(uuid)).exists();
	}

	public synchronized boolean delete(final UUID uuid) {
		return new File(this.getJsonPath(uuid)).delete();
	}

	private synchronized JsonObject read(final UUID uuid) {
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

	public synchronized Set<UUID> readAll() {
		final File[] files = new File(this.getDataFolder()).listFiles();
		if(files != null) {
			final Set<UUID> uuids = new HashSet<>();
			for(final File file : files) {
				final String name = file.getName();
				if(name.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.json$")) {
					uuids.add(UUID.fromString(name.split("\\.")[0]));
				}
			}
			return uuids;
		}
		return null;
	}

	public boolean write(final UUID uuid, final String password, final String pin, final int attempts, final String ip) {
		final JsonObject object = new JsonObject();
		object.addProperty(Property.PASSWORD.toString(), password);
		object.addProperty(Property.PIN.toString(), pin);
		object.addProperty(Property.AUTHED.toString(), this.players.getOrDefault(uuid, false));
		object.addProperty(Property.ATTEMPTS.toString(), attempts);
		object.addProperty(Property.IP.toString(), ip);
		object.addProperty(Property.TIMESTAMP.toString(), this.getFormattedTime(this.DATE_FORMAT));
		return this.write(uuid, object);
	}

	private synchronized boolean write(final UUID uuid, final JsonObject object) {
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

	private String getJsonPath(final UUID uuid) {
		return this.getDataFolder() + uuid.toString() + ".json";
	}

	private String getDataFolder() {
		return this.plugin.getDataFolder() + "/data/";
	}

	public String getDateFormat() {
		return this.DATE_FORMAT;
	}

	private String getFormattedTime(final String format) {
		return new SimpleDateFormat(format).format(new Date());
	}

}
