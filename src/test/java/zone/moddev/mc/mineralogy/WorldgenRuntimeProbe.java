package zone.moddev.mc.mineralogy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Test-only packaged-runtime worldgen probe. Never included in a release jar. */
@Mod(modid = WorldgenRuntimeProbe.MODID, name = "Mineralogy Worldgen Runtime Probe",
        version = "1", dependencies = "required-after:mineralogy;required-after:orespawn")
public final class WorldgenRuntimeProbe {
    static final String MODID = "mineralogyworldgenprobe";
    private static final Logger LOGGER = LogManager.getLogger(MODID);
    private static final String PHASE_PROPERTY = "mineralogy.runtimeWorldgenPhase";
    private static final String MARKER_NAME = "mineralogy-worldgen-probe.properties";
    private static final long REQUIRED_SEED = -4965128775892001975L;
    private static final int RADIUS = 4;
    private static final int FAMILY_CENTER_X = 0;
    private static final int FAMILY_CENTER_Z = 0;

    private static final Set<String> VOLCANIC = set("mineralogy:basalt", "mineralogy:rhyolite",
            "mineralogy:basaltic_glass", "mineralogy:scoria", "mineralogy:tuff", "mineralogy:pumice");
    private static final Set<String> INTRUSIVE = set("mineralogy:pegmatite", "mineralogy:diabase",
            "mineralogy:gabbro", "mineralogy:peridotite");
    private static final Set<String> SEDIMENTARY = set("mineralogy:shale", "mineralogy:conglomerate",
            "mineralogy:dolomite", "mineralogy:limestone", "mineralogy:siltstone",
            "mineralogy:rock_salt", "mineralogy:chert", "mineralogy:gypsum", "mineralogy:chalk");
    private static final Set<String> METAMORPHIC = set("mineralogy:marble", "mineralogy:slate",
            "mineralogy:schist", "mineralogy:gneiss", "mineralogy:phyllite",
            "mineralogy:amphibolite", "mineralogy:hornfels", "mineralogy:quartzite",
            "mineralogy:novaculite");
    private static final Set<String> ORES = set("mineralogy:sulfur_ore",
            "mineralogy:phosphorous_ore", "mineralogy:nitrate_ore");
    private boolean pending;

