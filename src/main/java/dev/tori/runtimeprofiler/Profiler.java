package dev.tori.runtimeprofiler;

import dev.tori.runtimeprofiler.config.Config;
import dev.tori.runtimeprofiler.util.UnitUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Standard implementation of {@link IProfiler}.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public class Profiler implements IProfiler {

    private final String label;
    private final LinkedList<String> path;
    private final LinkedHashMap<String, LocData> map;
    private final LocDataFactory factory;

    private boolean started;
    private String fullPath;
    private LocData currentLocData;

    /**
     * Constructs a new profiler with the given {@code label} and the
     * {@linkplain Config#defaultTimeUnit() default time unit}.
     *
     * @param label The label for this profiler
     */
    public Profiler(String label) {
        this(label, Config.defaultTimeUnit());
    }

    /**
     * Constructs a new profiler with the given {@code label} and {@linkplain TimeUnit time unit}.
     *
     * @param label    The label for this profiler
     * @param timeUnit The {@linkplain TimeUnit time unit} for this profiler
     */
    public Profiler(String label, TimeUnit timeUnit) {
        this.label = label;
        this.path = new LinkedList<>();
        this.map = new LinkedHashMap<>();
        this.factory = new LocDataFactory(timeUnit);
        this.started = false;
        this.fullPath = "";
        this.currentLocData = null;
    }

    @NotNull
    @Override
    @Contract(value = " -> new", pure = true)
    public String[] dataHeaders() {
        String abbr = UnitUtil.abbreviate(factory.timeUnit());
        return new String[]{"Location", "Visits", "Total (%s)".formatted(abbr), "Avg (%s)".formatted(abbr), "Min (%s)".formatted(abbr), "Max (%s)".formatted(abbr), "Path"};
    }

    @NotNull
    @Override
    public String[] toArray(@NotNull LocData data) {
        return new String[]{data.loc(), String.valueOf(data.visits()), String.valueOf(data.total()), String.valueOf(data.avg()), String.valueOf(data.minTime()), String.valueOf(data.maxTime()), data.path()};
    }

    /**
     * Resets and starts this profiler.
     *
     * @throws IllegalStateException if this profiler is already started.
     */
    @Override
    public void start() {
        if (started) {
            throw new IllegalStateException("Profiler already started");
        }
        map.clear();
        path.clear();
        fullPath = "";
        started = true;
    }

    /**
     * Stop this profiler.
     *
     * @throws IllegalStateException if this profiler is not started OR not fully popped.
     */
    @Override
    public void stop() {
        checkStarted();
        started = false;
        if (!fullPath.isEmpty()) {
            throw new IllegalStateException("Profiler tick ended before path was fully popped (remainder %s). Mismatched push/pop?".formatted(fullPath));
        }
    }

    @Override
    public void push(@NotNull String location) {
        checkStarted();
        checkLocationName(location);
        if (!fullPath.isEmpty()) {
            fullPath += Config.pathSeparator();
        }
        fullPath += location;
        path.push(fullPath);
        map.computeIfAbsent(fullPath, key -> factory.create(fullPath)).push();
    }

    @Override
    public void pop() {
        checkStarted();
        if (path.isEmpty()) {
            throw new IllegalStateException("Profiler already popped. Mismatched push/pop?");
        }
        LocData current = getCurrentLocData();
        if (current == null) {
            throw new IllegalStateException("Current LocData is null. Likely due to a mismatched push/pop");
        }
        current.pop();
        path.pop();
        fullPath = path.isEmpty() ? "" : path.getFirst();
        currentLocData = null;
    }

    /**
     * @return The label of this profiler.
     */
    public String getLabel() {
        return label;
    }

    public String getCurrentLocation() {
        return path.isEmpty() ? "" : path.getFirst();
    }

    @Nullable
    public LocData getCurrentLocData() {
        if (currentLocData == null) {
            currentLocData = map.get(fullPath);
        }
        return currentLocData;
    }

    public Set<Map.Entry<String, LocData>> getEntries() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    /**
     * @param location the location to check
     * @throws IllegalArgumentException if the given {@code location} contains the
     *                                  {@linkplain Config#pathSeparator() path separator}.
     */
    @ApiStatus.Internal
    private void checkLocationName(@NotNull String location) {
        if (location.contains(Config.pathSeparator())) {
            throw new IllegalArgumentException("Invalid path: " + location + ". Cannot contain path separator!");
        }
    }

    /**
     * @throws IllegalStateException if this profiler is not {@linkplain #started started}.
     */
    @ApiStatus.Internal
    @Contract(pure = true)
    private void checkStarted() {
        if (!started) {
            throw new IllegalStateException("Profiler not started");
        }
    }
}