package zone.moddev.mc.mineralogy.compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Internal bridges for three native-control-proven terrain consumers. */
public final class VanillaTerrainHooks {
    private static final TagKey<Block> DRIPSTONE_BASE_STONES = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("mineralogy", "dripstone_base_stones"));
    private static final TagKey<Block> FOREST_ROCK_SUBSTRATES = TagKey.create(
            Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("mineralogy", "forest_rock_substrates"));

    private VanillaTerrainHooks() { }

    /** Reuses one tag identity; no allocation or configuration work in feature loops. */
    public static TagKey<Block> dripstoneBaseStones() {
        return DRIPSTONE_BASE_STONES;
    }

    /** Preserve this target's native Feature.isStone check, adding only natural terrain. */
    public static boolean forestRockSubstrate(BlockState state) {
        return state.is(BlockTags.BASE_STONE_OVERWORLD) || state.is(FOREST_ROCK_SUBSTRATES);
    }
}
