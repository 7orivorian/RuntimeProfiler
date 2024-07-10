package dev.tori.runtimeprofiler;

import dev.tori.runtimeprofiler.config.Config;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * A factory class for creating instances of {@link LocData}.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public record LocDataFactory(@NotNull TimeUnit timeUnit) {

    /**
     * Creates a new instance of {@link LocData} based on the provided path. The path is processed to determine the
     * location component.
     *
     * @param path the path from which to create the {@link LocData}; must not be null.
     * @return a new instance of {@link LocData}.
     * @throws NullPointerException if the path is null.
     */
    @NotNull
    public LocData create(@NotNull String path) {
        int i = path.lastIndexOf(Config.pathSeparator());
        // loc includes any leading separator to make clear at a glance
        // that it's a sub-location in the event of duplicate loc names.
        String loc = (i == -1) ? path : path.substring(i);
        return new LocData(path, loc, timeUnit);
    }
}