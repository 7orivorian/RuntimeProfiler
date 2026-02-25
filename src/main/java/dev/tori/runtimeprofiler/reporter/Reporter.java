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

package dev.tori.runtimeprofiler.reporter;

import dev.tori.runtimeprofiler.profiler.IProfiler;
import dev.tori.runtimeprofiler.reporter.format.ReportFormat;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * The {@link Reporter} class is responsible for generating reports based on data from an associated {@link IProfiler}.
 * Reports can be directed to a file or printed to the console, depending on the specified {@linkplain #format report format}.
 * <p>
 * This class provides methods for configuring the reporting format, output path, and output stream,
 * as well as setting the profiler to generate the report for.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @see ReportType
 * @see ReportFormat
 * @see IReporter
 * @see IProfiler
 * @since 3.0.0
 */
public class Reporter implements IReporter {

    private @NotNull IProfiler profiler;

    private @Nullable ReportFormat format;
    private @Nullable Path path;
    private @Nullable PrintStream stream;

    /**
     * Creates a new {@link Reporter} bound to the specified profiler.
     *
     * @param profiler the {@link IProfiler} instance to be associated with the reporter,
     *                 must not be {@code null}
     * @return a new {@link Reporter} instance configured with the provided profiler
     */
    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Reporter write(@NotNull IProfiler profiler) {
        return new Reporter(profiler);
    }

    /**
     * Constructs a new {@link Reporter} instance associated with the specified {@link IProfiler}.
     *
     * @param profiler the {@link IProfiler} instance to be associated with this {@link Reporter}; must not be {@code null}
     * @see #write(IProfiler)
     */
    @Contract(pure = true)
    private Reporter(@NotNull IProfiler profiler) {
        this.profiler = profiler;

        this.format = null;

        this.path = null;
        this.stream = null;
    }

    /**
     * Generates a report based on the specified reporting format and outputs it either to a file or the console.
     * <p>
     * Throws an {@link IllegalStateException} if the reporter is not properly initialized with the necessary
     * format, output path, or output stream depending on the format type.
     * <p>
     * The report behavior depends on the {@link ReportFormat#type()}:
     * <ul>
     *     <li>{@code FILE}: Writes the report to a file located at the specified path. If the path is {@code null}, an exception is thrown.</li>
     *     <li>{@code CONSOLE}: Prints the report to the provided output stream. If the output stream is {@code null}, an exception is thrown.</li>
     * </ul>
     * For any other format type, an exception is thrown.
     *
     * @throws IllegalStateException if the report format, output path, or output stream is not properly initialized,
     *                               or if an invalid report type is specified.
     * @throws RuntimeException      if an I/O operation for file writing encounters an error.
     */
    @Override
    public void report() {
        if (format == null) {
            throw new IllegalStateException("Reporter has not been initialized. Please specify report format.");
        }
        switch (format.type()) {
            case FILE -> {
                if (path == null) {
                    throw new IllegalStateException("Reporter has not been initialized. Please specify directory for report file.");
                }
                try {
                    write(profiler, path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            case CONSOLE -> {
                if (stream == null) {
                    throw new IllegalStateException("Reporter has not been initialized. Please specify output stream.");
                }
                print(profiler, stream);
            }
            default -> throw new IllegalStateException("Reporter has not been initialized. Invalid report type.");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws IllegalStateException {@inheritDoc}
     */
    @NotNull
    @Override
    public ReportFormat format() {
        if (format == null) {
            throw new IllegalStateException("Reporter has not been initialized.");
        }
        return format;
    }

    /**
     * Sets the reporting format to be used by this {@link Reporter} and adjusts internal state
     * based on the {@link ReportFormat#type()}.
     * <p>
     * If the reporting format type is {@code FILE}, the output stream is cleared.
     * If the reporting format type is {@code CONSOLE}, the output path is cleared.
     *
     * @param format the {@code ReportFormat} to use for reporting, must not be {@code null}
     * @return the current {@link Reporter} instance with the updated format
     */
    public Reporter format(@NotNull ReportFormat format) {
        this.format = format;
        switch (this.format.type()) {
            case FILE -> stream = null;
            case CONSOLE -> path = null;
        }
        return this;
    }

    /**
     * Sets the output path where the generated report will be written.
     *
     * @param path the {@code Path} specifying the file location for the report,
     *             must not be {@code null}
     * @return the current {@link Reporter} instance with the updated output path
     */
    public Reporter to(@NotNull Path path) {
        this.path = path;
        return this;
    }

    /**
     * Sets the output stream where the generated report will be written.
     *
     * @param stream the {@code PrintStream} to which the report will be output,
     *               must not be {@code null}
     * @return the current {@link Reporter} instance with the updated output stream
     */
    public Reporter to(@NotNull PrintStream stream) {
        this.stream = stream;
        return this;
    }

    /**
     * Sets the profiler to generate the report for.
     *
     * @param profiler the {@link IProfiler} instance to associate with this {@link Reporter}; must not be {@code null}
     * @return the current {@link Reporter} instance with the updated profiler
     */
    public Reporter profiler(@NotNull IProfiler profiler) {
        this.profiler = profiler;
        return this;
    }
}