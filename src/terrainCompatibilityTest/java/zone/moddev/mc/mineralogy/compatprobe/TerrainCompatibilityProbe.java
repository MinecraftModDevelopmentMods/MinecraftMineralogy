package zone.moddev.mc.mineralogy.compatprobe;

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;

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
        CaveSamplePresets.register();
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener(this::started);
        if (Boolean.getBoolean("mineralogy.compatProbe.client")) ClientSmoke.register();
    }
    private void started(ServerStartedEvent event) {
        if (Boolean.getBoolean("mineralogy.compatProbe.client")) return;
        MinecraftServer server = event.getServer();
        try {
            if (Boolean.getBoolean("mineralogy.compatProbe.baseline")) {
                verifyNarrowChecks(server.overworld(), "baseline", false);
                finish(server, null);
                return;
            }
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
        verifyConstructionLoot(level, phase);
        verifyClassifications(phase);
        verifyCarverPolicy(level, phase);
        verifyGoatsAndCats(level, phase);

        verifyFeatures(level, phase);
        verifyNarrowChecks(level, phase, true);
        verifyRootsAndForestRocks(level, phase);
        if (!Boolean.getBoolean("mineralogy.compatProbe.constructionDisabled")) {
            String recipePhase = phase.equals("reload") ? "resource-reload"
                    : System.getProperty("mineralogy.compatProbe.cycle", "fresh").equals("reload") ? "persisted-reload" : "first";
            zone.moddev.mc.mineralogy.fixture.RecipeIntegrationAssertions.verify(new ServerStartedEvent(level.getServer()),
                    zone.moddev.mc.mineralogy.MineralogyConfig.makeRockCobblestoneEquivilent(), recipePhase);
            evidence.add(phase + ": native recipe/advancement managers and slab routes, mining/drops and furnace state/reload assertions passed");
        }
    }
    private void verifyCarverPolicy(ServerLevel level, String phase) throws Exception {
        var carver = level.registryAccess().registryOrThrow(Registry.CONFIGURED_CARVER_REGISTRY)
                .getHolderOrThrow(ResourceKey.create(Registry.CONFIGURED_CARVER_REGISTRY, id("minecraft:cave"))).value();
        require((Boolean)call(carver.worldCarver(), "canReplaceBlock", Blocks.STONE.defaultBlockState()), "Native stone carver control");
        require(!(Boolean)call(carver.worldCarver(), "canReplaceBlock", Blocks.BEDROCK.defaultBlockState()), "Native bedrock carver exclusion");
        for (String name : raw())
            require(!(Boolean)call(carver.worldCarver(), "canReplaceBlock", block(name).defaultBlockState()), "Broadened native carver hosts: " + name);
        evidence.add(phase + ": native positive carver hosts unchanged; stone accepted, bedrock and 31 Mineralogy rocks rejected. OreSpawn replacement follows carving, so no carver-tag addition");
    }
    private void verifyConstructionLoot(ServerLevel level, String phase) throws Exception {
        var registries = level.getServer().getLootTables();
        var tables = registries.getIds().stream()
                .filter(key -> key.getNamespace().equals("mineralogy") && key.getPath().startsWith("blocks/")).toList();
        require(tables.size() == 919, "Missing loaded Mineralogy loot tables: " + tables.size());
        boolean disabled = Boolean.getBoolean("mineralogy.compatProbe.constructionDisabled");
        int guarded = 0;
        for (var name : tables) {
            if (!name.getPath().matches("blocks/(lit_)?(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)")) continue;
            var table = registries.get(name);
            boolean entries = false;
            for (Object pool : (List<?>)field(table, "pools"))
                if (Array.getLength(field(pool, "entries")) > 0) entries = true;
            require(entries != disabled, "Native construction loot guard " + name);
            guarded++;
        }
        require(guarded == 864, "Loaded construction loot guard count: " + guarded);
        evidence.add(phase + ": all 919 native loot tables loaded; 864 construction pools " + (disabled ? "empty" : "retained"));
    }
    private void verifyClassifications(String phase) throws Exception {
        com.google.gson.JsonObject contract;
        try (var reader = new java.io.InputStreamReader(getClass().getResourceAsStream("/tag-compatibility-contract.json"), StandardCharsets.UTF_8)) {
            contract = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
        }
        int checked = 0;
        for (var entry : contract.getAsJsonObject("tags").entrySet()) {
            String path = entry.getKey();
            String namespace = path.substring(0, path.indexOf('/'));
            String kind = path.contains("/tags/blocks/") ? "blocks" : "items";
            String tagPath = path.substring(path.indexOf("/tags/" + kind + "/") + ("/tags/" + kind + "/").length(), path.length() - 5);
            for (var member : entry.getValue().getAsJsonArray()) {
                boolean optional = member.isJsonObject();
                String memberId = optional ? member.getAsJsonObject().get("id").getAsString() : member.getAsString();
                if (memberId.startsWith("#")) {
                    ResourceLocation inherited = id(memberId.substring(1));
                    if (kind.equals("blocks")) {
                        for (var holder : Registry.BLOCK.getTagOrEmpty(TagKey.create(Registry.BLOCK_REGISTRY, inherited))) {
                            require(holder.value().defaultBlockState().is(blockTag(namespace + ":" + tagPath)),
                                    "Lost inherited block member " + path + " from " + memberId);
                        }
                    } else {
                        for (var holder : Registry.ITEM.getTagOrEmpty(TagKey.create(Registry.ITEM_REGISTRY, inherited))) {
                            require(new ItemStack(holder.value()).is(TagKey.create(Registry.ITEM_REGISTRY, id(namespace + ":" + tagPath))),
                                    "Lost inherited item member " + path + " from " + memberId);
                        }
                    }
                    continue;
                }
                if (kind.equals("blocks")) {
                    Block value = Registry.BLOCK.get(id(memberId));
                    if (optional && (value == null || value == Blocks.AIR)) continue;
                    require(value != null && value != Blocks.AIR, "Missing required block " + memberId);
                    require(value.defaultBlockState().is(blockTag(namespace + ":" + tagPath)), "Loaded block classification " + path + " " + memberId);
                } else {
                    var value = Registry.ITEM.get(id(memberId));
                    if (optional && (value == null || value == Blocks.AIR.asItem())) continue;
                    require(value != null && value != Blocks.AIR.asItem(), "Missing required item " + memberId);
                    require(new ItemStack(value).is(TagKey.create(Registry.ITEM_REGISTRY, id(namespace + ":" + tagPath))), "Loaded item classification " + path + " " + memberId);
                }
                checked++;
            }
        }
        boolean enabled = zone.moddev.mc.mineralogy.MineralogyConfig.makeRockCobblestoneEquivilent();
        for (String name : List.of("mineralogy:basalt", "minecraft:basalt", "mineralogy:chert", "mineralogy:pumice")) {
            boolean wanted = enabled || name.endsWith(":chert") || name.endsWith(":pumice");
            for (String tag : List.of("forge:cobblestone", "forge:cobblestone/normal")) {
                require(block(name).defaultBlockState().is(blockTag(tag)) == wanted, "Block cobblestone option " + tag + " " + name);
                require(new ItemStack(block(name)).is(TagKey.create(Registry.ITEM_REGISTRY, id(tag))) == wanted, "Item cobblestone option " + tag + " " + name);
            }
        }
        for (String tag : List.of("forge:cobblestone", "forge:cobblestone/normal")) {
            require(Blocks.GOLD_BLOCK.defaultBlockState().is(blockTag(tag)), "Lost third-party block cobblestone " + tag);
            require(new ItemStack(Blocks.GOLD_BLOCK).is(TagKey.create(Registry.ITEM_REGISTRY, id(tag))), "Lost third-party item cobblestone " + tag);
        }
        for (String name : List.of("nitrate", "phosphorous", "sulfur")) {
            require(block("mineralogy:" + name + "_ore").defaultBlockState().is(blockTag("forge:ores")), "Ore aggregate " + name);
            require(new ItemStack(block("mineralogy:" + name + "_ore")).is(TagKey.create(Registry.ITEM_REGISTRY, id("forge:ores"))), "Item ore aggregate " + name);
        }
        for (String excluded : List.of("mineralogy:basalt_brick", "mineralogy:basalt_smooth", "minecraft:sandstone"))
            if (registered(excluded))
            require(!block(excluded).defaultBlockState().is(blockTag("mineralogy:raw_stones")), "Crafted/sandstone common stone " + excluded);
        evidence.add(phase + ": " + checked + " registered direct block/item classifications; recursive ore/storage aggregates and normal/root cobblestone option=" + enabled);
    }
    private void verifyGoatsAndCats(ServerLevel level, String phase) throws Exception {
        BlockPos pos = new BlockPos(768, 201, 192);
        level.getChunk(pos);
        net.minecraft.world.level.LevelAccessor view = (net.minecraft.world.level.LevelAccessor)Proxy.newProxyInstance(
                net.minecraft.world.level.LevelAccessor.class.getClassLoader(), new Class<?>[] {net.minecraft.world.level.LevelAccessor.class},
                (proxy, method, args) -> memberName(method.getDeclaringClass(), "getRawBrightness", method.getName()) ? 15 : method.invoke(level, args));
        Set<String> terrain = raw();
        for (String name : List.of("andesite", "basalt", "diorite", "granite", "sandstone", "tuff")) terrain.add("minecraft:" + name);
        terrain.add("minecraft:stone");
        for (String name : terrain) {
            level.setBlock(pos.below(), block(name).defaultBlockState(), 2);
            require(net.minecraft.world.entity.animal.goat.Goat.checkGoatSpawnRules(net.minecraft.world.entity.EntityType.GOAT, view,
                    net.minecraft.world.entity.MobSpawnType.NATURAL, pos, new Random(17)), "Native goat spawn " + name);
        }
        var cat = new net.minecraft.world.entity.animal.Cat(net.minecraft.world.entity.EntityType.CAT, level);
        var goal = new net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal(cat, 1.0D);
        List<String> furnaces = new ArrayList<>(List.of("minecraft:furnace"));
        for (String family : FAMILIES) for (String finish : List.of("", "_smooth", "_brick", "_smooth_brick")) {
            furnaces.add("mineralogy:" + family + finish + "_furnace");
            furnaces.add("mineralogy:lit_" + family + finish + "_furnace");
        }
        for (String name : furnaces) {
            Block value = Registry.BLOCK.get(id(name));
            if (value == null || value == Blocks.AIR) continue;
            level.setBlock(pos, value.defaultBlockState(), 2);
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
            require((Boolean)call(goal, "isValidTarget", level, pos) == false, "Cat lit-only targeting " + name);
        }
        level.setBlock(pos, Blocks.FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.FurnaceBlock.LIT, true), 2);
        require((Boolean)call(goal, "isValidTarget", level, pos), "Cat vanilla lit-furnace control");
        evidence.add(phase + ": native goat spawn on 37 terrain rocks + stone; horn-loss mechanics do not exist on this target; native pre-26.3 cat targets unchanged (Mineralogy furnaces excluded)");
    }
    private void verifyRootsAndForestRocks(ServerLevel level, String phase) throws Exception {
        Object registry = call(level.registryAccess(), "registryOrThrow", Registry.CONFIGURED_FEATURE_REGISTRY);
        Object roots = call(call(registry, "getHolderOrThrow", ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, id("minecraft:rooted_azalea_tree"))), "value");
        Object forest = call(call(registry, "getHolderOrThrow", ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, id("minecraft:forest_rock"))), "value");
        int index = 0;
        for (String name : List.of("minecraft:stone", "mineralogy:basalt", "mineralogy:chalk", "mineralogy:basalt_brick")) {
            if (!registered(name)) continue;
            BlockPos pos = new BlockPos(index++ * 48, level.getMinBuildHeight() + 10, 384);
            prepareRoom(level, pos, block(name), 8);
            call(call(roots, "feature"), "placeRootedDirt", level, call(roots, "config"), new Random(17), pos.getX(), pos.getZ(), pos.mutable());
            boolean rootPlaced = count(level, pos, 8, Blocks.ROOTED_DIRT) > 0;
            boolean expected = !name.endsWith("_brick");
            require(rootPlaced == expected, "Native azalea root placement " + name);
            prepareRoom(level, pos, block(name), 8);
            for (int y = level.getMinBuildHeight(); y < pos.getY(); y++) level.setBlock(new BlockPos(pos.getX(), y, pos.getZ()), block(name).defaultBlockState(), 2);
            call(forest, "place", level, level.getChunkSource().getGenerator(), new Random(17), pos.above());
            int placed = count(level, pos, 8, Blocks.MOSSY_COBBLESTONE);
            require((placed > 0) == expected, "Native forest-rock placement " + name);
            require(!block(name).defaultBlockState().is(blockTag("minecraft:azalea_grows_on")), "Bare rock was made tree soil " + name);
            evidence.add(phase + ": native root/forest-rock " + name + "=" + rootPlaced + "/" + placed);
        }
    }
    private void verifyNarrowChecks(ServerLevel level, String phase, boolean fixed) throws Exception {
        Object featureRegistry = call(level.registryAccess(), "registryOrThrow", Registry.CONFIGURED_FEATURE_REGISTRY);
        Object cluster = call(call(featureRegistry, "getHolderOrThrow", ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, id("minecraft:dripstone_cluster"))), "value");
        cluster = call(cluster, "feature");
        BlockPos center = new BlockPos(512, 200, 256);
        prepareRoom(level, center, Blocks.STONE, 12);
        for (String name : List.of("minecraft:stone", "mineralogy:basalt", "mineralogy:chalk", "mineralogy:basalt_brick")) {
            if (!registered(name)) continue;
            level.setBlock(center, block(name).defaultBlockState(), 2);
            boolean accepted = (Boolean)call(cluster, "canBeAdjacentToWater", level, center);
            evidence.add(phase + ": native water-pocket adjacency " + name + "=" + accepted);
            require(accepted == (name.equals("minecraft:stone") || fixed && !name.endsWith("_brick")), "Water-pocket native control " + name);
        }
        // Exercise the private column walker directly: air, a rock barrier, then
        // air again. A column must stop at natural rock, but not at crafted brick.
        Class<?> columnType = Class.forName("net.minecraft.world.level.levelgen.feature.LargeDripstoneFeature$LargeDripstone");
        Class<?> windType = Class.forName("net.minecraft.world.level.levelgen.feature.LargeDripstoneFeature$WindOffsetter");
        Constructor<?> columnConstructor = columnType.getDeclaredConstructor(BlockPos.class, boolean.class, int.class, double.class, double.class);
        columnConstructor.setAccessible(true);
        Object wind = callStatic(windType, "noWind");
        for (String name : List.of("minecraft:stone", "mineralogy:basalt", "mineralogy:basalt_brick")) {
            if (!registered(name)) continue;
            prepareRoom(level, center, Blocks.STONE, 12);
            for (int y = -8; y <= 0; y++) level.setBlock(center.offset(0, y, 0), (y == -2 ? block(name) : Blocks.AIR).defaultBlockState(), 2);
            Object column = columnConstructor.newInstance(center, false, 2, 0.5, 5.0);
            call(column, "placeBlocks", level, new Random(17), wind);
            boolean stopped = level.getBlockState(center.below(3)).isAir();
            evidence.add(phase + ": native large-column stop " + name + "=" + stopped);
            require(stopped == (name.equals("minecraft:stone") || fixed && !name.endsWith("_brick")), "Large-column native control " + name);
        }
        net.minecraft.world.level.block.WallBlock wall = (net.minecraft.world.level.block.WallBlock)Blocks.COBBLESTONE_WALL;
        var straight = wall.defaultBlockState().setValue(net.minecraft.world.level.block.WallBlock.NORTH_WALL, net.minecraft.world.level.block.state.properties.WallSide.LOW)
                .setValue(net.minecraft.world.level.block.WallBlock.SOUTH_WALL, net.minecraft.world.level.block.state.properties.WallSide.LOW);
        for (String name : List.of("minecraft:torch", "mineralogy:rocksaltlamp")) {
            var top = block(name).defaultBlockState();
            Method topUpdate = declaredMethod(wall.getClass(), "topUpdate", net.minecraft.world.level.LevelReader.class,
                    net.minecraft.world.level.block.state.BlockState.class, BlockPos.class, net.minecraft.world.level.block.state.BlockState.class);
            var result = (net.minecraft.world.level.block.state.BlockState)topUpdate.invoke(wall, level, straight, center.above(), top);
            boolean post = result.getValue(net.minecraft.world.level.block.WallBlock.UP);
            evidence.add(phase + ": native straight-wall post under " + name + "=" + post);
            require(post == (name.equals("minecraft:torch") || fixed), "Straight-wall torch control " + name);
            var junction = straight.setValue(net.minecraft.world.level.block.WallBlock.EAST_WALL, net.minecraft.world.level.block.state.properties.WallSide.LOW);
            result = (net.minecraft.world.level.block.state.BlockState)topUpdate.invoke(wall, level, junction, center.above(), top);
            require(result.getValue(net.minecraft.world.level.block.WallBlock.UP), "Junction-wall post " + name);
        }
    }
    private static ResourceLocation id(String value) { return new ResourceLocation(value); }
    private static Block block(String value) {
        Block result = Registry.BLOCK.get(id(value));
        require(result != null && result != Blocks.AIR, "Missing block " + value);
        return result;
    }
    private static boolean registered(String value) {
        Block valueBlock = Registry.BLOCK.get(id(value));
        return valueBlock != null && valueBlock != Blocks.AIR;
    }
    private static TagKey<Block> blockTag(String name) { return TagKey.create(Registry.BLOCK_REGISTRY, id(name)); }
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
        require(java.util.stream.StreamSupport.stream(Registry.BLOCK.getTagOrEmpty(terrainTag).spliterator(), false).count() == 37, "Loaded terrain cardinality");
        for (String name : terrain) {
            require(block(name).defaultBlockState().is(terrainTag), "Terrain omitted " + name);
            require(new ItemStack(block(name)).is(TagKey.create(Registry.ITEM_REGISTRY, id("mineralogy:terrain_rocks"))), "Item terrain omitted " + name);
            for (String tag : List.of("dripstone_replaceable_blocks", "moss_replaceable", "lush_ground_replaceable"))
                require(block(name).defaultBlockState().is(blockTag("minecraft:" + tag)), tag + " omitted " + name);
        }
        for (String tag : List.of("dripstone_replaceable_blocks", "moss_replaceable")) {
            require(Blocks.STONE.defaultBlockState().is(blockTag("minecraft:" + tag)), "Lost vanilla stone " + tag);
            require(Blocks.GOLD_BLOCK.defaultBlockState().is(blockTag("minecraft:" + tag)), "Lost third-party member " + tag);
        }
        for (String excluded : List.of("basalt_brick", "basalt_smooth", "basalt_slab", "basalt_furnace", "sulfur_ore", "sulfur_block", "rocksaltlamp", "rocksaltstreetlamp"))
            if (registered("mineralogy:" + excluded))
            require(!block("mineralogy:" + excluded).defaultBlockState().is(terrainTag), "Crafted/non-terrain member " + excluded);
        boolean constructionDisabled = Boolean.getBoolean("mineralogy.compatProbe.constructionDisabled");
        String[] mining = {"mineable/pickaxe", "needs_iron_tool", "needs_stone_tool"};
        int[] totals = constructionDisabled ? new int[]{53, 3, 9} : new int[]{1133, 123, 329};
        for (int index = 0; index < mining.length; index++) {
            long total = java.util.stream.StreamSupport.stream(Registry.BLOCK.getTagOrEmpty(blockTag("minecraft:" + mining[index])).spliterator(), false)
                    .filter(holder -> Registry.BLOCK.getKey(holder.value()).getNamespace().equals("mineralogy")).count();
            require(total == totals[index], "Loaded mining total " + mining[index] + "=" + total);
        }
        evidence.add(phase + ": 37 block/item terrain identities; all supported cave consumers; vanilla and third-party members retained");
    }
    private void verifyFeatures(ServerLevel level, String phase) throws Exception {
        Object featureRegistry = call(level.registryAccess(), "registryOrThrow", Registry.CONFIGURED_FEATURE_REGISTRY);
        for (String feature : List.of("moss_patch_bonemeal", "dripstone_cluster")) {
            ResourceKey<?> key = ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, id("minecraft:" + feature));
            Object holder = call(featureRegistry, "getHolderOrThrow", key);
            Object configured = call(holder, "value");
            int index = 0;
            for (String substrate : List.of("minecraft:stone", "mineralogy:basalt", "minecraft:basalt", "mineralogy:rhyolite", "mineralogy:chalk", "mineralogy:chert", "mineralogy:gypsum", "mineralogy:pumice", "mineralogy:basalt_brick")) {
                if (!registered(substrate)) continue;
                BlockPos center = new BlockPos(index++ * 32, 200, 64);
                int placed = 0;
                for (long seed : new long[] { 3, 17, 81, 109, 531 }) {
                    prepareRoom(level, center, block(substrate), 12);
                    call(configured, "place", level, level.getChunkSource().getGenerator(), new Random(seed), center.above());
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
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) level.getChunk(x, z);
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(-64, -64, -64), new BlockPos(79, 80, 79))) {
            String name = Registry.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
            if (name.startsWith("mineralogy:") || name.contains("dripstone") || name.contains("moss") || name.contains("cave_vines")
                    || name.equals("minecraft:air") || name.equals("minecraft:cave_air"))
                counts.merge(name, 1L, Long::sum);
        }
        evidence.add("Paired fresh cave sample: 81 chunks; seed -4965128775892001975; y=-64..80");
        counts.forEach((name, count) -> evidence.add(name + "=" + count));
    }
    private static final class ClientSmoke {
        private static boolean openedFixture;
        private static int ticks;
        private static int waiting;
        private static String lastScreen;
        private static Object confirmedBackup;
        private static void register() { net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.TickEvent.ClientTickEvent event) -> { if (event.phase == net.minecraftforge.event.TickEvent.Phase.END) tick(); }); }
        private static void tick() {
            net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
            Object screen = client.screen;
            if (screen != null && !screen.getClass().getName().equals(lastScreen)) {
                lastScreen = screen.getClass().getName();
                System.out.println("COMPAT_CLIENT_SCREEN " + lastScreen);
            }
            if (!openedFixture && screen instanceof net.minecraft.client.gui.screens.TitleScreen) {
                openedFixture = true;
                // 1.18.2 predates Quick Play. Open only the copied probe world,
                // through its native loader, and acknowledge its experimental pack.
                try {
                    call(client, "loadLevel", "compatibility-world");
                } catch (Exception problem) { throw new IllegalStateException(problem); }
            }
            if (screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen backup && screen != confirmedBackup) {
                // The copied disposable world is the only accepted target. Never
                // auto-confirm a user world or unrelated dialog.
                try {
                    confirmedBackup = screen;
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
    private static Object enumValue(String type, String name) throws Exception { return field(Class.forName(type), name); }
    private static Method declaredMethod(Class<?> type, String name, Class<?>... parameters) throws Exception {
        for (Method method : type.getDeclaredMethods()) {
            if (memberName(type, name, method.getName()) && Arrays.equals(method.getParameterTypes(), parameters)) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new NoSuchMethodException(type.getName() + "." + name);
    }
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
            if (type == float.class) type = Float.class;
            if (type == long.class) type = Long.class;
            if (args[index] != null && !type.isInstance(args[index])) return false;
        }
        return true;
    }
}
