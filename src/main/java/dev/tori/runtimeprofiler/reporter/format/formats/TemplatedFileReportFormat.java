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

package dev.tori.runtimeprofiler.reporter.format.formats;

import dev.tori.runtimeprofiler.profiler.IProfiler;
import dev.tori.runtimeprofiler.profiler.ProfilerNode;
import dev.tori.runtimeprofiler.reporter.IReporter;
import dev.tori.runtimeprofiler.reporter.ReportType;
import dev.tori.runtimeprofiler.reporter.format.ReportFormat;
import dev.tori.runtimeprofiler.util.Util;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Represents an abstract report format that is generated from a template file and
 * outputted to a text-based file format. This class provides the structure and utility
 * methods necessary for generating reports.
 * <p>
 * Subclasses must define the specifics of how report headers and body data are
 * formatted, as well as the file extension of the generated reports.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @see HTMLReportFormat
 * @see MarkdownReportFormat
 * @since 3.0.0
 */
public abstract class TemplatedFileReportFormat implements ReportFormat {

    protected final @NotNull String templateLocation;

    @Contract(pure = true)
    public TemplatedFileReportFormat(@NotNull String templateLocation) {
        this.templateLocation = templateLocation;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @NotNull
    @Override
    public abstract String fileExtension();

    protected abstract String header(@NotNull IProfiler profiler);

    protected abstract String data(@NotNull ProfilerNode entry, long profilerRuntime);

    @Contract(pure = true)
    @NotNull
    @Override
    public final ReportType type() {
        return ReportType.FILE;
    }

    @NotNull
    @Override
    public String stringify(@NotNull IProfiler profiler) {
        if (profiler.root() == null) {
            throw new IllegalStateException("Cannot generate report for empty profiler!");
        }

        String string = readTemplateFileAsString();
        TimeUnit timingPrecision = profiler.config().timingPrecision();

        String date = IReporter.dateNow(profiler);

        String label = profiler.label();
        String title = label + " " + date;

        string = string.replace("$tableheader", header(profiler));

        string = string.replaceAll("\\$title", title);
        string = string.replaceAll("\\$label", label);
        string = string.replaceAll("\\$date", date);
        String unitName = timingPrecision.name().toLowerCase(Locale.ROOT);
        string = string.replaceAll("\\$timeunit", unitName.substring(0, unitName.length() - 1));
        string = string.replaceAll("\\$abbrtimeunit", Util.abbreviate(timingPrecision));

        long profilerRuntime = profiler.root().sumTime();

        StringBuilder body = new StringBuilder();
        profiler.walk(node -> body.append(data(node, profilerRuntime)));
        string = string.replaceAll("\\$tablebody", body.toString());

        return string;
    }

    protected String readTemplateFileAsString() {
        return IReporter.readResourceAsString(templateLocation);
    }
}