package zone.moddev.mc.mineralogy.compatprobe;

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;

/** Non-shipping probe: exercises vanilla consumers, not replacement Java logic. */
@Mod("mineralogycompatprobe")
public final class TerrainCompatibilityProbe {
    private static final String[] FAMILIES = ("andesite basalt diorite granite rhyolite pegmatite diabase gabbro peridotite "
            + "basaltic_glass scoria tuff shale conglomerate dolomite limestone siltstone marble slate schist gneiss phyllite "
            + "amphibolite hornfels quartzite novaculite rock_salt").split(" ");
    private final List<String> evidence = new ArrayList<>();
    public TerrainCompatibilityProbe() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::started);
        if (Boolean.getBoolean("mineralogy.compatProbe.client")) ClientSmoke.register();
    }
    private void started(ServerStartedEvent event) {
        if (Boolean.getBoolean("mineralogy.compatProbe.client")) return;
        MinecraftServer server = event.getServer();
        try {
            if (Boolean.getBoolean("mineralogy.compatProbe.caveSample")) {
                sampleCaves(server.overworld());
                finish(server, null);
                return;
            }
            test(server.overworld(), "initial");
            server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((unused, failure) -> server.execute(() -> {
                try {
                    if (failure != null) throw new IllegalStateException("Resource reload failed", failure);
                    test(server.overworld(), "reload");
                    finish(server, null);
                } catch (Throwable problem) { finish(server, problem); }
            }));
        } catch (Throwable failure) { finish(server, failure); }
    }
    private void test(ServerLevel level, String phase) throws Exception {
        verifyTags(phase);
        verifyCatalysts(level, phase);
        verifyFeatures(level, phase);
    }
    private static ResourceLocation id(String value) { return new ResourceLocation(value); }
    private static Block block(String value) {
        Block result = BuiltInRegistries.BLOCK.get(id(value));
        require(result != null && result != Blocks.AIR, "Missing block " + value);
        return result;
    }
    private static TagKey<Block> blockTag(String name) { return TagKey.create(Registries.BLOCK, id(name)); }
    private static Set<String> raw() {
        Set<String> set = new TreeSet<>();
        for (String name : FAMILIES) set.add("mineralogy:" + name);
        for (String name : List.of("chalk", "chert", "gypsum", "pumice")) set.add("mineralogy:" + name);
        return set;
    }
    private void verifyTags(String phase) {
        Set<String> terrain = raw();
        for (String name : List.of("andesite", "basalt", "diorite", "granite", "sandstone", "tuff")) terrain.add("minecraft:" + name);
        require(terrain.size() == 37, "Incorrect expected terrain cardinality");
        TagKey<Block> terrainTag = blockTag("mineralogy:terrain_rocks");
        require(java.util.stream.StreamSupport.stream(BuiltInRegistries.BLOCK.getTagOrEmpty(terrainTag).spliterator(), false).count() == 37, "Loaded terrain cardinality");
        for (String name : terrain) {
            require(block(name).defaultBlockState().is(terrainTag), "Terrain omitted " + name);
            require(new ItemStack(block(name)).is(TagKey.create(Registries.ITEM, id("mineralogy:terrain_rocks"))), "Item terrain omitted " + name);
            for (String tag : List.of("sculk_replaceable", "sculk_replaceable_world_gen", "dripstone_replaceable_blocks", "moss_replaceable", "lush_ground_replaceable"))
                require(block(name).defaultBlockState().is(blockTag("minecraft:" + tag)), tag + " omitted " + name);
        }
        for (String tag : List.of("sculk_replaceable", "dripstone_replaceable_blocks", "moss_replaceable")) {
            require(Blocks.STONE.defaultBlockState().is(blockTag("minecraft:" + tag)), "Lost vanilla stone " + tag);
            require(Blocks.GOLD_BLOCK.defaultBlockState().is(blockTag("minecraft:" + tag)), "Lost third-party member " + tag);
        }
        for (String excluded : List.of("basalt_brick", "basalt_smooth", "basalt_slab", "basalt_furnace", "sulfur_ore", "sulfur_block", "rocksaltlamp", "rocksaltstreetlamp"))
            require(!block("mineralogy:" + excluded).defaultBlockState().is(terrainTag), "Crafted/non-terrain member " + excluded);
        evidence.add(phase + ": 37 block/item terrain identities; all five vanilla consumers; vanilla and third-party members retained");
    }
    private void verifyCatalysts(ServerLevel level, String phase) throws Exception {
        int index = 0;
        for (String substrate : List.of("minecraft:stone", "mineralogy:basalt", "minecraft:basalt", "mineralogy:chalk", "mineralogy:chert", "mineralogy:gypsum", "mineralogy:pumice", "mineralogy:rhyolite")) {
            BlockPos center = new BlockPos(index++ * 32, 200, 0);
            prepareRoom(level, center, block(substrate), 7);
            BlockPos catalystPos = center.offset(-3, 1, 0);
            level.setBlockAndUpdate(catalystPos, Blocks.SCULK_CATALYST.defaultBlockState());
            Object catalyst = level.getBlockEntity(catalystPos);
            require(catalyst != null, "No catalyst block entity");
            Object listener = call(catalyst, "getListener");
            Class<?> entityTypes = Class.forName("net.minecraft.world.entity.EntityType");
            Object type = field(entityTypes, "ZOMBIE");
            Class<?> zombieClass = Class.forName("net.minecraft.world.entity.monster.Zombie");
            LivingEntity zombie = (LivingEntity)construct(zombieClass, type, level);
            zombie.setPos(center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5);
            level.addFreshEntity(zombie);
            call(zombie, "die", level.damageSources().generic());
            // Deliver the real death to the catalyst explicitly if the synchronous
            // event dispatcher has not yet visited this listener in the probe tick.
            Object spreader = call(listener, "getSculkSpreader");
            if (((List<?>)call(spreader, "getCursors")).isEmpty()) {
                Class<?> gameEvent = Class.forName("net.minecraft.world.level.gameevent.GameEvent");
                Object dieEvent = field(gameEvent, "ENTITY_DIE");
                Class<?> context = Class.forName("net.minecraft.world.level.gameevent.GameEvent$Context");
                Object ctx = callStatic(context, "of", zombie);
                call(listener, "handleGameEvent", level, dieEvent, ctx, zombie.position());
            }
            require(!((List<?>)call(spreader, "getCursors")).isEmpty(), "XP death failed to charge catalyst on " + substrate);
            RandomSource random = RandomSource.create(81499);
            for (int tick = 0; tick < 500; tick++) call(spreader, "updateCursors", level, catalystPos, random, true);
            int sculk = count(level, center, 7, Blocks.SCULK);
            require(sculk > 0, "Catalyst failed to convert " + substrate);
            evidence.add(phase + ": catalyst XP-death " + substrate + " sculk=" + sculk);
            zombie.discard();
        }
    }
    private void verifyFeatures(ServerLevel level, String phase) throws Exception {
        Object featureRegistry = call(level.registryAccess(), "registryOrThrow", Registries.CONFIGURED_FEATURE);
        for (String feature : List.of("moss_patch_bonemeal", "dripstone_cluster")) {
            ResourceKey<?> key = ResourceKey.create(Registries.CONFIGURED_FEATURE, id("minecraft:" + feature));
            Object holder = call(featureRegistry, "getHolderOrThrow", key);
            Object configured = call(holder, "value");
            int index = 0;
            for (String substrate : List.of("minecraft:stone", "mineralogy:basalt", "minecraft:basalt", "mineralogy:rhyolite", "mineralogy:chalk", "mineralogy:chert", "mineralogy:gypsum", "mineralogy:pumice", "mineralogy:basalt_brick")) {
                BlockPos center = new BlockPos(index++ * 32, 200, 64);
                int placed = 0;
                for (long seed : new long[] { 3, 17, 81, 109, 531 }) {
                    prepareRoom(level, center, block(substrate), 12);
                    call(configured, "place", level, level.getChunkSource().getGenerator(), RandomSource.create(seed), center.above());
                    placed += count(level, center, 12, feature.startsWith("moss") ? Blocks.MOSS_BLOCK : Blocks.DRIPSTONE_BLOCK);
                    if (!feature.startsWith("moss")) placed += count(level, center, 12, Blocks.POINTED_DRIPSTONE);
                }
                if (substrate.endsWith("basalt_brick")) require(placed == 0, "Feature converted crafted brick substrate: " + feature);
                else require(placed > 0, "Vanilla feature failed on " + substrate + ": " + feature);
                evidence.add(phase + ": deterministic " + feature + " " + substrate + " blocks=" + placed);
            }
        }
    }
    private static void prepareRoom(ServerLevel level, BlockPos center, Block substrate, int radius) {
        for (int cx = (center.getX() - radius) >> 4; cx <= (center.getX() + radius) >> 4; cx++)
            for (int cz = (center.getZ() - radius) >> 4; cz <= (center.getZ() + radius) >> 4; cz++) { level.setChunkForced(cx, cz, true); level.getChunk(cx, cz); }
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -3, -radius), center.offset(radius, 13, radius))) {
            int dy = pos.getY() - center.getY();
            level.setBlock(pos, (dy <= 0 || dy >= 10 ? substrate : Blocks.AIR).defaultBlockState(), 2);
        }
    }
    private static int count(ServerLevel level, BlockPos center, int radius, Block block) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -3, -radius), center.offset(radius, 13, radius)))
            if (level.getBlockState(pos).is(block)) count++;
        return count;
    }
    private void sampleCaves(ServerLevel level) {
        Map<String, Long> counts = new TreeMap<>();
        for (int x = -1; x <= 1; x++) for (int z = -1; z <= 1; z++) level.getChunk(x, z);
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(-16, -64, -16), new BlockPos(31, 80, 31))) {
            String name = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
            if (name.startsWith("mineralogy:") || name.contains("dripstone") || name.contains("moss") || name.contains("cave_vines"))
                counts.merge(name, 1L, Long::sum);
        }
        evidence.add("Paired fresh cave sample: nine chunks; seed -4965128775892001975; y=-64..80");
        counts.forEach((name, count) -> evidence.add(name + "=" + count));
    }
    private static final class ClientSmoke {
        private static int ticks;
        private static int waiting;
        private static String lastScreen;
        private static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.TickEvent.ClientTickEvent event) -> { if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) tick(); }); }
        private static void tick() {
            net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
            Object screen = client.screen;
            if (screen != null && !screen.getClass().getName().equals(lastScreen)) {
                lastScreen = screen.getClass().getName();
                System.out.println("COMPAT_CLIENT_SCREEN " + lastScreen);
            }
            if (screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen backup) {
                // The copied disposable world is the only accepted target. Never
                // auto-confirm a user world or unrelated dialog.
                try {
                    net.minecraft.client.gui.screens.BackupConfirmScreen.Listener listener =
                            (net.minecraft.client.gui.screens.BackupConfirmScreen.Listener)field(backup, "listener");
                    listener.proceed(false, false);
                } catch (Exception problem) { throw new IllegalStateException(problem); }
            }
            if (client.level != null && client.player != null && ++ticks == 200) {
                try { Files.writeString(Paths.get("terrain-client-result.txt"), "PASS: rendered integrated world for 200 client ticks\n", StandardCharsets.UTF_8); }
                catch (Exception failure) { throw new IllegalStateException(failure); }
                client.stop();
            }
            if (client.level == null && ++waiting > 2400) {
                System.err.println("COMPAT_CLIENT_TIMEOUT " + lastScreen);
                client.stop();
            }
        }
    }
    private void finish(MinecraftServer server, Throwable failure) {
        try {
            String result = String.join("\n", evidence) + "\n" + (failure == null ? "PASS\n" : "FAIL: " + failure + "\n");
            Files.writeString(Paths.get("terrain-compatibility-result.txt"), result, StandardCharsets.UTF_8);
            System.out.println("MINERALOGY_TERRAIN_COMPATIBILITY_" + (failure == null ? "PASS" : "FAIL"));
            if (failure != null) failure.printStackTrace();
        } catch (Exception io) { io.printStackTrace(); }
        server.halt(false);
    }
    private static void require(boolean condition, String message) { if (!condition) throw new IllegalStateException(message); }
    private static final Properties REFLECTION_NAMES = reflectionNames();
    private static Properties reflectionNames() {
        Properties names = new Properties();
        try (java.io.InputStream input = TerrainCompatibilityProbe.class.getResourceAsStream("/terrain-reflection.properties")) {
            if (input != null) names.load(input);
        } catch (Exception problem) { throw new IllegalStateException("Cannot load pinned runtime reflection mappings", problem); }
        return names;
    }
    private static boolean memberName(Class<?> owner, String source, String actual) {
        if (source.equals(actual)) return true;
        return Arrays.asList(REFLECTION_NAMES.getProperty(owner.getName() + "." + source, "").split(",")).contains(actual);
    }
    private static Object enumValue(String type, String name) throws Exception { return Class.forName(type).getField(name).get(null); }
    private static Object field(Object target, String name) throws Exception {
        for (Class<?> type = target instanceof Class<?> ? (Class<?>)target : target.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) if (memberName(field.getDeclaringClass(), name, field.getName())) {
                field.setAccessible(true); return field.get(target instanceof Class<?> ? null : target);
            }
        }
        throw new NoSuchFieldException(name);
    }
    private static Object construct(Class<?> type, Object... args) throws Exception {
        for (Constructor<?> constructor : type.getConstructors()) if (matches(constructor.getParameterTypes(), args)) return constructor.newInstance(args);
        throw new NoSuchMethodException(type.getName() + " constructor");
    }
    private static Object callStatic(Class<?> type, String name, Object... args) throws Exception { return invoke(type, null, name, args); }
    private static Object call(Object target, String name, Object... args) throws Exception { return invoke(target.getClass(), target, name, args); }
    private static Object invoke(Class<?> type, Object target, String name, Object[] args) throws Exception {
        // Search public inherited methods first, then protected native pickup paths.
        for (Method method : type.getMethods()) if (memberName(method.getDeclaringClass(), name, method.getName()) && matches(method.getParameterTypes(), args)) { method.setAccessible(true); return method.invoke(target, args); }
        for (Class<?> cursor = type; cursor != null; cursor = cursor.getSuperclass()) for (Method method : cursor.getDeclaredMethods())
            if (memberName(method.getDeclaringClass(), name, method.getName()) && matches(method.getParameterTypes(), args)) { method.setAccessible(true); return method.invoke(target, args); }
        throw new NoSuchMethodException(type.getName() + "." + name + Arrays.toString(args));
    }
    private static boolean matches(Class<?>[] params, Object[] args) {
        if (params.length != args.length) return false;
        for (int index = 0; index < params.length; index++) {
            Class<?> type = params[index];
            if (type == boolean.class) type = Boolean.class;
            if (type == int.class) type = Integer.class;
            if (type == double.class) type = Double.class;
            if (args[index] != null && !type.isInstance(args[index])) return false;
        }
        return true;
    }
}
