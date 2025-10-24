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

package dev.tori.runtimeprofiler.config;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Profiler configuration preset.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 3.0.0
 */
@SuppressWarnings("UnusedReturnValue")
public class ConfigPreset {

    private @NotNull String rootId;
    private @NotNull String pathSeparator;
    private @NotNull TimeUnit timingPrecision;
    private @NotNull DateTimeFormatter dateTimeFormatter;
    private boolean autoStart;
    private int maxDepth;

    public ConfigPreset() {
        this.rootId = "root";
        this.pathSeparator = "/";
        this.timingPrecision = TimeUnit.NANOSECONDS;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss.SSS");
        this.autoStart = false;
        this.maxDepth = 1000;
    }

    @NotNull
    public String rootId() {
        return rootId;
    }

    /**
     * Sets the root identifier for this configuration preset. The root identifier
     * must not be empty and must not contain the current path separator.
     *
     * @param rootId the new root identifier to set; must be a non-null, non-empty string
     *               and must not contain the path separator.
     * @return the current {@code ConfigPreset} instance for method chaining.
     * @throws IllegalArgumentException if the provided {@code rootId} is empty or contains the path separator.
     * @throws NullPointerException     if the provided {@code rootId} is {@code null}.
     */
    public ConfigPreset rootId(@NotNull String rootId) {
        if (rootId.isEmpty() || rootId.contains(pathSeparator)) {
            throw new IllegalArgumentException("Invalid root id: '" + rootId + "'. Cannot be empty or contain path separator '" + pathSeparator + "'.");
        }
        this.rootId = rootId;
        return this;
    }

    @NotNull
    public String pathSeparator() {
        return pathSeparator;
    }

    /**
     * Sets the path separator for this configuration preset. The path separator
     * must not be empty.
     *
     * @param pathSeparator the new path separator to set; must be a non-null, non-empty string.
     * @return the current {@code ConfigPreset} instance for method chaining.
     * @throws IllegalArgumentException if the provided {@code pathSeparator} is empty.
     * @throws NullPointerException     if the provided {@code pathSeparator} is {@code null}.
     */
    public ConfigPreset pathSeparator(@NotNull String pathSeparator) {
        if (pathSeparator.isEmpty()) {
            throw new IllegalArgumentException("Path separator cannot be empty.");
        }
        this.pathSeparator = pathSeparator;
        return this;
    }

    @NotNull
    public TimeUnit timingPrecision() {
        return timingPrecision;
    }

    public ConfigPreset timingPrecision(@NotNull TimeUnit timingPrecision) {
        this.timingPrecision = timingPrecision;
        return this;
    }

    @NotNull
    public DateTimeFormatter dateTimeFormatter() {
        return dateTimeFormatter;
    }

    /**
     * Sets the {@link DateTimeFormatter} for this configuration preset based on the provided pattern.
     * The pattern must follow the rules defined in {@link DateTimeFormatter#ofPattern(String)}.
     *
     * @param pattern the pattern to be used for formatting and parsing date-times; must not be {@code null}.
     * @return the current {@code ConfigPreset} instance for method chaining.
     * @throws IllegalArgumentException if the given pattern is invalid.
     * @throws NullPointerException     if the provided {@code pattern} is {@code null}.
     */
    public ConfigPreset dateTimeFormatter(@NotNull String pattern) {
        return dateTimeFormatter(DateTimeFormatter.ofPattern(pattern));
    }

    public ConfigPreset dateTimeFormatter(@NotNull DateTimeFormatter formatter) {
        this.dateTimeFormatter = formatter;
        return this;
    }

    public boolean autoStart() {
        return autoStart;
    }

    public ConfigPreset autoStart(boolean autoStart) {
        this.autoStart = autoStart;
        return this;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public ConfigPreset maxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }
}