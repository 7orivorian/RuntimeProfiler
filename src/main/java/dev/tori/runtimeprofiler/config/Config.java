package dev.tori.runtimeprofiler.config;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public class Config {

    private static String pathSeparator = "/";
    private static TimeUnit defaultTimeUnit = TimeUnit.NANOSECONDS;

    public static String pathSeparator() {
        return pathSeparator;
    }

    public static void setPathSeparator(String pathSeparator) {
        Config.pathSeparator = pathSeparator;
    }

    public static TimeUnit defaultTimeUnit() {
        return defaultTimeUnit;
    }

    public static void setDefaultTimeUnit(TimeUnit defaultTimeUnit) {
        Config.defaultTimeUnit = defaultTimeUnit;
    }
}