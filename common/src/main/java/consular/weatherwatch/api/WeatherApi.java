package consular.weatherwatch.api;

import com.google.gson.JsonObject;

public interface WeatherApi {
    JsonObject fetchForecast(double latitude, double longitude);
    JsonObject fetchAstronomy(double latitude, double longitude, String date);
}

