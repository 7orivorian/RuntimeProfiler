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

package dev.tori.runtimeprofiler.config;

import org.jetbrains.annotations.Contract;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public final class Config {

    private static String pathSeparator = "/";
    private static TimeUnit defaultTimeUnit = TimeUnit.NANOSECONDS;

    @Contract(pure = true)
    private Config() {

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