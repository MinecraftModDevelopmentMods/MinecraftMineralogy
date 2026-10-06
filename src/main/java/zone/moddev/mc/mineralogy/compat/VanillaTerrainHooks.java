package zone.moddev.mc.mineralogy.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;

/** Internal bridge for the two narrowly scoped dripstone bytecode hooks. */
public final class VanillaTerrainHooks {
    private static final Tag.Named<Block> DRIPSTONE_BASE_STONES = BlockTags.createOptional(
            new ResourceLocation("mineralogy", "dripstone_base_stones"));

    private VanillaTerrainHooks() { }

    /** Retains one reload-aware tag; no allocations inside feature loops. */
    public static Tag<Block> dripstoneBaseStones() {
        return DRIPSTONE_BASE_STONES;
    }
}
