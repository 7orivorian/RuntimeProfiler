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

import dev.tori.runtimeprofiler.IProfiler;
import dev.tori.runtimeprofiler.LocData;
import org.jetbrains.annotations.NotNull;

import java.io.PrintStream;
import java.text.DecimalFormat;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.2.0
 */
public enum ConsoleWriter {
    FULL {
        @Override
        public void print(@NotNull IProfiler profiler, @NotNull PrintStream out, int maxPathDepth) {
            out.println(profiler.getLabel());
            profiler.getEntries().forEach(entry -> {
                LocData data = entry.getValue();
                if (data.depth() > maxPathDepth) {
                    return;
                }
                String percent = new DecimalFormat("#.###").format(((double) data.total() / profiler.getTotalRuntime()) * 100);
                out.println(data.loc() + ": visits=%s, Avg=%s, Min=%s, Max=%s, Runtime=%s, %s%s of Runtime"
                        .formatted(
                                data.visits(),
                                data.avg(),
                                data.minTime(),
                                data.maxTime(),
                                data.total(),
                                percent,
                                "%"
                        )
                );
            });
        }
    },
    AVG_ONLY {
        @Override
        public void print(@NotNull IProfiler profiler, @NotNull PrintStream out, int maxPathDepth) {
            out.println(profiler.getLabel());
            profiler.getEntries().forEach(entry -> {
                LocData data = entry.getValue();
                if (data.depth() > maxPathDepth) {
                    return;
                }
                out.println(data.loc() + ": visits=%s, Avg=%s, Min=%s, Max=%s"
                        .formatted(
                                data.visits(),
                                data.avg(),
                                data.minTime(),
                                data.maxTime()
                        )
                );
            });
        }
    };

    public void print(@NotNull IProfiler profiler, @NotNull PrintStream out) {
        print(profiler, out, Integer.MAX_VALUE);
    }

    public abstract void print(@NotNull IProfiler profiler, @NotNull PrintStream out, int maxPathDepth);
}