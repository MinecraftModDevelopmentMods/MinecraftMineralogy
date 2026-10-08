package zone.moddev.mc.mineralogy.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.BlockBlobFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Extend only the forest-rock substrate check, not global stone eligibility. */
@Mixin(BlockBlobFeature.class)
public abstract class ForestRockSubstrateMixin extends Feature<BlockStateConfiguration> {
    @Unique
    private static final TagKey<Block> mineralogy$forestRockSubstrates = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath("mineralogy", "forest_rock_substrates"));

    protected ForestRockSubstrateMixin(Codec<BlockStateConfiguration> codec) {
        super(codec);
    }

    @Redirect(method = "place", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/levelgen/feature/BlockBlobFeature;isStone(Lnet/minecraft/world/level/block/state/BlockState;)Z"),
            require = 1, expect = 1, allow = 1)
    private static boolean mineralogy$forestRockSubstrate(BlockState state) {
        return Feature.isStone(state) || state.is(mineralogy$forestRockSubstrates);
    }
}
