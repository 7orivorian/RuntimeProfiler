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
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Standard implementation of {@link IProfiler}.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public class Profiler implements IProfiler {

    private final @NotNull String label;
    private final @NotNull LinkedList<String> path;
    private final @NotNull LinkedHashMap<String, ProfileEntry> map;
    private final @NotNull ProfileEntryFactory factory;
    private final int maxDepth;
    /**
     * @since 3.0.0
     */
    private final boolean autoStart;

    private boolean started;
    private int depth;
    private @NotNull String fullPath;
    private @Nullable ProfileEntry currentEntry;

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
    public Profiler(String label, boolean autoStart) {
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
    public Profiler(String label, TimeUnit precision) {
        this(label, precision, Config.defaultMaxDepth());
    }

    /**
     * Constructs a new profiler with the given {@code label} and {@linkplain TimeUnit time unit}.
     * <p>
     * {@code autoStart} is set to the configured default value.
     *
     * @param label     A string identifier for this profiler. Must not be {@code null}.
     * @param precision The timing {@linkplain TimeUnit precision} for this profiler.
     * @param maxDepth  The maximum depth of the profiling path. Must be in range {@code [1, Integer.MAX_VALUE]}.
     * @throws IllegalArgumentException if {@code maxDepth} is less than or equal to zero.
     * @throws NullPointerException     if {@code label} or {@code precision} is {@code null}.
     * @since 1.2.0
     */
    public Profiler(String label, TimeUnit precision, int maxDepth) {
        this(label, precision, maxDepth, Config.defaultAutoStart());
    }

    /**
     * Constructs a new {@code Profiler} with the specified parameters.
     *
     * @param label     A string identifier for this profiler. Must not be {@code null}.
     * @param precision The {@link TimeUnit} used to determine the timing precision for this profiler. Must not be {@code null}.
     * @param maxDepth  The maximum depth of the profiling path. Must be in range {@code [1, Integer.MAX_VALUE]}.
     * @param autoStart Indicates whether the profiler should automatically start when pushed to.
     * @throws IllegalArgumentException if {@code maxDepth} is less than or equal to zero.
     * @throws NullPointerException     if {@code label} or {@code precision} is {@code null}.
     */
    public Profiler(@NotNull String label, @NotNull TimeUnit precision, @Range(from = 1, to = Integer.MAX_VALUE) int maxDepth, boolean autoStart) {
        this.label = label;
        this.path = new LinkedList<>();
        this.map = new LinkedHashMap<>();
        this.factory = new ProfileEntryFactory(precision);
        this.maxDepth = maxDepth;
        this.autoStart = autoStart;
        this.started = false;
        this.depth = 0;
        this.fullPath = "";
        this.currentEntry = null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public void start() {
        if (isStarted()) {
            throw new IllegalStateException("Profiler session already started");
        }
        map.clear();
        path.clear();
        fullPath = "";
        started = true;
        push("root");
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @NotNull
    @Override
    public ProfileEntry stop() {
        checkStarted();

        final ProfileEntry data = pop();
        started = false;
        if (!fullPath.isEmpty()) {
            throw new IllegalStateException("Profiler session ended before path was fully popped (remainder %s). Mismatched push/pop?".formatted(fullPath));
        }
        return data;
    }

    /**
     * {@inheritDoc}
     * <p>
     * If {@linkplain #autoStart} is enabled and this profiler is not
     * {@linkplain #isStarted() started}, this method will start the
     * profiler before pushing.
     *
     * @param id {@inheritDoc}
     * @throws IllegalStateException    {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @Override
    public void push(@NotNull String id) {
        if (!isStarted() && autoStart) {
            start();
        }

        checkStarted();
        checkId(id);

        if (++depth > maxDepth) {
            throw new IllegalStateException("Maximum path depth of %s exceeded".formatted(maxDepth));
        }

        if (!fullPath.isEmpty()) {
            fullPath += Config.pathSeparator();
        }
        fullPath += id;

        path.push(fullPath);
        map.computeIfAbsent(fullPath, key -> factory.create(fullPath, depth)).push();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @NotNull
    @Override
    public ProfileEntry pop() {
        checkStarted();

        if (path.isEmpty()) {
            throw new IllegalStateException("Profiler already popped. Mismatched push/pop?");
        }

        final ProfileEntry current = getCurrentEntry();
        if (current == null) {
            throw new IllegalStateException("Current ProfileEntry is null. Likely due to a mismatched push/pop");
        }

        depth--;
        current.pop();
        path.pop();
        fullPath = path.isEmpty() ? "" : path.getFirst();
        currentEntry = null;

        return current;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @since 1.1.0
     */
    @NotNull
    @UnmodifiableView
    @Override
    public Set<Map.Entry<String, ProfileEntry>> getEntries() {
        return Collections.unmodifiableSet(map.entrySet());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @since 1.1.0
     */
    @NotNull
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @since 1.1.0
     */
    @NotNull
    @Override
    public TimeUnit getTimingPrecision() {
        return factory.timeUnit();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public long getTotalRuntime() {
        return map.get("root").totalTime();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @since 1.2.0
     */
    @Override
    public int getDepth() {
        return depth;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @since 2.1.0
     */
    @Override
    public boolean isStarted() {
        return started;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @since 3.0.0
     */
    @Override
    public boolean canAutoStart() {
        return autoStart;
    }

    /**
     * Retrieves the full current profiling path.
     *
     * @return the full current path as a non-null string.
     */
    @NotNull
    public String getFullPath() {
        return fullPath;
    }

    @Nullable
    public ProfileEntry getCurrentEntry() {
        if (currentEntry == null) {
            currentEntry = map.get(fullPath);
        }
        return currentEntry;
    }

    /**
     * @param id the id to check
     * @throws IllegalArgumentException if the given {@code id} contains the
     *                                  {@linkplain Config#pathSeparator() path separator}.
     */
    @ApiStatus.Internal
    private void checkId(@NotNull String id) {
        final String pathSeparator = Config.pathSeparator();
        if (id.contains(pathSeparator)) {
            throw new IllegalArgumentException("Invalid path id: '" + id + "'. Cannot contain path separator '" + pathSeparator.translateEscapes() + "'!");
        }
    }

    /**
     * @throws IllegalStateException if this profiler is not {@linkplain #isStarted() started}.
     */
    @Contract(pure = true)
    @ApiStatus.Internal
    private void checkStarted() {
        if (!isStarted()) {
            throw new IllegalStateException("Profiler not started");
        }
    }
}