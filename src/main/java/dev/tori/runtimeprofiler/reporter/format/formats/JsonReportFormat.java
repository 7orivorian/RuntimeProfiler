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

import com.google.gson.*;
import dev.tori.runtimeprofiler.config.Config;
import dev.tori.runtimeprofiler.profiler.IProfiler;
import dev.tori.runtimeprofiler.profiler.ProfilerNode;
import dev.tori.runtimeprofiler.reporter.IReporter;
import dev.tori.runtimeprofiler.reporter.format.FileReportFormat;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.LinkedHashSet;

/**
 * Represents a JSON-based implementation of the {@link FileReportFormat} interface for generating
 * and exporting profiler reports in JSON format. This class provides functionality to serialize
 * profiling data, including the schema version, profiler metadata, and profiling entries,
 * into a structured JSON document.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @implNote An {@code IllegalStateException} will be thrown when attempting to stringify an
 * empty profiler instance without a root node.
 * @since 3.0.0
 */
public class JsonReportFormat implements FileReportFormat {

    /**
     * The schema version of the JSON report.
     * <p>
     * This value is incremented whenever the format changes in a way
     * that could conceivably break compatibility with parsers.
     */
    protected static final long SCHEMA_VERSION = 1;

    /**
     * A {@code Gson} instance used for JSON serialization and deserialization.
     */
    protected final @NotNull Gson gson;

    public JsonReportFormat() {
        this(new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .setStrictness(Strictness.STRICT)
                .setDateFormat("yyyy-MM-dd-HH-mm-ss.SSS")
                .registerTypeAdapter(ProfilerNode.class, new ProfilerNodeSerializer())
                .registerTypeAdapter(Config.class, new ConfigSerializer())
                .create()
        );
    }

    @Contract(pure = true)
    public JsonReportFormat(@NotNull Gson gson) {
        this.gson = gson;
    }

    @NotNull
    @Override
    public String stringify(@NotNull IProfiler profiler) {
        ProfilerNode root = profiler.root();
        if (root == null) {
            throw new IllegalStateException("Cannot generate report for empty profiler!");
        }

        JsonObject json = new JsonObject();

        json.addProperty("schema_version", SCHEMA_VERSION);
        json.addProperty("date_generated", IReporter.dateNow(profiler));

        JsonObject p = new JsonObject();
        p.addProperty("label", profiler.label());
        p.add("config", gson.toJsonTree(profiler.config(), Config.class));
        json.add("profiler", p);

        JsonArray entries = new JsonArray();
        entries.add(gson.toJsonTree(root, ProfilerNode.class));
        json.add("entries", entries);

        return gson.toJson(json);
    }

    @NotNull
    @Override
    public String fileExtension() {
        return ".json";
    }

    private static final class ConfigSerializer implements JsonSerializer<Config> {
        @Override
        public JsonElement serialize(Config src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.addProperty("root_id", src.rootId());
            json.addProperty("path_separator", src.pathSeparator());
            json.addProperty("timing_precision", src.timingPrecision().name());
            json.addProperty("date_time_pattern", src.dateTimePattern());
            json.addProperty("auto_start", src.autoStart());
            json.addProperty("max_depth", src.maxDepth());

            return json;
        }
    }

    private static final class ProfilerNodeSerializer implements JsonSerializer<ProfilerNode> {
        @Override
        public JsonElement serialize(ProfilerNode src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("id", src.id());
            json.addProperty("path", src.path());
            json.addProperty("visits", src.visits());
            json.addProperty("sum_time", src.sumTime());
            json.addProperty("min_time", src.minTime());
            json.addProperty("max_time", src.maxTime());

            JsonArray childrenArr = new JsonArray();
            LinkedHashSet<ProfilerNode> children = src.children();
            if (!children.isEmpty()) {
                for (ProfilerNode child : children) {
                    childrenArr.add(serialize(child, ProfilerNode.class, context));
                }
            }
            json.add("children", childrenArr);
            return json;
        }
    }
}