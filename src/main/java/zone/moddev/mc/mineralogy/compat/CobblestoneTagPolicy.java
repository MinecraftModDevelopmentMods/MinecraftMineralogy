package zone.moddev.mc.mineralogy.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SetTag;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.tags.TagContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
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

    static void apply(TagContainer manager) {
        TagCollection<Block> blockTags = manager.getOrEmpty(Registry.BLOCK_REGISTRY);
        TagCollection<Item> itemTags = manager.getOrEmpty(Registry.ITEM_REGISTRY);
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

        Mineralogy.LOGGER.debug("Applied Forge 37 cobblestone policy: enabled={}, rocks={}, "
                + "forgeItems={}, craftingItems={}, toolItems={}", enabled, configuredItems.size(),
                required(itemTags, COBBLESTONE).getValues().size(),
                required(itemTags, STONE_CRAFTING_MATERIALS).getValues().size(),
                required(itemTags, STONE_TOOL_MATERIALS).getValues().size());

        // Ingredients cache their expanded matching stacks independently of the
        // retained tag object, so invalidate them after every initial load/reload.
        Ingredient.invalidateAll();
    }

    private static Set<Block> rawRockBlocks(TagCollection<Block> tags) {
        Set<Block> blocks = new LinkedHashSet<>();
        for (Material material : MaterialData.allIncludingRockSalt()) {
            blocks.addAll(required(tags, familyTag(material)).getValues());
        }
        return blocks;
    }

    private static Set<Item> rawRockItems(TagCollection<Item> tags) {
        Set<Item> items = new LinkedHashSet<>();
        for (Material material : MaterialData.allIncludingRockSalt()) {
            items.addAll(required(tags, familyTag(material)).getValues());
        }
        return items;
    }

    private static ResourceLocation familyTag(Material material) {
        return new ResourceLocation(Mineralogy.MODID, "stones/" + material.id());
    }

    private static <T> Set<T> existing(TagCollection<T> collection, ResourceLocation id) {
        Tag<T> tag = collection.getTag(id);
        return tag == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tag.getValues());
    }

    private static void updateDerivedItemTag(TagCollection<Item> tags, ResourceLocation id,
            Set<Item> configuredItems, boolean enabled) {
        Set<Item> values = existing(tags, id);
        values.removeAll(configuredItems);
        if (enabled) values.addAll(configuredItems);
        addItem(values, "chert");
        addItem(values, "pumice");
        replaceElementsInPlace(required(tags, id), values);
    }

    private static <T> Tag<T> required(TagCollection<T> collection, ResourceLocation id) {
        Tag<T> tag = collection.getTag(id);
        if (tag == null) {
            throw new IllegalStateException("Missing required tag " + id);
        }
        return tag;
    }

    /**
     * Forge 37 recipes retain the concrete tag instance resolved while their
     * ingredients are parsed. Replacing the surrounding collection therefore
     * leaves recipes pointing at stale immutable contents. Update both fields
     * on that exact instance so membership tests and expanded stack lists agree.
     */
    @SuppressWarnings("unchecked")
    static <T> void replaceElementsInPlace(Tag<T> tag, Set<T> values) {
        if (!(tag instanceof SetTag)) {
            throw new IllegalStateException("Unsupported Forge 37 tag implementation "
                    + tag.getClass().getName());
        }
        SetTag<T> replacement = SetTag.create(values);
        for (Field field : SetTag.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            try {
                field.setAccessible(true);
                field.set(tag, field.get(replacement));
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException("Could not update retained Forge 37 tag field "
                        + field.getName(), ex);
            }
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
