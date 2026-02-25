package consular.weatherwatch.weatherwatch;

import net.minecraft.server.level.ServerLevel;
import sereneseasons.api.season.Season;
import sereneseasons.season.SeasonHandler;
import sereneseasons.season.SeasonSavedData;
import sereneseasons.season.SeasonTime;

import consular.weatherwatch.lib.SeasonLibrary;

public class SeasonSyncManager {
    private static int lastCheckedMonth = -1;

    public static void syncSeasons(ServerLevel level) {
        if (!level.isClientSide()) {
            int month = java.time.ZonedDateTime.now(WeatherWatch.getZoneId()).getMonthValue();

            if (month == lastCheckedMonth) {
                return;
            }
            lastCheckedMonth = month;

            Season.SubSeason newSubSeason = SeasonLibrary.getSubSeasonForServer(WeatherWatch.getZoneId());
            if (newSubSeason == null) {
                System.err.println("Failed to determine server sub-season (location lookup failed).");
                return;
            }

            SeasonSavedData savedData = SeasonHandler.getSeasonSavedData(level);
            Season.SubSeason currentSubSeason = new SeasonTime(savedData.seasonCycleTicks).getSubSeason();

            if (newSubSeason == currentSubSeason) {
                return;
            }

            savedData.seasonCycleTicks = SeasonTime.ZERO.getSubSeasonDuration() * newSubSeason.ordinal();
            synchronized (savedData) {
                savedData.notify();
            }
            SeasonHandler.sendSeasonUpdate(level);
        }
    }
}
