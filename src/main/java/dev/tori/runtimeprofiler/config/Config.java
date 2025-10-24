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

import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Global profiler configuration.
 * <p>
 * Configurations can be loaded from a {@linkplain ConfigPreset preset} or manually set.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 3.0.0
 */
public final class Config {

    private static final ConfigPreset cfg = new ConfigPreset();

    @Contract(pure = true)
    private Config() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }

    public void loadPreset(@NotNull ConfigPreset preset) {
        cfg.rootId(preset.rootId());
        cfg.pathSeparator(preset.pathSeparator());
        cfg.timingPrecision(preset.timingPrecision());
        cfg.dateTimeFormatter(preset.dateTimeFormatter());
        cfg.autoStart(preset.autoStart());
        cfg.maxDepth(preset.maxDepth());
    }

    @NotNull
    public static String rootId() {
        return cfg.rootId();
    }

    public static void setRootId(@NotNull String rootId) {
        cfg.rootId(rootId);
    }

    @NotNull
    public static String pathSeparator() {
        return cfg.pathSeparator();
    }

    public static void setPathSeparator(@NotNull String pathSeparator) {
        cfg.pathSeparator(pathSeparator);
    }

    @NotNull
    public static TimeUnit timingPrecision() {
        return cfg.timingPrecision();
    }

    public static void setTimingPrecision(@NotNull TimeUnit precision) {
        cfg.timingPrecision(precision);
    }

    @NotNull
    public static DateTimeFormatter dateTimeFormatter() {
        return cfg.dateTimeFormatter();
    }

    public static void setDateTimeFormatter(@NotNull String pattern) {
        cfg.dateTimeFormatter(pattern);
    }

    public static void setDateTimeFormatter(@NotNull DateTimeFormatter formatter) {
        cfg.dateTimeFormatter(formatter);
    }

    public static int maxDepth() {
        return cfg.maxDepth();
    }

    public static void setMaxDepth(@Range(from = 1, to = Integer.MAX_VALUE) int maxDepth) {
        cfg.maxDepth(maxDepth);
    }

    public static boolean autoStart() {
        return cfg.autoStart();
    }

    public static void setAutoStart(boolean autoStart) {
        cfg.autoStart(autoStart);
    }
}