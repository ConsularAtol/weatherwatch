package consular.weatherwatch.lib;

import net.minecraft.world.level.Level;

import sereneseasons.api.season.Season;
import sereneseasons.season.SeasonHandler;
import sereneseasons.season.SeasonSavedData;
import sereneseasons.season.SeasonTime;

import java.time.ZonedDateTime;

public class SeasonLibrary {
    public static Season.SubSeason getSubSeason(java.time.ZoneId zone, double latitude) {
        int month = ZonedDateTime.now(zone).getMonthValue();
        boolean southern = latitude < 0;
        if (southern) {
            month = (month + 6) % 12;
            if (month == 0) month = 12;
        }
        return switch (month) {
            case 12 -> Season.SubSeason.EARLY_WINTER;
            case 1 -> Season.SubSeason.MID_WINTER;
            case 2 -> Season.SubSeason.LATE_WINTER;
            case 3 -> Season.SubSeason.EARLY_SPRING;
            case 4 -> Season.SubSeason.MID_SPRING;
            case 5 -> Season.SubSeason.LATE_SPRING;
            case 6 -> Season.SubSeason.EARLY_SUMMER;
            case 7 -> Season.SubSeason.MID_SUMMER;
            case 8 -> Season.SubSeason.LATE_SUMMER;
            case 9 -> Season.SubSeason.EARLY_AUTUMN;
            case 10 -> Season.SubSeason.MID_AUTUMN;
            default -> Season.SubSeason.LATE_AUTUMN;
        };
    }

    public static Season.SubSeason getSubSeasonForServer(java.time.ZoneId zone) {
        try {
            LocationService locService = new LocationService();
            double[] loc = locService.getServerLocation();
            if (loc == null) return null;
            double latitude = loc[0];
            return getSubSeason(zone, latitude);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void applySeasonToWorld(net.minecraft.server.level.ServerLevel level, Season.SubSeason subSeason) {
        SeasonSavedData savedData = SeasonHandler.getSeasonSavedData(level);
        Season.SubSeason current = new SeasonTime(savedData.seasonCycleTicks).getSubSeason();
        if (current == subSeason) return;
        savedData.seasonCycleTicks = SeasonTime.ZERO.getSubSeasonDuration() * subSeason.ordinal();
        synchronized (savedData) { savedData.notify(); }
        SeasonHandler.sendSeasonUpdate(level);
    }
}
