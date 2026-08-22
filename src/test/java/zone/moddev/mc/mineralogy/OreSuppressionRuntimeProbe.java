package zone.moddev.mc.mineralogy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Test-classpath-only ore-cancellation acceptance probe. Never packaged in production. */
@Mod(modid = OreSuppressionRuntimeProbe.MODID, name = "Mineralogy Ore Suppression Runtime Probe",
        version = "1", dependencies = "required-after:mineralogy")
public final class OreSuppressionRuntimeProbe {
    static final String MODID = "mineralogyoresuppressionprobe";
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    private static final Set<Block> VANILLA_ORES = new HashSet<Block>(Arrays.asList(
            Blocks.COAL_ORE, Blocks.IRON_ORE, Blocks.GOLD_ORE, Blocks.REDSTONE_ORE,
            Blocks.LIT_REDSTONE_ORE, Blocks.LAPIS_ORE, Blocks.DIAMOND_ORE, Blocks.EMERALD_ORE));

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (Boolean.getBoolean("mineralogy.runtimeOreSuppressionProbe")) {
            MinecraftForge.ORE_GEN_BUS.register(this);
        }
    }

    @SubscribeEvent
    public void suppressVanillaOre(OreGenEvent.GenerateMinable event) {
        event.setResult(Event.Result.DENY);
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        if (!Boolean.getBoolean("mineralogy.runtimeOreSuppressionProbe")) {
            return;
        }

        try {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            WorldServer world = server.getWorld(0);
            long vanillaOreBlocks = 0;
            long mineralogyBlocks = 0;
            int chunks = 0;
            ChunkProviderServer provider = world.getChunkProvider();

            for (int chunkX = 95; chunkX < 106; ++chunkX) {
                for (int chunkZ = -105; chunkZ < -94; ++chunkZ) {
                    provider.provideChunk(chunkX, chunkZ);
                }
            }
            for (int chunkX = 95; chunkX < 106; ++chunkX) {
                for (int chunkZ = -105; chunkZ < -94; ++chunkZ) {
                    Chunk chunk = provider.provideChunk(chunkX, chunkZ);
                    chunk.populate(provider, provider.chunkGenerator);
                }
            }

            BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
            for (int chunkX = 96; chunkX < 105; ++chunkX) {
                for (int chunkZ = -104; chunkZ < -95; ++chunkZ) {
                    Chunk chunk = provider.provideChunk(chunkX, chunkZ);
                    require(chunk.isTerrainPopulated(),
                            "populated chunk " + chunkX + "," + chunkZ);
                    ++chunks;
                    for (int localX = 0; localX < 16; ++localX) {
                        for (int localZ = 0; localZ < 16; ++localZ) {
                            int x = (chunkX << 4) + localX;
                            int z = (chunkZ << 4) + localZ;
                            for (int y = 0; y < 128; ++y) {
                                Block block = chunk.getBlockState(cursor.setPos(x, y, z)).getBlock();
                                if (VANILLA_ORES.contains(block)) {
                                    ++vanillaOreBlocks;
                                }
                                ResourceLocation name = block.getRegistryName();
                                if (name != null && "mineralogy".equals(name.getNamespace())) {
                                    ++mineralogyBlocks;
                                }
                            }
                        }
                    }
                }
            }

            require(chunks == 81, "81 generated chunks");
            require(vanillaOreBlocks == 0, "cancelled vanilla ores remain absent");
            require(mineralogyBlocks > 0, "Mineralogy provider remains active");
            LOGGER.info("MINERALOGY_ORE_SUPPRESSION_RUNTIME_PROBE_PASS chunks={} "
                            + "vanillaOreBlocks={} mineralogyBlocks={} restoredVanillaOre=false",
                    chunks, vanillaOreBlocks, mineralogyBlocks);
            server.initiateShutdown();
        } catch (Throwable failure) {
            LOGGER.error("MINERALOGY_ORE_SUPPRESSION_RUNTIME_PROBE_FAIL", failure);
            throw failure;
        }
    }

    private static void require(boolean condition, String check) {
        if (!condition) {
            throw new IllegalStateException("Ore suppression runtime check failed: " + check);
        }
    }
}
