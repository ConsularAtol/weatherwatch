package consular.weatherwatch.weatherwatch;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.platform.Platform;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;
import consular.weatherwatch.lib.Config;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.PermissionLevel;

public class WeatherWatchCommands {

    private static final SuggestionProvider<CommandSourceStack> CONFIG_KEY_SUGGESTIONS = (ctx, builder) -> {
        List<String> keys = new ArrayList<>();
        keys.add("timeSync");
        keys.add("weatherSync");
        keys.add("moonSync");
        keys.add("ip-override");
        if (Platform.isModLoaded("sereneseasons")) {
            keys.add("seasonSync");
        }

        for (String key : keys) {
            if (key.toLowerCase().startsWith(builder.getRemainingLowerCase())) {
                builder.suggest(key);
            }
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> CONFIG_VALUE_SUGGESTIONS = (ctx, builder) -> {
        String key = StringArgumentType.getString(ctx, "key").toLowerCase();
        if (key.contains("sync")) {
            builder.suggest("true");
            builder.suggest("false");
        } else if (key.equals("ip-override")) {
            builder.suggest("server");
            builder.suggest(LocationFetcher.getPublicIP());
        }
        return builder.buildFuture();
    };

    public static void register() {
        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, selection) -> {
            register(dispatcher);
        });
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("weatherwatch")
                        .requires(Commands.hasPermission(new PermissionCheck.Require(new Permission.HasCommandLevel(PermissionLevel.ADMINS))))

                        .then(Commands.literal("config")
                                .then(Commands.argument("key", StringArgumentType.word())
                                        .suggests(CONFIG_KEY_SUGGESTIONS)
                                        .then(Commands.argument("value", StringArgumentType.word())
                                                .suggests(CONFIG_VALUE_SUGGESTIONS)
                                                .executes(ctx -> {
                                                    CommandSourceStack source = ctx.getSource();
                                                    String key = StringArgumentType.getString(ctx, "key");
                                                    String value = StringArgumentType.getString(ctx, "value");

                                                    String mappedKey = mapOptionToKey(key);
                                                    if (mappedKey == null) {
                                                        source.sendFailure(Component.literal("Unknown config key: " + key));
                                                        return 0;
                                                    }

                                                    boolean changed = false;

                                                    if ("ip-override".equals(mappedKey)) {
                                                        String prev = Config.DEFAULT.getIpOverride();
                                                        if (prev == null || !prev.equals(value)) {
                                                            Config.DEFAULT.set(mappedKey, value);
                                                            changed = true;
                                                        }
                                                    } else {
                                                        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                                                            source.sendFailure(Component.literal("Value must be true/false for " + key));
                                                            return 0;
                                                        }

                                                        boolean newVal = Boolean.parseBoolean(value);
                                                        boolean prevVal = getPrevBooleanValue(mappedKey);
                                                        if (prevVal != newVal) {
                                                            Config.DEFAULT.set(mappedKey, newVal);
                                                            changed = true;
                                                        }
                                                    }

                                                    if (changed) {
                                                        source.sendSuccess(() -> Component.literal("Updated " + mappedKey + " to " + value), true);
                                                    } else {
                                                        source.sendSuccess(() -> Component.literal("No change; " + mappedKey + " already set to " + value), false);
                                                    }
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                                )
                        )

                        .then(Commands.literal("fetchLocation")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();

                                    if (WeatherWatch.cachedLocation != null) {
                                        source.sendSuccess(() -> Component.literal(WeatherWatch.cachedLocation.toString()), false);
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    try {
                                        double[] loc = LocationFetcher.getServerLocation();
                                        String ip = LocationFetcher.getPublicIP();

                                        JsonObject locObj = new JsonObject();
                                        if (loc != null) {
                                            locObj.addProperty("latitude", loc[0]);
                                            locObj.addProperty("longitude", loc[1]);
                                        }
                                        if (ip != null) locObj.addProperty("ip", ip);

                                        if (!locObj.entrySet().isEmpty()) {
                                            source.sendSuccess(() -> Component.literal(locObj.toString()), false);
                                            return Command.SINGLE_SUCCESS;
                                        }

                                        source.sendFailure(Component.literal("Failed to fetch location data."));
                                        return 0;
                                    } catch (Exception e) {
                                        source.sendFailure(Component.literal("Failed to fetch location: " + e.getMessage()));
                                        return 0;
                                    }
                                })
                        )

                        .then(Commands.literal("fetchWeather")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    if (WeatherWatch.cachedWeather == null) {
                                        source.sendFailure(Component.literal(
                                                "No cached weather data. Weather Sync is likely off. Please note weather data updates every 5 minutes."
                                        ));
                                    } else {
                                        source.sendSuccess(() -> Component.literal(WeatherWatch.cachedWeather.toString()), false);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )

                        .then(Commands.literal("fetchMoonPhase")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    if (WeatherWatch.cachedMoonPhase == null) {
                                        source.sendFailure(Component.literal("No cached moon phase data."));
                                        return Command.SINGLE_SUCCESS;
                                    }

                                    try {
                                        JsonObject m = WeatherWatch.cachedMoonPhase;
                                        String extracted = m.has("moon_phase")
                                                ? String.valueOf(m.get("moon_phase").getAsDouble())
                                                : "<none>";
                                        source.sendSuccess(() -> Component.literal("moon_phase=" + extracted + " | raw=" + m), false);
                                    } catch (Exception e) {
                                        source.sendSuccess(() -> Component.literal(WeatherWatch.cachedMoonPhase.toString()), false);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )

                        .then(Commands.literal("forceResync")
                                .executes(ctx -> {
                                    CommandSourceStack source = ctx.getSource();
                                    WeatherWatch.weatherTickCounter = 6000;
                                    WeatherWatch.seasonTickCounter = 1200;

                                    try {
                                        WeatherSyncManager.syncWeather(source.getServer().overworld());
                                    } catch (Exception e) {
                                        source.sendFailure(Component.literal("Failed to sync: " + e.getMessage()));
                                        return 0;
                                    }

                                    source.sendSuccess(() -> Component.literal("Forced Weather Watch Re-Sync"), true);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
        );
    }

    private static boolean getPrevBooleanValue(String mappedKey) {
        return switch (mappedKey) {
            case "syncWeather" -> Config.DEFAULT.isSyncWeatherEnabled();
            case "syncSeasons" -> Config.DEFAULT.isSyncSeasonsEnabled();
            case "syncTime" -> Config.DEFAULT.isSyncTimeEnabled();
            case "syncMoonPhase" -> Config.DEFAULT.isSyncMoonPhaseEnabled();
            default -> false;
        };
    }

    private static String mapOptionToKey(String option) {
        if (option == null) return null;
        switch (option.toLowerCase()) {
            case "weathersync":
            case "syncweather":
            case "weather":
                return "syncWeather";
            case "seasonsync":
            case "syncseasons":
            case "seasons":
                return "syncSeasons";
            case "timesync":
            case "synctime":
            case "time":
                return "syncTime";
            case "moonsync":
            case "moon":
            case "syncmoon":
            case "syncmoonphase":
                return "syncMoonPhase";
            case "ip-override":
            case "ip":
                return "ip-override";
            default:
                return null;
        }
    }
}
