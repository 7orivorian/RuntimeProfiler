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

package profiler;

import dev.tori.runtimeprofiler.profiler.IProfiler;
import dev.tori.runtimeprofiler.profiler.Profiler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 3.0.0
 */
public class ProfilerTest {

    @NotNull
    @Contract("_ -> new")
    static IProfiler createProfiler(boolean autoStart) {
        return createProfiler(Integer.MAX_VALUE, autoStart);
    }

    @NotNull
    @Contract("_, _ -> new")
    static IProfiler createProfiler(int maxDepth, boolean autoStart) {
        // Specify every parameter explicitly to avoid accidental changes.
        return new Profiler("Test", TimeUnit.NANOSECONDS, maxDepth, autoStart);
    }

    @Test
    void testMultipleStart() {
        IProfiler profiler = createProfiler(false);
        profiler.start();

        Assertions.assertThrows(IllegalStateException.class, profiler::start);
    }

    @Test
    void testPushBeforeStart() {
        IProfiler profiler = createProfiler(false);

        Assertions.assertThrows(IllegalStateException.class, () -> profiler.push("test"));
    }

    @Test
    void testPushAfterStop() {
        IProfiler profiler = createProfiler(false);

        profiler.start();
        profiler.stop();

        Assertions.assertThrows(IllegalStateException.class, () -> profiler.push("test"));
    }

    @Test
    void testPopBeforeStart() {
        IProfiler profiler = createProfiler(false);

        Assertions.assertThrows(IllegalStateException.class, profiler::pop);

    }

    /* AutoStart */

    @Test
    void testAutoStartPush() {
        IProfiler profiler = createProfiler(true);

        Assertions.assertTrue(profiler.canAutoStart());

        Assertions.assertDoesNotThrow(() -> profiler.push("test"));
    }

    @Test
    void testAutoStartSwap() {
        IProfiler profiler = createProfiler(true);

        Assertions.assertTrue(profiler.canAutoStart());

        Assertions.assertDoesNotThrow(() -> profiler.swapIf("test"));
    }

    /* Depth */

    @Test
    void testMaxDepth() {
        IProfiler profiler = createProfiler(3, false);

        Assertions.assertThrows(IllegalStateException.class, () -> profiler.push("test"));
    }

    @Test
    void testDepthGetter() {
        IProfiler profiler = createProfiler(false);

        Assertions.assertEquals(0, profiler.getDepth());

        profiler.start();
        Assertions.assertEquals(1, profiler.getDepth());

        profiler.push("test");
        Assertions.assertEquals(2, profiler.getDepth());

        profiler.pop();
        Assertions.assertEquals(1, profiler.getDepth());

        profiler.stop();
        Assertions.assertEquals(0, profiler.getDepth());
    }

    /* Basic Getters */

    @Test
    void testIsStarted() {
        IProfiler profiler = createProfiler(true);

        Assertions.assertFalse(profiler.isStarted());

        profiler.start();
        Assertions.assertTrue(profiler.isStarted());
    }

    @Test
    void testRuntimeGetter() {
        IProfiler profiler = createProfiler(false);

        // The runtime of a profiler that has never been started should be 0,
        // and attempts to retrieve the runtime should not throw an exception.
        Assertions.assertDoesNotThrow(profiler::getTotalRuntime);
        Assertions.assertEquals(0, profiler.getTotalRuntime());
    }
}