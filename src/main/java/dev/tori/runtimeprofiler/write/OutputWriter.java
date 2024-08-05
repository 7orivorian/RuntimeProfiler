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
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.1.0
 */
public enum OutputWriter {

    CSV {
        @Override
        public void writeToPath(@NotNull IProfiler profiler, @NotNull Path path) throws IOException {
            checkPathExists(path);
            try (CSVWriter writer = new CSVWriter(new FileWriter(new File(String.valueOf(path), generateDateSuffix(profiler.getLabel()) + fileExtension())))) {
                writer.writeNext(profiler.dataHeaders(), true);
                profiler.getEntries().forEach(entry -> {
                    LocData data = entry.getValue();

                    writer.writeNext(profiler.toArray(data), true);
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
            String title = label + date;

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
            String title = label + date;

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

    public abstract void writeToPath(@NotNull IProfiler profiler, @NotNull Path path) throws IOException;

    public abstract String fileExtension();

    public void checkPathExists(@NotNull Path path) throws FileNotFoundException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException(path.toString());
        }
    }
}