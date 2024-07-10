package dev.tori.runtimeprofiler.util;

import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
@ApiStatus.Internal
public class Stopwatch {

    private long nanoTime;
    private long totalNanoTime;

    public Stopwatch() {
        this.reset();
    }

    public long totalNanoTime() {
        return totalNanoTime;
    }

    public long snap(TimeUnit timeUnit) {
        final long elapsed = System.nanoTime() - nanoTime;
        nanoTime = System.nanoTime();
        return timeUnit.convert(elapsed, TimeUnit.NANOSECONDS);
    }

    public long snapNanos() {
        final long elapsed = System.nanoTime() - nanoTime;
        nanoTime = System.nanoTime();
        return elapsed;
    }

    public Stopwatch reset() {
        totalNanoTime = nanoTime = System.nanoTime();
        return this;
    }
}