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
        assertTrue(chert.contains("} else {"));
        String rock = text("src/main/java/zone/moddev/mc/mineralogy/blocks/Rock.java");
        assertTrue(rock.contains("drops.add(new ItemStack(Blocks.COBBLESTONE));"));
        assertTrue(rock.contains("return;"));
        String slab = text("src/main/java/zone/moddev/mc/mineralogy/blocks/DoubleSlab.java");
        assertTrue(slab.contains("return 2;"));
        assertTrue(slab.contains("return new ItemStack(fullBlock);"));
        String furnace = text("src/main/java/zone/moddev/mc/mineralogy/tileentity/TileEntityRockFurnace.java");
        assertTrue(furnace.contains("public TileEntityRockFurnace()"));
        assertTrue(furnace.contains("getBlockState().getBlock()"));
        assertTrue(furnace.contains("getBurnModifier()"));
        assertTrue(furnace.contains("ItemStackHelper.loadAllItems"));
    }

    @Test
    public void cobblestonePolicyReappliesAfterReloadAndKeepsSpecialCases() throws Exception {
        String policy = text("src/main/java/zone/moddev/mc/mineralogy/compat/CobblestoneTagPolicy.java");
        assertTrue(policy.contains("addReloadListener"));
        assertTrue(policy.contains("MaterialData.allIncludingRockSalt()"));
        assertTrue(policy.contains("addBlock(blocks, \"chert\")"));
        assertTrue(policy.contains("addBlock(blocks, \"pumice\")"));
        assertTrue(policy.contains("addItem(items, \"chert\")"));
        assertTrue(policy.contains("addItem(items, \"pumice\")"));
        assertTrue(policy.contains("BlockTags.setCollection"));
        assertTrue(policy.contains("ItemTags.setCollection"));
    }

    @Test
    public void forge25OilRendererHookIsBoundedToMineralogyOil() throws Exception {
        String renderer = text("src/main/java/zone/moddev/mc/mineralogy/client/ClientOilRenderer.java");
        assertTrue(renderer.contains("state.getFluid().isEquivalentTo(MineralogyFluids.CRUDE_OIL)"));
        assertTrue(renderer.contains("blocks/crude_oil_still"));
        assertTrue(renderer.contains("blocks/crude_oil_flow"));
        String transformer = text("src/main/resources/coremods/mineralogy_legacy_leaves_fix.js");
        assertTrue(transformer.contains("mineralogy_crude_oil_renderer"));
        assertTrue(transformer.contains("BlockFluidRenderer"));
        assertTrue(transformer.contains("useOpaqueFluidPath"));
        assertTrue(transformer.contains("overrideSprites"));
        assertTrue(transformer.contains("overrideColor"));
    }

    @Test
    public void legacyWorldgenGuardProtectsIndexedExistingChunks() throws Exception {
        String hook = text("src/main/java/zone/moddev/mc/mineralogy/patching/LegacyWorldDataHook.java");
        assertTrue(hook.contains("indexLegacyChunks(worldDirectory)"));
        assertTrue(hook.contains("new byte[4096]"));
        assertTrue(hook.contains("LEGACY_MINERALOGY_CHUNKS.add(chunkKey(chunkX, chunkZ))"));
        assertTrue(hook.contains("shouldBlockWorldgenWrite"));

        String transformer = text("src/main/resources/coremods/mineralogy_legacy_leaves_fix.js");
        assertTrue(transformer.contains("previous.desc.endsWith(')Lnet/minecraft/nbt/NBTTagCompound;')"));
        assertTrue(transformer.contains("Mineralogy could not patch the Forge 25 legacy chunk loader"));
        assertTrue(transformer.contains("mineralogy_legacy_worldgen_guard"));
        assertTrue(transformer.contains("net.minecraft.world.gen.WorldGenRegion"));
        assertTrue(transformer.contains("shouldBlockWorldgenWrite"));
        assertTrue(transformer.contains("Mineralogy could not patch the Forge 25 legacy-world write guard"));
    }

    private static String text(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
    }
}
