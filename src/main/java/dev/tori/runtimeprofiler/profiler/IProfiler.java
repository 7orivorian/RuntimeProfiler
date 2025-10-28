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

package dev.tori.runtimeprofiler.profiler;

import dev.tori.runtimeprofiler.config.Config;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Represents a profiler interface for managing and tracking performance profiling
 * sessions in a hierarchical node structure. Each session consists of multiple
 * profiling entries, which are organized in a parent-child hierarchy.
 * <p>
 * {@linkplain IProfiler} provides methods to start and stop the profiling session,
 * manage entries (push/pop), retrieve the root and current entries, validate IDs,
 * and traverse the profiling structure.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @see ProfilerNode
 * @see Profiler
 * @since 3.0.0
 */
public interface IProfiler {

    /**
     * Collects all {@link ProfilerNode} instances traversed by the given profiler
     * into a {@link LinkedHashMap}, where the keys are the paths associated with the nodes.
     * The collection process is based on the profiler's traversal function.
     *
     * @param profiler the {@link IProfiler} instance used to traverse the profiling structure
     *                 and collect the nodes; must not be {@code null}.
     * @return a {@link LinkedHashMap} containing all collected {@link ProfilerNode} instances,
     * keyed by their associated paths.
     */
    @NotNull
    static LinkedHashMap<String, ProfilerNode> collectNodes(@NotNull IProfiler profiler) {
        LinkedHashMap<String, ProfilerNode> nodes = new LinkedHashMap<>();
        profiler.walk(node -> nodes.put(node.path(), node));
        return nodes;
    }

    /**
     * Collects all {@link ProfilerNode} instances traversed by the given profiler
     * into a {@link LinkedList}.
     * The collection process is based on the profiler's traversal function.
     *
     * @param profiler the {@link IProfiler} instance used to traverse the profiling structure
     *                 and collect the nodes; must not be {@code null}.
     * @return a {@link LinkedList} containing all {@link ProfilerNode} instances.
     */
    @NotNull
    static LinkedList<ProfilerNode> listNodes(@NotNull IProfiler profiler) {
        LinkedList<ProfilerNode> nodes = new LinkedList<>();
        profiler.walk(nodes::add);
        return nodes;
    }

    /**
     * Starts the profiling session for the profiler. This method initializes the
     * root profiling entry and clears any existing state in the profiler.
     * <p>
     * If the profiler has already been started, calling this method will throw
     * an {@link IllegalStateException}.
     *
     * @throws IllegalStateException if the profiler session is already started.
     */
    void start();

    /**
     * Stops the current profiling session by popping the current (root) profiling entry from the stack.
     * If the current entry is not fully popped or there is a mismatch in the push/pop operations,
     * an exception is thrown.
     *
     * @return the {@link ProfilerNode} that was stopped and removed from the stack.
     * @throws IllegalStateException if the profiler session ends before all paths are fully popped.
     */
    ProfilerNode stop();

    /**
     * Pushes a new profiling entry onto the stack. If the profiler is not started and
     * autoStart is enabled in the configuration, the profiler will be started automatically.
     *
     * @param id the identifier for the new profiling entry; must not be blank or contain
     *           the path separator defined in the profiler configuration.
     * @return the {@link ProfilerNode} representing the newly created and started profiling entry.
     * @throws IllegalStateException    if the profiler is not started and cannot be started automatically,
     *                                  or if the maximum path depth is exceeded.
     * @throws IllegalArgumentException if the provided ID is invalid or violates constraints.
     * @implSpec Implementations must validate the provided ID to ensure it is not blank and does not contain
     * the path separator defined in the profiler configuration.
     */
    @NotNull
    ProfilerNode push(@NotNull String id);

    /**
     * Pops the current profiling entry from the stack, marking it as stopped,
     * and sets the parent entry as the new current entry (without starting it).
     *
     * @return the {@link ProfilerNode} that was popped and stopped, or {@code null} if no entry was active.
     * @throws IllegalStateException if the profiler has already been fully popped or has not been started.
     */
    ProfilerNode pop();

    /**
     * Swaps the current profiling entry by popping the current entry from the stack,
     * then pushing a new profiling entry with the specified identifier.
     *
     * @param id the identifier for the new profiling entry; must not be blank or contain
     *           the path separator defined in the profiler configuration.
     * @return the {@link ProfilerNode} representing the newly created and started profiling entry.
     * @implNote Implementations may (and are encouraged to) delegate state and ID validation
     * to {@link #pop()} and {@link #push(String)} to avoid redundant checks.
     */
    @NotNull
    default ProfilerNode swap(@NotNull String id) {
        pop();
        return push(id);
    }

