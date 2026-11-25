/*
 * Author: DenySilver (Reforged Pixel Dungeon)
 * License: GPLv3
 */

package com.shatteredpixel.shatteredpixeldungeon.online;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.watabou.noosa.Game;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OnlineApiClient {

    public static final String BASE_URL = OnlineConfig.BASE_URL;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "OnlineApiClient-Worker");
        t.setDaemon(true);
        return t;
    });

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }

    public interface ApiCallback {
        void onSuccess(int statusCode, JsonValue body);
        void onError(int statusCode, String errorMessage);
    }

    public static void get(String path, ApiCallback callback) {
        request("GET", path, null, callback);
    }

    public static void post(String path, String jsonBody, ApiCallback callback) {
        request("POST", path, jsonBody, callback);
    }

    private static void request(String method, String path, String jsonBody, ApiCallback callback) {
        if (BASE_URL == null || BASE_URL.isEmpty()) {
            if (callback != null) {
                Game.runOnRenderThread(() -> callback.onError(-1, "Server URL not configured"));
            }
            return;
        }
        EXECUTOR.submit(() -> {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + path);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod(method);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                conn.setRequestProperty("Accept", "application/json");

                String token = SPDSettings.onlineToken();
                if (!token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Bearer " + token);
                }

                if (jsonBody != null) {
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                    }
                }

                int code = conn.getResponseCode();
                InputStream stream = (code >= 200 && code < 300)
                        ? conn.getInputStream()
                        : conn.getErrorStream();

                String rawResponse = readStream(stream);
                JsonValue parsed = rawResponse.isEmpty() ? null : new JsonReader().parse(rawResponse);

                if (code >= 200 && code < 300) {
                    if (callback != null) {
                        Game.runOnRenderThread(() -> callback.onSuccess(code, parsed));
                    }
                } else {
                    if (code == 401) {
                        SPDSettings.onlineLogout();
                    }
                    String msg = (parsed != null && parsed.has("message"))
                            ? parsed.getString("message")
                            : "HTTP " + code;
                    if (callback != null) {
                        Game.runOnRenderThread(() -> callback.onError(code, msg));
                    }
                }
            } catch (Exception e) {
                if (callback != null) {
                    String err = e.getMessage() != null ? e.getMessage() : "Network error";
                    Game.runOnRenderThread(() -> callback.onError(-1, err));
                }
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        });
    }

    private static String readStream(InputStream is) throws Exception {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}