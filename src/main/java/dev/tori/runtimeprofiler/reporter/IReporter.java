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
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Represents a reporter interface for managing and generating reports in different formats.
 * This interface provides utilities for file preparation, path validation, current date-time retrieval,
 * and abstract definitions for report writing and printing.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 3.0.0
 */
public interface IReporter {

    /**
     * Prepares the specified directory by ensuring it exists and is a valid directory.
     * If the directory does not exist, it will attempt to create it.
     * If the directory does not exist after the creation attempt or is not a valid directory,
     * a {@link FileNotFoundException} will be thrown.
     *
     * @param path the path of the directory to prepare; must not be {@code null}.
     * @throws IOException          if an I/O error occurs or if the directory validation fails.
     * @throws NullPointerException if either the path is {@code null}
     */
    static void prepareDir(@NotNull Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new FileNotFoundException(path.toString());
        }
    }

    /**
     * Checks whether the specified path exists in the file system.
     * If the path does not exist, a {@link FileNotFoundException} is thrown.
     *
     * @param path the {@link Path} object representing the file or directory to check; must not be {@code null}.
     * @throws FileNotFoundException if the specified path does not exist.
     */
    static void checkPathExists(@NotNull Path path) throws FileNotFoundException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException(path.toString());
        }
    }

    /**
     * Reads a resource file from the classpath and returns its content as a string.
     * This method uses the {@link ClassLoader} to locate the resource and reads
     * the file content with UTF-8 encoding.
     *
     * @param resourcePath the relative path to the resource in the classpath; must not be {@code null}.
     * @return the content of the resource as a string.
     * @throws IllegalArgumentException if the resource cannot be found at the specified path.
     */
    static String readResourceAsString(String resourcePath) {
        // Use the class loader to get the resource as an InputStream
        InputStream inputStream = IReporter.class.getClassLoader().getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IllegalArgumentException("Resource not found: " + resourcePath);
        }

        // Use Scanner to read the InputStream into a String
        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
            return scanner.useDelimiter("\\A").next();
        }
    }

    /**
     * Retrieves the current date and time as a formatted string based on the
     * date-time formatter provided by the specified profiler.
     *
     * @param profiler the profiler instance that provides the configuration for
     *                 date and time formatting; must not be {@code null}.
     * @return a non-null string representing the current date and time formatted
     * according to the profiler's configuration.
     */
    @NotNull
    static String dateNow(@NotNull IProfiler profiler) {
        return LocalDateTime.now().format(profiler.config().dateTimeFormatter());
    }

    /**
     * Generates and processes a report based on the current implementation.
     * <p>
     * The exact behavior of the report generation is determined by the class implementing this interface.
     *
     * @throws IllegalStateException if the reporter is not properly initialized or is in an invalid state.
     * @implSpec Implementations must ensure that the reporter is in a valid state before generating a report. This must include checking if {@link #format()} is {@code null}.
     */
    void report();

    /**
     * Retrieves the {@link ReportFormat} of this reporter.
     * The format determines how the report will be structured and represented.
     *
     * @return a non-null {@link ReportFormat} instance representing the current reporting format.
     * @throws IllegalStateException if the reporter has not been initialized with a format.
     * @implSpec Implementations must ensure that the format is not {@code null} before returning.
     */
    @NotNull
    ReportFormat format();

    /**
     * Writes the profiler data to the specified file path using the current reporting format.
     * The reporting format must be of type {@link ReportType#FILE}, otherwise an {@link IllegalStateException} is thrown.
     *
     * @param profiler the profiler instance containing the data to be written; must not be {@code null}.
     * @param path     the directory path where the file will be written; must not be {@code null} and must exist.
     * @throws IOException           if an I/O error occurs during writing.
     * @throws IllegalStateException if the current reporting format is not of type {@link ReportType#FILE}.
     * @throws FileNotFoundException if the specified path does not exist.
     * @throws NullPointerException  if {@code profiler} or {@code path} is {@code null}.
     */
    default void write(@NotNull IProfiler profiler, @NotNull Path path) throws IOException {
        if (format().type() != ReportType.FILE) {
            throw new IllegalStateException("Cannot write with a non-file report format");
        }
        checkPathExists(path);

        String string = format().stringify(profiler);
        String fileName = format().fileName(profiler);

        Files.writeString(new File(path.toString(), fileName).toPath(), string);
    }

    /**
     * Prints the profiler data to the given {@link PrintStream} using the current reporting format.
     * The reporting format must be of type {@link ReportType#CONSOLE}, otherwise an {@link IllegalStateException} is thrown.
     *
     * @param profiler the profiler instance containing the data to be printed; must not be {@code null}.
     * @param out      the {@link PrintStream} to which the data will be printed; must not be {@code null}.
     * @throws IllegalStateException if the reporting format is not of type {@link ReportType#CONSOLE}.
     * @throws NullPointerException  if {@code profiler} or {@code out} is {@code null}.
     */
    default void print(@NotNull IProfiler profiler, @NotNull PrintStream out) {
        if (format().type() != ReportType.CONSOLE) {
            throw new IllegalStateException("Cannot print with a non-console report format");
        }
        out.println(format().stringify(profiler));
    }
}