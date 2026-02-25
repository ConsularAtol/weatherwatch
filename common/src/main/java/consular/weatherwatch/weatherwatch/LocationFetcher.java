package consular.weatherwatch.weatherwatch;

import consular.weatherwatch.lib.LocationService;
import consular.weatherwatch.lib.Config;

public class LocationFetcher {
    private static final LocationService service = new LocationService(Config.DEFAULT);

    public static String getPublicIP() {
        return service.getPublicIP();
    }

    public static double[] getServerLocation() {
        return service.getServerLocation();
    }
}
