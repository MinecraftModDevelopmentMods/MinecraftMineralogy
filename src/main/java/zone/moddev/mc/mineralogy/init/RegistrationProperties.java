package zone.moddev.mc.mineralogy.init;

import zone.moddev.mc.mineralogy.Mineralogy;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Applies the stable registry identity before Minecraft 26.2 constructs an
 * item or block. The target derives description and loot identities during
 * construction, so assigning the registry entry afterward is too late.
 */
public final class RegistrationProperties {
    public static BlockBehaviour.Properties block(BlockBehaviour.Properties properties, String path) {
        return properties.setId(ResourceKey.create(Registries.BLOCK, id(path)));
    }

    public static Item.Properties item(Item.Properties properties, String path) {
        return properties.setId(ResourceKey.create(Registries.ITEM, id(path)));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(Mineralogy.MODID, path);
    }

    private RegistrationProperties() {
        throw new IllegalAccessError("Not an instantiable class");
    }
}