    /**
     * Swaps the current profiling entry by popping the current entry from the stack, then
     * pushing a new profiling entry with the specified identifier. If the current entry is
     * root (e.g., the profiler has not been pushed to since the initial {@link #start()}
     * call), it pushes a new profiling entry for the specified ID instead.
     *
     * @param id the identifier for the new profiling entry; must not be blank or contain
     *           the path separator defined in the profiler configuration.
     * @return the {@link ProfilerNode} representing the newly created or swapped profiling entry.
     * @throws IllegalArgumentException if the provided ID is invalid or violates constraints.
     * @throws IllegalStateException    if the profiler has not been started and cannot automatically start.
     */
    @NotNull
    default ProfilerNode swapIf(@NotNull String id) {
        ProfilerNode currentEntry = currentEntry();
        if (currentEntry == null || currentEntry.parent() == null) {
            return push(id);
        }
        return swap(id);
    }

    /**
     * Retrieves the root entry of the profiling structure.
     * The root entry will only be {@code null} if the profiler has never been started.
     *
     * @return the root {@link ProfilerNode} if it exists, or {@code null} if the profiler has never been started.
     */
    ProfilerNode root();

    /**
     * Retrieves the current profiling entry being tracked.
     *
     * @return the current {@link ProfilerNode} if available, or {@code null} if there is no active entry.
     */
    ProfilerNode currentEntry();

    /**
     * Retrieves the current configuration used by the profiler.
     *
     * @return the {@link Config} instance representing the profiler configuration.
     */
    @NotNull
    Config config();

    /**
     * Checks if the current profiling session has been started.
     *
     * @return {@code true} if the profiling session has been started,
     * otherwise {@code false}.
     */
    boolean isStarted();

    /**
     * Returns the label of this profiler. The label is a non-null, non-blank string that
     * identifies the profiler instance.
     *
     * @return the label associated with this profiler.
     */
    @NotNull
    String label();

    /**
     * Walks through the profiling tree starting from the root node and applies the given
     * {@link Consumer} action to each {@link ProfilerNode}.
     *
     * @param consumer the {@link Consumer} that defines the action to be applied to each
     *                 {@link ProfilerNode}; must not be {@code null}.
     * @throws IllegalStateException if the root node is {@code null}, which indicates the
     *                               profiler has not been run.
     * @see #walk(ProfilerNode, Consumer)
     */
    default void walk(@NotNull Consumer<ProfilerNode> consumer) {
        final ProfilerNode root = root();
        if (root == null) {
            throw new IllegalStateException("Cannot walk profiler without root node. You are likely seeing this exception because the profiler has never been run.");
        }
        walk(root, consumer);
    }

    /**
     * Recursively traverses a tree of {@link ProfilerNode} objects and applies a specified {@link Consumer}
     * action to each node in a depth-first manner.
     *
     * @param node     the root {@link ProfilerNode} to start the traversal from; must not be {@code null}.
     * @param consumer the {@link Consumer} that defines the action to be applied to each {@link ProfilerNode};
     *                 must not be {@code null}.
     */
    default void walk(@NotNull ProfilerNode node, @NotNull Consumer<ProfilerNode> consumer) {
        consumer.accept(node);
        for (ProfilerNode child : node.children()) {
            walk(child, consumer);
        }
    }

    /**
     * Validates the provided ID by ensuring it is not blank and does not contain
     * the path separator defined in the profiler configuration.
     *
     * @param id the ID to validate; must not be blank and cannot contain the path separator.
     * @throws IllegalArgumentException if the ID is blank or contains the path separator.
     * @see #config()
     */
    default void checkId(@NotNull String id) {
        if (id.isBlank()) {
            throw new IllegalArgumentException("Invalid path id: '" + id + "'. Cannot be blank!");
        }
        final String pathSeparator = config().pathSeparator();
        if (id.contains(pathSeparator)) {
            throw new IllegalArgumentException("Invalid path id: '" + id + "'. Cannot contain path separator '" + pathSeparator.translateEscapes() + "'!");
        }
    }

    /**
     * Validates that the profiler has been started.
     *
     * @throws IllegalStateException if the profiler has not been started.
     */
    default void checkStarted() {
        if (!isStarted()) {
            throw new IllegalStateException("Profiler not started!");
        }
    }
}