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

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public final class Config {

    /**
     * @since 1.2.0
     */
    private static int defaultMaxDepth = 100;
    private static String pathSeparator = "/";
    private static TimeUnit defaultTimeUnit = TimeUnit.NANOSECONDS;

    @Contract(pure = true)
    private Config() {

    }

    /**
     * @since 1.2.0
     */
    @Contract(pure = true)
    public static int defaultMaxDepth() {
        return defaultMaxDepth;
    }

    /**
     * @since 1.2.0
     */
    public static void setDefaultMaxDepth(int defaultMaxDepth) {
        Config.defaultMaxDepth = defaultMaxDepth;
    }

    @Contract(pure = true)
    public static String pathSeparator() {
        return pathSeparator;
    }

    public static void setPathSeparator(String pathSeparator) {
        Config.pathSeparator = pathSeparator;
    }

    @Contract(pure = true)
    public static TimeUnit defaultTimeUnit() {
        return defaultTimeUnit;
    }

    public static void setDefaultTimeUnit(TimeUnit defaultTimeUnit) {
        Config.defaultTimeUnit = defaultTimeUnit;
    }
}