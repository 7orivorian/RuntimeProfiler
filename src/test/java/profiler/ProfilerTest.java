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

import dev.tori.runtimeprofiler.config.Config;
import dev.tori.runtimeprofiler.profiler.IProfiler;
import dev.tori.runtimeprofiler.profiler.Profiler;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Config config = Config.of(Config.GLOBAL).maxDepth(maxDepth).autoStart(autoStart);
        return new Profiler("Test", config);
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

        Assertions.assertTrue(profiler.config().autoStart());

        Assertions.assertDoesNotThrow(() -> profiler.push("test"));
    }

    @Test
    void testAutoStartSwap() {
        IProfiler profiler = createProfiler(true);

        Assertions.assertTrue(profiler.config().autoStart());

        Assertions.assertDoesNotThrow(() -> profiler.swapIf("test"));
    }

    /* Depth */

    @Test
    void testMaxDepth() {
        IProfiler profiler = createProfiler(1, false);

        Assertions.assertDoesNotThrow(profiler::start); // Depth 0
        Assertions.assertDoesNotThrow(() -> profiler.push("test_1")); // Depth 1
        Assertions.assertThrows(IllegalStateException.class, () -> profiler.push("test_2")); // Depth 2, exceeding max depth
    }

    @Test
    void testEntryDepth() {
        IProfiler profiler = createProfiler(false);

        Assertions.assertNull(profiler.currentEntry());

        profiler.start();
        Assertions.assertEquals(0, profiler.currentEntry().depth());

        profiler.push("test");
        Assertions.assertEquals(1, profiler.currentEntry().depth());

        profiler.pop();
        Assertions.assertEquals(0, profiler.currentEntry().depth());

        profiler.stop();
        Assertions.assertNull(profiler.currentEntry());
    }

    /* Other */

    @Test
    void testIsStarted() {
        IProfiler profiler = createProfiler(false);

        Assertions.assertFalse(profiler.isStarted());

        profiler.start();
        Assertions.assertTrue(profiler.isStarted());
    }
}