package zone.moddev.mc.mineralogy.compat;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/** Internal bridge for the two narrowly scoped dripstone bytecode hooks. */
public final class VanillaTerrainHooks {
    private static final TagKey<Block> DRIPSTONE_BASE_STONES = TagKey.create(
            Registry.BLOCK_REGISTRY, new ResourceLocation("mineralogy", "dripstone_base_stones"));

    private VanillaTerrainHooks() { }

    /** Reuses one tag identity; no allocation or configuration work in feature loops. */
    public static TagKey<Block> dripstoneBaseStones() {
        return DRIPSTONE_BASE_STONES;
    }
}
