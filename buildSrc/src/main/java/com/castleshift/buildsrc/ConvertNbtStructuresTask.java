package com.castleshift.buildsrc;

import com.castleshift.buildsrc.nbt.V1_21ToV1_20NbtConverter;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Converts 1.21.1 structure NBT files (data-components item format) to
 * 1.20.1 format (legacy NBT tags), replacing the former python3/nbtlib
 * {@code convert_nbt_1_21_to_1_20.py} script.
 */
public abstract class ConvertNbtStructuresTask extends DefaultTask {

    // Not @InputDirectory: the directory may legitimately not exist (mirrors the
    // previous Exec task's `inputs.dir(inputDir).optional()`), which strict
    // directory-input validation would reject. Registered manually as an
    // optional input via getInputs().dir(...) in the constructor instead.
    @Internal
    public abstract DirectoryProperty getInputDir();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @Input
    public abstract Property<Integer> getTargetDataVersion();

    @Inject
    public ConvertNbtStructuresTask() {
        getTargetDataVersion().convention(3465);
        getInputs().dir(getInputDir()).optional().withPropertyName("inputDir");
    }

    @TaskAction
    public void convert() throws IOException {
        File inputDir = getInputDir().getAsFile().getOrNull();
        File outputDir = getOutputDir().get().getAsFile();

        if (inputDir == null || !inputDir.exists()) {
            getLogger().warn("Input directory does not exist: " + inputDir);
            getLogger().warn("Skipping NBT conversion");
            return;
        }

        Path inputPath = inputDir.toPath();
        Path outputPath = outputDir.toPath();
        Files.createDirectories(outputPath);

        int targetDataVersion = getTargetDataVersion().get();
        V1_21ToV1_20NbtConverter converter = new V1_21ToV1_20NbtConverter();
        int[] convertedCount = {0};

        try (Stream<Path> files = Files.walk(inputPath)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".nbt"))
                    .sorted()
                    .forEach(inputFile -> {
                        Path relative = inputPath.relativize(inputFile);
                        Path outputFile = outputPath.resolve(relative);
                        try {
                            Files.createDirectories(outputFile.getParent());
                            byte[] converted = converter.convert(Files.readAllBytes(inputFile), targetDataVersion);
                            Files.write(outputFile, converted);
                            convertedCount[0]++;
                        } catch (IOException e) {
                            throw new UncheckedIOException("Failed to convert " + inputFile, e);
                        } catch (Exception e) {
                            throw new GradleException("Failed to convert " + inputFile + ": " + e.getMessage(), e);
                        }
                    });
        }

        getLogger().lifecycle("NBT conversion: converted " + convertedCount[0] + " file(s) to DataVersion " + targetDataVersion);
    }
}
