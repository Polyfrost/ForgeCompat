package gg.essential.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public final class WebUtil {
    public static final WebUtil INSTANCE = new WebUtil();

    public static String fetchString(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) URI.create(url.replace(" ", "%20")).toURL().openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/4.76 (ForgeCompat)");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            try (InputStream in = connection.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException | IllegalArgumentException e) {
            return "Failed to fetch";
        }
    }
}
