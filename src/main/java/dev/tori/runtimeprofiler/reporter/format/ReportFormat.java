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

package dev.tori.runtimeprofiler.reporter.format;

import dev.tori.runtimeprofiler.profiler.IProfiler;
import dev.tori.runtimeprofiler.reporter.IReporter;
import dev.tori.runtimeprofiler.reporter.ReportType;
import dev.tori.runtimeprofiler.reporter.format.formats.*;
import org.jetbrains.annotations.NotNull;

/**
 * The {@code ReportFormat} interface represents various formats for generating and
 * exporting profiler reports. It supports different report types, such as file-based
 * formats (e.g., HTML, Markdown, CSV) and console-based formats (e.g., printable reports).
 * Each implementing format specifies how profiling data is transformed into a formatted
 * string or file representation.
 * <p>
 * Implementations of this interface may provide behavior specific to a particular report
 * format, including string serialization, file extension retrieval for file-based formats,
 * and generating filenames based on profiling data.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @see ReportType
 * @since 3.0.0
 */
public interface ReportFormat {

    /* File */
    ReportFormat HTML = new HTMLReportFormat();
    ReportFormat MD = new MarkdownReportFormat();
    ReportFormat CSV = new CSVReportFormat();
    ReportFormat JSON = new JsonReportFormat();

    /* Console */
    ReportFormat PRINT = new PrintReportFormat();

    /* Arrays for easy iteration */
    ReportFormat[] FILE_FORMATS = new ReportFormat[]{HTML, MD, CSV, JSON};
    ReportFormat[] CONSOLE_FORMATS = new ReportFormat[]{PRINT};
    ReportFormat[] ALL_FORMATS = new ReportFormat[]{HTML, MD, CSV, JSON, PRINT};

    /**
     * Converts the provided string into an appropriate {@code ReportFormat} instance.
     * The string is matched case-insensitively to one of the supported report formats.
     *
     * @param string the string representation of the desired report format. Must not be {@code null}.
     * @return the corresponding {@code ReportFormat} instance based on the input string.
     * @throws IllegalArgumentException if the provided string does not match any supported report format.
     */
    @NotNull
    static ReportFormat fromString(@NotNull String string) {
        return switch (string.toLowerCase()) {
            case "html" -> HTML;
            case "md", "markdown" -> MD;
            case "csv" -> CSV;
            case "json" -> JSON;
            case "print" -> PRINT;
            default -> throw new IllegalArgumentException("Invalid ReportFormat: '" + string + "'!");
        };
    }

    /**
     * Retrieves the type of the report format.
     *
     * @return the {@link ReportType} for the current report format, indicating if the format is file-based,
     * console-based, or undefined.
     */
    @NotNull
    ReportType type();

    /**
     * Converts the provided {@link IProfiler} instance into a formatted string representation
     * that adheres to the defined report format.
     *
     * @param profiler an instance of {@link IProfiler} that contains profiling-related data
     *                 to be transformed into a string representation. Must not be {@code null}.
     * @return a string representation of the provided {@link IProfiler} instance formatted
     * according to the specific report format. Will never be {@code null}.
     */
    @NotNull
    String stringify(@NotNull IProfiler profiler);

    /**
     * Retrieves the file extension associated with the specific report format.
     *
     * @return the file extension as a non-null {@code String} if the report format
     * is file-based.
     * @throws UnsupportedOperationException if the report type is not {@code ReportType.FILE}
     *                                       or method is not properly overridden.
     * @implSpec This method should be overridden by implementing classes where the
     * format type is {@code ReportType.FILE}.
     */
    @NotNull
    default String fileExtension() {
        if (type() != ReportType.FILE) {
            throw new UnsupportedOperationException("Cannot get file extension for non-file report format.");
        }
        throw new UnsupportedOperationException("Method fileExtension() must be overridden by implementing classes where type() is ReportType.FILE.");
    }

    /**
     * Generates a file name by combining the label from the given {@link IProfiler},
     * the current date, and the file extension corresponding to the report format.
     *
     * @param profiler an instance of {@link IProfiler} from which the label and date
     *                 information are derived. Must not be {@code null}.
     * @return the generated file name as a non-null {@code String}, formatted based on
     * the profiler's label, current timestamp, and file extension.
     * @throws UnsupportedOperationException if the report format is not of type {@link ReportType#FILE}.
     */
    @NotNull
    default String fileName(@NotNull IProfiler profiler) {
        if (type() != ReportType.FILE) {
            throw new UnsupportedOperationException("Cannot get file name for non-file report format.");
        }
        return profiler.label() + "_" + IReporter.dateNow(profiler) + fileExtension();
    }
}