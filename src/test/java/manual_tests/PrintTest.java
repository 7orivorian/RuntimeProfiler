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

import dev.tori.runtimeprofiler.write.ConsoleWriter;

import static manual_tests.TestUtils.mockProfilerData;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.2.0
 */
@SuppressWarnings("NonFinalUtilityClass")
public class PrintTest {

    public static void main(String[] args) {
        String property = System.getProperty("rtp.consolewriter");
        if (property == null) {
            property = "full";
        }

        ConsoleWriter.fromString(property).print(mockProfilerData(), System.out);
    }
}