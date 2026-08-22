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
        String ioc = source("src/main/java/zone/moddev/mc/mineralogy/ioc/MinIoC.java");
        String mod = source("src/main/java/zone/moddev/mc/mineralogy/Mineralogy.java");
        String tab = source("src/main/java/zone/moddev/mc/mineralogy/lib/util/MMDCreativeTab.java");

        assertTrue(ioc.contains("new ItemStack(net.minecraft.init.Items.IRON_PICKAXE)"));
        assertEquals(5, countOccurrences(ioc, ".addTab(\""));
        assertTrue(ioc.contains("provider.addTab(\"Rock\", true, Mineralogy.MODID)"));
        assertTrue(ioc.contains(".addTab(\"Stair\", true, Mineralogy.MODID)"));
        assertTrue(ioc.contains(".addTab(\"Slab\", true, Mineralogy.MODID)"));
        assertTrue(ioc.contains(".addTab(\"Wall\", true, Mineralogy.MODID)"));
        assertTrue(ioc.contains(".addTab(\"Item\", true, Mineralogy.MODID)"));
        assertTrue(ioc.contains(".addTab(Mineralogy.MODID, true, Mineralogy.MODID)"));
        assertTrue(mod.contains("setBlockIcon(tabs, \"Rock\", \"basalt\")"));
        assertTrue(mod.contains("setBlockIcon(tabs, \"Stair\", \"basalt_stairs\")"));
        assertTrue(mod.contains("setBlockIcon(tabs, \"Slab\", \"basalt_slab\")"));
        assertTrue(mod.contains("setBlockIcon(tabs, \"Wall\", \"basalt_wall\")"));
        assertTrue(mod.contains("tabs.setIcon(\"Item\", new ItemStack(sulfur))"));
        assertTrue(tab.contains("setBackgroundImageName(\"item_search.png\")"));
    }

    @Test
    public void centralRegistrationAndContentVisibilityKeepTheirPrecedence() throws Exception {
        String config = source("src/main/java/zone/moddev/mc/mineralogy/MineralogyConfig.java");
        String registration = source("src/main/java/zone/moddev/mc/mineralogy/util/RegistrationHelper.java");
        String items = source("src/main/java/zone/moddev/mc/mineralogy/init/Items.java");
        int read = config.indexOf("creativeTabPolicy = CreativeTabPolicy.read(config)");
        int guardedSave = config.indexOf("if (!existing)");

        assertTrue(read >= 0);
        assertTrue(guardedSave > read);
        assertTrue(registration.contains("addToTab(MineralogyConfig.creativeTabName(block), block)"));
        assertTrue(registration.contains("MineralogyConfig.isCreativeVisible(name)"));
        assertTrue(items.contains("mineralFertilizer.setCreativeTab(CreativeTabs.MATERIALS)"));
        assertTrue(items.contains("addToTab(MineralogyConfig.itemCreativeTabName(), item)"));
        assertTrue(items.contains("item.setCreativeTab(null)"));
    }

    private static String source(String path) throws Exception {
        return new String(Files.readAllBytes(new File(path).toPath()), StandardCharsets.UTF_8);
    }

    private static int countOccurrences(String value, String search) {
        int count = 0;
        int offset = 0;
        while ((offset = value.indexOf(search, offset)) >= 0) {
            count++;
            offset += search.length();
        }
        return count;
    }
}
