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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Profiler interface for hierarchical runtime profiling.
 * <p>
 * An {@code IProfiler} maintains a logical stack of profiling entries identified by caller-provided
 * {@linkplain String ids}. Calls to {@link #push(String)} and {@link #pop()} delineate nested
 * scopes; {@link #start()} and {@link #stop()} bracket a single profiling session.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @implNote Unless otherwise documented by a particular implementation, instances are not thread-safe
 * and are intended for single-threaded use within a session.
 * @see Profiler
 * @since 1.0.0
 */
public interface IProfiler {

    /**
     * Resets the state of the profiler.
     * <p>
     * Returns the profiler to its initial unstarted state. This method is typically used
     * to prepare the profiler for a fresh start or to discard previous profiling session data.
     *
     * @throws IllegalStateException if the profiler is currently started.
     * @implSpec Implementations must reset the profiler's internal state to its initial unstarted state.
     */
    void reset();

    /**
     * Starts a new profiling session, resetting any previously collected state and
     * transitioning this profiler to the started state.
     *
     * @throws IllegalStateException if the profiler is already started.
     * @implSpec Implementations must reset the profiler's internal state to its initial unstarted state with {@link #reset()}.
     */
    void start();

    /**
     * Stops the current profiling session.
     *
     * @return the root {@link ProfileEntry}.
     * @throws IllegalStateException if the profiler is not started, or if there are
     *                               unpopped entries (except for the root) when attempting to stop.
     * @implSpec Implementations must require that all pushed entries have been popped (except for the root) at
     * the time of stopping, and this method must return the root {@link ProfileEntry}.
     */
    @NotNull
    ProfileEntry stop();

    /**
     * Pushes a {@link ProfileEntry} with the given id to the stack.
     *
     * @param id a string identifier for the new entry. Must not be {@code null}, and must
     *           not contain the configured {@linkplain Config#pathSeparator() path separator}.
     * @throws IllegalStateException    if the profiler is not started.
     * @throws IllegalArgumentException if the given {@code id} is {@code null} or contains the configured
     *                                  {@linkplain Config#pathSeparator() path separator}.
     */
    void push(@NotNull String id);

    /**
     * Pops the current {@link ProfileEntry} from the stack.
     *
     * @return the popped {@link ProfileEntry}.
     * @throws IllegalStateException if the profiler is not started OR the stack is empty.
     */
    @NotNull
    ProfileEntry pop();

    /**
     * Pops the current {@link ProfileEntry} from the stack and pushes a new one with the given id.
     *
     * @param id the id to push to.
     * @return the popped {@link ProfileEntry}.
     * @throws IllegalStateException    if the profiler is not started.
     * @throws IllegalArgumentException if the given {@code id} contains the configured
     *                                  {@linkplain Config#pathSeparator() path separator}.
     * @see #push(String)
     * @see #pop()
     */
    @NotNull
    default ProfileEntry swap(@NotNull String id) {
        final ProfileEntry popped = pop();
        push(id);
        return popped;
    }

    /**
     * Swaps the top of the stack for a new entry, or simply pushes if the current top of the stack is root.
     * <p>
     * More formally, if {@code depth > 1} this method swaps the stack, otherwise it pushes
     * without popping.
     *
     * @param id a string identifier for the new entry. Must not be {@code null}, and must
     *           not contain the configured {@linkplain Config#pathSeparator() path separator}.
     * @return the popped {@link ProfileEntry} if {@code depth > 1}, otherwise {@code null}.
     * @throws IllegalStateException    if the profiler is not started.
     * @throws IllegalArgumentException if the given {@code id} is {@code null} or contains the configured
     *                                  {@linkplain Config#pathSeparator() path separator}.
     * @see #push(String)
     * @see #swap(String)
     */
    @Nullable
    default ProfileEntry swapIf(@NotNull String id) {
        if (getDepth() <= 1) {
            push(id);
            return null;
        }
        return swap(id);
    }

    /**
     * Retrieves the top {@link ProfileEntry} from the stack without removing it.
     *
     * @return the top {@link ProfileEntry} on the stack, or {@code null} if the stack is empty.
     */
    @Nullable
    ProfileEntry getCurrentEntry();

    /**
     * Retrieves an unmodifiable view of the entries in the profiler's map.
     *
     * @return a set of map entries, where each entry represents a key-value pair
     * in the profiler's map. The set is unmodifiable, ensuring that the
     * entries cannot be altered.
     * @since 1.1.0
     */
    @NotNull
    @UnmodifiableView
    Set<Map.Entry<String, ProfileEntry>> getEntries();

    /**
     * Retrieves the profiler's label.
     *
     * @return the profiler's label as a non-null string.
     * @since 1.1.0
     */
    @NotNull
    String getLabel();

    /**
     * Retrieves the timing precision used by the profiler.
     *
     * @return the {@link TimeUnit} representing the timing precision used for measurements.
     * @since 1.1.0
     */
    @NotNull
    TimeUnit getTimingPrecision();

    /**
     * Retrieves the total runtime of the profiler in the
     * {@linkplain #getTimingPrecision() configured precision} by
     * getting the total runtime of the root {@link ProfileEntry}.
     *
     * @return the total runtime of the profiler in the {@linkplain #getTimingPrecision() configured precision}.
     * @since 1.1.0
     */
    long getTotalRuntime();

    /**
     * Retrieves the current depth of the profiling stack. This will be {@code 0} if the profiler is not started.
     *
     * @return the current stack depth as an integer.
     */
    int getDepth();

    /**
     * Checks if the profiler is currently started.
     *
     * @return {@code true} if the profiler is started, {@code false} otherwise.
     * @since 3.0.0
     */
    boolean isStarted();

    /**
     * Checks if the profiler is configured to automatically start.
     *
     * @return {@code true} if the profiler is configured to start automatically, {@code false} otherwise.
     * @since 3.0.0
     */
    boolean canAutoStart();
}