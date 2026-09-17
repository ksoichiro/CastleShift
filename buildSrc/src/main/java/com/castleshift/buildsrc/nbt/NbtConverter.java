package com.castleshift.buildsrc.nbt;

/**
 * Converts NBT structure files between Minecraft versions.
 * Input and output are raw gzip-compressed NBT bytes to avoid
 * coupling consumers to a specific NBT library.
 */
public interface NbtConverter {
    byte[] convert(byte[] input, int targetDataVersion);
}
