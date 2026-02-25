package consular.weatherwatch.util;

public class MoonCalculator {
    public static double computeMoonPhaseDecimal(java.time.LocalDate date) {
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();

        int Y = year;
        int M = month;
        if (M <= 2) {
            Y -= 1;
            M += 12;
        }
        int A = Y / 100;
        int B = 2 - A + (A / 4);
        double jd = Math.floor(365.25 * (Y + 4716)) + Math.floor(30.6001 * (M + 1)) + day + B - 1524.5;

        double knownNewMoonJD = 2451550.1;
        double synodicMonth = 29.530588853;

        double daysSinceNew = jd - knownNewMoonJD;
        double newMoons = daysSinceNew / synodicMonth;
        double phase = newMoons - Math.floor(newMoons);
        if (phase < 0) phase += 1.0;
        return phase;
    }

    public static int toMinecraftIndex(double decimal) {
        if (decimal >= 0.4375 && decimal < 0.5625) return 0;
        if (decimal >= 0.5625 && decimal < 0.6875) return 1;
        if (decimal >= 0.6875 && decimal < 0.8125) return 2;
        if (decimal >= 0.8125 && decimal < 0.9375) return 3;
        if (decimal >= 0.9375 || decimal < 0.0625) return 4;
        if (decimal >= 0.0625 && decimal < 0.1875) return 5;
        if (decimal >= 0.1875 && decimal < 0.3125) return 6;
        if (decimal >= 0.3125 && decimal < 0.4375) return 7;
        return -1;
    }
}

