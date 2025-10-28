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
import dev.tori.runtimeprofiler.reporter.format.ConsoleReportFormat;
import dev.tori.runtimeprofiler.util.Util;
import org.jetbrains.annotations.NotNull;

import static dev.tori.runtimeprofiler.reporter.IReporter.dateNow;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 3.0.0
 */
public class PrintReportFormat implements ConsoleReportFormat {

    @NotNull
    @Override
    public String stringify(@NotNull IProfiler profiler) {
        if (profiler.root() == null) {
            throw new IllegalStateException("Cannot generate report for empty profiler!");
        }

        StringBuilder builder = new StringBuilder();

        builder.append(profiler.label()).append(" - ").append(dateNow(profiler)).append("\n");
        builder.append("Time Unit: ").append(profiler.config().timingPrecision()).append("\n\n");

        final long profilerRuntime = profiler.root().sumTime();

        profiler.walk(node -> {
            long avgTime = (node.sumTime() <= 0 || node.visits() <= 0) ? 0 : node.sumTime() / node.visits();
            String percentOfRoot = Util.percentString(node.sumTime(), profilerRuntime, "#.###", true);

            ProfilerNode parent = node.parent();
            String percentOfParent = parent == null ? "N/A" : Util.percentString(node.sumTime(), parent.sumTime(), "#.###") + " of Parent";

            String s = node.path() + ": visits=%s, Avg=%s, Min=%s, Max=%s, Sum=%s, %s, %s of Session".formatted(
                    node.visits(),
                    avgTime,
                    node.minTime(),
                    node.maxTime(),
                    node.sumTime(),
                    percentOfParent,
                    percentOfRoot
            );

            builder.append(s).append("\n");
        });
        builder.append("\n");

        return builder.toString();
    }
}