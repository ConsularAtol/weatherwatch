package consular.weatherwatch.weatherwatch;

import com.google.gson.JsonObject;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import sereneseasons.api.SSGameRules;

import java.time.ZoneId;

import consular.weatherwatch.lib.Config;

public final class WeatherWatch {
    public static final String MOD_ID = "weatherwatch";

    public static String TIMEZONE_ID = "";
    public static int weatherTickCounter = 0;
    public static int seasonTickCounter = 0;
    public static JsonObject cachedLocation;
    public static JsonObject cachedWeather;
    public static JsonObject cachedMoonPhase;

    public static void init() {
        WeatherWatchCommands.register();

        if (!Platform.isModLoaded("sereneseasons")) {
            System.out.println("Serene Seasons is not installed! Ignoring season syncing.");
        }

        LifecycleEvent.SERVER_STARTED.register(server -> {
            server.overworld().getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(true, server);

            double[] location = LocationFetcher.getServerLocation();
            if (location != null) {
                System.out.println("Server is located at: Lat " + location[0] + ", Lon " + location[1]);
                JsonObject locObj = new JsonObject();
                locObj.addProperty("latitude", location[0]);
                locObj.addProperty("longitude", location[1]);
                String ip = LocationFetcher.getPublicIP();
                if (ip != null) locObj.addProperty("ip", ip);
                WeatherWatch.cachedLocation = locObj;
            } else {
                System.err.println("Failed to fetch server location.");
                WeatherWatch.cachedLocation = null;
            }
            if (Config.DEFAULT.isSyncWeatherEnabled()) {
                server.overworld().getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
            } else {
                server.overworld().getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(true, server);
            }
            WeatherSyncManager.syncWeather(server.overworld());
            if (Config.DEFAULT.isSyncSeasonsEnabled() && Platform.isModLoaded("sereneseasons")) {
                for (ServerLevel world : server.getAllLevels()) {
                    SeasonSyncManager.syncSeasons(world);
                }
            }
        });

        TickEvent.SERVER_POST.register(server -> {
            if (Config.DEFAULT.isSyncTimeEnabled()) {
                server.overworld().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
            } else {
                server.overworld().getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(true, server);
            }
            if (Config.DEFAULT.isSyncTimeEnabled()) {
                MinecraftTimeSync.syncRealTimeToMinecraft(server.overworld());
            }
            WeatherWatch.weatherTickCounter++;
            if (WeatherWatch.weatherTickCounter >= 6000 && (Config.DEFAULT.isSyncWeatherEnabled() || Config.DEFAULT.isSyncMoonPhaseEnabled())) {
                WeatherSyncManager.syncWeather(server.overworld());
                WeatherWatch.weatherTickCounter = 0;
            }

            WeatherWatch.seasonTickCounter++;
            if (Config.DEFAULT.isSyncSeasonsEnabled() && Platform.isModLoaded("sereneseasons") && WeatherWatch.seasonTickCounter >= 1200) {
                WeatherWatch.seasonTickCounter = 0;
                server.overworld().getGameRules().getRule(SSGameRules.RULE_DOSEASONCYCLE).set(true, server);
                for (ServerLevel world : server.getAllLevels()) {
                    SeasonSyncManager.syncSeasons(world);
                }
            }
        });
    }

    public static java.time.ZoneId getZoneId() {
        if (TIMEZONE_ID == null || TIMEZONE_ID.trim().isEmpty()) {
            return ZoneId.systemDefault();
        }
        String tz = TIMEZONE_ID.trim();
        try {
            return ZoneId.of(tz);
        } catch (Exception e) {
            System.out.println("Invalid timezone ID '" + TIMEZONE_ID + "', falling back to system default");
            return ZoneId.systemDefault();
        }
    }
}
