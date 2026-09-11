package zone.moddev.mc.mineralogy.mixin;

import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import zone.moddev.mc.mineralogy.compat.CobblestoneTagPolicy;

/**
 * Rebind the affected named sets immediately after Minecraft 26.2 commits
 * pending tags and static components, which also covers every later /reload.
 */
@Mixin(ReloadableServerResources.class)
public abstract class ReloadableServerResourcesMixin {
    @Inject(method = "updateComponentsAndStaticRegistryTags", at = @At("TAIL"))
    private void mineralogy$rebindCobblestoneTags(CallbackInfo callback) {
        ReloadableServerResources resources = (ReloadableServerResources) (Object) this;
        CobblestoneTagPolicy.apply(resources.fullRegistries().lookup());
    }
}
