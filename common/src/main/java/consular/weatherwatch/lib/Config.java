package consular.weatherwatch.lib;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Path DEFAULT_PATH = Path.of("config", "weather_sync.json");
    private JsonObject config;
    private final Path path;

    public static final Config DEFAULT = new Config(DEFAULT_PATH);

    public Config(Path path) {
        this.path = path == null ? DEFAULT_PATH : path;
        load();
    }

    public Config() { this(null); }

    private void load() {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path.getParent());
                String defaultJson = "{\n" +
                        "  \"syncWeather\": true,\n" +
                        "  \"syncSeasons\": true,\n" +
                        "  \"syncTime\": false,\n" +
                        "  \"syncMoonPhase\": false,\n" +
                        "  \"ip-override\": \"server\"\n" +
                        "}";
                Files.writeString(path, defaultJson);
            }
            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(path))) {
                config = JsonParser.parseReader(reader).getAsJsonObject();
            }
            boolean changed = false;
            if (!config.has("syncWeather")) { config.addProperty("syncWeather", true); changed = true; }
            if (!config.has("syncSeasons")) { config.addProperty("syncSeasons", true); changed = true; }
            if (!config.has("syncTime")) { config.addProperty("syncTime", false); changed = true; }
            if (!config.has("syncMoonPhase")) { config.addProperty("syncMoonPhase", false); changed = true; }
            if (!config.has("ip-override")) { config.addProperty("ip-override", "server"); changed = true; }
            if (changed) persist();
        } catch (Exception e) {
            e.printStackTrace();
            config = new JsonObject();
            config.addProperty("syncWeather", true);
            config.addProperty("syncSeasons", true);
            config.addProperty("syncTime", false);
            config.addProperty("syncMoonPhase", false);
            config.addProperty("ip-override", "server");
        }
    }

    public boolean isSyncWeatherEnabled() { return config.get("syncWeather").getAsBoolean(); }
    public boolean isSyncSeasonsEnabled() { return config.get("syncSeasons").getAsBoolean(); }
    public boolean isSyncTimeEnabled() { return config.get("syncTime").getAsBoolean(); }
    public boolean isSyncMoonPhaseEnabled() { return config.get("syncMoonPhase").getAsBoolean(); }
    public String getIpOverride() { return config.get("ip-override").getAsString(); }

    public void set(String key, boolean value) { config.addProperty(key, value); persistQuietly(); }
    public void set(String key, String value) { config.addProperty(key, value); persistQuietly(); }

    private void persistQuietly() { try { persist(); } catch (Exception e) { e.printStackTrace(); } }
    private void persist() throws Exception { Files.createDirectories(path.getParent()); Files.writeString(path, config.toString()); }
}
