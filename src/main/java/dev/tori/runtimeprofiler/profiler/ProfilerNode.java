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

import dev.tori.runtimeprofiler.util.Stopwatch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.concurrent.TimeUnit;

/**
 * The {@link ProfilerNode} class represents a node in a profiling hierarchy that tracks
 * the performance data of specific operations or processes. Each node maintains timing information
 * such as total time, maximum time, and minimum time for operations as well as hierarchical
 * relationships with parent and child nodes.
 * <p>
 * A {@link ProfilerNode} is also associated with an identifier and a path that reflects its
 * hierarchical position. Timing is managed using an associated {@link Stopwatch} instance, and
 * units of time are determined by the {@link Stopwatch}'s {@linkplain TimeUnit}.
 * <p>
 * Instances of this class are immutable but internally updatable for profiling statistics
 * such as visit counts and elapsed times.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @see Profiler
 * @see IProfiler
 * @since 3.0.0
 */
public class ProfilerNode {

    private final ProfilerNode parent;
    private final @NotNull LinkedHashSet<ProfilerNode> children;

    private final @NotNull String id, path;
    private final @NotNull Stopwatch stopwatch;
    private final int depth;

    private long visits;

    private long sumTime;
    private long maxTime;
    private long minTime;

    public ProfilerNode(@NotNull String id, @NotNull ProfilerNode parent, @NotNull TimeUnit timeUnit) {
        this(id, id, parent, timeUnit);
    }

    public ProfilerNode(@NotNull String id, @NotNull String path, @Nullable ProfilerNode parent, @NotNull TimeUnit timeUnit) {
        this.id = id;
        this.path = path;
        this.parent = parent;
        this.children = new LinkedHashSet<>();
        this.stopwatch = new Stopwatch(timeUnit);

        if (parent != null) {
            this.depth = parent.depth + 1;
            parent.children.add(this);
        } else {
            this.depth = 0;
        }

        this.visits = 0L;
        this.sumTime = 0L;
        this.maxTime = Long.MIN_VALUE;
        this.minTime = Long.MAX_VALUE;
    }

    /**
     * Starts the stopwatch associated with this {@link ProfilerNode}, marking the beginning
     * of a timing interval.
     *
     * @return the current instance of {@link ProfilerNode}.
     */
    public ProfilerNode start() {
        stopwatch.start();
        return this;
    }

    /**
     * Stops the stopwatch associated with this {@link ProfilerNode}, recording the elapsed time
     * since the last call to {@code start}. Updates the profiling statistics, including the
     * visit count, total elapsed time, maximum elapsed time, and minimum elapsed time.
     *
     * @return the current instance of {@link ProfilerNode}.
     */
    public ProfilerNode stop() {
        visits++;
        long elapsed = stopwatch.snap();
        sumTime += elapsed;
        maxTime = Math.max(elapsed, maxTime);
        minTime = Math.min(elapsed, minTime);
        return this;
    }

    /**
     * Retrieves the depth of this {@link ProfilerNode} in its hierarchy.
     * The depth indicates the level of this node, where the root has a depth of 0.
     *
     * @return the depth of this {@link ProfilerNode}.
     */
    public int depth() {
        return depth;
    }

    /**
     * Retrieves the total number of visits recorded for this {@link ProfilerNode}.
     *
     * @return the number of times this node has been visited.
     */
    public long visits() {
        return visits;
    }

    /**
     * Retrieves the total accumulated time recorded for this {@link ProfilerNode}.
     *
     * @return the total time in the unit of the associated {@linkplain Stopwatch}.
     */
    public long sumTime() {
        return sumTime;
    }

    /**
     * Retrieves the maximum time recorded in a single visit for this {@link ProfilerNode}.
     *
     * @return the maximum elapsed time recorded in the unit of the associated {@linkplain Stopwatch}.
     */
    public long maxTime() {
        return maxTime;
    }

    /**
     * Retrieves the minimum time recorded in a single visit for this {@link ProfilerNode}.
     *
     * @return the minimum elapsed time recorded in the unit of the associated {@linkplain Stopwatch}.
     */
    public long minTime() {
        return minTime;
    }

    /**
     * Retrieves the {@link TimeUnit} associated with the {@link ProfilerNode}.
     * The {@link TimeUnit} defines the unit of time used for measurements
     * within this node.
     *
     * @return the {@link TimeUnit} used for time measurements, guaranteed to be non-null.
     */
    @NotNull
    public TimeUnit timeUnit() {
        return stopwatch.timeUnit();
    }

    /**
     * Retrieves the path associated with this {@link ProfilerNode}.
     * The path represents the identifier of this node prefixed by its location within the profiling hierarchy.
     *
     * @return the path of this {@link ProfilerNode}, guaranteed to be non-null.
     */
    @NotNull
    public String path() {
        return path;
    }

    /**
     * Retrieves the identifier of this {@link ProfilerNode}.
     *
     * @return the identifier of this {@link ProfilerNode}, guaranteed to be non-null.
     */
    @NotNull
    public String id() {
        return id;
    }

    /**
     * Retrieves the parent {@link ProfilerNode} of this node in the profiling hierarchy.
     * This method returns {@code null} if this node has no parent, indicating that it is
     * the root node.
     *
     * @return the parent {@link ProfilerNode}, or {@code null} if this node has no parent.
     */
    public ProfilerNode parent() {
        return parent;
    }

    /**
     * Retrieves the set of child {@link ProfilerNode} instances associated with this node.
     * The children represent the next level in the profiling hierarchy, with each child
     * representing a distinct profiling context.
     *
     * @return a {@link LinkedHashSet} containing the child nodes, guaranteed to be non-null.
     */
    @NotNull
    public LinkedHashSet<ProfilerNode> children() {
        return children;
    }

    /**
     * Compares this {@link ProfilerNode} with the specified object for equality.
     * Returns {@code true} if the specified object is an instance of {@link ProfilerNode}
     * and the paths of both instances are equal.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if the specified object is equal to this {@link ProfilerNode},
     * otherwise {@code false}.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProfilerNode node = (ProfilerNode) o;
        return path.equals(node.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public String toString() {
        return "Node{" +
                "visits=" + visits +
                ", parent=" + (parent == null ? null : "'" + parent.id + '\'') +
                ", id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", children=" + children.size() +
                '}';
    }
}