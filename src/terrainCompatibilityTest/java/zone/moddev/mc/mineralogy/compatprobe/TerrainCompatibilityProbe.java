package zone.moddev.mc.mineralogy.compatprobe;

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
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
        ServerStartedEvent.BUS.addListener(this::started);
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
        verifyClassifications(phase);
        verifyCarverPolicy(phase);
        verifyGoatsAndCats(level, phase);
        verifyBats(level, phase);
        verifyCubes(level, phase);
        verifyCatalysts(level, phase);
        verifyFeatures(level, phase);
        verifyNarrowChecks(level, phase, true);
        verifyRootsAndForestRocks(level, phase);
        if (!Boolean.getBoolean("mineralogy.compatProbe.constructionDisabled")) {
            String recipePhase = phase.equals("reload") ? "resource-reload"
                    : System.getProperty("mineralogy.compatProbe.cycle", "fresh").equals("reload") ? "persisted-reload" : "first";
            zone.moddev.mc.mineralogy.fixture.RecipeIntegrationAssertions.verify(new ServerStartedEvent(level.getServer()),
                    zone.moddev.mc.mineralogy.MineralogyConfig.makeRockCobblestoneEquivilent(), recipePhase);
            evidence.add(phase + ": native recipe/advancement managers, stone spear, slab routes, mining/drops and furnace state/reload assertions passed");
        }
    }
    private void verifyCarverPolicy(String phase) {
        Set<String> terrain = raw();
        for (String name : List.of("andesite", "basalt", "diorite", "granite", "sandstone", "tuff", "stone")) terrain.add("minecraft:" + name);
        for (String name : terrain)
            require(!block(name).defaultBlockState().is(blockTag("minecraft:uncarvable")), "Terrain unexpectedly excluded from native carving: " + name);
        require(Blocks.BEDROCK.defaultBlockState().is(blockTag("minecraft:uncarvable")), "Missing native uncarvable control");
        evidence.add(phase + ": native carver exclusion policy permits all 37 terrain rocks + stone and still excludes bedrock; no carver eligibility change");
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
            String kind = path.contains("/tags/block/") ? "block" : "item";
            String tagPath = path.substring(path.indexOf("/tags/" + kind + "/") + ("/tags/" + kind + "/").length(), path.length() - 5);
            for (var member : entry.getValue().getAsJsonArray()) {
                boolean optional = member.isJsonObject();
                String memberId = optional ? member.getAsJsonObject().get("id").getAsString() : member.getAsString();
                if (memberId.startsWith("#")) continue; // Indirect policies have independent checks below.
                if (kind.equals("block")) {
                    Block value = BuiltInRegistries.BLOCK.getValue(id(memberId));
                    if (optional && (value == null || value == Blocks.AIR)) continue;
                    require(value != null && value != Blocks.AIR, "Missing required block " + memberId);
                    require(value.defaultBlockState().is(blockTag(namespace + ":" + tagPath)), "Loaded block classification " + path + " " + memberId);
                } else {
                    var value = BuiltInRegistries.ITEM.getValue(id(memberId));
                    if (optional && (value == null || value == Blocks.AIR.asItem())) continue;
                    require(value != null && value != Blocks.AIR.asItem(), "Missing required item " + memberId);
                    require(new ItemStack(value).is(TagKey.create(Registries.ITEM, id(namespace + ":" + tagPath))), "Loaded item classification " + path + " " + memberId);
                }
                checked++;
            }
        }
        boolean enabled = zone.moddev.mc.mineralogy.MineralogyConfig.makeRockCobblestoneEquivilent();
        for (String name : List.of("mineralogy:basalt", "minecraft:basalt", "mineralogy:chert", "mineralogy:pumice")) {
            boolean wanted = enabled || name.endsWith(":chert") || name.endsWith(":pumice");
            for (String tag : List.of("c:cobblestones", "c:cobblestones/normal", "forge:cobblestone")) {
                require(block(name).defaultBlockState().is(blockTag(tag)) == wanted, "Block cobblestone option " + tag + " " + name);
                require(new ItemStack(block(name)).is(TagKey.create(Registries.ITEM, id(tag))) == wanted, "Item cobblestone option " + tag + " " + name);
            }
        }
        for (String tag : List.of("c:cobblestones", "c:cobblestones/normal")) {
            require(Blocks.GOLD_BLOCK.defaultBlockState().is(blockTag(tag)), "Lost third-party block cobblestone " + tag);
            require(new ItemStack(Blocks.GOLD_BLOCK).is(TagKey.create(Registries.ITEM, id(tag))), "Lost third-party item cobblestone " + tag);
        }
        for (String name : List.of("nitrate", "phosphorous", "sulfur")) {
            require(block("mineralogy:" + name + "_ore").defaultBlockState().is(blockTag("c:ores")), "Ore aggregate " + name);
            require(new ItemStack(block("mineralogy:" + name + "_ore")).is(TagKey.create(Registries.ITEM, id("c:ores"))), "Item ore aggregate " + name);
        }
        for (String color : List.of("white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray", "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"))
            require(block("mineralogy:drywall_" + (color.equals("light_gray") ? "silver" : color)).defaultBlockState().is(blockTag("c:dyed")), "Dyed aggregate " + color);
        for (String excluded : List.of("mineralogy:basalt_brick", "mineralogy:basalt_smooth", "minecraft:sandstone"))
            if (registered(excluded))
            require(!block(excluded).defaultBlockState().is(blockTag("mineralogy:raw_stones")), "Crafted/sandstone common stone " + excluded);
        evidence.add(phase + ": " + checked + " registered direct block/item classifications; recursive ore/dyed aggregates and normal/root cobblestone option=" + enabled);
    }
    private void verifyGoatsAndCats(ServerLevel level, String phase) throws Exception {
        BlockPos pos = new BlockPos(768, 201, 192);
        level.getChunk(pos);
        net.minecraft.world.level.LevelAccessor view = (net.minecraft.world.level.LevelAccessor)Proxy.newProxyInstance(
                net.minecraft.world.level.LevelAccessor.class.getClassLoader(), new Class<?>[] {net.minecraft.world.level.LevelAccessor.class},
                (proxy, method, args) -> method.getName().equals("getRawBrightness") ? 15 : method.invoke(level, args));
        Set<String> terrain = raw();
        for (String name : List.of("andesite", "basalt", "diorite", "granite", "sandstone", "tuff")) terrain.add("minecraft:" + name);
        terrain.add("minecraft:stone");
        for (String name : terrain) {
            level.setBlock(pos.below(), block(name).defaultBlockState(), 2);
            require(net.minecraft.world.entity.animal.goat.Goat.checkGoatSpawnRules(net.minecraft.world.entity.EntityTypes.GOAT, view,
                    net.minecraft.world.entity.EntitySpawnReason.NATURAL, pos, RandomSource.create(17)), "Native goat spawn " + name);
        }
        var behavior = new net.minecraft.world.entity.ai.behavior.RamTarget(goat -> net.minecraft.util.valueproviders.UniformInt.of(1, 2),
                net.minecraft.world.entity.ai.targeting.TargetingConditions.forCombat(), 1.0F, goat -> 1.0D,
                goat -> net.minecraft.sounds.SoundEvents.GOAT_RAM_IMPACT, goat -> net.minecraft.sounds.SoundEvents.GOAT_HORN_BREAK);
        int horns = 0;
        Set<String> hornControls = raw();
        for (String name : List.of("andesite", "basalt", "diorite", "granite", "tuff", "stone")) hornControls.add("minecraft:" + name);
        if (registered("mineralogy:basalt_brick")) hornControls.add("mineralogy:basalt_brick");
        for (String name : hornControls) {
            var goat = new net.minecraft.world.entity.animal.goat.Goat(net.minecraft.world.entity.EntityTypes.GOAT, level);
            goat.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            goat.setDeltaMovement(new net.minecraft.world.phys.Vec3(1, 0, 0));
            goat.getBrain().setMemory(net.minecraft.world.entity.ai.memory.MemoryModuleType.RAM_TARGET, goat.position().add(10, 0, 0));
            level.setBlock(pos.east(), block(name).defaultBlockState(), 2);
            level.setBlock(pos.east().above(), Blocks.AIR.defaultBlockState(), 2);
            boolean expected = !List.of("mineralogy:rock_salt", "mineralogy:scoria", "mineralogy:siltstone", "mineralogy:chalk", "mineralogy:gypsum", "mineralogy:pumice", "mineralogy:basalt_brick").contains(name);
            require((Boolean)call(behavior, "hasRammedHornBreakingBlock", level, goat) == expected, "Native horn collision " + name);
            call(behavior, "tick", level, goat, 1L);
            boolean dropped = !goat.hasLeftHorn() || !goat.hasRightHorn();
            require(dropped == expected, "Actual horn loss " + name);
            if (dropped) horns++;
            goat.discard();
        }
        var cat = new net.minecraft.world.entity.animal.feline.Cat(net.minecraft.world.entity.EntityTypes.CAT, level);
        var goal = new net.minecraft.world.entity.ai.goal.CatSitOnBlockGoal(cat, 1.0D);
        List<String> furnaces = new ArrayList<>(List.of("minecraft:furnace"));
        for (String family : FAMILIES) for (String finish : List.of("", "_smooth", "_brick", "_smooth_brick")) {
            furnaces.add("mineralogy:" + family + finish + "_furnace");
            furnaces.add("mineralogy:lit_" + family + finish + "_furnace");
        }
        for (String name : furnaces) {
            Block value = BuiltInRegistries.BLOCK.getValue(id(name));
            if (value == null || value == Blocks.AIR) continue;
            level.setBlock(pos, value.defaultBlockState(), 2);
            level.setBlock(pos.above(), Blocks.AIR.defaultBlockState(), 2);
            require((Boolean)call(goal, "isValidTarget", level, pos) == name.startsWith("mineralogy:lit_"), "Cat lit-only targeting " + name);
        }
        level.setBlock(pos, Blocks.FURNACE.defaultBlockState().setValue(net.minecraft.world.level.block.FurnaceBlock.LIT, true), 2);
        require((Boolean)call(goal, "isValidTarget", level, pos), "Cat vanilla lit-furnace control");
        evidence.add(phase + ": native goat spawn on 37 terrain rocks + stone; actual horn loss on " + horns + " hard/vanilla controls with soft/crafted negatives; every registered cat furnace target checked lit/unlit");
    }
    private void verifyRootsAndForestRocks(ServerLevel level, String phase) throws Exception {
        Object registry = call(level.registryAccess(), "lookupOrThrow", Registries.FEATURE);
        Object roots = call(call(registry, "getOrThrow", ResourceKey.create(Registries.FEATURE, id("minecraft:rooted_azalea_tree"))), "value");
        Object forest = call(call(registry, "getOrThrow", ResourceKey.create(Registries.FEATURE, id("minecraft:forest_rock"))), "value");
        int index = 0;
        for (String name : List.of("minecraft:stone", "mineralogy:basalt", "mineralogy:chalk", "mineralogy:basalt_brick")) {
            if (!registered(name)) continue;
            BlockPos pos = new BlockPos(index++ * 48, level.getMinY() + 10, 384);
            prepareRoom(level, pos, block(name), 8);
            call(roots, "placeRootedDirt", level, RandomSource.create(17), pos.getX(), pos.getZ(), pos.mutable());
            boolean rootPlaced = count(level, pos, 8, Blocks.ROOTED_DIRT) > 0;
            boolean expected = !name.endsWith("_brick");
            require(rootPlaced == expected, "Native azalea root placement " + name);
            prepareRoom(level, pos, block(name), 8);
            for (int y = level.getMinY(); y < pos.getY(); y++) level.setBlock(new BlockPos(pos.getX(), y, pos.getZ()), block(name).defaultBlockState(), 2);
            call(forest, "place", level, level.getChunkSource().getGenerator(), RandomSource.create(17), pos.above());
            int placed = count(level, pos, 8, Blocks.MOSSY_COBBLESTONE);
            require((placed > 0) == expected, "Native forest-rock placement " + name);
            require(!block(name).defaultBlockState().is(blockTag("minecraft:azalea_grows_on")), "Bare rock was made tree soil " + name);
            evidence.add(phase + ": native root/forest-rock " + name + "=" + rootPlaced + "/" + placed);
        }
    }
    private void verifyNarrowChecks(ServerLevel level, String phase, boolean fixed) throws Exception {
        Object featureRegistry = call(level.registryAccess(), "lookupOrThrow", Registries.FEATURE);
        Object cluster = call(call(featureRegistry, "getOrThrow", ResourceKey.create(Registries.FEATURE, id("minecraft:dripstone_cluster"))), "value");
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
            call(column, "placeBlocks", level, RandomSource.create(17), wind);
            boolean stopped = level.getBlockState(center.below(3)).isAir();
            evidence.add(phase + ": native large-column stop " + name + "=" + stopped);
            require(stopped == (name.equals("minecraft:stone") || fixed && !name.endsWith("_brick")), "Large-column native control " + name);
        }
        net.minecraft.world.level.block.WallBlock wall = (net.minecraft.world.level.block.WallBlock)Blocks.COBBLESTONE_WALL;
        var straight = wall.defaultBlockState().setValue(net.minecraft.world.level.block.WallBlock.NORTH, net.minecraft.world.level.block.state.properties.WallSide.LOW)
                .setValue(net.minecraft.world.level.block.WallBlock.SOUTH, net.minecraft.world.level.block.state.properties.WallSide.LOW);
        for (String name : List.of("minecraft:torch", "mineralogy:rocksaltlamp")) {
            var top = block(name).defaultBlockState();
            Method topUpdate = wall.getClass().getDeclaredMethod("topUpdate", net.minecraft.world.level.LevelReader.class,
                    net.minecraft.world.level.block.state.BlockState.class, BlockPos.class, net.minecraft.world.level.block.state.BlockState.class);
            topUpdate.setAccessible(true);
            var result = (net.minecraft.world.level.block.state.BlockState)topUpdate.invoke(wall, level, straight, center.above(), top);
            boolean post = result.getValue(net.minecraft.world.level.block.WallBlock.UP);
            evidence.add(phase + ": native straight-wall post under " + name + "=" + post);
            require(post == (name.equals("minecraft:torch") || fixed), "Straight-wall torch control " + name);
            var junction = straight.setValue(net.minecraft.world.level.block.WallBlock.EAST, net.minecraft.world.level.block.state.properties.WallSide.LOW);
            result = (net.minecraft.world.level.block.state.BlockState)topUpdate.invoke(wall, level, junction, center.above(), top);
            require(result.getValue(net.minecraft.world.level.block.WallBlock.UP), "Junction-wall post " + name);
        }
    }
    private static Identifier id(String value) { return Identifier.parse(value); }
    private static Block block(String value) {
        Block result = BuiltInRegistries.BLOCK.getValue(id(value));
        require(result != null && result != Blocks.AIR, "Missing block " + value);
        return result;
    }
    private static boolean registered(String value) {
        Block valueBlock = BuiltInRegistries.BLOCK.getValue(id(value));
        return valueBlock != null && valueBlock != Blocks.AIR;
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
            if (registered("mineralogy:" + excluded))
            require(!block("mineralogy:" + excluded).defaultBlockState().is(terrainTag), "Crafted/non-terrain member " + excluded);
        boolean constructionDisabled = Boolean.getBoolean("mineralogy.compatProbe.constructionDisabled");
        String[] mining = {"mineable/pickaxe", "needs_iron_tool", "needs_stone_tool"};
        int[] totals = constructionDisabled ? new int[]{53, 3, 9} : new int[]{1133, 123, 329};
        for (int index = 0; index < mining.length; index++) {
            long total = java.util.stream.StreamSupport.stream(BuiltInRegistries.BLOCK.getTagOrEmpty(blockTag("minecraft:" + mining[index])).spliterator(), false)
                    .filter(holder -> BuiltInRegistries.BLOCK.getKey(holder.value()).getNamespace().equals("mineralogy")).count();
            require(total == totals[index], "Loaded mining total " + mining[index] + "=" + total);
        }
        evidence.add(phase + ": 37 block/item terrain identities; all five vanilla consumers; vanilla and third-party members retained");
    }
    private void verifyBats(ServerLevel level, String phase) throws Exception {
        // The real target predicate and loaded tags decide eligibility. Only the
        // environment's light/height is controlled so asynchronous lighting and
        // probabilistic ambient spawning cannot make this regression test flaky.
        BlockPos position = new BlockPos(640, 201, 128);
        level.getChunk(position);
        level.setBlockAndUpdate(position, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(position.above(), Blocks.AIR.defaultBlockState());
        int[] brightness = {0};
        boolean[] belowSurface = {true};
        net.minecraft.world.level.LevelAccessor view =
                (net.minecraft.world.level.LevelAccessor)Proxy.newProxyInstance(
                        net.minecraft.world.level.LevelAccessor.class.getClassLoader(),
                        new Class<?>[] {net.minecraft.world.level.LevelAccessor.class}, (proxy, method, args) -> {
                            if (method.getName().equals("getMaxLocalRawBrightness")) return brightness[0];
                            if (method.getName().equals("getHeightmapPos"))
                                return belowSurface[0] ? position.above(10) : position;
                            return method.invoke(level, args);
                        });
        Set<String> terrain = raw();
        for (String name : List.of("andesite", "basalt", "diorite", "granite", "sandstone", "tuff"))
            terrain.add("minecraft:" + name);
        List<String> supported = new ArrayList<>(List.of("minecraft:stone", "minecraft:deepslate", "minecraft:gold_block"));
        supported.addAll(terrain);
        for (String name : supported) {
            level.setBlockAndUpdate(position.below(), block(name).defaultBlockState());
            boolean accepted = false;
            RandomSource random = RandomSource.create(81499);
            for (int attempt = 0; attempt < 32; attempt++)
                accepted |= net.minecraft.world.entity.ambient.Bat.checkBatSpawnRules(
                        net.minecraft.world.entity.EntityTypes.BAT, view,
                        net.minecraft.world.entity.EntitySpawnReason.NATURAL, position, random);
            require(accepted, "Native bat spawn predicate rejected " + name);
            if (name.equals("minecraft:stone") || name.equals("minecraft:deepslate") || name.equals("minecraft:gold_block"))
                evidence.add(phase + ": vanilla/third-party bat control accepted " + name);
            require(block(name).defaultBlockState().is(blockTag("minecraft:bats_spawnable_on")),
                    "Bat tag omitted " + name);
        }
        for (String name : List.of("basalt_brick", "basalt_smooth", "basalt_slab", "basalt_furnace",
                "sulfur_ore", "sulfur_block", "rocksaltlamp", "rocksaltstreetlamp")) {
            if (!registered("mineralogy:" + name)) continue;
            level.setBlockAndUpdate(position.below(), block("mineralogy:" + name).defaultBlockState());
            RandomSource random = RandomSource.create(81499);
            for (int attempt = 0; attempt < 32; attempt++)
                require(!net.minecraft.world.entity.ambient.Bat.checkBatSpawnRules(
                        net.minecraft.world.entity.EntityTypes.BAT, view,
                        net.minecraft.world.entity.EntitySpawnReason.NATURAL, position, random),
                        "Native bat predicate accepted excluded " + name);
        }
        level.setBlockAndUpdate(position.below(), Blocks.STONE.defaultBlockState());
        brightness[0] = 15;
        RandomSource random = RandomSource.create(81499);
        for (int attempt = 0; attempt < 32; attempt++)
            require(!net.minecraft.world.entity.ambient.Bat.checkBatSpawnRules(
                    net.minecraft.world.entity.EntityTypes.BAT, view, net.minecraft.world.entity.EntitySpawnReason.NATURAL,
                    position, random), "Bat spawned in bright control");
        brightness[0] = 0;
        belowSurface[0] = false;
        random = RandomSource.create(81499);
        for (int attempt = 0; attempt < 32; attempt++)
            require(!net.minecraft.world.entity.ambient.Bat.checkBatSpawnRules(
                    net.minecraft.world.entity.EntityTypes.BAT, view, net.minecraft.world.entity.EntitySpawnReason.NATURAL,
                    position, random), "Bat spawned above surface control");
        evidence.add(phase + ": native Bat.checkBatSpawnRules accepted 37 terrain substrates + stone/deepslate/third-party gold; eight exclusions, bright and surface controls rejected");
    }
    private void verifyCubes(ServerLevel level, String phase) throws Exception {
        Class<?> cubeClass;
        try { cubeClass = Class.forName("net.minecraft.world.entity.monster.cubemob.SulfurCube"); }
        catch (ClassNotFoundException absent) { return; }
        Set<String> full = raw();
        Object type = Class.forName("net.minecraft.world.entity.EntityTypes").getField("SULFUR_CUBE").get(null);
        int count = 0;
        Object archetypeRegistry = call(level.registryAccess(), "lookupOrThrow", Registries.SULFUR_CUBE_ARCHETYPE);
        Object slowBouncy = call(call(archetypeRegistry, "getOrThrow", ResourceKey.create(Registries.SULFUR_CUBE_ARCHETYPE, id("minecraft:slow_bouncy"))), "value");
        for (String name : full) {
            var item = BuiltInRegistries.ITEM.getValue(id(name));
            if (item == null || item == Blocks.AIR.asItem()) continue;
            ItemStack stack = new ItemStack(item);
            require(stack.is(TagKey.create(Registries.ITEM, id("minecraft:sulfur_cube_swallowable"))), "Not swallowable " + name);
            require(stack.is(TagKey.create(Registries.ITEM, id("minecraft:sulfur_cube_archetype/slow_bouncy"))), "Not slow_bouncy " + name);
            Object cube = construct(cubeClass, type, level);
            call(cube, "setBaby", false);
            require((Boolean)call(cube, "canHoldItem", stack), "Cube rejects " + name);
            Object body = enumValue("net.minecraft.world.entity.EquipmentSlot", "BODY");
            Class<?> itemEntityClass = Class.forName("net.minecraft.world.entity.item.ItemEntity");
            Object itemEntity = construct(itemEntityClass, level, 0.0, 200.0, 0.0, stack.copy());
            call(cube, "pickUpItem", level, itemEntity);
            require(((ItemStack)call(cube, "getItemBySlot", body)).is(item), "Cube did not absorb " + name);
            require(((List<?>)call(cube, "matchingArchetypes", stack)).contains(slowBouncy), "Missing actual Slow Bouncy archetype " + name);
            count++;
        }
        require(count == 31, "Every raw cube item must be registered");
        int negatives = 0;
        for (var item : BuiltInRegistries.ITEM) {
            String name = BuiltInRegistries.ITEM.getKey(item).toString();
            if (!name.startsWith("mineralogy:") || full.contains(name)) continue;
            ItemStack stack = new ItemStack(item);
            require(!stack.is(TagKey.create(Registries.ITEM, id("minecraft:sulfur_cube_swallowable"))), "Indirect non-raw swallowable item " + name);
            Object cube = construct(cubeClass, type, level);
            call(cube, "setBaby", false);
            require(!(Boolean)call(cube, "canHoldItem", stack), "Cube accepted excluded " + name);
            negatives++;
        }
        evidence.add(phase + ": actual sulfur-cube pickup and Slow Bouncy for " + count + " raw rocks; all " + negatives + " non-raw Mineralogy items rejected, including indirect inheritance");
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
            Class<?> entityTypes = Class.forName("net.minecraft.world.entity.EntityTypes");
            Object type = entityTypes.getField("ZOMBIE").get(null);
            Class<?> zombieClass = Class.forName("net.minecraft.world.entity.monster.zombie.Zombie");
            LivingEntity zombie = (LivingEntity)construct(zombieClass, type, level);
            zombie.setPos(center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5);
            level.addFreshEntity(zombie);
            call(zombie, "die", level.damageSources().generic());
            // Deliver the real death to the catalyst explicitly if the synchronous
            // event dispatcher has not yet visited this listener in the probe tick.
            Object spreader = call(listener, "getSculkSpreader");
            if (((List<?>)call(spreader, "getCursors")).isEmpty()) {
                Class<?> gameEvent = Class.forName("net.minecraft.world.level.gameevent.GameEvent");
                Object dieEvent = gameEvent.getField("ENTITY_DIE").get(null);
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
        Object featureRegistry = call(level.registryAccess(), "lookupOrThrow", Registries.FEATURE);
        for (String feature : List.of("moss_patch_bonemeal", "dripstone_cluster")) {
            ResourceKey<?> key = ResourceKey.create(Registries.FEATURE, id("minecraft:" + feature));
            Object holder = call(featureRegistry, "getOrThrow", key);
            Object configured = call(holder, "value");
            int index = 0;
            for (String substrate : List.of("minecraft:stone", "mineralogy:basalt", "minecraft:basalt", "mineralogy:rhyolite", "mineralogy:chalk", "mineralogy:chert", "mineralogy:gypsum", "mineralogy:pumice", "mineralogy:basalt_brick")) {
                if (!registered(substrate)) continue;
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
        for (int x = -4; x <= 4; x++) for (int z = -4; z <= 4; z++) level.getChunk(x, z);
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(-64, -64, -64), new BlockPos(79, 80, 79))) {
            String name = BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString();
            if (name.startsWith("mineralogy:") || name.contains("dripstone") || name.contains("moss") || name.contains("cave_vines")
                    || name.equals("minecraft:air") || name.equals("minecraft:cave_air"))
                counts.merge(name, 1L, Long::sum);
        }
        evidence.add("Paired fresh cave sample: 81 chunks; seed -4965128775892001975; y=-64..80");
        counts.forEach((name, count) -> evidence.add(name + "=" + count));
    }
    private static final class ClientSmoke {
        private static int ticks;
        private static int waiting;
        private static String lastScreen;
        private static Object confirmedBackup;
        private static void register() { net.minecraftforge.event.TickEvent.ClientTickEvent.Post.BUS.addListener(event -> tick()); }
        private static void tick() {
            net.minecraft.client.Minecraft client = net.minecraft.client.Minecraft.getInstance();
            Object screen = client.gui.screen();
            if (screen != null && !screen.getClass().getName().equals(lastScreen)) {
                lastScreen = screen.getClass().getName();
                System.out.println("COMPAT_CLIENT_SCREEN " + lastScreen);
            }
            if (screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen backup && screen != confirmedBackup) {
                // The copied disposable world is the only accepted target. Never
                // auto-confirm a user world or unrelated dialog.
                try {
                    confirmedBackup = screen;
                    net.minecraft.client.gui.screens.BackupConfirmScreen.Listener listener =
                            (net.minecraft.client.gui.screens.BackupConfirmScreen.Listener)field(backup, "onProceed");
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
    private static Object enumValue(String type, String name) throws Exception { return Class.forName(type).getField(name).get(null); }
    private static Object field(Object target, String name) throws Exception {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(target); }
            catch (NoSuchFieldException ignored) { }
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
        for (Method method : type.getMethods()) if (method.getName().equals(name) && matches(method.getParameterTypes(), args)) return method.invoke(target, args);
        for (Class<?> cursor = type; cursor != null; cursor = cursor.getSuperclass()) for (Method method : cursor.getDeclaredMethods())
            if (method.getName().equals(name) && matches(method.getParameterTypes(), args)) { method.setAccessible(true); return method.invoke(target, args); }
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
