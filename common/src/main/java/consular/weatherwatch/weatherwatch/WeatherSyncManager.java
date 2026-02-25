package consular.weatherwatch.weatherwatch;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import consular.weatherwatch.lib.WeatherLibrary;
import consular.weatherwatch.lib.WeatherResult;
import consular.weatherwatch.lib.MoonResult;
import consular.weatherwatch.lib.TimeLibrary;
import consular.weatherwatch.lib.Config;

import java.util.concurrent.CompletableFuture;

public class WeatherSyncManager {
    private static final WeatherLibrary library = new WeatherLibrary();
    private static final TimeLibrary timeLib = new TimeLibrary();

    public static void syncWeather(ServerLevel world) {
        try {
            MinecraftServer server = world.getServer();
            if (server == null) return;

            boolean needWeather = Config.DEFAULT.isSyncWeatherEnabled();
            boolean needMoon = Config.DEFAULT.isSyncMoonPhaseEnabled();
            if (!needWeather && !needMoon) return;

            CompletableFuture.supplyAsync(() -> {
                WeatherResult res = null;
                MoonResult moon = null;
                try {
                    if (needWeather) res = library.fetchWeatherForServer();
                } catch (Exception ignored) { res = null; }
                try {
                    if (needMoon) moon = library.fetchMoonPhaseForServer();
                } catch (Exception ignored) { moon = null; }
                return new Object[]{res, moon};
            }).thenAccept(resultArr -> {
                try {
                    WeatherResult res = (WeatherResult) resultArr[0];
                    MoonResult moon = (MoonResult) resultArr[1];
                    server.execute(() -> applyResults(world, res, moon));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void applyResults(ServerLevel world, WeatherResult res, MoonResult moon) {
        try {
            if (res != null) {
                WeatherWatch.cachedWeather = res.getRaw();
                if (Config.DEFAULT.isSyncWeatherEnabled()) {
                    world.setWeatherParameters(0, 24000, res.isRaining(), res.isThundering());
                }
                if (res.isSevere()) {
                    broadcastSevereWeather(world.getServer(), res.getConditionText());
                }
            }

            if (moon != null && Config.DEFAULT.isSyncMoonPhaseEnabled()) {
                double decimal = moon.getDecimalPhase();
                int idx = moon.getMinecraftIndex();
                WeatherWatch.cachedMoonPhase = new JsonObject();
                WeatherWatch.cachedMoonPhase.addProperty("moon_phase", decimal);
                WeatherWatch.cachedMoonPhase.addProperty("minecraft_moon_phase", idx);
                long newTime = timeLib.computeWorldTimeForMoonIndex(world.getDayTime(), idx);
                world.setDayTime(newTime);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void broadcastSevereWeather(MinecraftServer server, String conditionText) {
        Component warning = Component.literal("WARNING: The server's location is currently undergoing SEVERE WEATHER. Please stay safe if you're in this area!")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

        Component details = Component.literal("Detected condition: " + conditionText)
                .withStyle(ChatFormatting.RED);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(warning, false);
            player.sendSystemMessage(details, false);
        }
    }
}
