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

import dev.tori.runtimeprofiler.profiler.IProfiler;
import dev.tori.runtimeprofiler.profiler.ProfilerNode;
import dev.tori.runtimeprofiler.util.Util;
import org.jetbrains.annotations.NotNull;

/**
 * {@link HTMLReportFormat} is a concrete implementation of the {@link TemplatedFileReportFormat} class.
 * It is used to generate reports in an HTML file format, using a predefined HTML template.
 * This class defines the specific file extension, table header structure, and data formatting
 * requirements for HTML-based reports.
 * <p>
 * The HTML template is loaded from the specified location, and report content is dynamically
 * generated based on profiler data.
 * <p>
 * This format includes a table structure with detailed profiling metrics, such as visit counts,
 * average time, minimum time, maximum time, sum of times, and percentage contributions to
 * parent and total executions.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 3.0.0
 */
public class HTMLReportFormat extends TemplatedFileReportFormat {

    protected static final String HTML_TEMPLATE = "templates/template.html";

    public HTMLReportFormat() {
        this(HTML_TEMPLATE);
    }

    public HTMLReportFormat(@NotNull String templateLocation) {
        super(templateLocation);
    }

    @NotNull
    @Override
    public String fileExtension() {
        return ".html";
    }

    @Override
    protected String header(@NotNull IProfiler profiler) {
        return "<tr>" +
                "<th>ID</th>" +
                "<th>Visits</th>" +
                "<th>Avg (<unitabbr>$abbrtimeunit</unitabbr>)</th>" +
                "<th>Min (<unitabbr>$abbrtimeunit</unitabbr>)</th>" +
                "<th>Max (<unitabbr>$abbrtimeunit</unitabbr>)</th>" +
                "<th>Sum (<unitabbr>$abbrtimeunit</unitabbr>)</th>" +
                "<th>% of Parent</th>" +
                "<th>% of Session</th>" +
                "<th>Path</th>" +
                "</tr>";
    }

    @Override
    protected String data(@NotNull ProfilerNode node, long profilerRuntime) {
        String percentOfRoot = Util.percentString(node.sumTime(), profilerRuntime, "#.###", true);

        ProfilerNode parent = node.parent();
        String percentOfParent = parent == null ? "N/A" : Util.percentString(node.sumTime(), parent.sumTime(), "#.###", true);

        long avgTime = (node.sumTime() <= 0 || node.visits() <= 0) ? 0 : node.sumTime() / node.visits();

        String id = parent == null ? node.id() : '/' + node.id();
        String unit = Util.abbreviate(node.timeUnit());
        return "<tr>" +
                "<th>%s</th>".formatted(id) +
                "<td>%s</td>".formatted(node.visits()) +
                "<td><duration unit=\"%s\" original=\"%s\">%s</duration></td>".formatted(unit, avgTime, avgTime) +
                "<td><duration unit=\"%s\" original=\"%s\">%s</duration></td>".formatted(unit, node.minTime(), node.minTime()) +
                "<td><duration unit=\"%s\" original=\"%s\">%s</duration></td>".formatted(unit, node.maxTime(), node.maxTime()) +
                "<td><duration unit=\"%s\" original=\"%s\">%s</duration></td>".formatted(unit, node.sumTime(), node.sumTime()) +
                "<td>%s</td>".formatted(percentOfParent) +
                "<td>%s</td>".formatted(percentOfRoot) +
                "<td>%s</td>".formatted(node.path()) +
                "</tr>";
    }
}