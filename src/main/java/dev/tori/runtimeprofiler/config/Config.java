/*
 * MIT License
 *
 * Copyright (c) 2025-2026 7orivorian.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.tori.runtimeprofiler.config;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Represents a configuration preset for the {@link dev.tori.runtimeprofiler.profiler.Profiler} class.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 3.0.0
 */
@SuppressWarnings("UnusedReturnValue")
public class Config {

    /**
     * A globally accessible, mutable instance of the {@code Config} class.
     * <p>
     * This configuration serves as a default or shared preset across the library,
     * and can be modified in implementations of the library as needed.
     */
    public static final @NotNull Config GLOBAL = new Config();

    /**
     * Represents the identifier of the root node in a profiling tree.
     * <p>
     * The identifier must adhere to the following constraints:
     * <ul>
     *     <li>It must be a non-null, non-blank string.</li>
     *     <li>It must not contain the path separator defined for the configuration.</li>
     * </ul>
     */
    private @NotNull String rootId;
    /**
     * Represents the character or string used as a delimiter to separate parts of a hierarchical path.
     * <p>
     * This value must be a non-null, non-empty string.
     */
    private @NotNull String pathSeparator;
    /**
     * Represents the precision of the timing values used when profiling. A {@link TimeUnit} of {@link TimeUnit#NANOSECONDS nanoseconds} is recommended.
     */
    private @NotNull TimeUnit timingPrecision;
    /**
     * Specifies the pattern used for formatting and parsing date-time values in the configuration.
     * This pattern follows the rules defined by {@link DateTimeFormatter#ofPattern(String)}.
     * It must be a valid, non-null string that represents a date-time format.
     */
    private @NotNull String dateTimePattern;
    /**
     * Represents the {@link DateTimeFormatter} used to format and parse date-times (e.g., when generating reports).
     */
    private @NotNull DateTimeFormatter dateTimeFormatter;
    /**
     * Indicates whether a profiler should automatically start when pushed to.
     */
    private boolean autoStart;
    /**
     * Represents the maximum depth of a profiler's profiling tree.
     */
    private int maxDepth;

    /**
     * Creates a new {@link Config} instance by copying the configuration settings
     * from the provided {@code other} {@link Config} instance.
     *
     * @param other the {@link Config} instance whose settings will be copied; must not be {@code null}.
     * @return a new {@link Config} instance with the same settings as the provided {@code other}.
     * @throws NullPointerException if the provided {@code other} is {@code null}.
     */
    public static Config of(@NotNull Config other) {
        return new Config().copy(other);
    }

    /**
     * Creates a new {@link Config} instance with default configuration settings.
     */
    public Config() {
        this(
                "root",
                "/",
                TimeUnit.NANOSECONDS,
                "yyyy-MM-dd-HH-mm-ss.SSS",
                false,
                1000
        );
    }

    /**
     * Constructs a new {@link Config} instance with the specified parameters.
     *
     * @see #rootId
     * @see #pathSeparator
     * @see #timingPrecision
     * @see #dateTimeFormatter
     * @see #autoStart
     * @see #maxDepth
     */
    @Contract(pure = true)
    public Config(
            @NotNull String rootId,
            @NotNull String pathSeparator,
            @NotNull TimeUnit timingPrecision,
            @NotNull String dateTimePattern,
            boolean autoStart,
            int maxDepth
    ) {
        this.rootId = rootId;
        this.pathSeparator = pathSeparator;
        this.timingPrecision = timingPrecision;
        this.dateTimePattern = dateTimePattern;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(dateTimePattern);
        this.autoStart = autoStart;
        this.maxDepth = maxDepth;
    }

    /**
     * Copies the configuration settings from the provided {@code cfg} {@link Config} instance
     * into the current {@link Config} instance.
     *
     * @param cfg the {@link Config} instance whose settings will be copied; must not be {@code null}.
     * @return the current {@link Config} instance with updated settings based on the provided {@code cfg}.
     * @throws NullPointerException if the provided {@code cfg} is {@code null}.
     */
    public Config copy(@NotNull Config cfg) {
        this.rootId = cfg.rootId;
        this.pathSeparator = cfg.pathSeparator;
        this.timingPrecision = cfg.timingPrecision;
        this.dateTimePattern = cfg.dateTimePattern;
        this.dateTimeFormatter = cfg.dateTimeFormatter;
        this.autoStart = cfg.autoStart;
        this.maxDepth = cfg.maxDepth;
        return this;
    }

    @NotNull
    public String rootId() {
        return rootId;
    }

    /**
     * Sets the root identifier for this configuration preset. The root identifier
     * must not be blank and must not contain the current path separator.
     *
     * @param rootId the new root identifier to set; must be a non-null, non-blank string
     *               and must not contain the path separator.
     * @return the current {@link Config} instance for method chaining.
     * @throws IllegalArgumentException if the provided {@code rootId} is blank or contains the path separator.
     * @throws NullPointerException     if the provided {@code rootId} is {@code null}.
     */
    public Config rootId(@NotNull String rootId) {
        if (rootId.isBlank() || rootId.contains(pathSeparator)) {
            throw new IllegalArgumentException("Invalid root id: '" + rootId + "'. Cannot be blank or contain path separator '" + pathSeparator + "'.");
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
     * @return the current {@link Config} instance for method chaining.
     * @throws IllegalArgumentException if the provided {@code pathSeparator} is empty.
     * @throws NullPointerException     if the provided {@code pathSeparator} is {@code null}.
     */
    public Config pathSeparator(@NotNull String pathSeparator) {
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

    public Config timingPrecision(@NotNull TimeUnit unit) {
        this.timingPrecision = unit;
        return this;
    }

    @NotNull
    public String dateTimePattern() {
        return dateTimePattern;
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
     * @return the current {@link Config} instance for method chaining.
     * @throws IllegalArgumentException if the given pattern is invalid.
     * @throws NullPointerException     if the provided {@code pattern} is {@code null}.
     */
    public Config dateTimeFormatter(@NotNull String pattern) {
        this.dateTimePattern = pattern;
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return this;
    }

    public boolean autoStart() {
        return autoStart;
    }

    public Config autoStart(boolean autoStart) {
        this.autoStart = autoStart;
        return this;
    }

    public int maxDepth() {
        return maxDepth;
    }

    /**
     * Sets the maximum depth for this configuration preset. The depth value must be
     * a positive integer greater than or equal to 1.
     *
     * @param maxDepth the maximum depth to set; must be a positive integer within the
     *                 range {@code [1, Integer.MAX_VALUE]}.
     * @return the current {@link Config} instance for method chaining.
     */
    public Config maxDepth(@Range(from = 1, to = Integer.MAX_VALUE) int maxDepth) {
        this.maxDepth = maxDepth;
        return this;
    }
}