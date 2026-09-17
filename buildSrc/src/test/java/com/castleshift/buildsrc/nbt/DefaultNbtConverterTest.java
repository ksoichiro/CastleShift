package com.castleshift.buildsrc.nbt;

import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultNbtConverterTest {

    @TempDir
    Path tempDir;

    @Test
    void convertsDataVersionOnlyPreservingAllOtherData() {
        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", 3953);
        root.putString("author", "test");
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new DefaultNbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        assertEquals(3465, result.getInt("DataVersion"));
        assertEquals("test", result.getString("author"));
    }

    @Test
    void handlesNbtWithoutDataVersionField() {
        CompoundTag root = new CompoundTag();
        root.putString("author", "test");
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new DefaultNbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        assertEquals(3465, result.getInt("DataVersion"));
        assertEquals("test", result.getString("author"));
    }

    @Test
    void producesDeterministicOutputForSameInput() {
        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", 3953);
        root.putString("data", "value");
        byte[] inputBytes = nbtToBytes(root);

        DefaultNbtConverter converter = new DefaultNbtConverter();
        byte[] output1 = converter.convert(inputBytes, 3465);
        byte[] output2 = converter.convert(inputBytes, 3465);

        assertArrayEquals(output1, output2);
    }

    @Test
    void outputIsACompleteGzipStreamThatStandardReadersAccept() throws IOException {
        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", 3953);
        root.putString("author", "test");
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new DefaultNbtConverter().convert(inputBytes, 3465);

        // Querz's own reader tolerates a gzip stream with no deflate trailer,
        // so a round trip through this library cannot detect a truncated file.
        // Decompress with the JDK reader instead, which is what Minecraft and
        // every other gzip consumer effectively does.
        byte[] decompressed;
        try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(outputBytes))) {
            decompressed = gzip.readAllBytes();
        }
        // The payload must also be the complete NBT document, not just a
        // non-empty prefix that happened to decompress.
        CompoundTag reparsed = (CompoundTag) new NBTDeserializer(false).fromBytes(decompressed).getTag();
        assertEquals(3465, reparsed.getInt("DataVersion"));
        assertEquals("test", reparsed.getString("author"));
    }

    private byte[] nbtToBytes(CompoundTag tag) {
        try {
            Path file = tempDir.resolve("temp.nbt");
            NBTUtil.write(new NamedTag("", tag), file.toFile());
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private CompoundTag bytesToNbt(byte[] bytes) {
        try {
            Path file = tempDir.resolve("result.nbt");
            Files.write(file, bytes);
            return (CompoundTag) NBTUtil.read(file.toFile()).getTag();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
