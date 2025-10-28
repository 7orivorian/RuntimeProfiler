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

package dev.tori.runtimeprofiler.util;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * Assorted internal utility methods.
 * <p>
 * This class is not intended for external use and is subject to change without notice.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 3.0.0
 */
@ApiStatus.Internal
public final class Util {

    @Contract(pure = true)
    private Util() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }

    /**
     * Converts a {@link TimeUnit} to its abbreviated string representation.
     *
     * @param timeUnit the time unit to be abbreviated; must not be {@code null}
     * @return the abbreviated string representation of the given time unit
     * @throws NullPointerException if the timeUnit is {@code null}
     */
    @NotNull
    @Contract(pure = true)
    public static String abbreviate(@NotNull TimeUnit timeUnit) {
        return switch (timeUnit) {
            case NANOSECONDS -> "ns";
            case MICROSECONDS -> "us";
            case MILLISECONDS -> "ms";
            case SECONDS -> "s";
            case MINUTES -> "m";
            case HOURS -> "h";
            case DAYS -> "d";
        };
    }

    /**
     * Formats the percentage result of dividing the numerator by the denominator using the specified pattern.
     *
     * @param numerator   the value representing the numerator in the percentage calculation
     * @param denominator the value representing the denominator in the percentage calculation
     * @param pattern     the pattern used to format the percentage value
     * @return a formatted string representation of the computed percentage value
     */
    @NotNull
    public static String percentString(double numerator, double denominator, String pattern) {
        return percentString(numerator, denominator, pattern, false);
    }

    /**
     * Formats the percentage result of dividing the numerator by the denominator using the specified
     * pattern and optionally appends a percentage symbol to the formatted value.
     *
     * @param numerator    the value representing the numerator in the percentage calculation
     * @param denominator  the value representing the denominator in the percentage calculation
     * @param pattern      the pattern used to format the percentage value
     * @param appendSymbol whether to append a percentage symbol ("%") to the formatted string
     * @return a formatted string representation of the computed percentage value, optionally including
     * a percentage symbol
     */
    @NotNull
    public static String percentString(double numerator, double denominator, String pattern, boolean appendSymbol) {
        return new DecimalFormat(pattern).format(percent(numerator, denominator)) + (appendSymbol ? "%" : "");
    }

    /**
     * Calculates the percentage result of dividing the numerator by the denominator.
     * If the denominator is zero, the result will be 0 to avoid division by zero.
     *
     * @param numerator   the value representing the numerator in the percentage calculation
     * @param denominator the value representing the denominator in the percentage calculation
     * @return the computed percentage value (numerator divided by denominator, multiplied by 100),
     * or 0 if the denominator is zero
     */
    @Contract(pure = true)
    public static double percent(double numerator, double denominator) {
        if (denominator == 0) {
            return 0;
        }
        return (numerator * 100) / denominator;
    }
}