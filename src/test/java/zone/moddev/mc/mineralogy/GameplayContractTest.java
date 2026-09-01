package zone.moddev.mc.mineralogy;

import org.junit.Test;
import zone.moddev.mc.mineralogy.data.MaterialData;
import zone.moddev.mc.mineralogy.blocks.Ore;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.blocks.RockRelief;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.blocks.RockStairs;
import zone.moddev.mc.mineralogy.blocks.RockWall;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class GameplayContractTest {
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
        assertTrue(groups.contains("register(event, \"rock\", \"basalt\")"));
        assertTrue(groups.contains("register(event, \"stair\", \"basalt_stairs\")"));
        assertTrue(groups.contains("register(event, \"slab\", \"basalt_slab\")"));
        assertTrue(groups.contains("register(event, \"wall\", \"basalt_wall\")"));
        assertTrue(groups.contains("register(event, \"item\", \"sulfur_dust\")"));
        assertTrue(groups.contains("CreativeModeTabs.INGREDIENTS"));
        assertTrue(groups.contains(".withSearchBar()"));
        assertTrue(groups.contains("CreativeModeTabEvent.Register"));

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

		String lamp = text("src/main/java/zone/moddev/mc/mineralogy/blocks/RockSaltLamp.java");
		assertTrue(lamp.contains("facing.getAxis().isVertical()"));
		assertTrue(lamp.contains("Block.canSupportCenter(world, supportPos, facing)"));
    }

    @Test
    public void cobblestonePolicyReappliesAfterReloadAndKeepsSpecialCases() throws Exception {
        String policy = text("src/main/java/zone/moddev/mc/mineralogy/compat/CobblestoneTagPolicy.java");
        assertTrue(policy.contains("onTagsUpdated(TagsUpdatedEvent event)"));
        assertFalse(policy.contains("onServerAboutToStart"));
        assertTrue(policy.contains("event.getRegistryAccess()"));
        assertTrue(policy.contains("event.shouldUpdateStaticData()"));
        assertTrue(policy.contains("MaterialData.allIncludingRockSalt()"));
        assertTrue(policy.contains("\"stones/\" + material.id()"));
        assertTrue(policy.contains("rawRockHolders(blockRegistry"));
        assertTrue(policy.contains("rawRockHolders(itemRegistry"));
        assertTrue(policy.contains("\"chert\", \"pumice\""));
        assertTrue(policy.contains("blockRegistry.bindTags(blockTags)"));
        assertTrue(policy.contains("itemRegistry.bindTags(itemTags)"));
        assertTrue(policy.contains("STONE_CRAFTING_MATERIALS"));
        assertTrue(policy.contains("STONE_TOOL_MATERIALS"));
        assertTrue(policy.contains("TagKey.create"));
        assertFalse(policy.contains("java.lang.reflect"));
        assertTrue(policy.contains("Ingredient.invalidateAll()"));
    }

    @Test
    public void optionalGunpowderDustsCannotCollapseToTwoIngredients() throws Exception {
        String generator = text("scripts/generate-recipes.ps1");
        assertTrue(generator.contains("ItemTagNotEmptyCondition 'forge:dusts/carbon'"));
        assertTrue(generator.contains("ItemTagNotEmptyCondition 'forge:dusts/coal'"));
        assertTrue(generator.contains("type = 'forge:not'"));
        assertTrue(generator.contains("type = 'forge:tag_empty'"));
    }

    @Test
    public void forge40OilUsesNativeFlowingFluidRendering() throws Exception {
        String fluid = text("src/main/java/zone/moddev/mc/mineralogy/init/MineralogyFluids.java");
        assertTrue(fluid.contains("ForgeFlowingFluid.Source"));
        assertTrue(fluid.contains("ForgeFlowingFluid.Flowing"));
        assertTrue(fluid.contains("LiquidBlock"));
        assertTrue(fluid.contains("MineralogyBucketItem"));
        assertTrue(fluid.contains("blocks/crude_oil_still"));
        assertTrue(fluid.contains("blocks/crude_oil_flow"));
        String client = text("src/main/java/zone/moddev/mc/mineralogy/client/ClientSetup.java");
        assertTrue(client.contains("ItemBlockRenderTypes.setRenderLayer(MineralogyFluids.CRUDE_OIL.get()"));
        assertTrue(client.contains("RenderType.translucent()"));
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

        String transformer = text("src/main/resources/coremods/mineralogy_legacy_world_fix.js");
        assertTrue(transformer.contains("mineralogy_legacy_chunk_data"));
        assertTrue(transformer.contains("net.minecraft.world.level.chunk.storage.ChunkStorage"));
        assertTrue(transformer.contains("prepareLegacyChunk"));
        assertTrue(transformer.contains("finalizeLegacyChunk"));

		String mappings = text("src/main/java/zone/moddev/mc/mineralogy/patching/PatchHandler.java");
		assertTrue(mappings.contains("GRASS_PATH"));
		assertTrue(mappings.contains("DIRT_PATH"));
		assertTrue(mappings.contains("SWEET_BERRIES_PICK"));
		assertTrue(mappings.contains("SWEET_BERRY_BUSH_PICK"));
		assertTrue(mappings.contains("ForgeRegistries.SOUND_EVENTS"));
		assertTrue(mappings.contains("@Mod.EventBusSubscriber(modid = Mineralogy.MODID)"));
		assertTrue(mappings.contains("event.getMappings(ForgeRegistries.Keys.BLOCKS"));
    }

    @Test
    public void legacyFlatteningUsesTheExpandedArrayAndReinstallsTheSelectedWorldMapping() throws Exception {
        String hook = text("src/main/java/zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook.java");
        assertTrue(hook.contains("expandFlatteningTable(highestStateId + 1)"));
        assertTrue(hook.contains("type.getComponentType() != Dynamic.class"));
        assertTrue(hook.contains("legacyStates[stateId] = BlockStateData.parse(stateNbt)"));
        assertTrue(hook.contains("unsafe.putObjectVolatile(base, offset, expanded)"));
        assertTrue(hook.contains("captureLegacyLevelData(CompoundTag root, Path levelPath)"));
        assertTrue(hook.contains("writeSidecar(levelDat.getParentFile(), blocks)"));
        assertTrue(hook.contains("prepareLegacyWorld(levelDat)"));

        String build = text("build.gradle");
        assertTrue(build.contains("dependsOn tasks.named('processResources')"));
        assertTrue(build.contains("mineralogy%%${mainOutput}"));
        assertTrue(build.contains("from processedResources"));
        assertTrue(build.contains("data/mineralogy/orespawn/provider.json"));
        assertTrue(build.contains("Eclipse output contains stale processed production resources"));
        assertTrue(build.contains("examples/mineralogy-provider.json"));
        assertTrue(build.contains("Eclipse output is missing bundled documentation"));
    }

    private static String text(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
    }
}
