package consular.weatherwatch.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class DefaultWeatherApiClient implements WeatherApi {
    private static final String USER_AGENT = "Mozilla/5.0";

    @Override
    public JsonObject fetchForecast(double latitude, double longitude) {
        try {
            String weatherApiUrl = String.format(Locale.US,
                    "https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&hourly=precipitation,cloudcover&current_weather=true&timezone=auto",
                    latitude, longitude);

            HttpURLConnection conn = (HttpURLConnection) new URL(weatherApiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                try (java.io.InputStream err = conn.getErrorStream()) {
                    if (err != null) {
                        java.util.Scanner s = new java.util.Scanner(err).useDelimiter("\\A");
                        String body = s.hasNext() ? s.next() : "<empty>";
                        System.out.println("Weather API response body: " + (body.length() > 1000 ? body.substring(0, 1000) + "..." : body));
                    }
                } catch (Exception ignored) {}
                System.out.println("Weather API returned HTTP " + code + " for URL: " + weatherApiUrl);
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception e) {
            System.out.println("Error fetching forecast: " + e.getMessage());
            return null;
        }
    }

    @Override
    public JsonObject fetchAstronomy(double latitude, double longitude, String date) {
        try {
            String astroApiUrl = String.format(Locale.US,
                    "https://api.open-meteo.com/v1/astronomy?latitude=%.6f&longitude=%.6f&date=%s&timezone=auto",
                    latitude, longitude, date);

            HttpURLConnection conn = (HttpURLConnection) new URL(astroApiUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", USER_AGENT);

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) {
                try (java.io.InputStream err = conn.getErrorStream()) {
                    if (err != null) {
                        java.util.Scanner s = new java.util.Scanner(err).useDelimiter("\\A");
                        String body = s.hasNext() ? s.next() : "<empty>";
                        System.out.println("Astronomy API response body: " + (body.length() > 1000 ? body.substring(0, 1000) + "..." : body));
                    }
                } catch (Exception ignored) {}
                System.out.println("Astronomy API returned HTTP " + code + " for URL: " + astroApiUrl);
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (Exception e) {
            System.out.println("Error fetching astronomy: " + e.getMessage());
            return null;
        }
    }
}

