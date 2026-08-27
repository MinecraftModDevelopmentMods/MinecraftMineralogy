package zone.moddev.mc.mineralogy.compat;

import java.lang.reflect.Field;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.registries.ForgeRegistries;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.data.Material;
import zone.moddev.mc.mineralogy.data.MaterialData;

/** Applies the legacy cobblestone option to target-native block and item tags. */
public final class CobblestoneTagPolicy {
    private static final ResourceLocation COBBLESTONE = new ResourceLocation("forge", "cobblestone");
    private static final ResourceLocation STONE_CRAFTING_MATERIALS =
            new ResourceLocation("minecraft", "stone_crafting_materials");
    private static final ResourceLocation STONE_TOOL_MATERIALS =
            new ResourceLocation("minecraft", "stone_tool_materials");

    private CobblestoneTagPolicy() {
    }

    /** Apply after the initial server tag load and after every data reload. */
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        apply(event.getTagManager());
    }

    static void apply(ITagCollectionSupplier manager) {
        ITagCollection<Block> blockTags = manager.getBlockTags();
        ITagCollection<Item> itemTags = manager.getItemTags();
        Set<Block> configuredBlocks = rawRockBlocks(blockTags);
        Set<Item> configuredItems = rawRockItems(itemTags);

        boolean enabled = MineralogyConfig.makeRockCobblestoneEquivilent();

        Set<Block> blocks = existing(blockTags, COBBLESTONE);
        blocks.removeAll(configuredBlocks);
        if (enabled) blocks.addAll(configuredBlocks);
        addBlock(blocks, "chert");
        addBlock(blocks, "pumice");
        replaceElementsInPlace(required(blockTags, COBBLESTONE), blocks);

        Set<Item> items = existing(itemTags, COBBLESTONE);
        items.removeAll(configuredItems);
        if (enabled) items.addAll(configuredItems);
        addItem(items, "chert");
        addItem(items, "pumice");
        replaceElementsInPlace(required(itemTags, COBBLESTONE), items);

        // Nested tags were already expanded before this event. Update their
        // retained objects as well so vanilla tool recipes see the same
        // configured membership as recipes that use forge:cobblestone directly.
        updateDerivedItemTag(itemTags, STONE_CRAFTING_MATERIALS, configuredItems, enabled);
        updateDerivedItemTag(itemTags, STONE_TOOL_MATERIALS, configuredItems, enabled);

        Mineralogy.LOGGER.debug("Applied Forge 36 cobblestone policy: enabled={}, rocks={}, "
                + "forgeItems={}, craftingItems={}, toolItems={}", enabled, configuredItems.size(),
                required(itemTags, COBBLESTONE).getAllElements().size(),
                required(itemTags, STONE_CRAFTING_MATERIALS).getAllElements().size(),
                required(itemTags, STONE_TOOL_MATERIALS).getAllElements().size());

        // Ingredients cache their expanded matching stacks independently of the
        // retained tag object, so invalidate them after every initial load/reload.
        Ingredient.invalidateAll();
    }

    private static Set<Block> rawRockBlocks(ITagCollection<Block> tags) {
        Set<Block> blocks = new LinkedHashSet<>();
        for (Material material : MaterialData.allIncludingRockSalt()) {
            blocks.addAll(required(tags, familyTag(material)).getAllElements());
        }
        return blocks;
    }

    private static Set<Item> rawRockItems(ITagCollection<Item> tags) {
        Set<Item> items = new LinkedHashSet<>();
        for (Material material : MaterialData.allIncludingRockSalt()) {
            items.addAll(required(tags, familyTag(material)).getAllElements());
        }
        return items;
    }

    private static ResourceLocation familyTag(Material material) {
        return new ResourceLocation(Mineralogy.MODID, "stones/" + material.id());
    }

    private static <T> Set<T> existing(ITagCollection<T> collection, ResourceLocation id) {
        ITag<T> tag = collection.get(id);
        return tag == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tag.getAllElements());
    }

    private static void updateDerivedItemTag(ITagCollection<Item> tags, ResourceLocation id,
            Set<Item> configuredItems, boolean enabled) {
        Set<Item> values = existing(tags, id);
        values.removeAll(configuredItems);
        if (enabled) values.addAll(configuredItems);
        addItem(values, "chert");
        addItem(values, "pumice");
        replaceElementsInPlace(required(tags, id), values);
    }

    private static <T> ITag<T> required(ITagCollection<T> collection, ResourceLocation id) {
        ITag<T> tag = collection.get(id);
        if (tag == null) {
            throw new IllegalStateException("Missing required tag " + id);
        }
        return tag;
    }

    /**
     * Forge 36 recipes retain the concrete tag instance resolved while their
     * ingredients are parsed. Replacing the surrounding collection therefore
     * leaves recipes pointing at stale immutable contents. Update both fields
     * on that exact instance so membership tests and expanded stack lists agree.
     */
    @SuppressWarnings("unchecked")
    static <T> void replaceElementsInPlace(ITag<T> tag, Set<T> values) {
        if (!(tag instanceof Tag)) {
            throw new IllegalStateException("Unsupported Forge 36 tag implementation "
                    + tag.getClass().getName());
        }
        Tag<T> retained = (Tag<T>) tag;
        setRetainedField(retained, "field_241282_b_", List.class, ImmutableList.copyOf(values));
        setRetainedField(retained, "field_241283_c_", Set.class, ImmutableSet.copyOf(values));
    }

    private static void setRetainedField(Tag<?> retained, String mappedName,
            Class<?> fieldType, Object value) {
        Field selected = null;
        try {
            selected = Tag.class.getDeclaredField(mappedName);
        } catch (NoSuchFieldException ignored) {
            // The production jar uses obfuscated field names. Their distinct
            // List/Set types are stable on Forge 36 and identify them safely.
            for (Field field : Tag.class.getDeclaredFields()) {
                if (fieldType.isAssignableFrom(field.getType())) {
                    if (selected != null) {
                        throw new IllegalStateException("Ambiguous Forge 36 tag "
                                + fieldType.getSimpleName() + " field");
                    }
                    selected = field;
                }
            }
        }
        if (selected == null) {
            throw new IllegalStateException("Missing Forge 36 tag "
                    + fieldType.getSimpleName() + " field");
        }
        try {
            selected.setAccessible(true);
            selected.set(retained, value);
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("Could not update retained Forge 36 tag", ex);
        }
    }

    private static void addBlock(Set<Block> blocks, String name) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Mineralogy.MODID, name));
        if (block != null) blocks.add(block);
    }

    private static void addItem(Set<Item> items, String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Mineralogy.MODID, name));
        if (item != null) items.add(item);
    }
}
