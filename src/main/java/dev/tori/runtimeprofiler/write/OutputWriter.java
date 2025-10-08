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

package dev.tori.runtimeprofiler.write;

import com.opencsv.CSVWriter;
import dev.tori.runtimeprofiler.IProfiler;
import dev.tori.runtimeprofiler.LocData;
import dev.tori.runtimeprofiler.util.IOUtil;
import dev.tori.runtimeprofiler.util.UnitUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * The OutputWriter enum provides different implementations for writing profiler data
 * to various file formats such as CSV, HTML, and Markdown. Each implementation specifies
 * the corresponding file format and manages the structure and writing of data entries.
 * <p>
 * Example Usage:
 * <pre>{@code
 * // Path to write the output to.
 * Path path = new File(System.getProperty("user.dir") + "\\output").toPath();
 * // Ensure the output directory exists and is a valid directory.
 * OutputWriter.prepareDir(path);
 * // Write the profiler data to the specified path.
 * OutputWriter.HTML.writeToPath(profiler, path);
 * }</pre>
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.1.0
 */
public enum OutputWriter {
    CSV {
        @Override
        public void writeToPath(@NotNull IProfiler profiler, @NotNull Path path) throws IOException {
            checkPathExists(path);
            try (CSVWriter writer = new CSVWriter(new FileWriter(new File(String.valueOf(path), generateDateSuffix(profiler.getLabel()) + fileExtension())))) {
                writer.writeNext(LocData.csvHeaders(profiler.getTimingPrecision()), true);
                profiler.getEntries().forEach(entry -> {
                    LocData data = entry.getValue();

                    writer.writeNext(data.csvRow(), true);
                });
            }
        }

        @Override
        public String fileExtension() {
            return ".csv";
        }
    },
    HTML {
        @Override
        public void writeToPath(@NotNull IProfiler profiler, @NotNull Path path) throws IOException {
            checkPathExists(path);

            String template = IOUtil.readResourceAsString(HTML_TEMPLATE);
            TimeUnit timingPrecision = profiler.getTimingPrecision();

            String date = generateDateSuffix();

            String label = profiler.getLabel();
            String title = label + " " + date;

            template = template.replace("$tableheader", LocData.headerHTML());

            template = template.replaceAll("\\$title", title);
            template = template.replaceAll("\\$label", label);
            template = template.replaceAll("\\$date", date);
            String unitName = timingPrecision.name().toLowerCase(Locale.ROOT);
            template = template.replaceAll("\\$timeunit", unitName.substring(0, unitName.length() - 1));
            template = template.replaceAll("\\$abbrtimeunit", UnitUtil.abbreviate(timingPrecision));

            Set<Map.Entry<String, LocData>> entries = profiler.getEntries();

            StringBuilder body = new StringBuilder();
            entries.forEach(entry -> {
                LocData data = entry.getValue();
                String percent = new DecimalFormat("#.###").format(((double) data.total() / profiler.getTotalRuntime()) * 100);
                body.append(data.dataHTML(percent));
            });

            template = template.replaceAll("\\$tablebody", body.toString());

            Files.writeString(new File(path.toString(), label + "_" + date + fileExtension()).toPath(), template);
        }

        @Override
        public String fileExtension() {
            return ".html";
        }
    },
    MARKDOWN {
        @Override
        public void writeToPath(@NotNull IProfiler profiler, @NotNull Path path) throws IOException {
            checkPathExists(path);

            String template = IOUtil.readResourceAsString(MD_TEMPLATE);
            TimeUnit timingPrecision = profiler.getTimingPrecision();

            String date = generateDateSuffix();

            String label = profiler.getLabel();
            String title = label + " " + date;

            template = template.replace("$tableheader", LocData.headerMD());

            template = template.replaceAll("\\$title", title);
            template = template.replaceAll("\\$label", label);
            template = template.replaceAll("\\$date", date);
            String unitName = timingPrecision.name().toLowerCase(Locale.ROOT);
            template = template.replaceAll("\\$timeunit", unitName.substring(0, unitName.length() - 1));
            template = template.replaceAll("\\$abbrtimeunit", UnitUtil.abbreviate(timingPrecision));

            Set<Map.Entry<String, LocData>> entries = profiler.getEntries();

            StringBuilder body = new StringBuilder();
            entries.forEach(entry -> {
                LocData data = entry.getValue();
                String percent = new DecimalFormat("#.###").format(((double) data.total() / profiler.getTotalRuntime()) * 100);
                body.append(data.dataMD(percent));
            });
            template = template.replaceAll("\\$tablebody", body.toString());

            Files.writeString(new File(path.toString(), label + "_" + date + fileExtension()).toPath(), template);
        }

        @Override
        public String fileExtension() {
            return ".md";
        }
    };

    private static final String HTML_TEMPLATE = "templates/template.html";
    private static final String MD_TEMPLATE = "templates/template.md";

    @Contract(pure = true)
    OutputWriter() {

    }

    /**
     * Parses the given string to return the corresponding {@code OutputWriter} instance.
     *
     * @param name the name of the output writer; must not be {@code null}.
     * @return the {@code OutputWriter} instance matching the given name.
     * @throws IllegalArgumentException if the given name does not match any valid {@code OutputWriter}.
     */
    public static OutputWriter fromString(@NotNull String name) {
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "csv" -> CSV;
            case "html" -> HTML;
            case "markdown", "md" -> MARKDOWN;
            default -> throw new IllegalArgumentException("Invalid OutputWriter name: " + name);
        };
    }

    /**
     * Prepares the specified directory by ensuring it exists and is a valid directory.
     * If the directory does not exist, it will attempt to create it.
     * If the directory does not exist after the creation attempt or is not a valid directory,
     * a {@link FileNotFoundException} will be thrown.
     *
     * @param path the path of the directory to prepare; must not be {@code null}.
     * @throws IOException if an I/O error occurs or if the directory validation fails.
     */
    public static void prepareDir(@NotNull Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createDirectory(path);
        }
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new FileNotFoundException(path.toString());
        }
    }

    /**
     * Generates a filename with the given name and extension, suffixed with the current date and time.
     *
     * @param name the base name for the file; must not be {@code null}.
     * @return the generated filename with the current date and time suffix.
     */
    @NotNull
    private static String generateDateSuffix(@NotNull String name) {
        return name + "_" + generateDateSuffix();
    }

    /**
     * Generates a filename with the given name and extension, suffixed with the current date and time.
     *
     * @return the generated filename with the current date and time suffix.
     */
    @NotNull
    private static String generateDateSuffix() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss.SSS");
        LocalDateTime now = LocalDateTime.now();
        return now.format(formatter);
    }

    /**
     * Writes the data generated by the provided profiler to the specified file path.
     * Implementation may vary depending on the type of output writer.
     *
     * @param profiler the profiler instance containing the data to write; must not be {@code null}
     * @param path     the path where the output will be written; must not be {@code null}
     * @throws IOException if an I/O error occurs during the write operation
     */
    public abstract void writeToPath(@NotNull IProfiler profiler, @NotNull Path path) throws IOException;

    /**
     * Returns the file extension associated with the specific implementation of the output writer.
     *
     * @return the file extension as a string (e.g., ".csv").
     */
    public abstract String fileExtension();

    /**
     * Checks whether the specified path exists in the file system.
     * If the path does not exist, a {@link FileNotFoundException} is thrown.
     *
     * @param path the {@link Path} object representing the file or directory to check; must not be {@code null}.
     * @throws FileNotFoundException if the specified path does not exist.
     */
    public void checkPathExists(@NotNull Path path) throws FileNotFoundException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException(path.toString());
        }
    }
}