package zone.moddev.mc.mineralogy.compat;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Field;

import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.Bootstrap;

import org.junit.BeforeClass;
import org.junit.Test;

public class CobblestoneTagPolicyTest {
    @BeforeClass
    public static void initializeRegistries() {
        SharedConstants.tryDetectVersion();
        try {
            Field field = Bootstrap.class.getDeclaredField("isBootstrapped");
            field.setAccessible(true);
            if (!field.getBoolean(null)) {
                field.setBoolean(null, true);
                BuiltInRegistries.bootStrap();
            }
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Unable to initialize the isolated tag registry", exception);
        }
    }

    @Test
    public void rebuiltTagPreservesVanillaAndAddsOrRemovesConfiguredElements() {
        Holder<Item> vanilla = holder(Blocks.COBBLESTONE.asItem());
        Holder<Item> mineralogyStandIn = holder(Blocks.BASALT.asItem());
        TagKey<Item> cobblestone = TagKey.create(Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath("forge", "cobblestone"));
        Map<TagKey<Item>, List<Holder<Item>>> tags = new IdentityHashMap<>();
        tags.put(cobblestone, list(vanilla));

        CobblestoneTagPolicy.updateTag(tags, BuiltInRegistries.ITEM, Registries.ITEM,
                cobblestone.location(), set(mineralogyStandIn), true);
        assertEquals(list(vanilla, mineralogyStandIn), tags.get(cobblestone));

        CobblestoneTagPolicy.updateTag(tags, BuiltInRegistries.ITEM, Registries.ITEM,
                cobblestone.location(), set(mineralogyStandIn), false);
        assertEquals(list(vanilla), tags.get(cobblestone));
    }

    private static Holder<Item> holder(Item item) {
        return BuiltInRegistries.ITEM.getHolderOrThrow(
                BuiltInRegistries.ITEM.getResourceKey(item).get());
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
