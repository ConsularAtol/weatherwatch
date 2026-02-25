package consular.weatherwatch.lib;

import com.google.gson.JsonObject;

public class WeatherResult {
    private final JsonObject raw;
    private final String timezone;
    private final int weatherCode;
    private final double precipitation;
    private final String conditionText;
    private final boolean isRaining;
    private final boolean isThundering;
    private final boolean isSevere;

    public WeatherResult(JsonObject raw, String timezone, int weatherCode, double precipitation, String conditionText, boolean isRaining, boolean isThundering, boolean isSevere) {
        this.raw = raw;
        this.timezone = timezone;
        this.weatherCode = weatherCode;
        this.precipitation = precipitation;
        this.conditionText = conditionText;
        this.isRaining = isRaining;
        this.isThundering = isThundering;
        this.isSevere = isSevere;
    }

    public JsonObject getRaw() { return raw; }
    public String getTimezone() { return timezone; }
    public int getWeatherCode() { return weatherCode; }
    public double getPrecipitation() { return precipitation; }
    public String getConditionText() { return conditionText; }
    public boolean isRaining() { return isRaining; }
    public boolean isThundering() { return isThundering; }
    public boolean isSevere() { return isSevere; }
}