    public WorldgenRuntimeProbe() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        String phase = System.getProperty(PHASE_PROPERTY, "").trim();
        pending = "fresh".equals(phase) || "reload".equals(phase);
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (!pending || event.phase != TickEvent.Phase.END) {
            return;
        }
        pending = false;
        runProbe();
    }

    private void runProbe() {
        String phase = System.getProperty(PHASE_PROPERTY, "").trim();
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        try {
            WorldServer overworld = requireWorld(server, 0);
            require(overworld.getSeed() == REQUIRED_SEED, "preferred seed");
            Path marker = worldRoot(server).resolve(MARKER_NAME);
            Properties previous = "reload".equals(phase) ? read(marker) : null;
            require("reload".equals(phase) || !Files.exists(marker), "fresh marker isolation");

            int centerX;
            int centerZ;
            if (previous == null) {
                int[] center = findOceanRegion(overworld);
                centerX = center[0];
                centerZ = center[1];
            } else {
                centerX = Integer.parseInt(previous.getProperty("center_chunk_x"));
                centerZ = Integer.parseInt(previous.getProperty("center_chunk_z"));
            }

            long started = System.nanoTime();
            boolean fresh = previous == null;
            loadPopulationBorder(overworld, centerX, centerZ, RADIUS, fresh);
            Audit audit = auditOverworld(overworld, centerX, centerZ, RADIUS);
            loadPopulationBorder(overworld, FAMILY_CENTER_X, FAMILY_CENTER_Z, RADIUS, fresh);
            audit.merge(auditOverworld(overworld, FAMILY_CENTER_X, FAMILY_CENTER_Z, RADIUS));
            long elapsedMillis = (System.nanoTime() - started) / 1_000_000L;
            require(audit.volcanic > 0, "volcanic rock family");
            require(audit.intrusive > 0, "intrusive rock family");
            require(audit.sedimentary > 0, "sedimentary rock family");
            require(audit.metamorphic > 0, "metamorphic rock family");
            for (Map.Entry<String, Long> ore : audit.oreCounts.entrySet()) {
                require(ore.getValue() > 0, ore.getKey());
            }
            require(audit.oil > 0, "covered crude-oil deposit");
            auditNether(requireWorld(server, -1), fresh);
            Properties current = audit.properties(centerX, centerZ);

            if (previous == null) {
                write(marker, current);
            } else {
                for (String key : current.stringPropertyNames()) {
                    require(current.getProperty(key).equals(previous.getProperty(key)),
                            "reload changed " + key + " from " + previous.getProperty(key)
                                    + " to " + current.getProperty(key));
                }
                previous.setProperty("reload_verified", "true");
                write(marker, previous);
            }

            LOGGER.info("MINERALOGY_WORLDGEN_RUNTIME_PROBE_PASS phase={} chunks=162 oceanCenter={},{} "
                            + "families={}/{}/{}/{} ores={}/{}/{} oil={} coveredColumns={} "
                            + "oceanOil={} generationAuditMs={} netherMineralogy=0",
                    phase, centerX, centerZ, audit.volcanic, audit.intrusive,
                    audit.sedimentary, audit.metamorphic,
                    audit.oreCounts.get("mineralogy:sulfur_ore"),
                    audit.oreCounts.get("mineralogy:phosphorous_ore"),
                    audit.oreCounts.get("mineralogy:nitrate_ore"),
                    audit.oil, audit.coveredOilColumns, audit.oceanOil, elapsedMillis);
            server.initiateShutdown();
        } catch (Throwable failure) {
            LOGGER.error("MINERALOGY_WORLDGEN_RUNTIME_PROBE_FAIL", failure);
            server.initiateShutdown();
            throw failure;
        }
    }

    private static Audit auditOverworld(WorldServer world, int centerX, int centerZ, int radius) {
        Audit audit = new Audit();
        Map<Long, Integer> highestOil = new LinkedHashMap<>();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int chunkZ = centerZ - radius; chunkZ <= centerZ + radius; chunkZ++) {
            for (int chunkX = centerX - radius; chunkX <= centerX + radius; chunkX++) {
                Chunk chunk = world.getChunkProvider().provideChunk(chunkX, chunkZ);
                require(chunk.isTerrainPopulated(), "populated Overworld chunk " + chunkX + "," + chunkZ);
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        int x = (chunkX << 4) + localX;
                        int z = (chunkZ << 4) + localZ;
                        for (int y = 0; y < 256; y++) {
                            IBlockState state = chunk.getBlockState(cursor.setPos(x, y, z));
                            String id = id(state.getBlock());
                            int family = family(state, id);
                            if (family == 1) audit.volcanic++;
                            else if (family == 2) audit.intrusive++;
                            else if (family == 3) audit.sedimentary++;
                            else if (family == 4) audit.metamorphic++;
                            if (ORES.contains(id)) {
                                audit.oreCounts.put(id, audit.oreCounts.get(id) + 1L);
                            }
                            if ("mineralogy:crude_oil".equals(id)) {
                                audit.oil++;
                                Biome biome = world.getBiome(cursor);
                                require(BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN),
                                        "oil biome at " + cursor + " was " + biome.getRegistryName());
                                require(y <= 48, "oil height " + y + " at " + cursor);
                                audit.oceanOil++;
                                long column = (((long) x) << 32) ^ (z & 0xffffffffL);
                                Integer prior = highestOil.get(column);
                                if (prior == null || y > prior) highestOil.put(column, y);
                            }
                        }
                    }
                }
            }
        }
        for (Map.Entry<Long, Integer> entry : highestOil.entrySet()) {
            int x = (int) (entry.getKey() >> 32);
            int z = (int) (long) entry.getKey();
            int top = entry.getValue();
            for (int cover = 1; cover <= 2; cover++) {
                IBlockState state = world.getBlockState(cursor.setPos(x, top + cover, z));
                Material material = state.getMaterial();
                require(material.isSolid() && !material.isLiquid(),
                        "oil cover " + cover + " at " + cursor + " was " + id(state.getBlock()));
            }
            audit.coveredOilColumns++;
        }
        return audit;
    }

    private static void auditNether(WorldServer world, boolean fresh) {
        loadPopulationBorder(world, 80, -80, 1, fresh);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        long mineralogy = 0;
        for (int chunkZ = -81; chunkZ <= -79; chunkZ++) {
            for (int chunkX = 79; chunkX <= 81; chunkX++) {
                Chunk chunk = world.getChunkProvider().provideChunk(chunkX, chunkZ);
                for (int localZ = 0; localZ < 16; localZ++) {
                    for (int localX = 0; localX < 16; localX++) {
                        int x = (chunkX << 4) + localX;
                        int z = (chunkZ << 4) + localZ;
                        for (int y = 0; y < 256; y++) {
                            if ("mineralogy".equals(namespace(chunk.getBlockState(
                                    cursor.setPos(x, y, z)).getBlock()))) mineralogy++;
                        }
                    }
                }
            }
        }
        require(mineralogy == 0L, "Nether terrain replacement count " + mineralogy);
    }

    private static int[] findOceanRegion(WorldServer world) {
        int bestX = 0;
        int bestZ = 0;
        int bestOceans = -1;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int centerZ = -252; centerZ <= 252; centerZ += 9) {
            for (int centerX = -252; centerX <= 252; centerX += 9) {
                int oceans = 0;
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                        Biome biome = world.getBiomeProvider().getBiome(
                                cursor.setPos(((centerX + dx) << 4) + 8, 64,
                                        ((centerZ + dz) << 4) + 8));
                        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)) oceans++;
                    }
                }
                if (oceans > bestOceans) {
                    bestOceans = oceans;
                    bestX = centerX;
                    bestZ = centerZ;
                }
                if (oceans == 81) return new int[] { centerX, centerZ };
            }
        }
        require(bestOceans >= 60, "ocean-rich 81-chunk region; best was " + bestOceans);
        return new int[] { bestX, bestZ };
    }

    private static void loadPopulationBorder(WorldServer world, int centerX, int centerZ,
            int radius, boolean fresh) {
        ChunkProviderServer provider = world.getChunkProvider();
        if (fresh) {
            for (int chunkZ = centerZ - radius - 1; chunkZ <= centerZ + radius + 1; chunkZ++) {
                for (int chunkX = centerX - radius - 1; chunkX <= centerX + radius + 1; chunkX++) {
                    if (provider.getLoadedChunk(chunkX, chunkZ) == null) {
                        Chunk chunk = provider.chunkGenerator.generateChunk(chunkX, chunkZ);
                        provider.loadedChunks.put(ChunkPos.asLong(chunkX, chunkZ), chunk);
                        chunk.onLoad();
                    }
                }
            }
        }
        for (int chunkZ = centerZ - radius - 1; chunkZ <= centerZ + radius + 1; chunkZ++) {
            for (int chunkX = centerX - radius - 1; chunkX <= centerX + radius + 1; chunkX++) {
                Chunk chunk = provider.getLoadedChunk(chunkX, chunkZ);
                if (chunk == null) chunk = provider.provideChunk(chunkX, chunkZ);
                chunk.populate(provider, provider.chunkGenerator);
            }
        }
    }

    private static WorldServer requireWorld(MinecraftServer server, int dimension) {
        WorldServer world = server.getWorld(dimension);
        if (world == null) {
            DimensionManager.initDimension(dimension);
            world = DimensionManager.getWorld(dimension);
        }
        require(world != null, "dimension " + dimension);
        return world;
    }

    private static int family(IBlockState state, String id) {
        if (VOLCANIC.contains(id)) return 1;
        if (INTRUSIVE.contains(id)) return 2;
        if (SEDIMENTARY.contains(id) || "minecraft:sandstone".equals(id)) return 3;
        if (METAMORPHIC.contains(id)) return 4;
        if ("minecraft:stone".equals(id)) {
            int metadata = state.getBlock().getMetaFromState(state);
            if (metadata == 5) return 1;
            if (metadata == 1 || metadata == 3) return 2;
        }
        return 0;
    }

    private static String id(Block block) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        return id == null ? "" : id.toString();
    }

    private static String namespace(Block block) {
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        return id == null ? "" : id.getNamespace();
    }

    private static Path worldRoot(MinecraftServer server) {
        return server.getActiveAnvilConverter().getFile(server.getFolderName(), "level.dat")
                .toPath().toAbsolutePath().normalize().getParent();
    }

    private static Properties read(Path path) {
        require(Files.isRegularFile(path), "reload marker " + path);
        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
            return properties;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read " + path, exception);
        }
    }

    private static void write(Path path, Properties properties) {
        try {
            Files.createDirectories(path.getParent());
            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                for (String key : new java.util.TreeSet<>(properties.stringPropertyNames())) {
                    writer.write(key + "=" + properties.getProperty(key));
                    writer.newLine();
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not write " + path, exception);
        }
    }

    private static Set<String> set(String... values) {
        return Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(values)));
    }

    private static void require(boolean condition, String check) {
        if (!condition) throw new IllegalStateException("Mineralogy worldgen check failed: " + check);
    }

    private static final class Audit {
        long volcanic;
        long intrusive;
        long sedimentary;
        long metamorphic;
        long oil;
        long oceanOil;
        long coveredOilColumns;
        final Map<String, Long> oreCounts = new LinkedHashMap<>();

        Audit() {
            for (String ore : ORES) oreCounts.put(ore, 0L);
        }

        Properties properties(int centerX, int centerZ) {
            Properties properties = new Properties();
            properties.setProperty("seed", Long.toString(REQUIRED_SEED));
            properties.setProperty("center_chunk_x", Integer.toString(centerX));
            properties.setProperty("center_chunk_z", Integer.toString(centerZ));
            properties.setProperty("family_center_chunk_x", Integer.toString(FAMILY_CENTER_X));
            properties.setProperty("family_center_chunk_z", Integer.toString(FAMILY_CENTER_Z));
            properties.setProperty("chunks", "162");
            properties.setProperty("family_volcanic", Long.toString(volcanic));
            properties.setProperty("family_intrusive", Long.toString(intrusive));
            properties.setProperty("family_sedimentary", Long.toString(sedimentary));
            properties.setProperty("family_metamorphic", Long.toString(metamorphic));
            properties.setProperty("ore_sulfur", Long.toString(oreCounts.get("mineralogy:sulfur_ore")));
            properties.setProperty("ore_phosphorous",
                    Long.toString(oreCounts.get("mineralogy:phosphorous_ore")));
            properties.setProperty("ore_nitrate", Long.toString(oreCounts.get("mineralogy:nitrate_ore")));
            properties.setProperty("oil", Long.toString(oil));
            properties.setProperty("ocean_oil", Long.toString(oceanOil));
            properties.setProperty("covered_oil_columns", Long.toString(coveredOilColumns));
            return properties;
        }

        void merge(Audit other) {
            volcanic += other.volcanic;
            intrusive += other.intrusive;
            sedimentary += other.sedimentary;
            metamorphic += other.metamorphic;
            oil += other.oil;
            oceanOil += other.oceanOil;
            coveredOilColumns += other.coveredOilColumns;
            for (Map.Entry<String, Long> ore : other.oreCounts.entrySet()) {
                oreCounts.put(ore.getKey(), oreCounts.get(ore.getKey()) + ore.getValue());
            }
        }
    }
}
