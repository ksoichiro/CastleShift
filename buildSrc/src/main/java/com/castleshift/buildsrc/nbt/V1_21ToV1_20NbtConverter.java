package com.castleshift.buildsrc.nbt;

import net.querz.nbt.io.NBTDeserializer;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.ByteTag;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.NumberTag;
import net.querz.nbt.tag.Tag;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

/**
 * Converts 1.21.1 structure NBT (data-components item format) to 1.20.1
 * format (legacy NBT tags).
 */
public class V1_21ToV1_20NbtConverter implements NbtConverter {

    @Override
    public byte[] convert(byte[] input, int targetDataVersion) {
        NamedTag namedTag;
        try {
            namedTag = new NBTDeserializer(true).fromBytes(input);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        CompoundTag root = (CompoundTag) namedTag.getTag();

        root.putInt("DataVersion", targetDataVersion);

        // Convert separate block_entities field
        if (root.containsKey("block_entities")) {
            ListTag<?> blockEntities = root.getListTag("block_entities");
            if (blockEntities != null && blockEntities.size() > 0) {
                ListTag<CompoundTag> converted = new ListTag<>(CompoundTag.class);
                for (int i = 0; i < blockEntities.size(); i++) {
                    converted.add(convertBlockEntity((CompoundTag) blockEntities.get(i)));
                }
                root.put("block_entities", converted);
            }
        }

        // Convert inline blocks[].nbt
        if (root.containsKey("blocks")) {
            ListTag<?> blocks = root.getListTag("blocks");
            if (blocks != null) {
                for (int i = 0; i < blocks.size(); i++) {
                    CompoundTag block = (CompoundTag) blocks.get(i);
                    if (block.containsKey("nbt")) {
                        CompoundTag blockNbt = block.getCompoundTag("nbt");
                        if (blockNbt.containsKey("Items") || blockNbt.containsKey("Item")) {
                            block.put("nbt", convertBlockEntity(blockNbt));
                        }
                    }
                }
            }
        }

        // Convert entities (item frames, armor stands, etc.)
        if (root.containsKey("entities")) {
            ListTag<?> entities = root.getListTag("entities");
            if (entities != null) {
                for (int i = 0; i < entities.size(); i++) {
                    CompoundTag entity = (CompoundTag) entities.get(i);
                    if (entity.containsKey("nbt")) {
                        CompoundTag nbt = entity.getCompoundTag("nbt");
                        entity.put("nbt", convertEntityNbt(nbt));
                    }
                }
            }
        }

        return DefaultNbtConverter.serializeToBytes(namedTag);
    }

    private static CompoundTag convertBlockEntity(CompoundTag blockEntity) {
        CompoundTag result = new CompoundTag();
        for (Map.Entry<String, Tag<?>> entry : blockEntity.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        ListTag<?> items = blockEntity.containsKey("Items") ? blockEntity.getListTag("Items") : null;
        if (items != null && items.size() > 0) {
            ListTag<CompoundTag> convertedItems = new ListTag<>(CompoundTag.class);
            for (int i = 0; i < items.size(); i++) {
                CompoundTag item = (CompoundTag) items.get(i);
                if (item.containsKey("item")) {
                    // New 1.21.1 format: {slot, item: {id, count, components}}
                    convertedItems.add(convertItem121To120(item));
                } else {
                    // Legacy/mixed format: {Slot, id, count}
                    convertedItems.add(convertLegacyItemFormat(item));
                }
            }
            result.put("Items", convertedItems);
        }

        // A singular "Item" field holds one stack instead of an inventory, in the shape
        // item frames use: {id, count, components}. Vanilla block entities that hold a
        // single stack keep it under other keys -- "item" for a decorated pot or a
        // brushable block, "RecordItem" for a jukebox, "Book" for a lectern -- so this
        // branch is here to match convertEntityNbt and the python converter it replaces,
        // not because vanilla writes "Item" on a block entity today.
        if (blockEntity.containsKey("Item")) {
            CompoundTag item = blockEntity.getCompoundTag("Item");
            if (item != null) {
                result.put("Item", convertSingleItem(item));
            }
        }

        return result;
    }

    private static CompoundTag convertItem121To120(CompoundTag item121) {
        CompoundTag item120 = new CompoundTag();

        // slot (int) -> Slot (byte)
        if (item121.containsKey("slot")) {
            item120.putByte("Slot", (byte) item121.getInt("slot"));
        }

        // Extract item data from nested "item" field
        CompoundTag itemData = item121.getCompoundTag("item");
        if (itemData == null) {
            return item120;
        }

        // id -> id (string)
        if (itemData.containsKey("id")) {
            item120.putString("id", itemData.getString("id"));
        }

        // count (int) -> Count (byte)
        if (itemData.containsKey("count")) {
            item120.putByte("Count", (byte) itemData.getInt("count"));
        }

        // components -> tag
        if (itemData.containsKey("components")) {
            CompoundTag components = itemData.getCompoundTag("components");
            if (components != null && components.size() > 0) {
                CompoundTag tagData = convertComponentsToTag(components);
                if (tagData.size() > 0) {
                    item120.put("tag", tagData);
                }
            }
        }

        return item120;
    }

    private static CompoundTag convertLegacyItemFormat(CompoundTag item) {
        CompoundTag item120 = new CompoundTag();

        // Copy Slot (ensure Byte type)
        if (item.containsKey("Slot")) {
            Tag<?> slotTag = item.get("Slot");
            if (slotTag instanceof ByteTag) {
                item120.put("Slot", slotTag);
            } else {
                item120.putByte("Slot", (byte) ((NumberTag<?>) slotTag).asInt());
            }
        }

        // Copy id
        if (item.containsKey("id")) {
            item120.putString("id", item.getString("id"));
        }

        // count -> Count (byte)
        if (item.containsKey("count")) {
            item120.putByte("Count", (byte) item.getInt("count"));
        } else if (item.containsKey("Count")) {
            Tag<?> countTag = item.get("Count");
            if (countTag instanceof ByteTag) {
                item120.put("Count", countTag);
            } else {
                item120.putByte("Count", (byte) ((NumberTag<?>) countTag).asInt());
            }
        }

        // Copy tag if present
        if (item.containsKey("tag")) {
            item120.put("tag", item.getCompoundTag("tag"));
        }

        return item120;
    }

    private static CompoundTag convertEntityNbt(CompoundTag nbt) {
        CompoundTag result = new CompoundTag();
        for (Map.Entry<String, Tag<?>> entry : nbt.entrySet()) {
            result.put(entry.getKey(), entry.getValue());
        }

        // Convert single Item field (item frames)
        if (nbt.containsKey("Item")) {
            CompoundTag item = nbt.getCompoundTag("Item");
            if (item != null) {
                result.put("Item", convertSingleItem(item));
            }
        }

        // Convert ArmorItems / HandItems lists if present (armor stands)
        for (String fieldName : new String[] {"ArmorItems", "HandItems"}) {
            if (nbt.containsKey(fieldName)) {
                ListTag<?> items = nbt.getListTag(fieldName);
                if (items != null) {
                    ListTag<CompoundTag> converted = new ListTag<>(CompoundTag.class);
                    for (int i = 0; i < items.size(); i++) {
                        converted.add(convertSingleItem((CompoundTag) items.get(i)));
                    }
                    result.put(fieldName, converted);
                }
            }
        }

        String id = nbt.getString("id");
        // Current painting templates use `variant` and a horizontal `facing`.
        // Legacy versions require `Motive` and call the same horizontal value
        // `Facing`. This is distinct from item frames' six-direction Facing ID.
        if ("minecraft:painting".equals(id)) {
            if (nbt.containsKey("variant")) {
                result.putString("Motive", nbt.getString("variant"));
            }
            if (nbt.containsKey("facing")) {
                result.putByte("Facing", nbt.getByte("facing"));
            }
        }

        return result;
    }

    private static CompoundTag convertSingleItem(CompoundTag item121) {
        CompoundTag item120 = new CompoundTag();

        if (item121.containsKey("id")) {
            item120.putString("id", item121.getString("id"));
        }

        if (item121.containsKey("count")) {
            item120.putByte("Count", (byte) item121.getInt("count"));
        } else if (item121.containsKey("Count")) {
            item120.put("Count", item121.get("Count"));
        }

        if (item121.containsKey("components")) {
            CompoundTag components = item121.getCompoundTag("components");
            if (components != null && components.size() > 0) {
                CompoundTag tagData = convertComponentsToTag(components);
                if (tagData.size() > 0) {
                    item120.put("tag", tagData);
                }
            }
        }

        // Preserve existing tag if present
        if (item121.containsKey("tag")) {
            item120.put("tag", item121.getCompoundTag("tag"));
        }

        return item120;
    }

    private static CompoundTag convertComponentsToTag(CompoundTag components) {
        CompoundTag tagData = new CompoundTag();

        // minecraft:custom_data -> direct copy to tag
        if (components.containsKey("minecraft:custom_data")) {
            CompoundTag customData = components.getCompoundTag("minecraft:custom_data");
            if (customData != null) {
                for (Map.Entry<String, Tag<?>> entry : customData.entrySet()) {
                    tagData.put(entry.getKey(), entry.getValue());
                }
            }
        }

        // minecraft:enchantments -> tag.Enchantments
        if (components.containsKey("minecraft:enchantments")) {
            CompoundTag enchantComp = components.getCompoundTag("minecraft:enchantments");
            if (enchantComp != null && enchantComp.containsKey("levels")) {
                CompoundTag levels = enchantComp.getCompoundTag("levels");
                if (levels != null) {
                    ListTag<CompoundTag> enchantments = new ListTag<>(CompoundTag.class);
                    for (Map.Entry<String, Tag<?>> entry : levels.entrySet()) {
                        CompoundTag ench = new CompoundTag();
                        ench.putString("id", entry.getKey());
                        ench.putShort("lvl", (short) ((NumberTag<?>) entry.getValue()).asInt());
                        enchantments.add(ench);
                    }
                    tagData.put("Enchantments", enchantments);
                }
            }
        }

        // minecraft:custom_name / minecraft:lore -> tag.display
        CompoundTag displayData = new CompoundTag();
        if (components.containsKey("minecraft:custom_name")) {
            displayData.putString("Name", components.getString("minecraft:custom_name"));
        }
        if (components.containsKey("minecraft:lore")) {
            displayData.put("Lore", components.get("minecraft:lore"));
        }
        if (displayData.size() > 0) {
            tagData.put("display", displayData);
        }

        // minecraft:damage -> tag.Damage
        if (components.containsKey("minecraft:damage")) {
            tagData.putInt("Damage", components.getInt("minecraft:damage"));
        }

        // minecraft:unbreakable -> tag.Unbreakable
        if (components.containsKey("minecraft:unbreakable")) {
            CompoundTag unbreakable = components.getCompoundTag("minecraft:unbreakable");
            if (unbreakable != null) {
                tagData.putByte("Unbreakable", (byte) 1);
            }
        }

        return tagData;
    }
}
