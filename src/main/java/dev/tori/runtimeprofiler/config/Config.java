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

package dev.tori.runtimeprofiler.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.concurrent.TimeUnit;

/**
 * Global profiler configuration.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public final class Config {

    private static @NotNull String pathSeparator = "/";
    private static @NotNull TimeUnit defaultPrecision = TimeUnit.NANOSECONDS;
    /**
     * @since 1.2.0
     */
    private static int defaultMaxDepth = 1000;
    /**
     * @since 3.0.0
     */
    private static boolean defaultAutoStart = false;
    /**
     * @since 3.0.0
     */
    private static @NotNull String defaultRootId = "root";

    @Contract(pure = true)
    private Config() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }

    /**
     * Returns the configured path separator.
     *
     * @return the default path separator as a string.
     */
    @NotNull
    @Contract(pure = true)
    public static String pathSeparator() {
        return pathSeparator;
    }

    /**
     * Sets the default path separator for all profilers. The provided path separator
     * cannot be empty.
     *
     * @param pathSeparator the new path separator to set. Must be a non-empty string.
     * @throws IllegalArgumentException if the provided path separator is empty.
     */
    public static void setPathSeparator(@NotNull String pathSeparator) {
        if (pathSeparator.isEmpty()) {
            throw new IllegalArgumentException("Path separator cannot be empty.");
        }
        Config.pathSeparator = pathSeparator;
    }

    /**
     * @since 1.2.0
     */
    @Contract(pure = true)
    public static int defaultMaxDepth() {
        return defaultMaxDepth;
    }

    /**
     * Sets the default maximum depth for all profilers.
     *
     * @param defaultMaxDepth the new default maximum depth to set. Must be an integer
     *                        value in range {@code [1, Integer.MAX_VALUE]}.
     * @since 1.2.0
     */
    public static void setDefaultMaxDepth(@Range(from = 1, to = Integer.MAX_VALUE) int defaultMaxDepth) {
        Config.defaultMaxDepth = defaultMaxDepth;
    }

    @NotNull
    @Contract(pure = true)
    public static TimeUnit defaultTimeUnit() {
        return defaultPrecision;
    }

    public static void setDefaultPrecision(@NotNull TimeUnit precision) {
        Config.defaultPrecision = precision;
    }

    /**
     * @since 3.0.0
     */
    @Contract(pure = true)
    public static boolean defaultAutoStart() {
        return defaultAutoStart;
    }

    /**
     * @since 3.0.0
     */
    public static void setDefaultAutoStart(boolean defaultAutoStart) {
        Config.defaultAutoStart = defaultAutoStart;
    }

    /**
     * @since 3.0.0
     */
    @NotNull
    @Contract(pure = true)
    public static String defaultRootId() {
        return defaultRootId;
    }

    /**
     * Sets the default root ID for all profilers. The provided root ID cannot be empty
     * or contain the {@linkplain #pathSeparator path separator} defined in the configuration.
     *
     * @param defaultRootId the new default root ID to set. Must be a non-null, non-empty string,
     *                      and must not contain the {@linkplain #pathSeparator path separator}.
     * @throws IllegalArgumentException if the provided root ID is empty or contains the path separator.
     * @since 3.0.0
     */
    public static void setDefaultRootId(@NotNull String defaultRootId) {
        if (defaultRootId.isEmpty() || defaultRootId.contains(pathSeparator)) {
            throw new IllegalArgumentException("Invalid root id: '" + defaultRootId + "'. Cannot be empty or contain path separator '" + pathSeparator + "'.");
        }
        Config.defaultRootId = defaultRootId;
    }
}