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

package dev.tori.runtimeprofiler.profiler;

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

    private static final ProfileEntry BLANK_ENTRY = new ProfileEntry("", "", 0, TimeUnit.NANOSECONDS);

    private final @NotNull String label;
    private final @NotNull LinkedList<String> stack;
    /**
     * A map storing profiler entries, where each key is the full path of
     * associated {@link ProfileEntry} value.
     * The entries are maintained in the order in which they were inserted.
     * This map is used internally by the profiler to manage and organize
     * profiling data.
     *
     * <ul>
     *   <li>Key: A unique, non-null string representing the full path of the associated {@link ProfileEntry}.</li>
     *   <li>Value: A {@link ProfileEntry} representing the profiling data associated with the key.</li>
     * </ul>
     * <p>
     * This field is guaranteed to be non-null and uses a {@link LinkedHashMap}
     * implementation to ensure insertion order is preserved.
     *
     * @see ProfileEntry
     * @see LinkedHashMap
     */
    private final @NotNull LinkedHashMap<String, ProfileEntry> map;
    private final int maxDepth;
    /**
     * Indicates whether the profiler should automatically start when it is pushed to.
     *
     * @see #push(String)
     */
    private final boolean autoStart;
    /**
     * Specifies the timing precision of the {@code Profiler}.
     * <p>
     * The value is represented as a {@link TimeUnit}, which determines the unit of time
     * used for measuring and reporting execution durations within the profiler.
     * <p>
     * Must not be {@code null}.
     *
     * @since 3.0.0
     */
    private final @NotNull TimeUnit precision;

    private boolean started;
    private int depth;
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
        this.precision = precision;
        this.maxDepth = maxDepth;
        this.autoStart = autoStart;

        this.stack = new LinkedList<>();
        this.map = new LinkedHashMap<>();

        this.started = false;

        this.reset();
    }


    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public void reset() {
        if (isStarted()) {
            throw new IllegalStateException("Cannot reset profiler while it is started.");
        }

        stack.clear();
        map.clear();
        depth = 0;
        currentEntry = null;
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

        // Reset state
        reset();

        // Start session
        started = true;
        push(Config.defaultRootId());
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

        if (depth > 1) {
            if (currentEntry == null) {
                throw new IllegalStateException("Profiler session ended before path was fully popped. Mismatched push/pop?");
            }
            throw new IllegalStateException("Profiler session ended before path was fully popped (remainder %s). Mismatched push/pop?".formatted(currentEntry.path()));
        }

        final ProfileEntry data = pop();

        started = false;
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

        String path;
        if (currentEntry == null) {
            path = id;
        } else {
            path = currentEntry.path() + Config.pathSeparator() + id;
        }
        currentEntry = new ProfileEntry(path, id, depth, precision);

        final ProfileEntry entry = currentEntry;

        stack.push(path);
        map.computeIfAbsent(path, key -> entry).start();
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

        if (currentEntry == null) {
            throw new IllegalStateException("Profiler already fully popped. Mismatched push/pop?");
        }

        final ProfileEntry popped = currentEntry.stop();

        stack.pop();

        if (stack.isEmpty()) {
            currentEntry = null;
        } else {
            currentEntry = map.get(stack.getFirst());
        }

        depth--;

        return popped;
    }

    @Nullable
    @Override
    public ProfileEntry getCurrentEntry() {
        return currentEntry;
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
        return precision;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public long getTotalRuntime() {
        return map.getOrDefault(Config.defaultRootId(), BLANK_ENTRY).totalTime();
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