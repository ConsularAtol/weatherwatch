package consular.weatherwatch.weatherwatch;

import net.minecraft.server.level.ServerLevel;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import consular.weatherwatch.lib.TimeLibrary;
import consular.weatherwatch.lib.WeatherLibrary;
import consular.weatherwatch.lib.Config;

public class MinecraftTimeSync {

    private static final TimeLibrary timeLib = new TimeLibrary();
    private static final WeatherLibrary weatherLib = new WeatherLibrary();
    private static final AtomicBoolean tzFetchInProgress = new AtomicBoolean(false);

    public static void syncRealTimeToMinecraft(ServerLevel world) {
        if (Config.DEFAULT.isSyncTimeEnabled()) {
            if (WeatherWatch.TIMEZONE_ID == null || WeatherWatch.TIMEZONE_ID.isBlank()) {
                if (tzFetchInProgress.compareAndSet(false, true)) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            String tz = weatherLib.fetchTimezoneForServer();
                            if (tz != null && !tz.isBlank()) {
                                WeatherWatch.TIMEZONE_ID = tz;
                            }
                        } catch (Exception ignored) {
                        } finally {
                            tzFetchInProgress.set(false);
                        }
                    });
                }
            }
        }

        ZonedDateTime realTime = ZonedDateTime.now(WeatherWatch.getZoneId());

        long currentWorldTime = world.getDayTime();
        long candidate = timeLib.computeAlignedWorldTime(currentWorldTime, realTime);

        world.setDayTime(candidate);
    }
}
