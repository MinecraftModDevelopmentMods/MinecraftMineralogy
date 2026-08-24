package zone.moddev.mc.mineralogy.compat;

import java.util.LinkedHashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
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
        blockTags.getTagMap().put(COBBLESTONE, Tag.Builder.<Block>create().addAll(blocks).build(COBBLESTONE));
        BlockTags.setCollection(blockTags);

        TagCollection<Item> itemTags = ItemTags.getCollection();
        Set<Item> items = existing(itemTags, COBBLESTONE);
        items.removeAll(configuredItems);
        if (MineralogyConfig.makeRockCobblestoneEquivilent()) items.addAll(configuredItems);
        addItem(items, "chert");
        addItem(items, "pumice");
        itemTags.getTagMap().put(COBBLESTONE, Tag.Builder.<Item>create().addAll(items).build(COBBLESTONE));
        ItemTags.setCollection(itemTags);
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

    private static void addBlock(Set<Block> blocks, String name) {
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(Mineralogy.MODID, name));
        if (block != null) blocks.add(block);
    }

    private static void addItem(Set<Item> items, String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Mineralogy.MODID, name));
        if (item != null) items.add(item);
    }
}
