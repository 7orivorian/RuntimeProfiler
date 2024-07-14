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

package dev.tori.runtimeprofiler.write;

import dev.tori.runtimeprofiler.Profiler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * A utility class for writing profiler data to a file in a specified directory. This class ensures that the file
 * names are suffixed with the current date and time down to the millisecond to avoid overwriting existing files.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 * @deprecated use {@link OutputWriter} instead.
 */
@Deprecated(since = "1.1.0", forRemoval = true)
public final class DataWriter {

    /**
     * Cannot be instantiated.
     */
    @Contract(pure = true)
    private DataWriter() {

    }

    /**
     * Writes the profiler data to a specified directory as a CSV file. The output file is suffixed with the current date and time
     * to ensure uniqueness.
     *
     * @param profiler the profiler containing the data to be written; must not be {@code null}.
     * @param path     the directory where the CSV file will be written; must not be {@code null} and must exist.
     * @throws IOException           if an I/O error occurs or the specified path does not exist.
     * @throws FileNotFoundException if the specified path does not exist.
     * @deprecated use {@link OutputWriter} instead.
     */
    @Deprecated(since = "1.1.0", forRemoval = true)
    public static void writeToDir(@NotNull Profiler profiler, @NotNull Path path) throws IOException {
        OutputWriter.CSV.writeToPath(profiler, path);
    }
}