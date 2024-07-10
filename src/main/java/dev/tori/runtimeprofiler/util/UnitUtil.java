package dev.tori.runtimeprofiler.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
@ApiStatus.Internal
public class UnitUtil {

    public static String abbreviate(TimeUnit timeUnit) {
        return switch (timeUnit) {
            case NANOSECONDS -> "ns";
            case MICROSECONDS -> "us";
            case MILLISECONDS -> "ms";
            case SECONDS -> "s";
            case MINUTES -> "m";
            case HOURS -> "h";
            case DAYS -> "d";
        };
    }
}