/*
 * Copyright (c) 2024 7orivorian.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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