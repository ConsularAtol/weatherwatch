package consular.weatherwatch.util;

import java.time.ZonedDateTime;

public final class TimeConverter {
    private TimeConverter() {}

    public static long computeAlignedWorldTime(long currentWorldTime, ZonedDateTime realTime) {
        int hour = realTime.getHour();
        int minute = realTime.getMinute();

        long desiredTimeOfDay;
        if (hour >= 6) {
            desiredTimeOfDay = Math.round(((hour * 1000) - 6000) + (minute * 16.666666666666666));
        } else {
            desiredTimeOfDay = Math.round(((hour * 1000) + 18000) + (minute * 16.666666666666666));
        }

        long currentDay = currentWorldTime / 24000L;
        long candidate = currentDay * 24000L + (desiredTimeOfDay % 24000L);
        if (candidate < currentWorldTime) candidate += 24000L;
        return candidate;
    }
}
