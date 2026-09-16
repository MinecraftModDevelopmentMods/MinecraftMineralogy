package zone.moddev.mc.mineralogy;

import org.junit.Test;
import zone.moddev.mc.mineralogy.data.MaterialData;
import zone.moddev.mc.mineralogy.blocks.Ore;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.blocks.RockRelief;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.blocks.RockStairs;
import zone.moddev.mc.mineralogy.blocks.RockWall;
import zone.moddev.mc.mineralogy.init.MineralogyItemGroups;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class GameplayContractTest {
    @Test
    public void reliefOpenCentreDoesNotCullItsSupportingBlockFace() throws Exception {
        String relief = text("src/main/java/zone/moddev/mc/mineralogy/blocks/RockRelief.java");
        assertTrue(relief.contains("VoxelShape getOcclusionShape(BlockState state)"));
        assertTrue(relief.contains("VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos,"));
        assertTrue(relief.contains("return Shapes.empty();"));
    }

    @Test
    public void novaculiteAndRockFamilyContractsAreStable() {
        assertEquals(3.0D, MaterialData.NOVACULITE.hardness, 0.0D);
        assertEquals(15.0D, MaterialData.NOVACULITE.blastResistance, 0.0D);
        assertEquals(1, MaterialData.NOVACULITE.toolHardnessLevel);
        assertEquals(27, MaterialData.allIncludingRockSalt().size());
        Set<String> ids = new HashSet<String>();
        MaterialData.allIncludingRockSalt().forEach(material -> ids.add(material.id()));
        assertEquals(27, ids.size());
        assertTrue(ids.contains("rock_salt"));
    }

    @Test
    public void creativePolicyUsesReliefBeforeSlabAndFiveGroups() throws Exception {
        String source = text("src/main/java/zone/moddev/mc/mineralogy/CreativeTabPolicy.java");
        assertTrue(source.indexOf("RockRelief.class") < source.indexOf("RockSlab.class"));
        assertTrue(source.contains("enum ContentGroup { ROCK, STAIR, SLAB, WALL, ITEM }"));
        String groups = text("src/main/java/zone/moddev/mc/mineralogy/init/MineralogyItemGroups.java");
        assertTrue(groups.contains("register(helper, \"rock\", \"basalt\")"));
        assertTrue(groups.contains("register(helper, \"stair\", \"basalt_stairs\")"));
        assertTrue(groups.contains("register(helper, \"slab\", \"basalt_slab\")"));
        assertTrue(groups.contains("register(helper, \"wall\", \"basalt_wall\")"));
        assertTrue(groups.contains("register(helper, \"item\", \"sulfur_dust\")"));
        assertTrue(groups.contains("CreativeModeTabs.INGREDIENTS"));
        assertTrue(groups.contains(".withSearchBar()"));
        assertTrue(groups.contains("BuildCreativeModeTabContentsEvent"));
        assertTrue(groups.replace("\r", "").contains(
                "@SubscribeEvent\n\tpublic static void buildTabContents(BuildCreativeModeTabContentsEvent event)"));
        assertTrue(groups.contains("Registries.CREATIVE_MODE_TAB"));
        assertNotNull(MineralogyItemGroups.class
                .getDeclaredMethod("buildTabContents", BuildCreativeModeTabContentsEvent.class)
                .getAnnotation(SubscribeEvent.class));

        CreativeTabPolicy grouped = new CreativeTabPolicy(true);
        assertTrue(grouped.groupTabsByType());
        assertEquals(CreativeTabPolicy.ContentGroup.ROCK, grouped.groupFor(Rock.class));
        assertEquals(CreativeTabPolicy.ContentGroup.ROCK, grouped.groupFor(Ore.class));
        assertEquals(CreativeTabPolicy.ContentGroup.STAIR, grouped.groupFor(RockStairs.class));
        assertEquals(CreativeTabPolicy.ContentGroup.SLAB, grouped.groupFor(RockSlab.class));
        assertEquals(CreativeTabPolicy.ContentGroup.WALL, grouped.groupFor(RockWall.class));
        assertEquals(CreativeTabPolicy.ContentGroup.ITEM, grouped.groupFor(RockRelief.class));
        assertEquals(CreativeTabPolicy.ContentGroup.ITEM, grouped.groupFor(Object.class));
        assertFalse(new CreativeTabPolicy(false).groupTabsByType());
    }

    @Test
    public void dropsSlabsAndFurnacePersistenceFollowAcceptedSemantics() throws Exception {
        String chert = text("src/main/java/zone/moddev/mc/mineralogy/blocks/Chert.java");
        assertTrue(chert.contains("if (prng.nextInt(10) == 0)"));
        assertTrue(chert.contains("return super.getDrops(state, builder);"));
        String rock = text("src/main/java/zone/moddev/mc/mineralogy/blocks/Rock.java");
        assertTrue(rock.contains("return Collections.singletonList(new ItemStack(Blocks.COBBLESTONE));"));
        String slab = text("src/main/java/zone/moddev/mc/mineralogy/blocks/DoubleSlab.java");
        assertTrue(slab.contains("new ItemStack(drops, 2)"));
        assertTrue(slab.contains("Collections.singletonList(new ItemStack(fullBlock))"));
        String furnace = text("src/main/java/zone/moddev/mc/mineralogy/tileentity/TileEntityRockFurnace.java");
        assertTrue(furnace.contains("public TileEntityRockFurnace(BlockPos pos, BlockState state)"));
        assertTrue(furnace.contains("BlockState state = getBlockState()"));
        assertTrue(furnace.contains("Block block = state.getBlock()"));
        assertTrue(furnace.contains("getBurnModifier()"));
        assertTrue(furnace.contains("ContainerHelper.loadAllItems"));
		String furnaceBlock = text("src/main/java/zone/moddev/mc/mineralogy/blocks/RockFurnace.java");
		assertTrue(furnaceBlock.contains("BlockState newState = newBlock.defaultBlockState()"));
		assertTrue(furnaceBlock.contains("try {"));
		assertTrue(furnaceBlock.contains("finally {"));
		assertTrue(furnaceBlock.contains("tileEntity.setBlockState(newState)"));
		assertTrue(furnaceBlock.contains("world.setBlockEntity(tileEntity)"));
		assertTrue(furnaceBlock.contains("shouldChangedStateKeepBlockEntity"));
		assertTrue(furnaceBlock.contains("previousState.getBlock() instanceof RockFurnace"));

		String lamp = text("src/main/java/zone/moddev/mc/mineralogy/blocks/RockSaltLamp.java");
		assertTrue(lamp.contains("facing.getAxis().isVertical()"));
		assertTrue(lamp.contains("Block.canSupportCenter(world, supportPos, facing)"));
    }

    @Test
    public void cobblestonePolicyReappliesAfterReloadAndKeepsSpecialCases() throws Exception {
        String policy = text("src/main/java/zone/moddev/mc/mineralogy/compat/CobblestoneTagPolicy.java");
        assertTrue(policy.contains("onTagsUpdated(TagsUpdatedEvent event)"));
        assertTrue(policy.contains("event.getLookupProvider()"));
        assertTrue(policy.contains("event.shouldUpdateStaticData()"));
        assertTrue(policy.contains("MaterialData.allIncludingRockSalt()"));
        assertTrue(policy.contains("\"stones/\" + material.id()"));
        assertTrue(policy.contains("rawRockHolders(blockRegistry"));
        assertTrue(policy.contains("rawRockHolders(itemRegistry"));
        assertTrue(policy.contains("\"chert\", \"pumice\""));
        assertTrue(policy.contains("HolderSet.Named<T> tag"));
        assertTrue(policy.contains("tag.bind(replacement)"));
        assertTrue(policy.contains("reference.bindTags(tags)"));
        assertTrue(policy.contains("STONE_CRAFTING_MATERIALS"));
        assertTrue(policy.contains("STONE_TOOL_MATERIALS"));
        assertTrue(policy.contains("COMMON_COBBLESTONES"));
        assertTrue(policy.contains("COMMON_NORMAL_COBBLESTONES"));
        assertTrue(policy.contains("MINERALOGY_COBBLESTONE"));
        assertTrue(policy.contains("TagKey.create"));
        assertFalse(policy.contains("java.lang.reflect"));
        assertFalse(policy.contains("Ingredient.invalidateAll()"));
        String accessTransformer = text("src/main/resources/META-INF/accesstransformer.cfg");
        assertTrue(accessTransformer.contains("Holder$Reference bindTags"));
        assertTrue(accessTransformer.contains("HolderSet$Named bind"));
        String metadata = text("src/main/resources/META-INF/neoforge.mods.toml");
        assertTrue(metadata.contains("[[accessTransformers]]"));
        assertTrue(metadata.contains("file=\"META-INF/accesstransformer.cfg\""));
    }

	@Test
	public void neoForge261ConstructionReceivesStableIdsBeforeRegistration() throws Exception {
		String helper = text("src/main/java/zone/moddev/mc/mineralogy/init/RegistrationProperties.java");
		assertTrue(helper.contains("properties.setId(ResourceKey.create(Registries.BLOCK"));
		assertTrue(helper.contains("properties.setId(ResourceKey.create(Registries.ITEM"));
		String fluids = text("src/main/java/zone/moddev/mc/mineralogy/init/MineralogyFluids.java");
		assertTrue(fluids.contains("RegistrationProperties.block("));
		assertTrue(fluids.contains("RegistrationProperties.item("));
		String items = text("src/main/java/zone/moddev/mc/mineralogy/init/Items.java");
		assertTrue(items.contains("RegistrationProperties.item(new Item.Properties(), name.getPath())"));
		assertTrue(items.contains(".useBlockDescriptionPrefix()"));
	}

    @Test
    public void optionalGunpowderDustsCannotCollapseToTwoIngredients() throws Exception {
        String generator = text("scripts/generate-recipes.ps1");
        assertTrue(generator.contains("ItemTagNotEmptyCondition 'c:dusts/carbon'"));
        assertTrue(generator.contains("ItemTagNotEmptyCondition 'c:dusts/coal'"));
        assertTrue(generator.contains("type = 'neoforge:not'"));
        assertTrue(generator.contains("type = 'neoforge:tag_empty'"));
    }

    @Test
    public void neoForge261UsesNativeFluidModelRegistration() throws Exception {
        String fluid = text("src/main/java/zone/moddev/mc/mineralogy/init/MineralogyFluids.java");
        assertTrue(fluid.contains("BaseFlowingFluid.Source"));
        assertTrue(fluid.contains("BaseFlowingFluid.Flowing"));
        assertTrue(fluid.contains("LiquidBlock"));
        assertTrue(fluid.contains("MineralogyBucketItem"));
        assertTrue(fluid.contains("blocks/crude_oil_still"));
        assertTrue(fluid.contains("blocks/crude_oil_flow"));
        String bucketModel = text("src/main/resources/assets/mineralogy/models/item/crude_oil_bucket.json");
        assertTrue(bucketModel.contains("\"parent\": \"minecraft:item/generated\""));
        assertTrue(bucketModel.contains("\"layer0\": \"mineralogy:items/crude_oil_bucket\""));
        assertFalse(bucketModel.contains("forge:fluid_container"));
        String client = text("src/main/java/zone/moddev/mc/mineralogy/client/ClientSetup.java");
        assertTrue(client.contains("RegisterFluidModelsEvent"));
        assertTrue(client.contains("FluidModel.Unbaked"));
        assertTrue(client.contains("new Material(CRUDE_OIL_STILL, true)"));
        assertTrue(client.contains("new Material(CRUDE_OIL_FLOW, true)"));
        assertTrue(client.contains("event.register(model, MineralogyFluids.CRUDE_OIL,"));
        assertFalse(client.contains("ItemBlockRenderTypes.setRenderLayer"));
        for (String model : new String[] { "pane_n", "pane_ne", "pane_ns", "pane_nse", "pane_nsew",
                "rocksaltlamp", "rocksaltlamp_down", "rocksaltlamp_wall", "rocksaltstreetlamp" }) {
            assertTrue(text("src/main/resources/assets/mineralogy/models/block/" + model + ".json")
                    .contains("\"render_type\": \"cutout\""));
        }
        assertFalse(new File("src/main/java/zone/moddev/mc/mineralogy/client/ClientOilRenderer.java").exists());
    }

    @Test
    public void legacyWorldConversionPreservesIndexedChunksAndFurnaces() throws Exception {
        String hook = text("src/main/java/zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook.java");
        assertTrue(hook.contains("blocks.length != 4096"));
        assertTrue(hook.contains("MineralogyLegacyPreserveChunk"));
        assertTrue(hook.contains("TerrainPopulated"));
        assertTrue(hook.contains("LightPopulated"));
        assertTrue(hook.contains("rewriteLegacyRockFurnaceTileEntities"));
		assertTrue(hook.contains("tileEntity.putString(\"id\", ROCK_FURNACE_TILE_ENTITY)"));

        String transformer = text("src/main/java/zone/moddev/mc/mineralogy/mixin/SimpleRegionStorageMixin.java");
        assertTrue(transformer.contains("SimpleRegionStorage.class"));
        assertTrue(transformer.contains("prepareLegacyChunk"));
        assertTrue(transformer.contains("finalizeLegacyChunk"));
        assertTrue(transformer.contains("upgradeChunkTag(Lnet/minecraft/nbt/CompoundTag;I"
                + "Lnet/minecraft/nbt/CompoundTag;I)Lnet/minecraft/nbt/CompoundTag;"));
        assertTrue(hook.contains("new File(worldDirectory, \"region\")"));
        assertTrue(hook.contains("dimensions/minecraft/overworld/region"));

		String mappings = text("src/main/java/zone/moddev/mc/mineralogy/patching/PatchHandler.java");
		assertTrue(mappings.contains("GRASS_PATH"));
		assertTrue(mappings.contains("DIRT_PATH"));
		assertTrue(mappings.contains("SWEET_BERRIES_PICK"));
		assertTrue(mappings.contains("SWEET_BERRY_BUSH_PICK"));
        assertTrue(mappings.contains("DeferredRegister.create(BuiltInRegistries.SOUND_EVENT"));
		assertTrue(mappings.contains("addAlias(SWEET_BERRIES_PICK, SWEET_BERRY_BUSH_PICK)"));
		assertTrue(mappings.contains("registerAliases(IEventBus modBus)"));
    }

    @Test
    public void legacyFlatteningUsesTheExpandedArrayAndReinstallsTheSelectedWorldMapping() throws Exception {
        String hook = text("src/main/java/zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook.java");
        assertTrue(hook.contains("expandFlatteningTable(highestStateId + 1)"));
        assertTrue(hook.contains("BlockStateDataAccessor.mineralogy$getLegacyStateMap()"));
        assertTrue(hook.contains("legacyStates[stateId] = new Dynamic<>(NbtOps.INSTANCE"));
        assertTrue(hook.contains("writeLegacyBlockState(legacyState(block, meta))"));
        assertTrue(hook.contains("result.putString(\"Name\""));
        assertTrue(hook.contains("result.put(\"Properties\", properties)"));
        assertFalse(hook.contains("NbtUtils.writeBlockState"));
        assertTrue(hook.contains("captureLegacyLevelData(CompoundTag root,"));
        assertTrue(hook.contains("LevelStorageSource.LevelDirectory levelDirectory)"));
        assertTrue(hook.contains("root.getCompoundOrEmpty(\"FML\")"));
        assertTrue(hook.contains("root.getCompoundOrEmpty(\"fml\")"));
        String transformer = text("src/main/java/zone/moddev/mc/mineralogy/mixin/CommonHooksMixin.java");
        assertTrue(transformer.contains("net.neoforged.neoforge.common.CommonHooks"));
        assertTrue(transformer.contains("LevelStorageSource.LevelDirectory"));
        String tableMixin = text("src/main/java/zone/moddev/mc/mineralogy/mixin/BlockStateDataMixin.java");
        assertTrue(tableMixin.contains("Arrays.copyOf(MAP, 65_536)"));
        assertTrue(tableMixin.contains("Arrays.copyOf(BLOCK_DEFAULTS, 4_096)"));
        assertTrue(new File("src/main/resources/mineralogy.mixins.json").isFile());
        assertFalse(new File("src/main/resources/META-INF/coremods.json").exists());
        assertFalse(new File("src/main/resources/coremods/mineralogy_legacy_world_fix.js").exists());
        assertTrue(hook.contains("writeSidecar(levelDat.getParentFile(), blocks)"));
        assertTrue(hook.contains("prepareLegacyWorld(levelDat)"));

        String build = text("build.gradle");
        assertTrue(build.contains("dependsOn tasks.named('processResources')"));
        assertTrue(build.contains("from layout.buildDirectory.dir('resources/main')"));
        assertTrue(build.contains("into layout.projectDirectory.dir('bin/main')"));
        assertTrue(build.contains("data/mineralogy/orespawn/provider.json"));
        assertTrue(build.contains("Eclipse must consume only Gradle-processed production resources"));
        assertTrue(build.contains("examples/mineralogy-provider.json"));
        assertTrue(build.contains("Eclipse output is missing ${relative}"));
    }

    @Test
    public void minecraft2612UsesJava25WorldItemAndFurnaceApis() throws Exception {
        String ore = text("src/main/java/zone/moddev/mc/mineralogy/blocks/Ore.java");
        assertTrue(ore.contains("builder.getLevel().getRandom()"));

        String rock = text("src/main/java/zone/moddev/mc/mineralogy/blocks/Rock.java");
        assertTrue(rock.contains("ItemInstance toolInstance"));
        assertTrue(rock.contains("toolInstance instanceof ItemStack tool"));

        String fertilizer = text("src/main/java/zone/moddev/mc/mineralogy/items/MineralFertilizer.java");
        assertTrue(fertilizer.contains("ItemStack phantomBonemeal = new ItemStack(Items.BONE_MEAL, 27)"));

        String furnace = text("src/main/java/zone/moddev/mc/mineralogy/tileentity/TileEntityRockFurnace.java");
        assertTrue(furnace.contains("ItemStackTemplate remainder = fuel.getCraftingRemainder()"));
        assertTrue(furnace.contains("recipe.value().assemble(new SingleRecipeInput(input))"));

        String reload = text("src/main/java/zone/moddev/mc/mineralogy/mixin/ReloadableServerResourcesMixin.java");
        assertTrue(reload.contains("updateComponentsAndStaticRegistryTags"));
    }

    private static String text(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
    }
}
