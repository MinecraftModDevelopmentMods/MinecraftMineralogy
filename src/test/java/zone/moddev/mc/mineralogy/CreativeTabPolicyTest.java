package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.junit.Test;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import zone.moddev.mc.mineralogy.CreativeTabPolicy.ContentGroup;
import zone.moddev.mc.mineralogy.blocks.Ore;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.blocks.RockRelief;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.blocks.RockStairs;
import zone.moddev.mc.mineralogy.blocks.RockWall;

public class CreativeTabPolicyTest {
    @Test
    public void groupingDefaultsToDisabled() {
        assertFalse(CreativeTabPolicy.defaults().groupTabsByType());
    }

    @Test
    public void historicalConfigurationCanEnableGroupingWithoutSaving() {
        Configuration config = mock(Configuration.class);
        when(config.getBoolean(eq(CreativeTabPolicy.GROUP_TABS_BY_TYPE),
                eq(CreativeTabPolicy.CATEGORY), eq(false), anyString())).thenReturn(true);

        assertTrue(CreativeTabPolicy.read(config).groupTabsByType());
        verify(config).getBoolean(eq("GROUP_TABS_BY_TYPE"), eq("options"),
                eq(false), anyString());
        verify(config, never()).save();
    }

    @Test
    public void blockFamiliesUseTheFiveMineralogySixGroups() {
        CreativeTabPolicy policy = new CreativeTabPolicy(true);

        assertEquals(ContentGroup.ROCK, policy.groupFor(Rock.class));
        assertEquals(ContentGroup.ROCK, policy.groupFor(Ore.class));
        assertEquals(ContentGroup.STAIR, policy.groupFor(RockStairs.class));
        assertEquals(ContentGroup.SLAB, policy.groupFor(RockSlab.class));
        assertEquals(ContentGroup.WALL, policy.groupFor(RockWall.class));
        assertEquals(ContentGroup.ITEM, policy.groupFor(RockFurnace.class));
        assertEquals(ContentGroup.ITEM, policy.groupFor(Block.class));
    }

    @Test
    public void reliefsUseItemsRatherThanTheirSlabSuperclass() {
        assertEquals(ContentGroup.ITEM,
                new CreativeTabPolicy(true).groupFor(RockRelief.class));
    }

    @Test
    public void tabCreationIsLazyAndUsesStableLabelsAndIcons() throws Exception {
        String source = source("src/main/java/zone/moddev/mc/mineralogy/MineralogyCreativeTabs.java");

        assertTrue(source.contains("policy.groupTabsByType() ? ItemGroup.TAB : MainGroup.TAB"));
        assertTrue(source.contains("create(\"mineralogy.rock\", \"mineralogy:basalt\")"));
        assertTrue(source.contains("create(\"mineralogy.stair\", \"mineralogy:basalt_stairs\")"));
        assertTrue(source.contains("create(\"mineralogy.slab\", \"mineralogy:basalt_slab\")"));
        assertTrue(source.contains("create(\"mineralogy.wall\", \"mineralogy:basalt_wall\")"));
        assertTrue(source.contains("create(\"mineralogy.item\", \"mineralogy:sulfur_dust\")"));
        assertTrue(source.contains("return icon == null ? Items.IRON_PICKAXE : icon"));
        assertTrue(source.contains("tab.setBackgroundImageName(\"item_search.png\")"));
    }

    @Test
    public void centralRegistrationAndContentVisibilityKeepTheirPrecedence() throws Exception {
        String source = source("src/main/java/zone/moddev/mc/mineralogy/Mineralogy.java");
        int read = source.indexOf("creativeTabPolicy = CreativeTabPolicy.read(config)");
        int fluids = source.indexOf("MineralogyFluids.register();");
        int guardedSave = source.indexOf("if (!configWasPresent)");

        assertTrue(read >= 0);
        assertTrue(fluids > read);
        assertTrue(guardedSave > read);
        assertTrue(source.contains("b.setCreativeTab(MineralogyCreativeTabs.forBlock(b))"));
        assertTrue(source.contains("itemBlock.setCreativeTab(MineralogyCreativeTabs.forBlock(b))"));
        assertTrue(source.contains("MineralogyCreativeTabs.forItem(CreativeTabs.MATERIALS)"));
        assertTrue(source.contains("item.setCreativeTab(null)"));
    }

    private static String source(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
    }
}
