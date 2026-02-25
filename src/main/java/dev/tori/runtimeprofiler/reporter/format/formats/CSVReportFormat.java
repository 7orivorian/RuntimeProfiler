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

package dev.tori.runtimeprofiler.reporter.format.formats;

import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import dev.tori.runtimeprofiler.profiler.IProfiler;
import dev.tori.runtimeprofiler.profiler.ProfilerNode;
import dev.tori.runtimeprofiler.reporter.format.FileReportFormat;
import dev.tori.runtimeprofiler.reporter.format.ReportFormat;
import dev.tori.runtimeprofiler.util.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringWriter;

/**
 * The {@link CSVReportFormat} class represents a file-based report format specifically for
 * generating profiler reports in the CSV (Comma Separated Values) format. This implementation
 * adheres to the {@link ReportFormat} interface and provides methods for serializing profiling
 * data into a CSV string representation, as well as defining the expected file extension for
 * this format.
 * <p>
 * The class supports customization of the CSV structure by allowing the specification of
 * custom separator, quote, and escape characters. By default, if no configuration is provided,
 * it uses the following characters:
 * <ul>
 *     <li>Separator: ','</li>
 *     <li>Quote: '"'</li>
 *     <li>Escape: '"'</li>
 * </ul>
 * <p>
 * The resulting CSV output includes the profiler's data structured in rows and columns
 * that detail profiling results such as execution counts, average timings, minimum and
 * maximum timings, and percentage calculations relative to both parent and total session data.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @implNote An {@code IllegalStateException} will be thrown when attempting to stringify an
 * empty profiler instance without a root node.
 * @since 3.0.0
 */
public class CSVReportFormat implements FileReportFormat {

    protected final @NotNull Character separator;
    protected final @NotNull Character quotechar;
    protected final @NotNull Character escapechar;

    @Contract(pure = true)
    public CSVReportFormat() {
        this(',', '"', '"');
    }

    @Contract(pure = true)
    public CSVReportFormat(@NotNull Character separator, @NotNull Character quotechar, @NotNull Character escapechar) {
        this.separator = separator;
        this.quotechar = quotechar;
        this.escapechar = escapechar;
    }

    @NotNull
    @Override
    public String stringify(@NotNull IProfiler profiler) {
        if (profiler.root() == null) {
            throw new IllegalStateException("Cannot generate report for empty profiler!");
        }

        String abbr = Util.abbreviate(profiler.config().timingPrecision());

        StringWriter writer = new StringWriter();
        ICSVWriter csv = new CSVWriterBuilder(writer)
                .withEscapeChar(escapechar)
                .withSeparator(separator)
                .withQuoteChar(quotechar)
                .build();

        try (csv) {
            String[] firstRow = {"ID", "Visits", "Avg (%s)".formatted(abbr), "Min (%s)".formatted(abbr), "Max (%s)".formatted(abbr), "Sum (%s)".formatted(abbr), "% of Parent", "% of Session", "Path"};
            csv.writeNext(firstRow, true);

            final long profilerRuntime = profiler.root().sumTime();

            profiler.walk(node -> {
                String percentOfRoot = Util.percentString(node.sumTime(), profilerRuntime, "#.###");

                ProfilerNode parent = node.parent();
                String percentOfParent = parent == null ? "N/A" : Util.percentString(node.sumTime(), parent.sumTime(), "#.###");

                String nodeId = parent == null ? node.id() : '/' + node.id();

                long avgTime = (node.sumTime() <= 0 || node.visits() <= 0) ? 0 : node.sumTime() / node.visits();

                String[] data = {nodeId, String.valueOf(node.visits()), String.valueOf(avgTime), String.valueOf(node.minTime()), String.valueOf(node.maxTime()), String.valueOf(node.sumTime()), percentOfParent, percentOfRoot, node.path()};
                csv.writeNext(data, true);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    @NotNull
    @Override
    public String fileExtension() {
        return ".csv";
    }
}