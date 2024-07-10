package dev.tori.runtimeprofiler;

import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public interface IProfiler {

    @NotNull
    String[] dataHeaders();

    @NotNull
    String[] toArray(@NotNull LocData data);

    void start();

    void stop();

    void push(@NotNull String location);

    void pop();

    default void swap(@NotNull String location) {
        pop();
        push(location);
    }
}