package com.castleshift.buildsrc.nbt;

import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NBTSerializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.GZIPOutputStream;

/**
 * Re-stamps DataVersion only; used when the source and target formats are
 * already compatible (both post-1.20.5 data-components).
 */
public class DefaultNbtConverter implements NbtConverter {

    @Override
    public byte[] convert(byte[] input, int targetDataVersion) {
        try {
            NamedTag namedTag = new NBTDeserializer(true).fromBytes(input);
            CompoundTag root = (CompoundTag) namedTag.getTag();

            root.putInt("DataVersion", targetDataVersion);

            return serializeToBytes(namedTag);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Serialize a NamedTag to gzip-compressed bytes with deterministic output.
     *
     * <p>The gzip stream is owned and closed here rather than left to
     * {@code NBTSerializer(true)}: that constructor wraps the output in a
     * GZIPOutputStream and only flushes it, so the deflate trailer is never
     * written. Querz's own reader tolerates the truncated result, but every
     * other gzip consumer - Minecraft included - rejects it.
     */
    protected static byte[] serializeToBytes(NamedTag namedTag) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            new NBTSerializer(false).toStream(namedTag, gzip);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return bos.toByteArray();
    }
}
