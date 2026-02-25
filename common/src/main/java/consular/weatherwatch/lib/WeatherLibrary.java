package consular.weatherwatch.lib;

import com.google.gson.JsonObject;
import consular.weatherwatch.api.WeatherApi;
import consular.weatherwatch.api.DefaultWeatherApiClient;
import consular.weatherwatch.util.MoonCalculator;
import consular.weatherwatch.weatherwatch.WeatherWatch;

import java.time.LocalDate;

public class WeatherLibrary {
    private final WeatherApi api;

    public WeatherLibrary() {
        this.api = new DefaultWeatherApiClient();
    }

    public WeatherLibrary(WeatherApi api) {
        this.api = api;
    }

    public WeatherResult fetchWeather(double lat, double lon) {
        JsonObject weather = api.fetchForecast(lat, lon);
        if (weather == null) return null;
        String tz = weather.has("timezone") ? weather.get("timezone").getAsString() : null;
        int weatherCode = -1;
        double precipitation = 0.0;
        try {
            if (weather.has("current_weather")) {
                JsonObject cw = weather.getAsJsonObject("current_weather");
                weatherCode = cw.get("weathercode").getAsInt();
                String currentTime = cw.get("time").getAsString();
                if (weather.has("hourly")) {
                    JsonObject hourly = weather.getAsJsonObject("hourly");
                    if (hourly.has("time") && hourly.has("precipitation")) {
                        for (int i = 0; i < hourly.getAsJsonArray("time").size(); i++) {
                            if (hourly.getAsJsonArray("time").get(i).getAsString().equals(currentTime)) {
                                precipitation = hourly.getAsJsonArray("precipitation").get(i).getAsDouble();
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {}

        String conditionText = mapWeatherCodeToText(weatherCode).toLowerCase();
        boolean isRaining = isPrecipitationWeatherCode(weatherCode) || precipitation > 0;
        boolean isThundering = isThunderWeatherCode(weatherCode);
        boolean isSevere = isThundering || containsSevereKeyword(conditionText);

        return new WeatherResult(weather, tz, weatherCode, precipitation, conditionText, isRaining, isThundering, isSevere);
    }

    public WeatherResult fetchWeatherForServer() {
        try {
            LocationService locService = new LocationService(Config.DEFAULT);
            double[] loc = locService.getServerLocation();
            if (loc == null) return null;
            return fetchWeather(loc[0], loc[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public String fetchTimezoneForServer() {
        try {
            WeatherResult res = fetchWeatherForServer();
            if (res == null) return null;
            String tz = res.getTimezone();
            return (tz == null || tz.trim().isEmpty()) ? null : tz.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean containsSevereKeyword(String conditionText) {
        String[] severeKeywords = {"tornado","hurricane","cyclone","blizzard","severe thunderstorm","thundersquall","violent storm"};
        for (String k : severeKeywords) if (conditionText.contains(k)) return true;
        return false;
    }

    private static String mapWeatherCodeToText(int code) {
        return switch (code) {
            case 0 -> "Clear";
            case 1 -> "Mainly clear";
            case 2 -> "Partly cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Fog";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing Drizzle";
            case 61, 63, 65 -> "Rain";
            case 66, 67 -> "Freezing Rain";
            case 71, 73, 75 -> "Snow";
            case 77 -> "Snow Grains";
            case 80, 81, 82 -> "Showers";
            case 85, 86 -> "Snow Showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with hail";
            default -> "Unknown";
        };
    }

    private static boolean isPrecipitationWeatherCode(int code) {
        return (code >= 51 && code <= 67) || (code >= 71 && code <= 77) || (code >= 80 && code <= 86) || (code == 95) || (code == 96) || (code == 99);
    }

    private static boolean isThunderWeatherCode(int code) {
        return code >= 95;
    }

    public MoonResult fetchMoonPhase(double lat, double lon) {
        String date = LocalDate.now(WeatherWatch.getZoneId()).toString();
        JsonObject astro = api.fetchAstronomy(lat, lon, date);
        double decimal = -1.0;
        if (astro != null && astro.has("moon_phase")) {
            try { decimal = astro.get("moon_phase").getAsDouble(); } catch (Exception ignored) {}
        }
        if (decimal < 0) {
            decimal = MoonCalculator.computeMoonPhaseDecimal(LocalDate.now(WeatherWatch.getZoneId()));
        }
        int idx = MoonCalculator.toMinecraftIndex(decimal);
        return new MoonResult(decimal, idx);
    }

    public MoonResult fetchMoonPhaseForServer() {
        try {
            LocationService locService = new LocationService(Config.DEFAULT);
            double[] loc = locService.getServerLocation();
            if (loc == null) return null;
            return fetchMoonPhase(loc[0], loc[1]);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
