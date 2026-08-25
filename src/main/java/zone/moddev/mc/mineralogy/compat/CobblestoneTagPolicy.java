package zone.moddev.mc.mineralogy.compat;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
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

    public static void onServerAboutToStart(FMLServerAboutToStartEvent event) {
        apply();
        event.getServer().getResourceManager().addReloadListener(CobblestoneTagPolicy::onReload);
    }

    private static void onReload(IResourceManager resources) {
        apply();
    }

    public static void apply() {
        Set<Block> configuredBlocks = rawRockBlocks();
        Set<Item> configuredItems = new LinkedHashSet<>();
        for (Block block : configuredBlocks) configuredItems.add(block.asItem());

        TagCollection<Block> blockTags = BlockTags.getCollection();
        Set<Block> blocks = existing(blockTags, COBBLESTONE);
        blocks.removeAll(configuredBlocks);
        if (MineralogyConfig.makeRockCobblestoneEquivilent()) blocks.addAll(configuredBlocks);
        addBlock(blocks, "chert");
        addBlock(blocks, "pumice");
        replaceElements(blockTags, blocks);
        BlockTags.setCollection(blockTags);

        TagCollection<Item> itemTags = ItemTags.getCollection();
        Set<Item> items = existing(itemTags, COBBLESTONE);
        items.removeAll(configuredItems);
        if (MineralogyConfig.makeRockCobblestoneEquivilent()) items.addAll(configuredItems);
        addItem(items, "chert");
        addItem(items, "pumice");
        replaceElements(itemTags, items);
        ItemTags.setCollection(itemTags);

        // Recipes retain the Tag instance resolved while their ingredients are
        // parsed. Mutating that instance and invalidating Forge's ingredient
        // caches makes the configured membership effective on first load and
        // every data reload.
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

    private static <T> Set<T> existing(TagCollection<T> collection, ResourceLocation id) {
        Tag<T> tag = collection.get(id);
        return tag == null ? new LinkedHashSet<>() : new LinkedHashSet<>(tag.getAllElements());
    }

    private static <T> void replaceElements(TagCollection<T> collection, Set<T> values) {
        Tag<T> tag = collection.get(COBBLESTONE);
        if (tag == null) {
            collection.getTagMap().put(COBBLESTONE,
                    Tag.Builder.<T>create().addAll(values).build(COBBLESTONE));
            return;
        }
        Collection<T> elements = tag.getAllElements();
        elements.clear();
        elements.addAll(values);
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
