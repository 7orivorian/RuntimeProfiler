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

import java.util.concurrent.TimeUnit;

/**
 * A factory class for creating instances of {@link LocData}.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public record LocDataFactory(@NotNull TimeUnit timeUnit) {

    /**
     * Creates a new instance of {@link LocData} based on the provided path. The path is processed to determine the
     * location component.
     *
     * @param path the path from which to create the {@link LocData}; must not be null.
     * @return a new instance of {@link LocData}.
     * @throws NullPointerException if the path is null.
     */
    @NotNull
    public LocData create(@NotNull String path) {
        int i = path.lastIndexOf(Config.pathSeparator());
        // loc includes any leading separator to make clear at a glance
        // that it's a sub-location in the event of duplicate loc names.
        String loc = (i == -1) ? path : path.substring(i);
        return new LocData(path, loc, timeUnit);
    }
}