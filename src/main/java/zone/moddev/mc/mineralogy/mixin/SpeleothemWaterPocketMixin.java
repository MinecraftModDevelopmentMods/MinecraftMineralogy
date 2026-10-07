package zone.moddev.mc.mineralogy.mixin;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Only the dripstone pool's containment check needs the extended stone set. */
@Mixin(targets = "net.minecraft.world.level.levelgen.feature.SpeleothemClusterFeature")
public abstract class SpeleothemWaterPocketMixin {
    @Redirect(method = "canBeAdjacentToWater", at = @At(value = "FIELD",
            target = "Lnet/minecraft/tags/BlockTags;BASE_STONE_OVERWORLD:Lnet/minecraft/tags/TagKey;"), require = 1, expect = 1, allow = 1)
    private TagKey<Block> mineralogy$poolSubstrates() {
        return TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("mineralogy", "dripstone_base_stones"));
    }
}
