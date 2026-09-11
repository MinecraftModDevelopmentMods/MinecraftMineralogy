package zone.moddev.mc.mineralogy.mixin;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Minecraft 26.2 throws when a block tag is queried before the first tag
 * load. OreSpawn 4.0.16 can make that query during common setup when an older
 * global profile contains tag-backed ore hosts. Defer those hosts as an empty
 * early result; OreSpawn rebakes the same unchanged profile after the world's
 * tags have loaded.
 */
@Mixin(targets = "zone.moddev.mc.orespawn.worldgen.OreSpawnOreGeneration", remap = false)
public abstract class OreSpawnEarlyTagCompatibilityMixin {
    @Inject(method = "resolveTag", at = @At("HEAD"), cancellable = true, remap = false)
    private static void mineralogy$deferUnboundOreHostTag(TagKey<Block> tag,
            CallbackInfoReturnable<Set<Block>> callback) {
        Set<Block> resolved = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Block block : BuiltInRegistries.BLOCK) {
            try {
                if (block.defaultBlockState().is(tag)) {
                    resolved.add(block);
                }
            } catch (IllegalStateException unboundTags) {
                callback.setReturnValue(Collections.emptySet());
                return;
            }
        }
        callback.setReturnValue(resolved);
    }
}
