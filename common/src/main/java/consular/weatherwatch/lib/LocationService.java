package consular.weatherwatch.lib;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LocationService {
    private final Config config;

    public LocationService() {
        this.config = new Config(null);
    }

    public LocationService(Config config) {
        this.config = config == null ? new Config(null) : config;
    }

    public String getPublicIP() {
        try {
            URL url = new URL("https://api64.ipify.org?format=json");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();
                return jsonResponse.get("ip").getAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public double[] getServerLocation() {
        try {
            String ip = getPublicIP();
            String override = config.getIpOverride();
            if (override != null && !"server".equalsIgnoreCase(override)) {
                ip = override;
            }
            if (ip == null) {
                System.err.println("Failed to retrieve public IP.");
                return null;
            }

            URL url = new URL("http://ip-api.com/json/" + ip);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                JsonObject jsonResponse = JsonParser.parseReader(reader).getAsJsonObject();

                if (jsonResponse.has("status") && "fail".equals(jsonResponse.get("status").getAsString())) {
                    System.err.println("Failed to retrieve location: " + jsonResponse.get("message").getAsString());
                    return null;
                }

                double latitude = jsonResponse.get("lat").getAsDouble();
                double longitude = jsonResponse.get("lon").getAsDouble();
                return new double[]{latitude, longitude};
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
