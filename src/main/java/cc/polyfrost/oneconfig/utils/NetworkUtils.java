package cc.polyfrost.oneconfig.utils;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public final class NetworkUtils {
    private NetworkUtils() {
    }

    public static String getString(String url) {
        JsonElement element = null;
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "OneConfig/1.0.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
                StringBuilder out = new StringBuilder();
                char[] buffer = new char[4096];
                for (int n; (n = reader.read(buffer)) != -1; ) out.append(buffer, 0, n);
                return out.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static JsonElement getJsonElement(String url) {
        String body = getString(url);
        try {
            return body == null ? null : new JsonParser().parse(body);
        } catch (Exception e) {
            return null;
        }
    }
}
