package zone.moddev.mc.mineralogy.compat;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;

import org.junit.Test;

public class CobblestoneTagPolicyTest {
    @Test
    public void rebuiltTagPreservesVanillaAndAddsOrRemovesConfiguredElements() {
        Holder<Item> vanilla = holder(Blocks.COBBLESTONE.asItem());
        Holder<Item> mineralogyStandIn = holder(Blocks.BASALT.asItem());
        TagKey<Item> cobblestone = TagKey.create(Registry.ITEM_REGISTRY,
                new ResourceLocation("forge", "cobblestone"));
        Map<TagKey<Item>, List<Holder<Item>>> tags = new IdentityHashMap<>();
        tags.put(cobblestone, list(vanilla));

        CobblestoneTagPolicy.updateTag(tags, Registry.ITEM, Registry.ITEM_REGISTRY,
                cobblestone.location(), set(mineralogyStandIn), true);
        assertEquals(list(vanilla, mineralogyStandIn), tags.get(cobblestone));

        CobblestoneTagPolicy.updateTag(tags, Registry.ITEM, Registry.ITEM_REGISTRY,
                cobblestone.location(), set(mineralogyStandIn), false);
        assertEquals(list(vanilla), tags.get(cobblestone));
    }

    private static Holder<Item> holder(Item item) {
        return Registry.ITEM.getHolderOrThrow(Registry.ITEM.getResourceKey(item).get());
    }

    @SafeVarargs
    private static <T> Set<T> set(T... values) {
        Set<T> result = new LinkedHashSet<>();
        for (T value : values) result.add(value);
        return result;
    }

    @SafeVarargs
    private static <T> List<T> list(T... values) {
        List<T> result = new ArrayList<>();
        for (T value : values) result.add(value);
        return result;
    }
}
