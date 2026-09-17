package com.castleshift.buildsrc.nbt;

import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.StringTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class V1_21ToV1_20NbtConverterTest {

    @TempDir
    Path tempDir;

    @Test
    void convertsDataVersion() {
        CompoundTag root = createStructureWithBlockEntities(Collections.emptyList());
        root.putInt("DataVersion", 3953);
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new V1_21ToV1_20NbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        assertEquals(3465, result.getInt("DataVersion"));
    }

    @Test
    void convertsItemWithComponentsToTagFormatInBlockEntities() {
        // 1.21.1 format: {slot: 0, item: {id: "minecraft:stone", count: 1, components: {minecraft:custom_data: {foo: "bar"}}}}
        CompoundTag customData = new CompoundTag();
        customData.putString("foo", "bar");
        CompoundTag components = new CompoundTag();
        components.put("minecraft:custom_data", customData);

        CompoundTag itemData = new CompoundTag();
        itemData.putString("id", "minecraft:diamond_sword");
        itemData.putInt("count", 1);
        itemData.put("components", components);

        CompoundTag item = new CompoundTag();
        item.putInt("slot", 0);
        item.put("item", itemData);

        ListTag<CompoundTag> items = new ListTag<>(CompoundTag.class);
        items.add(item);

        CompoundTag blockEntity = new CompoundTag();
        blockEntity.put("Items", items);

        CompoundTag root = createStructureWithBlockEntities(List.of(blockEntity));
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new V1_21ToV1_20NbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        CompoundTag resultBe = (CompoundTag) result.getListTag("block_entities").get(0);
        ListTag<?> resultItems = resultBe.getListTag("Items");
        CompoundTag resultItem = (CompoundTag) resultItems.get(0);

        // 1.20.1 format: {Slot: 0b, id: "minecraft:diamond_sword", Count: 1b, tag: {foo: "bar"}}
        assertEquals((byte) 0, resultItem.getByte("Slot"));
        assertEquals("minecraft:diamond_sword", resultItem.getString("id"));
        assertEquals((byte) 1, resultItem.getByte("Count"));
        CompoundTag tagData = resultItem.getCompoundTag("tag");
        assertEquals("bar", tagData.getString("foo"));
    }

    @Test
    void convertsTheSingularItemOfABlockEntity() {
        // "Item" carries a single stack in the shape item frames use, rather than an
        // "Items" inventory. Vanilla block entities use other keys for their single
        // stack, so this covers the field itself rather than a specific block.
        CompoundTag itemData = new CompoundTag();
        itemData.putString("id", "minecraft:music_disc_cat");
        itemData.putInt("count", 1);

        CompoundTag blockEntity = new CompoundTag();
        blockEntity.putString("CustomName", "keep me");
        blockEntity.put("Item", itemData);

        CompoundTag root = createStructureWithBlockEntities(List.of(blockEntity));
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new V1_21ToV1_20NbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        CompoundTag resultBe = (CompoundTag) result.getListTag("block_entities").get(0);
        CompoundTag resultItem = resultBe.getCompoundTag("Item");

        // 1.20.1 format: {id: "minecraft:music_disc_cat", Count: 1b}
        assertEquals("minecraft:music_disc_cat", resultItem.getString("id"));
        assertEquals((byte) 1, resultItem.getByte("Count"));
        // Fields the converter does not touch are carried over untouched.
        assertEquals("keep me", resultBe.getString("CustomName"));
    }

    @Test
    void leavesABlockEntityWithAnEmptyItemsListUntouched() {
        // convertBlockEntity used to return the original instance in this case, and now
        // always returns a copy. The copy has to carry every field over unchanged --
        // including the empty list, whose element type must not be rewritten.
        CompoundTag blockEntity = new CompoundTag();
        blockEntity.putString("CustomName", "keep me");
        blockEntity.put("Items", new ListTag<>(CompoundTag.class));

        CompoundTag root = createStructureWithBlockEntities(List.of(blockEntity));
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new V1_21ToV1_20NbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        CompoundTag resultBe = (CompoundTag) result.getListTag("block_entities").get(0);
        assertEquals(2, resultBe.size());
        assertEquals("keep me", resultBe.getString("CustomName"));
        assertEquals(0, resultBe.getListTag("Items").size());
    }

    @Test
    void convertsTheSingularItemOfAnInlineBlocksNbtEntry() {
        CompoundTag itemData = new CompoundTag();
        itemData.putString("id", "minecraft:brick");
        itemData.putInt("count", 1);

        CompoundTag blockNbt = new CompoundTag();
        blockNbt.put("Item", itemData);

        CompoundTag block = new CompoundTag();
        block.put("nbt", blockNbt);
        ListTag<CompoundTag> blocks = new ListTag<>(CompoundTag.class);
        blocks.add(block);

        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", 3953);
        root.put("blocks", blocks);
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new V1_21ToV1_20NbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        CompoundTag resultBlock = (CompoundTag) result.getListTag("blocks").get(0);
        CompoundTag resultItem = resultBlock.getCompoundTag("nbt").getCompoundTag("Item");
        assertEquals("minecraft:brick", resultItem.getString("id"));
        assertEquals((byte) 1, resultItem.getByte("Count"));
    }

    @Test
    void convertsEnchantmentsInComponents() {
        CompoundTag levels = new CompoundTag();
        levels.putInt("minecraft:sharpness", 5);
        CompoundTag enchantments = new CompoundTag();
        enchantments.put("levels", levels);

        CompoundTag components = new CompoundTag();
        components.put("minecraft:enchantments", enchantments);

        CompoundTag itemData = new CompoundTag();
        itemData.putString("id", "minecraft:diamond_sword");
        itemData.putInt("count", 1);
        itemData.put("components", components);

        CompoundTag item = new CompoundTag();
        item.putInt("slot", 0);
        item.put("item", itemData);

        ListTag<CompoundTag> items = new ListTag<>(CompoundTag.class);
        items.add(item);

        CompoundTag blockEntity = new CompoundTag();
        blockEntity.put("Items", items);

        CompoundTag root = createStructureWithBlockEntities(List.of(blockEntity));
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new V1_21ToV1_20NbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        CompoundTag resultBe = (CompoundTag) result.getListTag("block_entities").get(0);
        CompoundTag resultItem = (CompoundTag) resultBe.getListTag("Items").get(0);
        CompoundTag tag = resultItem.getCompoundTag("tag");
        ListTag<?> enchList = tag.getListTag("Enchantments");
        assertEquals(1, enchList.size());
        CompoundTag ench = (CompoundTag) enchList.get(0);
        assertEquals("minecraft:sharpness", ench.getString("id"));
        assertEquals((short) 5, ench.getShort("lvl"));
    }

    @Test
    void convertsDisplayNameAndLoreInComponents() {
        CompoundTag components = new CompoundTag();
        components.putString("minecraft:custom_name", "{\"text\":\"My Sword\"}");
        ListTag<StringTag> lore = new ListTag<>(StringTag.class);
        lore.addString("line1");
        lore.addString("line2");
        components.put("minecraft:lore", lore);

        CompoundTag itemData = new CompoundTag();
        itemData.putString("id", "minecraft:stone");
        itemData.putInt("count", 1);
        itemData.put("components", components);

        CompoundTag item = new CompoundTag();
        item.putInt("slot", 0);
        item.put("item", itemData);

        ListTag<CompoundTag> items = new ListTag<>(CompoundTag.class);
        items.add(item);

        CompoundTag blockEntity = new CompoundTag();
        blockEntity.put("Items", items);

        CompoundTag root = createStructureWithBlockEntities(List.of(blockEntity));
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new V1_21ToV1_20NbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        CompoundTag resultBe = (CompoundTag) result.getListTag("block_entities").get(0);
        CompoundTag resultItem = (CompoundTag) resultBe.getListTag("Items").get(0);
        CompoundTag tag = resultItem.getCompoundTag("tag");
        CompoundTag display = tag.getCompoundTag("display");
        assertEquals("{\"text\":\"My Sword\"}", display.getString("Name"));
        ListTag<?> resultLore = display.getListTag("Lore");
        assertEquals(2, resultLore.size());
    }

    @Test
    void convertsDamageAndUnbreakableInComponents() {
        CompoundTag components = new CompoundTag();
        components.putInt("minecraft:damage", 42);
        CompoundTag unbreakable = new CompoundTag();
        unbreakable.putByte("show_in_tooltip", (byte) 1);
        components.put("minecraft:unbreakable", unbreakable);

        CompoundTag itemData = new CompoundTag();
        itemData.putString("id", "minecraft:stone");
        itemData.putInt("count", 1);
        itemData.put("components", components);

        CompoundTag item = new CompoundTag();
        item.putInt("slot", 0);
        item.put("item", itemData);

        ListTag<CompoundTag> items = new ListTag<>(CompoundTag.class);
        items.add(item);

        CompoundTag blockEntity = new CompoundTag();
        blockEntity.put("Items", items);

        CompoundTag root = createStructureWithBlockEntities(List.of(blockEntity));
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new V1_21ToV1_20NbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        CompoundTag resultBe = (CompoundTag) result.getListTag("block_entities").get(0);
        CompoundTag resultItem = (CompoundTag) resultBe.getListTag("Items").get(0);
        CompoundTag tag = resultItem.getCompoundTag("tag");
        assertEquals(42, tag.getInt("Damage"));
        assertEquals((byte) 1, tag.getByte("Unbreakable"));
    }

    @Test
    void convertsInlineBlocksNbtItems() {
        CompoundTag customData = new CompoundTag();
        customData.putString("key", "value");
        CompoundTag components = new CompoundTag();
        components.put("minecraft:custom_data", customData);

        CompoundTag itemData = new CompoundTag();
        itemData.putString("id", "minecraft:stone");
        itemData.putInt("count", 3);
        itemData.put("components", components);

        CompoundTag item = new CompoundTag();
        item.putInt("slot", 1);
        item.put("item", itemData);

        ListTag<CompoundTag> items = new ListTag<>(CompoundTag.class);
        items.add(item);

        CompoundTag blockNbt = new CompoundTag();
        blockNbt.put("Items", items);

        CompoundTag block = new CompoundTag();
        block.put("nbt", blockNbt);

        ListTag<CompoundTag> blocks = new ListTag<>(CompoundTag.class);
        blocks.add(block);

        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", 3953);
        root.put("blocks", blocks);
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new V1_21ToV1_20NbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        CompoundTag resultBlock = (CompoundTag) result.getListTag("blocks").get(0);
        CompoundTag resultNbt = resultBlock.getCompoundTag("nbt");
        CompoundTag resultItem = (CompoundTag) resultNbt.getListTag("Items").get(0);
        assertEquals((byte) 1, resultItem.getByte("Slot"));
        assertEquals("minecraft:stone", resultItem.getString("id"));
        assertEquals((byte) 3, resultItem.getByte("Count"));
        CompoundTag tag = resultItem.getCompoundTag("tag");
        assertEquals("value", tag.getString("key"));
    }

    @Test
    void convertsLegacyFormatItemsCountLowercaseToCountByte() {
        // Legacy/mixed format: {Slot: 0b, id: "minecraft:stone", count: 3}
        CompoundTag item = new CompoundTag();
        item.putByte("Slot", (byte) 0);
        item.putString("id", "minecraft:stone");
        item.putInt("count", 3);

        ListTag<CompoundTag> items = new ListTag<>(CompoundTag.class);
        items.add(item);

        CompoundTag blockEntity = new CompoundTag();
        blockEntity.put("Items", items);

        CompoundTag root = createStructureWithBlockEntities(List.of(blockEntity));
        byte[] inputBytes = nbtToBytes(root);

        byte[] outputBytes = new V1_21ToV1_20NbtConverter().convert(inputBytes, 3465);

        CompoundTag result = bytesToNbt(outputBytes);
        CompoundTag resultBe = (CompoundTag) result.getListTag("block_entities").get(0);
        CompoundTag resultItem = (CompoundTag) resultBe.getListTag("Items").get(0);
        assertEquals((byte) 0, resultItem.getByte("Slot"));
        assertEquals("minecraft:stone", resultItem.getString("id"));
        assertEquals((byte) 3, resultItem.getByte("Count"));
    }

    @Test
    void producesDeterministicOutput() {
        CompoundTag root = createStructureWithBlockEntities(Collections.emptyList());
        root.putInt("DataVersion", 3953);
        byte[] inputBytes = nbtToBytes(root);

        V1_21ToV1_20NbtConverter converter = new V1_21ToV1_20NbtConverter();
        byte[] output1 = converter.convert(inputBytes, 3465);
        byte[] output2 = converter.convert(inputBytes, 3465);

        assertArrayEquals(output1, output2);
    }

    // --- Helpers ---

    private CompoundTag createStructureWithBlockEntities(List<CompoundTag> entities) {
        CompoundTag root = new CompoundTag();
        root.putInt("DataVersion", 3953);
        ListTag<CompoundTag> beList = new ListTag<>(CompoundTag.class);
        beList.addAll(entities);
        root.put("block_entities", beList);
        return root;
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
