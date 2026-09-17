package com.castleshift.buildsrc;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Copies JSON data files from 1.21.1 to 1.20.1, renaming top-level
 * directories where Minecraft 1.21 changed from plural to singular naming.
 * Replaces the former python3 {@code copy_data_1_21_to_1_20.py} script.
 */
public abstract class CopyDataFilesTask extends DefaultTask {

    // 1.21 (singular) -> 1.20 (plural)
    private static final Map<String, String> DIR_RENAME_MAP = Map.of(
            "loot_table", "loot_tables",
            "structure", "structures",
            "advancement", "advancements",
            "recipe", "recipes",
            "predicate", "predicates",
            "item_modifier", "item_modifiers"
    );

    private static final String SKIP_EXTENSION = ".nbt";
    private static final String SKIP_FILENAME = ".DS_Store";

    // Not @InputDirectory: the directory may legitimately not exist. Registered
    // manually as an optional input via getInputs().dir(...) in the constructor.
    @Internal
    public abstract DirectoryProperty getInputDir();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDir();

    @Inject
    public CopyDataFilesTask() {
        getInputs().dir(getInputDir()).optional().withPropertyName("inputDir");
    }

    @TaskAction
    public void copy() throws IOException {
        File inputDir = getInputDir().getAsFile().getOrNull();
        File outputDir = getOutputDir().get().getAsFile();

        if (inputDir == null || !inputDir.exists()) {
            getLogger().warn("Input directory does not exist: " + inputDir);
            getLogger().warn("Skipping data file copy");
            return;
        }

        Path inputPath = inputDir.toPath();
        Path outputPath = outputDir.toPath();
        int[] copiedCount = {0};

        try (Stream<Path> files = Files.walk(inputPath)) {
            files.filter(Files::isRegularFile)
                    .filter(p -> !p.toString().endsWith(SKIP_EXTENSION))
                    .filter(p -> !p.getFileName().toString().equals(SKIP_FILENAME))
                    .sorted()
                    .forEach(srcFile -> {
                        Path relative = inputPath.relativize(srcFile);
                        Path destRelative = renameTopLevelDir(relative);
                        Path destFile = outputPath.resolve(destRelative);
                        try {
                            Files.createDirectories(destFile.getParent());
                            Files.copy(srcFile, destFile,
                                    StandardCopyOption.REPLACE_EXISTING,
                                    StandardCopyOption.COPY_ATTRIBUTES);
                            copiedCount[0]++;
                        } catch (IOException e) {
                            throw new UncheckedIOException("Failed to copy " + srcFile, e);
                        }
                    });
        }

        getLogger().lifecycle("Data file copy: copied " + copiedCount[0] + " file(s)");
    }

    private static Path renameTopLevelDir(Path relativePath) {
        if (relativePath.getNameCount() == 0) {
            return relativePath;
        }
        String top = relativePath.getName(0).toString();
        String renamed = DIR_RENAME_MAP.get(top);
        if (renamed == null) {
            return relativePath;
        }
        Path result = Path.of(renamed);
        for (int i = 1; i < relativePath.getNameCount(); i++) {
            result = result.resolve(relativePath.getName(i));
        }
        return result;
    }
}
