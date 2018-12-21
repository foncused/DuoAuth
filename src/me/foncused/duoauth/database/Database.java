package me.foncused.duoauth.database;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.foncused.duoauth.DuoAuth;
import me.foncused.duoauth.enumerable.DatabaseOption;
import me.foncused.duoauth.utility.DuoAuthUtilities;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Database {

	private DuoAuth plugin;
	private Map<String, Boolean> players;
	private DatabaseOption option;
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

	public Database(final DuoAuth plugin, final Map<String, Boolean> players, final DatabaseOption option) {
		this.plugin = plugin;
		this.players = players;
		this.option = option;
	}

	public synchronized String readPassword(final String uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.PASSWORD;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsString();
		}
		this.readError(uuid, property);
		return null;
	}

	public synchronized boolean writePassword(final String uuid, final String password) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.PASSWORD;
		if(object != null) {
			object.addProperty(property.toString(), password);
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}

	public synchronized String readPIN(final String uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.PIN;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsString();
		}
		this.readError(uuid, property);
		return null;
	}

	public synchronized boolean writePIN(final String uuid, final String pin) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.PIN;
		if(object != null) {
			object.addProperty(property.toString(), pin);
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}

	public synchronized boolean readAuthed(final String uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.AUTHED;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsBoolean();
		}
		this.readError(uuid, property);
		return true;
	}

	public synchronized boolean writeAuthed(final String uuid, final boolean authed) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.AUTHED;
		if(object != null) {
			object.addProperty(property.toString(), authed);
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}

	public synchronized int readAttempts(final String uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.ATTEMPTS;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsInt();
		}
		this.readError(uuid, property);
		return -1;
	}

	public synchronized boolean writeAttempts(final String uuid, final int attempts) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.ATTEMPTS;
		if(object != null) {
			object.addProperty(property.toString(), attempts);
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}

	public synchronized String readAddress(final String uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.IP;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsString();
		}
		this.readError(uuid, property);
		return null;
	}

	public synchronized boolean writeAddress(final String uuid, final String ip) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.IP;
		if(object != null) {
			object.addProperty(property.toString(), ip);
			return this.write(uuid, object);
		}
		this.writeError(uuid, property);
		return false;
	}

	public synchronized String readTimestamp(final String uuid) {
		final JsonObject object = this.read(uuid);
		final Property property = Property.TIMESTAMP;
		final String p = property.toString();
		if(object != null && object.has(p)) {
			return object.get(p).getAsString();
		}
		this.readError(uuid, property);
		return null;
	}

	public synchronized boolean writeTimestamp(final String uuid) {
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

	private void readError(final String uuid, final Property property) {
		DuoAuthUtilities.consoleSevere("Unable to read property '" + property.toString() + "' from file " + this.getJsonPath(uuid));
	}

	private void writeError(final String uuid, final Property property) {
		DuoAuthUtilities.consoleSevere("Unable to write property '" + property.toString() + "' from file " + this.getJsonPath(uuid));
	}

	public synchronized boolean contains(final String uuid) {
		return new File(this.getJsonPath(uuid)).exists();
	}

	public synchronized boolean delete(final String uuid) {
		return new File(this.getJsonPath(uuid)).delete();
	}

	private synchronized JsonObject read(final String uuid) {
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

	public synchronized Set<String> readAll() {
		final File[] files = new File(this.getDataFolder()).listFiles();
		if(files != null) {
			final Set<String> uuids = new HashSet<>();
			for(final File file : files) {
				final String name = file.getName();
				if(name.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.json$")) {
					uuids.add(name.split("\\.")[0]);
				}
			}
			return uuids;
		}
		return null;
	}

	public boolean write(final String uuid, final String password, final String pin, final int attempts, final String ip) {
		final JsonObject object = new JsonObject();
		object.addProperty(Property.PASSWORD.toString(), password);
		object.addProperty(Property.PIN.toString(), pin);
		object.addProperty(Property.AUTHED.toString(), this.players.getOrDefault(uuid, false));
		object.addProperty(Property.ATTEMPTS.toString(), attempts);
		object.addProperty(Property.IP.toString(), ip);
		object.addProperty(Property.TIMESTAMP.toString(), this.getFormattedTime(this.DATE_FORMAT));
		return this.write(uuid, object);
	}

	private synchronized boolean write(final String uuid, final JsonObject object) {
		try {
			final String dataPath = this.getDataFolder();
			final File data = new File(dataPath);
			if(!(data.exists()) && (!(data.mkdirs()))) {
				DuoAuthUtilities.consoleSevere("Unable to create directory " + dataPath);
				return false;
			}
			if(data.exists()) {
				final String jsonPath = this.getJsonPath(uuid);
				final File json = new File(jsonPath);
				if(!(json.exists()) && (!(json.createNewFile()))) {
					DuoAuthUtilities.consoleSevere("Unable to create file " + jsonPath);
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

	private String getJsonPath(final String uuid) {
		return this.getDataFolder() + uuid + ".json";
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
