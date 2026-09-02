package zone.moddev.mc.mineralogy.compat;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TagsUpdatedEvent;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.data.Material;
import zone.moddev.mc.mineralogy.data.MaterialData;

/** Applies the legacy cobblestone option to Forge 50 block and item tags. */
public final class CobblestoneTagPolicy {
    private static final ResourceLocation COBBLESTONE = ResourceLocation.fromNamespaceAndPath("forge", "cobblestone");
    private static final ResourceLocation STONE_CRAFTING_MATERIALS =
            ResourceLocation.fromNamespaceAndPath("minecraft", "stone_crafting_materials");
    private static final ResourceLocation STONE_TOOL_MATERIALS =
            ResourceLocation.fromNamespaceAndPath("minecraft", "stone_tool_materials");

    private CobblestoneTagPolicy() {
    }

    /** Apply after the initial tag load and every server-side data reload. */
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.shouldUpdateStaticData()) {
            apply(event.getRegistryAccess());
        }
    }

    static void apply(RegistryAccess access) {
        Registry<Block> blockRegistry = access.registryOrThrow(Registries.BLOCK);
        Registry<Item> itemRegistry = access.registryOrThrow(Registries.ITEM);
        Set<Holder<Block>> configuredBlocks = rawRockHolders(blockRegistry, Registries.BLOCK);
        Set<Holder<Item>> configuredItems = rawRockHolders(itemRegistry, Registries.ITEM);
        boolean enabled = MineralogyConfig.makeRockCobblestoneEquivilent();

        Map<TagKey<Block>, List<Holder<Block>>> blockTags = snapshot(blockRegistry);
        updateTag(blockTags, blockRegistry, Registries.BLOCK, COBBLESTONE,
                configuredBlocks, enabled, "chert", "pumice");
        blockRegistry.bindTags(blockTags);

        Map<TagKey<Item>, List<Holder<Item>>> itemTags = snapshot(itemRegistry);
        updateTag(itemTags, itemRegistry, Registries.ITEM, COBBLESTONE,
                configuredItems, enabled, "chert", "pumice");
        updateTag(itemTags, itemRegistry, Registries.ITEM, STONE_CRAFTING_MATERIALS,
                configuredItems, enabled, "chert", "pumice");
        updateTag(itemTags, itemRegistry, Registries.ITEM, STONE_TOOL_MATERIALS,
                configuredItems, enabled, "chert", "pumice");
        itemRegistry.bindTags(itemTags);

        Ingredient.invalidateAll();
        Mineralogy.LOGGER.debug("Applied Forge 50 cobblestone policy: enabled={}, rocks={}, "
                + "forgeItems={}, craftingItems={}, toolItems={}", enabled, configuredItems.size(),
                size(itemRegistry, Registries.ITEM, COBBLESTONE),
                size(itemRegistry, Registries.ITEM, STONE_CRAFTING_MATERIALS),
                size(itemRegistry, Registries.ITEM, STONE_TOOL_MATERIALS));
    }

    static <T> Map<TagKey<T>, List<Holder<T>>> snapshot(Registry<T> registry) {
        Map<TagKey<T>, List<Holder<T>>> result = new IdentityHashMap<>();
        registry.getTags().forEach(pair -> result.put(pair.getFirst(), holders(pair.getSecond())));
        return result;
    }

    private static <T> List<Holder<T>> holders(HolderSet.Named<T> values) {
        List<Holder<T>> result = new ArrayList<>();
        values.forEach(result::add);
        return result;
    }

    private static <T> Set<Holder<T>> rawRockHolders(Registry<T> registry,
            ResourceKey<? extends Registry<T>> registryKey) {
        Set<Holder<T>> values = new LinkedHashSet<>();
        for (Material material : MaterialData.allIncludingRockSalt()) {
            TagKey<T> tag = TagKey.create(registryKey,
                    ResourceLocation.fromNamespaceAndPath(Mineralogy.MODID, "stones/" + material.id()));
            registry.getTag(tag).ifPresent(named -> named.forEach(values::add));
        }
        return values;
    }

    static <T> void updateTag(Map<TagKey<T>, List<Holder<T>>> tags, Registry<T> registry,
            ResourceKey<? extends Registry<T>> registryKey, ResourceLocation id,
            Set<Holder<T>> configured, boolean enabled, String... unconditionalNames) {
        TagKey<T> key = TagKey.create(registryKey, id);
        Set<Holder<T>> values = new LinkedHashSet<>(tags.getOrDefault(key, List.of()));
        values.removeAll(configured);
        if (enabled) {
            values.addAll(configured);
        }
        for (String name : unconditionalNames) {
            ResourceKey<T> valueKey = ResourceKey.create(registryKey,
                    ResourceLocation.fromNamespaceAndPath(Mineralogy.MODID, name));
            registry.getHolder(valueKey).ifPresent(values::add);
        }
        tags.put(key, new ArrayList<>(values));
    }

    private static <T> int size(Registry<T> registry, ResourceKey<? extends Registry<T>> registryKey,
            ResourceLocation id) {
        return registry.getTag(TagKey.create(registryKey, id)).map(HolderSet.Named::size).orElse(0);
    }
}
