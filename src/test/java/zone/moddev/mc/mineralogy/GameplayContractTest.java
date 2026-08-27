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
        assertTrue(groups.contains("create(\"rock\", \"basalt\")"));
        assertTrue(groups.contains("create(\"stair\", \"basalt_stairs\")"));
        assertTrue(groups.contains("create(\"slab\", \"basalt_slab\")"));
        assertTrue(groups.contains("create(\"wall\", \"basalt_wall\")"));
        assertTrue(groups.contains("create(\"item\", \"sulfur_dust\")"));
        assertTrue(groups.contains("ItemGroup.MATERIALS"));

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
        assertTrue(furnace.contains("public TileEntityRockFurnace()"));
        assertTrue(furnace.contains("BlockState state = getBlockState()"));
        assertTrue(furnace.contains("Block block = state.getBlock()"));
        assertTrue(furnace.contains("getBurnModifier()"));
        assertTrue(furnace.contains("ItemStackHelper.loadAllItems"));

		String lamp = text("src/main/java/zone/moddev/mc/mineralogy/blocks/RockSaltLamp.java");
		assertTrue(lamp.contains("facing.getAxis().isVertical()"));
		assertTrue(lamp.contains("Block.hasEnoughSolidSide(world, supportPos, facing)"));
    }

    @Test
    public void cobblestonePolicyReappliesAfterReloadAndKeepsSpecialCases() throws Exception {
        String policy = text("src/main/java/zone/moddev/mc/mineralogy/compat/CobblestoneTagPolicy.java");
        assertTrue(policy.contains("onTagsUpdated(TagsUpdatedEvent event)"));
        assertFalse(policy.contains("onServerAboutToStart"));
        assertTrue(policy.contains("event.getTagManager()"));
        assertTrue(policy.contains("MaterialData.allIncludingRockSalt()"));
        assertTrue(policy.contains("addBlock(blocks, \"chert\")"));
        assertTrue(policy.contains("addBlock(blocks, \"pumice\")"));
        assertTrue(policy.contains("addItem(items, \"chert\")"));
        assertTrue(policy.contains("addItem(items, \"pumice\")"));
        assertTrue(policy.contains("replaceElementsInPlace(required(blockTags, COBBLESTONE), blocks)"));
        assertTrue(policy.contains("replaceElementsInPlace(required(itemTags, COBBLESTONE), items)"));
        assertTrue(policy.contains("STONE_CRAFTING_MATERIALS"));
        assertTrue(policy.contains("STONE_TOOL_MATERIALS"));
        assertTrue(policy.contains("setRetainedField"));
        assertFalse(policy.contains("TagRegistryManager.fetchTags"));
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
    public void forge36OilUsesNativeFlowingFluidRendering() throws Exception {
        String fluid = text("src/main/java/zone/moddev/mc/mineralogy/fluids/MineralogyFluids.java");
        assertTrue(fluid.contains("ForgeFlowingFluid.Source"));
        assertTrue(fluid.contains("ForgeFlowingFluid.Flowing"));
        assertTrue(fluid.contains("FlowingFluidBlock"));
        assertTrue(fluid.contains("BucketItem"));
        assertTrue(fluid.contains("blocks/crude_oil_still"));
        assertTrue(fluid.contains("blocks/crude_oil_flow"));
        String client = text("src/main/java/zone/moddev/mc/mineralogy/client/ClientSetup.java");
        assertTrue(client.contains("RenderTypeLookup.setRenderLayer(MineralogyFluids.CRUDE_OIL"));
        assertTrue(client.contains("RenderType.getTranslucent()"));
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
		assertTrue(hook.contains("normalizeLegacyTileEntityIds(level)"));
		assertTrue(new File("src/main/java/zone/moddev/mc/mineralogy/patching/LegacyTileEntityIdNormalizer.java").exists());

        String transformer = text("src/main/resources/coremods/mineralogy_legacy_world_fix.js");
        assertTrue(transformer.contains("mineralogy_legacy_chunk_data"));
        assertTrue(transformer.contains("net.minecraft.world.chunk.storage.ChunkLoader"));
        assertTrue(transformer.contains("prepareLegacyChunk"));
        assertTrue(transformer.contains("finalizeLegacyChunk"));
    }

    @Test
    public void legacyFlatteningUsesTheExpandedArrayAndReinstallsTheSelectedWorldMapping() throws Exception {
        String hook = text("src/main/java/zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook.java");
        assertTrue(hook.contains("expandFlatteningTable(highestStateId + 1)"));
        assertTrue(hook.contains("valuesField.getType().getComponentType() != Dynamic.class"));
        assertTrue(hook.contains("addEntry.invoke"));
        assertTrue(hook.contains("WorldPersistenceHooks.addHook"));
        assertTrue(hook.contains("writeSidecar(levelDat.getParentFile(), blocks)"));
        assertTrue(hook.contains("install(levelDat.getParentFile(), CompressedStreamTools.readCompressed(input)"));

        String build = text("build.gradle");
        assertTrue(build.contains("dependsOn tasks.named('processResources')"));
        assertTrue(build.contains("mineralogy%%${mainOutput}${File.pathSeparator}"));
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
