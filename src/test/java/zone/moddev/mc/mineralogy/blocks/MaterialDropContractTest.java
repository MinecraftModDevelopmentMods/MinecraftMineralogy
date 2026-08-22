package zone.moddev.mc.mineralogy.blocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import zone.moddev.mc.mineralogy.MinecraftTestBootstrap;
import zone.moddev.mc.mineralogy.data.MaterialData;

public class MaterialDropContractTest {
    @BeforeClass
    public static void registerVanilla() {
        MinecraftTestBootstrap.registerVanilla();
    }

    @Test
    public void novaculiteUsesMineralogySixMaterialProperties() {
        assertEquals(3.0D, MaterialData.NOVACULITE.hardness, 0.0D);
        assertEquals(15.0D, MaterialData.NOVACULITE.blastResistance, 0.0D);
        assertEquals(1, MaterialData.NOVACULITE.toolHardnessLevel);
    }

    @Test
    public void normalDoubleSlabHarvestAlwaysReturnsTwoMatchingSlabs() {
        DoubleSlab doubleSlab = doubleSlab(Blocks.STONE_SLAB, Blocks.STONE);

        assertDrop(doubleSlab.getDrops(null, BlockPos.ORIGIN,
                doubleSlab.getDefaultState(), 0), Blocks.STONE_SLAB, 2);
        assertDrop(doubleSlab.getDrops(null, BlockPos.ORIGIN,
                doubleSlab.getDefaultState(), 3), Blocks.STONE_SLAB, 2);
    }

    @Test
    public void silkTouchReturnsOneMatchingFullBlockAndPickBlockReturnsOneSlab() {
        DoubleSlab doubleSlab = doubleSlab(Blocks.STONE_SLAB, Blocks.STONE);

        assertTrue(doubleSlab.canSilkHarvest());
        assertStack(doubleSlab.getSilkTouchDrop(doubleSlab.getDefaultState()),
                Blocks.STONE, 1);
        assertStack(doubleSlab.getItem(null, BlockPos.ORIGIN,
                doubleSlab.getDefaultState()), Blocks.STONE_SLAB, 1);
    }

    @Test
    public void compatibilityConstructorKeepsAFullBlocksWorthOfSlabDrops() {
        DoubleSlab doubleSlab = new DoubleSlab(1.5F, 10.0F, 0,
                SoundType.STONE, Blocks.STONE_SLAB);

        assertStack(doubleSlab.getSilkTouchDrop(doubleSlab.getDefaultState()),
                Blocks.STONE_SLAB, 2);
    }

    @Test
    public void everyFinishSuppliesItsExactFullBlockToTheDoubleSlab() throws Exception {
        String source = mineralogySource();

        assertTrue(source.contains("rockSlabPair.PairedBlock, rockPair.PairedBlock"));
        assertTrue(source.contains("brickSlabPair.PairedBlock, brickPair.PairedBlock"));
        assertTrue(source.contains("smoothSlabPair.PairedBlock, smoothPair.PairedBlock"));
        assertTrue(source.contains("smoothBrickSlabPair.PairedBlock, smoothBrickPair.PairedBlock"));
        assertTrue(source.contains("addStoneType(MaterialData.ROCK_SALT"));
    }

    @Test
    public void chertAndDropCobblestoneRemainReplacementPolicies() throws Exception {
        String chert = source("src/main/java/zone/moddev/mc/mineralogy/blocks/Chert.java");
        String rock = source("src/main/java/zone/moddev/mc/mineralogy/blocks/Rock.java");

        assertTrue(chert.contains("drops.add(new ItemStack(Items.FLINT"));
        assertTrue(chert.contains("} else {\n            super.getDrops"));
        assertTrue(rock.contains("} else {\n            super.getDrops"));
    }

    private static DoubleSlab doubleSlab(Block slab, Block fullBlock) {
        return new DoubleSlab(1.5F, 10.0F, 0, SoundType.STONE, slab, fullBlock);
    }

    private static void assertDrop(List<ItemStack> drops, Block block, int count) {
        assertEquals(1, drops.size());
        assertStack(drops.get(0), block, count);
    }

    private static void assertStack(ItemStack stack, Block block, int count) {
        assertSame(Item.getItemFromBlock(block), stack.getItem());
        assertEquals(count, stack.getCount());
    }

    private static String mineralogySource() throws Exception {
        return source("src/main/java/zone/moddev/mc/mineralogy/init/Blocks.java");
    }

    private static String source(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()),
                StandardCharsets.UTF_8);
    }
}
