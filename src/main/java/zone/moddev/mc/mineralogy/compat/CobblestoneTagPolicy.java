package zone.moddev.mc.mineralogy.compat;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ITagCollection;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tags.TagRegistryManager;
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

    private CobblestoneTagPolicy() {
    }

    /** Apply after the initial server tag load and after every data reload. */
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        apply(event.getTagManager());
    }

    static void apply(ITagCollectionSupplier manager) {
        Set<Block> configuredBlocks = rawRockBlocks();
        Set<Item> configuredItems = new LinkedHashSet<>();
        for (Block block : configuredBlocks) configuredItems.add(block.asItem());

        ITagCollection<Block> blockTags = manager.getBlockTags();
        Set<Block> blocks = existing(blockTags, COBBLESTONE);
        blocks.removeAll(configuredBlocks);
        if (MineralogyConfig.makeRockCobblestoneEquivilent()) blocks.addAll(configuredBlocks);
        addBlock(blocks, "chert");
        addBlock(blocks, "pumice");

        ITagCollection<Item> itemTags = manager.getItemTags();
        Set<Item> items = existing(itemTags, COBBLESTONE);
        items.removeAll(configuredItems);
        if (MineralogyConfig.makeRockCobblestoneEquivilent()) items.addAll(configuredItems);
        addItem(items, "chert");
        addItem(items, "pumice");

        ITagCollectionSupplier configured = new ITagCollectionSupplier() {
            @Override
            public ITagCollection<Block> getBlockTags() {
                return replace(blockTags, blocks);
            }

            @Override
            public ITagCollection<Item> getItemTags() {
                return replace(itemTags, items);
            }

            @Override
            public ITagCollection<Fluid> getFluidTags() {
                return manager.getFluidTags();
            }

            @Override
            public ITagCollection<EntityType<?>> getEntityTypeTags() {
                return manager.getEntityTypeTags();
            }

            @Override
            public Map<ResourceLocation, ITagCollection<?>> getCustomTagTypes() {
                return manager.getCustomTagTypes();
            }
        };

        // Forge 36 snapshots the loaded tags in immutable collections before
        // firing TagsUpdatedEvent. Rebuild only the two cobblestone collections
        // and refetch the named wrappers used by blocks, items and recipes.
        TagRegistryManager.fetchTags(configured);

        // Ingredients cache their expanded matching stacks independently of the
        // named tag wrapper, so invalidate them after every initial load/reload.
        Ingredient.invalidateAll();
    }

    private static Set<Block> rawRockBlocks() {
        Set<Block> blocks = new LinkedHashSet<>();
        for (Material material : MaterialData.allIncludingRockSalt()) {
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Mineralogy.MODID, material.id()));
            if (block != null) blocks.add(block);
        }
        return blocks;
    }

    private static <T> Set<T> existing(ITagCollection<T> collection, ResourceLocation id) {
        ITag<T> tag = collection.get(id);
        return tag == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tag.getAllElements());
    }

    private static <T> ITagCollection<T> replace(ITagCollection<T> collection, Set<T> values) {
        if (collection.get(COBBLESTONE) == null) {
            throw new IllegalStateException("Missing required tag " + COBBLESTONE);
        }
        Map<ResourceLocation, ITag<T>> tags = new LinkedHashMap<>(collection.getIDTagMap());
        tags.put(COBBLESTONE, ITag.getTagOf(values));
        return ITagCollection.getTagCollectionFromMap(tags);
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
