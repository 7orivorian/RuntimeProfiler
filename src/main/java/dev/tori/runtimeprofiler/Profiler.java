/*
 * Copyright (c) 2024-2025 7orivorian.
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
    /**
     * @since 3.0.0
     */
    private final boolean autoStart;

    private boolean started;
    private int depth;
    private String fullPath;
    private LocData currentLocData;

    /**
     * Constructs a new profiler with the given {@code label}.
     * <p>
     * {@code precision}, {@code maxDepth}, and {@code autoStart} are set to the configured default values.
     *
     * @param label A string identifier for this profiler. Must not be {@code null}.
     * @throws IllegalArgumentException if {@code maxDepth} is less than or equal to zero.
     * @throws NullPointerException     if {@code label} is {@code null}.
     */
    public Profiler(String label) {
        this(label, Config.defaultTimeUnit(), Config.defaultMaxDepth());
    }

    /**
     * Constructs a new profiler with the given {@code label} and {@code autoStart}.
     * <p>
     * {@code precision} and {@code maxDepth} are set to the configured default values.
     *
     * @param label     A string identifier for this profiler. Must not be {@code null}.
     * @param autoStart Indicates whether the profiler should automatically start when pushed to.
     * @throws IllegalArgumentException if {@code maxDepth} is less than or equal to zero.
     * @throws NullPointerException     if {@code label} is {@code null}.
     */
    public Profiler(@NotNull String label, boolean autoStart) {
        this(label, Config.defaultTimeUnit(), Config.defaultMaxDepth(), autoStart);
    }

    /**
     * Constructs a new profiler with the given {@code label} and {@linkplain TimeUnit time unit}.
     * <p>
     * {@code maxDepth} and {@code autoStart} are set to the configured default values.
     *
     * @param label     A string identifier for this profiler. Must not be {@code null}.
     * @param precision The timing {@linkplain TimeUnit precision} for this profiler
     * @throws IllegalArgumentException if {@code maxDepth} is less than or equal to zero.
     * @throws NullPointerException     if {@code label} is {@code null}.
     */
    public Profiler(@NotNull String label, @NotNull TimeUnit precision) {
        this(label, precision, Config.defaultMaxDepth());
    }

    /**
     * Constructs a new profiler with the given {@code label} and {@linkplain TimeUnit time unit}.
     * <p>
     * {@code autoStart} is set to the configured default value.
     *
     * @param label     A string identifier for this profiler. Must not be {@code null}.
     * @param precision The timing {@linkplain TimeUnit precision} for this profiler.
     * @param maxDepth  The maximum path depth of this profiler; must be greater than {@code 0}.
     * @throws IllegalArgumentException if {@code maxDepth} is less than or equal to zero.
     * @throws NullPointerException     if {@code label} or {@code precision} is {@code null}.
     * @since 1.2.0
     */
    public Profiler(@NotNull String label, @NotNull TimeUnit precision, int maxDepth) {
        this(label, precision, maxDepth, Config.defaultAutoStart());
    }

    /**
     * Constructs a new {@code Profiler} with the specified parameters.
     *
     * @param label     A string identifier for this profiler. Must not be {@code null}.
     * @param precision The {@link TimeUnit} used to determine the timing precision for this profiler. Must not be {@code null}.
     * @param maxDepth  The maximum depth of the profiling path. Must be greater than zero.
     * @param autoStart Indicates whether the profiler should automatically start when pushed to.
     * @throws IllegalArgumentException if {@code maxDepth} is less than or equal to zero.
     * @throws NullPointerException     if {@code label} or {@code precision} is {@code null}.
     */
    public Profiler(@NotNull String label, @NotNull TimeUnit precision, int maxDepth, boolean autoStart) {
        if (maxDepth <= 0) {
            throw new IllegalArgumentException("maxDepth must be greater than zero");
        }
        this.label = label;
        this.path = new LinkedList<>();
        this.map = new LinkedHashMap<>();
        this.factory = new LocDataFactory(precision);
        this.maxDepth = maxDepth;
        this.autoStart = autoStart;
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
     * Stops this profiler if it is started and fully popped, otherwise throws an {@link IllegalStateException}.
     *
     * @return the root {@link LocData}.
     * @throws IllegalStateException if this profiler is not started OR not fully popped.
     */
    @Override
    public LocData stop() {
        checkStarted();

        final LocData data = pop();
        started = false;
        if (!fullPath.isEmpty()) {
            throw new IllegalStateException("Profiler tick ended before path was fully popped (remainder %s). Mismatched push/pop?".formatted(fullPath));
        }
        return data;
    }

    /**
     * Pushes the given location to the profiler stack.
     * <p>
     * If {@linkplain #autoStart} is enabled, this method will automatically start the profiler if it is not already started.
     *
     * @param location the location to push onto the stack. Must not contain the {@linkplain Config#pathSeparator() path separator}, and must not be {@code null}.
     * @throws IllegalStateException    if this profiler is not {@linkplain #started}.
     * @throws IllegalArgumentException if the given {@code location} contains the
     *                                  {@linkplain Config#pathSeparator() path separator}.
     */
    @Override
    public void push(@NotNull String location) {
        if (autoStart && !started) {
            start();
        }

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

    /**
     * Pops the current location from the stack.
     *
     * @return the popped {@link LocData}.
     * @throws IllegalStateException if this profiler is not {@linkplain #started} OR the stack is empty.
     */
    @Override
    public LocData pop() {
        checkStarted();

        if (path.isEmpty()) {
            throw new IllegalStateException("Profiler already popped. Mismatched push/pop?");
        }

        final LocData current = getCurrentLocData();
        if (current == null) {
            throw new IllegalStateException("Current LocData is null. Likely due to a mismatched push/pop");
        }

        depth--;
        current.pop();
        path.pop();
        fullPath = path.isEmpty() ? "" : path.getFirst();
        currentLocData = null;

        return current;
    }

    /**
     * Swaps the top of the stack or simply pushes if the current top of the stack is root.
     * <p>
     * More formally, this method pops the stack if {@code depth > 1}, otherwise pushes without popping.
     *
     * @param location the location to push onto the stack. Must not contain the {@linkplain Config#pathSeparator() path separator}, and must not be {@code null}.
     * @return the popped {@link LocData} if {@code depth > 1}, otherwise {@code null}.
     * @throws IllegalStateException    if this profiler is not {@linkplain #started}.
     * @throws IllegalArgumentException if the given {@code location} contains the
     *                                  {@linkplain Config#pathSeparator() path separator}.
     */
    @Nullable
    @Override
    public LocData swapIf(@NotNull String location) {
        if (depth == 1) {
            push(location);
            return null;
        }
        return swap(location);
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

    /**
     * @return the total runtime of the root {@link LocData}.
     * @throws IllegalStateException if the profiler is {@link #started}.
     */
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

    /**
     * Checks if the profiler is currently started.
     *
     * @return {@code true} if the profiler is started, {@code false} otherwise.
     * @since 2.1.0
     */
    @Override
    public boolean isStarted() {
        return started;
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
        final String pathSeparator = Config.pathSeparator();
        if (location.contains(pathSeparator)) {
            throw new IllegalArgumentException("Invalid path: " + location + ". Cannot contain path separator '" + pathSeparator.translateEscapes() + "'!");
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