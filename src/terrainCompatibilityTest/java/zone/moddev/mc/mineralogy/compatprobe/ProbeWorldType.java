package zone.moddev.mc.mineralogy.compatprobe;

import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;

/** Deterministic non-shipping flat world, without parsing incomplete CLI JSON. */
final class ProbeWorldType {
    private static final DeferredRegister<ForgeWorldType> TYPES =
            DeferredRegister.create(ForgeRegistries.WORLD_TYPES,"mineralogycompatprobe");
    static void register() {
        TYPES.register("probe",() -> new ForgeWorldType((biomes,settings,seed,json) ->
                new FlatChunkGenerator(FlatGenerationSettings.func_242869_a(biomes))));
        TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
