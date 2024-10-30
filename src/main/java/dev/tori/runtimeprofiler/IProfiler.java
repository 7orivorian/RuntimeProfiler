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
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public interface IProfiler {

    /**
     * Resets and starts this profiler.
     *
     * @throws IllegalStateException if this profiler is already started.
     */
    void start();

    /**
     * Stops this profiler.
     *
     * @return the root {@link LocData}.
     * @throws IllegalStateException if this profiler is not started OR not fully popped.
     */
    LocData stop();

    /**
     * Pushes the given location to the stack.
     *
     * @param location the location to push to.
     * @throws IllegalStateException    if this profiler is not started.
     * @throws IllegalArgumentException if the given {@code location} contains the
     *                                  {@linkplain Config#pathSeparator() path separator}.
     */
    void push(@NotNull String location);

    /**
     * Pops the current location from the stack.
     *
     * @return the popped {@link LocData}.
     * @throws IllegalStateException if this profiler is not started OR the stack is empty.
     */
    LocData pop();

    /**
     * @param location the location to push to.
     * @return the popped {@link LocData}.
     * @throws IllegalStateException    if this profiler is not started.
     * @throws IllegalArgumentException if the given {@code location} contains the
     *                                  {@linkplain Config#pathSeparator() path separator}.
     */
    default LocData swap(@NotNull String location) {
        final LocData data = pop();
        push(location);
        return data;
    }

    /**
     * Swaps the top of the stack or simply pushes if the current top of the stack is root.
     * <p>
     * More formally, this method pops the stack if {@code depth > 1}, otherwise pushes without popping.
     *
     * @param location the location to push to.
     * @return the popped {@link LocData} if {@code depth > 1}, otherwise {@code null}.
     * @throws IllegalStateException    if this profiler is not started.
     * @throws IllegalArgumentException if the given {@code location} contains the
     *                                  {@linkplain Config#pathSeparator() path separator}.
     */
    LocData swapIf(@NotNull String location);

    /**
     * @since 1.1.0
     */
    String getLabel();

    /**
     * @since 1.1.0
     */
    TimeUnit getTimingPrecision();

    /**
     * @since 1.1.0
     */
    Set<Map.Entry<String, LocData>> getEntries();

    /**
     * @since 1.1.0
     */
    long getTotalRuntime();
}