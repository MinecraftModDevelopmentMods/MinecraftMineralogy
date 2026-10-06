package zone.moddev.mc.mineralogy.compatprobe;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/** Isolated native Forge 40 presets; Minecraft 1.18 has no world-preset codec. */
final class CaveSamplePresets {
    private static final DeferredRegister<ForgeWorldPreset> PRESETS =
            DeferredRegister.create(ForgeRegistries.Keys.WORLD_TYPES, "mineralogycompatprobe");

    static {
        PRESETS.register("probe", () -> new ForgeWorldPreset((ForgeWorldPreset.IChunkGeneratorFactory)
                (access, seed, settings) -> new net.minecraft.world.level.levelgen.FlatLevelSource(
                        access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY),
                        net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings.getDefault(
                                access.registryOrThrow(Registry.BIOME_REGISTRY),
                                access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY)))));
        for (String name : new String[]{"dripstone", "lush"}) {
            PRESETS.register(name, () -> new ForgeWorldPreset((ForgeWorldPreset.IChunkGeneratorFactory)
                    (access, seed, settings) -> new NoiseBasedChunkGenerator(
                            access.registryOrThrow(Registry.STRUCTURE_SET_REGISTRY),
                            access.registryOrThrow(Registry.NOISE_REGISTRY),
                            new FixedBiomeSource(access.registryOrThrow(Registry.BIOME_REGISTRY)
                                    .getHolderOrThrow(ResourceKey.create(Registry.BIOME_REGISTRY,
                                            new ResourceLocation("minecraft", name + "_caves")))),
                            seed, access.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY)
                                    .getHolderOrThrow(NoiseGeneratorSettings.OVERWORLD))));
        }
    }

    static void register() {
        PRESETS.register(net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext.get().getModEventBus());
    }
}
