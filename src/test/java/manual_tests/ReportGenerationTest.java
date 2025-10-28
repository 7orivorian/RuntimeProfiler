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

package manual_tests;

import dev.tori.runtimeprofiler.profiler.IProfiler;
import dev.tori.runtimeprofiler.reporter.IReporter;
import dev.tori.runtimeprofiler.reporter.ReportType;
import dev.tori.runtimeprofiler.reporter.Reporter;
import dev.tori.runtimeprofiler.reporter.format.ReportFormat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.1.0
 */
@SuppressWarnings("NonFinalUtilityClass")
public class ReportGenerationTest {

    public static void main(String[] args) throws IOException {
        String property = System.getProperty("rtp.format");
        if (property == null) {
            property = "all";
        }

        Path path = new File(System.getProperty("user.dir") + "\\output").toPath();
        IProfiler profiler = TestUtils.mockProfiler();

        if (property.equals("all")) {
            IReporter.prepareDir(path);

            for (ReportFormat format : ReportFormat.ALL_FORMATS) {
                Reporter reporter = Reporter.write(profiler).format(format);
                if (format.type() == ReportType.FILE) {
                    reporter.to(path);
                } else {
                    reporter.to(System.out);
                }
                reporter.report();
            }
            return;
        }

        ReportFormat format = ReportFormat.fromString(property);

        if (format.type() == ReportType.FILE) {
            IReporter.prepareDir(path);

            Reporter.write(profiler).to(path).format(format).report();
        } else {
            Reporter.write(profiler).to(System.out).format(format).report();
        }
    }
}