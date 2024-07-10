package dev.tori.runtimeprofiler.write;

import com.opencsv.CSVWriter;
import dev.tori.runtimeprofiler.LocData;
import dev.tori.runtimeprofiler.Profiler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * A utility class for writing profiler data to a CSV file in a specified directory. This class ensures that the file
 * names are suffixed with the current date and time down to the millisecond to avoid overwriting existing files.
 *
 * @author <a href="https://github.com/7orivorian">7orivorian</a>
 * @since 1.0.0
 */
public class DataWriter {
    /**
     * Writes the profiler data to a specified directory. The output CSV file is suffixed with the current date and time
     * to ensure uniqueness.
     *
     * @param profiler the profiler containing the data to be written; must not be null.
     * @param path     the directory where the CSV file will be written; must not be null and must exist.
     * @throws IOException           if an I/O error occurs or the specified path does not exist.
     * @throws FileNotFoundException if the specified path does not exist.
     */
    public static void writeToDir(@NotNull Profiler profiler, @NotNull Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new FileNotFoundException(path.toString());
        }
        try (CSVWriter writer = new CSVWriter(new FileWriter(new File(String.valueOf(path), generateDateSuffix(profiler.getLabel(), ".csv"))))) {
            writer.writeNext(profiler.dataHeaders(), true);
            profiler.getEntries().forEach(entry -> {
                LocData data = entry.getValue();

                writer.writeNext(profiler.toArray(data), true);
            });
        }
    }

    /**
     * Generates a filename with the given name and extension, suffixed with the current date and time.
     *
     * @param name      the base name for the file; must not be null.
     * @param extension the file extension; must not be null.
     * @return the generated filename with the current date and time suffix.
     */
    @SuppressWarnings("SameParameterValue")
    @NotNull
    private static String generateDateSuffix(@NotNull String name, @NotNull String extension) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss.SSS");
        LocalDateTime now = LocalDateTime.now();
        String suffix = now.format(formatter);
        return name + "_" + suffix + extension;
    }
}