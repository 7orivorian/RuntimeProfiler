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

import dev.tori.runtimeprofiler.Profiler;
import dev.tori.runtimeprofiler.write.OutputWriter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.1.0
 */
@SuppressWarnings("NonFinalUtilityClass")
public class GenerateMDTest {

    public static void main(String[] args) throws IOException {
        Profiler profiler = run();

        OutputWriter.MARKDOWN.writeToPath(profiler, new File("E:\\IntelliJ Projects\\Java\\RuntimeProfiler\\output").toPath());
    }

    private static Profiler run() {
        Profiler profiler = new Profiler("TestProfiler", TimeUnit.NANOSECONDS);
        profiler.start();

        profiler.push("Loop_A");
        boolean b = false;
        for (int i = 0; i < 100; i++) {
            profiler.push("Loop_AA");
            for (int j = 0; j < 100; j++) {
                profiler.push("Loop_AB");
                for (int k = 0; k < 100; k++) {
                    profiler.push("Loop_AC");
                    b = !b;
                    profiler.pop();
                }
                profiler.pop();
            }
            profiler.pop();
        }
        profiler.swap("Loop_B");
        for (int i = 0; i < 1000; i++) {
            b = !b;
        }
        profiler.pop();

        profiler.stop();
        return profiler;
    }
}