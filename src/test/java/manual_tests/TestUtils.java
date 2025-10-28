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

package manual_tests;

import dev.tori.runtimeprofiler.profiler.IProfiler;
import dev.tori.runtimeprofiler.profiler.Profiler;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 2.1.0
 */
class TestUtils {

    static IProfiler mockProfiler() {
        Profiler profiler = new Profiler("TestProfiler");
        profiler.start();

        profiler.push("Loop_A");
        int b = 0;
        for (int i = 0; i < 10; i++) {
            profiler.push("Loop_B");
            for (int k = 0; k < 10; k++) {
                profiler.push("Flip_A");
                b++;
                profiler.pop();
            }
            for (int k = 0; k < 10; k++) {
                profiler.push("Flip_B");
                b++;
                profiler.pop();
            }
            profiler.pop();
        }
        profiler.swap("Loop_C");
        for (int i = 0; i < 100; i++) {
            b++;
        }
        profiler.pop();

        profiler.stop();

        System.out.println(b);

        return profiler;
    }
}