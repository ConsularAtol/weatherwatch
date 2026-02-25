package consular.weatherwatch.lib;

import java.time.ZonedDateTime;

public class TimeLibrary {
    public long computeAlignedWorldTime(long currentWorldTime, ZonedDateTime realTime) {
        return consular.weatherwatch.util.TimeConverter.computeAlignedWorldTime(currentWorldTime, realTime);
    }

    public long computeWorldTimeForMoonIndex(long currentWorldTime, int desiredIndex) {
        long dayTime = currentWorldTime % 24000L;
        long fullDays = currentWorldTime / 24000L;
        long baseFullDays = fullDays - (fullDays % 8) + (desiredIndex % 8);
        long newTime = baseFullDays * 24000L + dayTime;
        while (newTime < currentWorldTime) {
            baseFullDays += 8;
            newTime = baseFullDays * 24000L + dayTime;
        }
        return newTime;
    }
}
