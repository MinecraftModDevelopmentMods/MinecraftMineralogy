package zone.moddev.mc.mineralogy.compatprobe;

import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/** Disposable flat fixture, without the old server's JSON registry-ops path. */
final class ProbeWorldType {
    private static final DeferredRegister<ForgeWorldType> TYPES =
            DeferredRegister.create(ForgeRegistries.WORLD_TYPES, "mineralogycompatprobe");
    static {
        TYPES.register("probe", () -> new ForgeWorldType((ForgeWorldType.IBasicChunkGeneratorFactory)
                (biomes, settings, seed) -> new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(biomes))));
    }
    static void register() {
        TYPES.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
    }
}
