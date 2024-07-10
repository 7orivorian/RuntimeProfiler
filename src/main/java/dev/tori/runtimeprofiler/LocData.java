package dev.tori.runtimeprofiler;

import dev.tori.runtimeprofiler.util.Stopwatch;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public class LocData {

    private final @NotNull String path;
    private final @NotNull String loc;
    private final @NotNull TimeUnit timeUnit;
    private final @NotNull Stopwatch stopwatch;

    private long total = 0L;
    private long maxTime = Long.MIN_VALUE;
    private long minTime = Long.MAX_VALUE;
    private long visits = 0L;

    public LocData(@NotNull String path, @NotNull String loc, @NotNull TimeUnit timeUnit) {
        this.path = path;
        this.loc = loc;
        this.timeUnit = timeUnit;
        this.stopwatch = new Stopwatch();
    }

    public void push() {
        stopwatch.snap(timeUnit);
    }

    public void pop() {
        visits++;
        long elapsed = stopwatch.snap(timeUnit);
        total += elapsed;
        maxTime = Math.max(elapsed, maxTime);
        minTime = Math.min(elapsed, minTime);
    }

    @NotNull
    public String path() {
        return path;
    }

    @NotNull
    public String loc() {
        return loc;
    }

    public long total() {
        return total;
    }

    public long avg() {
        return total / visits;
    }

    public long maxTime() {
        return maxTime;
    }

    public long minTime() {
        return minTime;
    }

    public long visits() {
        return visits;
    }
}