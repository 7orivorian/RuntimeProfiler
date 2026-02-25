/*
 * Copyright (c) 2025 7orivorian.
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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link Profiler} class is an implementation of the {@link IProfiler} interface,
 * responsible for managing and tracking profiling sessions. It maintains an internal hierarchy
 * of {@link ProfilerNode} instances representing profiling paths and their respective entries.
 * This class provides methods for starting, stopping, and navigating the profiling structure.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @implNote This class is not thread-safe.
 * @see IProfiler
 * @see ProfilerNode
 * @since 3.0.0
 */
public class Profiler implements IProfiler {

    /**
     * The label for this profiler. This is a non-null, non-blank string that
     * identifies the profiler instance.
     * <p>
     * Profiler labels are not necessarily required to be unique, but making them
     * such is recommended for usability as generated reports often use them in
     * file names, etc.
     */
    protected final @NotNull String label;
    /**
     * Immutable configuration settings which determine the
     * behavior and operational parameters of the profiler.
     */
    protected final @NotNull Config config;
    /**
     * A mapping of profiling paths to their associated {@link ProfilerNode} objects.
     * This map is used internally to manage and lookup profiling entries
     * based on their paths.
     * <p>
     * The keys in this map represent the unique path strings for each profiling entry,
     * while the values are the corresponding {@link ProfilerNode} instances that store
     * profiling data.
     */
    protected final @NotNull Map<String, ProfilerNode> lookup;

    /**
     * Represents the root entry node of the profiling structure within the {@link Profiler}.
     * <p>
     * This variable is nullable, as the root entry may not yet be initialized until a
     * profiling session is started.
     */
    protected ProfilerNode root;
    /**
     * Represents the current node being actively profiled in the {@link Profiler}.
     * <p>
     * This variable holds a reference to the active {@link ProfilerNode} in the profiling stack.
     * It tracks the current state of profiling, allowing operations such as pushing, popping,
     * and accessing the current profiling entry.
     * <p>
     * The {@code current} field may be {@code null} if no profiling session is active
     * or if all profiling entries have been popped.
     */
    protected ProfilerNode current;

    /**
     * Constructs a new {@link Profiler} instance using the global configuration.
     * <p>
     * This constructor delegates to {@link Profiler#Profiler(String, Config)}
     * with the default global configuration.
     *
     * @param label the label for this profiler; must not be blank or {@code null}.
     */
    @Contract(pure = true)
    public Profiler(@NotNull String label) {
        this(label, Config.GLOBAL);
    }

    /**
     * Constructs a new {@link Profiler} instance using the specified configuration.
     *
     * @param label  the label for this profiler; must not be blank or {@code null}.
     * @param config the configuration settings for the profiler; must not be {@code null}.
     */
    @Contract(pure = true)
    public Profiler(@NotNull String label, @NotNull Config config) {
        if (label.isBlank()) {
            throw new IllegalArgumentException("Profiler label cannot be blank.");
        }
        this.label = label;
        this.config = config;
        this.lookup = new HashMap<>();

        this.root = null;
        this.current = null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public void start() {
        if (isStarted()) {
            throw new IllegalStateException("Profiler already started.");
        }

        lookup.clear();
        current = null;
        root = null;

        current = root = new ProfilerNode(config.rootId(), config.rootId(), null, config.timingPrecision());
        lookup.put(root.path(), root);

        root.start();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @Override
    public ProfilerNode stop() {
        if (current != null && current.parent() != null) {
            throw new IllegalStateException("Profiler session ended before path was fully popped. Mismatched push/pop? Remainger: %s".formatted(current.path()));
        }
        return pop();
    }

    /**
     * {@inheritDoc}
     * <p>
     * The method checks and validates the provided ID and updates the current entry path
     * and hierarchy. If the maximum allowed path depth is exceeded, an exception is thrown.
     *
     * @param id {@inheritDoc}
     * @return the {@link ProfilerNode} {@inheritDoc}
     * @throws IllegalStateException    {@inheritDoc}
     * @throws IllegalArgumentException {@inheritDoc}
     */
    @NotNull
    @Override
    public ProfilerNode push(@NotNull String id) {
        if (config.autoStart() && !isStarted()) {
            start();
        }

        checkStarted();
        checkId(id);

        // current will never be null here, as checkStarted() would have thrown an exception if it was.
        String path = current.path() + config.pathSeparator() + id;

        // Check if this path has already been visited, if so, assign the
        // existing node as the current entry, otherwise create a new node
        // and assign it as the current entry.
        current = lookup.computeIfAbsent(path, p -> new ProfilerNode(id, p, current, config.timingPrecision()));

        // Check if the maximum path depth has been exceeded.
        if (current.depth() > config.maxDepth()) {
            throw new IllegalStateException("Maximum path depth of %s exceeded".formatted(config.maxDepth()));
        }

        // Start profiling the current entry.
        return current.start();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @NotNull
    @Override
    public ProfilerNode pop() {
        checkStarted();

        if (current == null) {
            throw new IllegalStateException("Profiler already fully popped. Mismatched push/pop?");
        }

        final ProfilerNode popped = current.stop();
        current = current.parent();
        return popped;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public ProfilerNode root() {
        return root;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public ProfilerNode currentEntry() {
        return current;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @NotNull
    @Override
    public Config config() {
        return config;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return current != null;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @NotNull
    @Override
    public String label() {
        return label;
    }
}