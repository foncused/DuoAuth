package me.foncused.duoauth.spigot.lib.foncused;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class Resolver {

    public static String getName(final UUID uuid) {
        try {
            final HttpURLConnection http = (HttpURLConnection) new URL(
                    String.format(
                            "https://api.mojang.com/user/profile/%s",
                            uuid.toString()
                    )
            ).openConnection();
            http.setRequestMethod("GET");
            http.connect();
            final InputStreamReader isr = new InputStreamReader(http.getInputStream());
            final BufferedReader br = new BufferedReader(isr);
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }
            br.close();
            isr.close();
            http.disconnect();
            return (String) ((JSONObject) new JSONParser().parse(responseBuilder.toString())).get("name");
        } catch(final Exception e) {
            return null;
        }
    }

    public static UUID getUUID(final String name) {
        try {
            final HttpURLConnection http = (HttpURLConnection) new URL(
                    String.format("https://api.mojang.com/users/profiles/minecraft/%s", name)
            ).openConnection();
            http.setRequestMethod("GET");
            http.connect();
            final InputStreamReader isr = new InputStreamReader(http.getInputStream());
            final BufferedReader br = new BufferedReader(isr);
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                responseBuilder.append(line);
            }
            br.close();
            isr.close();
            http.disconnect();
            final StringBuilder sb = new StringBuilder((String) ((JSONObject) new JSONParser().parse(responseBuilder.toString())).get("id"));
            sb.insert(8, "-");
            sb.insert(13, "-");
            sb.insert(18, "-");
            sb.insert(23, "-");
            return UUID.fromString(sb.toString());
        } catch(final Exception e) {
            return null;
        }
    }

}
