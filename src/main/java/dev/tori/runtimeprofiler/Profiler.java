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

import dev.tori.runtimeprofiler.config.Config;
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
    private final int maxDepth;

    private boolean started;
    private int depth;
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
     * @param label     The label for this profiler
     * @param precision The timing {@linkplain TimeUnit precision} for this profiler
     */
    public Profiler(String label, TimeUnit precision) {
        this(label, precision, Config.defaultMaxDepth());
    }

    /**
     * Constructs a new profiler with the given {@code label} and {@linkplain TimeUnit time unit}.
     *
     * @param label     The label for this profiler.
     * @param precision The timing {@linkplain TimeUnit precision} for this profiler.
     * @param maxDepth  The maximum path depth of this profiler; must be greater than {@code 0}.
     * @since 1.2.0
     */
    public Profiler(@NotNull String label, @NotNull TimeUnit precision, int maxDepth) {
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("maxDepth must be greater than zero");
        }
        this.label = label;
        this.path = new LinkedList<>();
        this.map = new LinkedHashMap<>();
        this.factory = new LocDataFactory(precision);
        this.maxDepth = maxDepth;
        this.started = false;
        this.depth = 0;
        this.fullPath = "";
        this.currentLocData = null;
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
        push("root");
    }

    /**
     * Stop this profiler.
     *
     * @throws IllegalStateException if this profiler is not started OR not fully popped.
     */
    @Override
    public void stop() {
        checkStarted();
        pop();
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
        if (++depth > maxDepth) {
            throw new IllegalStateException("Maximum path depth of %s exceeded".formatted(maxDepth));
        }
        fullPath += location;
        path.push(fullPath);
        map.computeIfAbsent(fullPath, key -> factory.create(fullPath, depth)).push();
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
        depth--;
        current.pop();
        path.pop();
        fullPath = path.isEmpty() ? "" : path.getFirst();
        currentLocData = null;
    }

    @Override
    public boolean swapIf(@NotNull String location) {
        if (depth == 1) {
            push(location);
            return false;
        }
        swap(location);
        return true;
    }

    /**
     * @return The label of this profiler.
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * @since 1.1.0
     */
    @Override
    public TimeUnit getTimingPrecision() {
        return factory.timeUnit();
    }

    @Override
    public Set<Map.Entry<String, LocData>> getEntries() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    @Override
    public long getTotalRuntime() {
        if (started) {
            throw new IllegalStateException("Profiler is still running");
        }
        return map.get("root").total();
    }

    public String getFullPath() {
        return fullPath;
    }

    @Nullable
    public LocData getCurrentLocData() {
        if (currentLocData == null) {
            currentLocData = map.get(fullPath);
        }
        return currentLocData;
    }

    /**
     * @return the current path {@linkplain #depth}.
     * @since 1.2.0
     */
    public int getCurrentDepth() {
        return depth;
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