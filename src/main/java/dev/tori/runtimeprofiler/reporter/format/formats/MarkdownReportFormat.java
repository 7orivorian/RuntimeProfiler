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
 * {@link MarkdownReportFormat} is a concrete implementation of the {@link TemplatedFileReportFormat} class.
 * It is used to generate reports in a Markdown file format, using a predefined Markdown template.
 * This class defines the specific file extension, table header structure, and data formatting
 * requirements for Markdown-based reports.
 * <p>
 * The Markdown template is loaded from the specified location, and report content is dynamically
 * generated based on profiler data.
 * <p>
 * This format includes a table structure with detailed profiling metrics, such as visit counts,
 * average time, minimum time, maximum time, sum of times, and percentage contributions to
 * parent and total executions.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 3.0.0
 */
public class MarkdownReportFormat extends TemplatedFileReportFormat {

    protected static final String MD_TEMPLATE = "templates/template.md";

    public MarkdownReportFormat() {
        super(MD_TEMPLATE);
    }

    @NotNull
    @Override
    public String fileExtension() {
        return ".md";
    }

    @Override
    protected String header(@NotNull IProfiler profiler) {
        return "<tr>" +
                "<th>ID</th>" +
                "<th>Visits</th>" +
                "<th>Avg ($abbrtimeunit)</th>" +
                "<th>Min ($abbrtimeunit)</th>" +
                "<th>Max ($abbrtimeunit)</th>" +
                "<th>Sum ($abbrtimeunit)</th>" +
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

        String id = parent == null ? node.id() : '/' + node.id();
        long avgTime = (node.sumTime() <= 0 || node.visits() <= 0) ? 0 : node.sumTime() / node.visits();
        return "<tr>" +
                "<th>%s</th>".formatted(id) +
                "<td>%s</td>".formatted(node.visits()) +
                "<td>%s</td>".formatted(avgTime) +
                "<td>%s</td>".formatted(node.minTime()) +
                "<td>%s</td>".formatted(node.maxTime()) +
                "<td>%s</td>".formatted(node.sumTime()) +
                "<td>%s</td>".formatted(percentOfParent) +
                "<td>%s</td>".formatted(percentOfRoot) +
                "<td>%s</td>".formatted(node.path()) +
                "</tr>";
    }
}