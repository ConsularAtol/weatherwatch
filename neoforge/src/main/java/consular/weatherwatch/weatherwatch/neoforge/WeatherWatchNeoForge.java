package consular.weatherwatch.weatherwatch.neoforge;

import consular.weatherwatch.weatherwatch.WeatherWatch;
import net.neoforged.fml.common.Mod;

@Mod(WeatherWatch.MOD_ID)
public final class WeatherWatchNeoForge {
    public WeatherWatchNeoForge() {
        // Run our common setup.
        WeatherWatch.init();
    }
}
