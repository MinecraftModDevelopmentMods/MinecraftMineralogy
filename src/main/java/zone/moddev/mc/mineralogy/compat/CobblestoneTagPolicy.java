package zone.moddev.mc.mineralogy.compat;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.TagsUpdatedEvent;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.data.Material;
import zone.moddev.mc.mineralogy.data.MaterialData;

/** Applies the legacy cobblestone option to Forge 65 canonical and compatibility tags. */
public final class CobblestoneTagPolicy {
    private static final Identifier COBBLESTONES = Identifier.fromNamespaceAndPath("c", "cobblestones");
    private static final Identifier LEGACY_COBBLESTONE =
            Identifier.fromNamespaceAndPath("forge", "cobblestone");
    private static final Identifier STONE_CRAFTING_MATERIALS =
            Identifier.fromNamespaceAndPath("minecraft", "stone_crafting_materials");
    private static final Identifier STONE_TOOL_MATERIALS =
            Identifier.fromNamespaceAndPath("minecraft", "stone_tool_materials");

    private CobblestoneTagPolicy() {
    }

    /** Reapply on clients after receiving the server's tag packet. */
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.shouldUpdateStaticData()) {
            apply(event.getRegistryAccess());
        }
    }

    /** Rebind the already-loaded named sets on the server or client. */
    public static void apply(HolderLookup.Provider access) {
        HolderLookup.RegistryLookup<Block> blockRegistry = access.lookupOrThrow(Registries.BLOCK);
        HolderLookup.RegistryLookup<Item> itemRegistry = access.lookupOrThrow(Registries.ITEM);
        Set<Holder<Block>> configuredBlocks = rawRockHolders(blockRegistry, Registries.BLOCK);
        Set<Holder<Item>> configuredItems = rawRockHolders(itemRegistry, Registries.ITEM);
        boolean enabled = MineralogyConfig.makeRockCobblestoneEquivilent();

        updateTag(blockRegistry, Registries.BLOCK, COBBLESTONES,
                configuredBlocks, enabled, "chert", "pumice");
        updateTag(blockRegistry, Registries.BLOCK, LEGACY_COBBLESTONE,
                configuredBlocks, enabled, "chert", "pumice");

        updateTag(itemRegistry, Registries.ITEM, COBBLESTONES,
                configuredItems, enabled, "chert", "pumice");
        updateTag(itemRegistry, Registries.ITEM, LEGACY_COBBLESTONE,
                configuredItems, enabled, "chert", "pumice");
        updateTag(itemRegistry, Registries.ITEM, STONE_CRAFTING_MATERIALS,
                configuredItems, enabled, "chert", "pumice");
        updateTag(itemRegistry, Registries.ITEM, STONE_TOOL_MATERIALS,
                configuredItems, enabled, "chert", "pumice");

        Mineralogy.LOGGER.debug("Applied Forge 65 cobblestone policy: enabled={}, rocks={}, "
                + "canonicalItems={}, legacyItems={}, craftingItems={}, toolItems={}",
                enabled, configuredItems.size(),
                size(itemRegistry, Registries.ITEM, COBBLESTONES),
                size(itemRegistry, Registries.ITEM, LEGACY_COBBLESTONE),
                size(itemRegistry, Registries.ITEM, STONE_CRAFTING_MATERIALS),
                size(itemRegistry, Registries.ITEM, STONE_TOOL_MATERIALS));
    }

    private static <T> List<Holder<T>> holders(HolderSet.Named<T> values) {
        List<Holder<T>> result = new ArrayList<>();
        values.forEach(result::add);
        return result;
    }

    private static <T> Set<Holder<T>> rawRockHolders(HolderLookup.RegistryLookup<T> registry,
            ResourceKey<? extends Registry<T>> registryKey) {
        Set<Holder<T>> values = new LinkedHashSet<>();
        for (Material material : MaterialData.allIncludingRockSalt()) {
            TagKey<T> tag = TagKey.create(registryKey,
                    Identifier.fromNamespaceAndPath(Mineralogy.MODID, "stones/" + material.id()));
            registry.get(tag).ifPresent(named -> named.forEach(values::add));
        }
        return values;
    }

    static <T> void updateTag(HolderLookup.RegistryLookup<T> registry,
            ResourceKey<? extends Registry<T>> registryKey, Identifier id,
            Set<Holder<T>> configured, boolean enabled, String... unconditionalNames) {
        TagKey<T> key = TagKey.create(registryKey, id);
        HolderSet.Named<T> tag = registry.get(key).orElse(null);
        if (tag == null) {
            Mineralogy.LOGGER.warn("Cannot update missing compatibility tag {}", key.location());
            return;
        }
        Set<Holder<T>> unconditional = new LinkedHashSet<>();
        for (String name : unconditionalNames) {
            ResourceKey<T> valueKey = ResourceKey.create(registryKey,
                    Identifier.fromNamespaceAndPath(Mineralogy.MODID, name));
            registry.get(valueKey).ifPresent(unconditional::add);
        }
        List<Holder<T>> previous = holders(tag);
        List<Holder<T>> replacement = rebuildValues(previous, configured, enabled, unconditional);
        tag.bind(replacement);
        rebindHolderMembership(key, previous, configured, unconditional, replacement);
    }

    /**
     * HolderSet.Named owns the iterable tag contents, while Holder.Reference owns
     * the membership queried by ItemStack.is(TagKey). Forge's normal tag loader
     * refreshes both views. A post-load compatibility rebind must do the same or
     * recipe ingredients keep rejecting holders that appear in the named set.
     */
    private static <T> void rebindHolderMembership(TagKey<T> key,
            List<Holder<T>> previous, Set<Holder<T>> configured,
            Set<Holder<T>> unconditional, List<Holder<T>> replacement) {
        Set<Holder<T>> affected = new LinkedHashSet<>(previous);
        affected.addAll(configured);
        affected.addAll(unconditional);
        Set<Holder<T>> included = new LinkedHashSet<>(replacement);

        for (Holder<T> holder : affected) {
            if (!(holder instanceof Holder.Reference<T> reference)) {
                continue;
            }
            Set<TagKey<T>> tags = reference.tags()
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
            if (included.contains(holder)) {
                tags.add(key);
            } else {
                tags.remove(key);
            }
            reference.bindTags(tags);
        }
    }

    static <T> List<T> rebuildValues(List<T> existing, Set<T> configured,
            boolean enabled, Set<T> unconditional) {
        Set<T> values = new LinkedHashSet<>(existing);
        values.removeAll(configured);
        if (enabled) {
            values.addAll(configured);
        }
        values.addAll(unconditional);
        return new ArrayList<>(values);
    }

    private static <T> int size(HolderLookup.RegistryLookup<T> registry,
            ResourceKey<? extends Registry<T>> registryKey,
            Identifier id) {
        return registry.get(TagKey.create(registryKey, id)).map(HolderSet.Named::size).orElse(0);
    }
}
