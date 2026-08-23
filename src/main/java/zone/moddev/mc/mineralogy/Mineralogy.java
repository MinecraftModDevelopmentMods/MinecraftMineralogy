package zone.moddev.mc.mineralogy;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import zone.moddev.mc.mineralogy.documentation.DocumentationExporter;
import zone.moddev.mc.mineralogy.fluids.MineralogyFluids;
import zone.moddev.mc.mineralogy.init.MineralogyRegistry;
import zone.moddev.mc.mineralogy.ioc.MinIoC;
import zone.moddev.mc.mineralogy.lib.exceptions.TabNotFoundException;
import zone.moddev.mc.mineralogy.lib.interfaces.IDynamicTabProvider;
import zone.moddev.mc.mineralogy.migration.LegacyOreConfigMigrator;
import zone.moddev.mc.mineralogy.patching.PatchHandler;
import zone.moddev.mc.mineralogy.util.BlockItemPair;

@Mod(
        modid = Mineralogy.MODID,
        name = Mineralogy.NAME,
        version = Mineralogy.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies = "required-after:orespawn@[4.0.7,5.0.0)")
public class Mineralogy {
    public static final String MODID = "mineralogy";
    public static final String NAME = "Mineralogy";
    public static final String VERSION = "6.0.1.112021";
    public static final Logger LOGGER = LogManager.getFormatterLogger(MODID);

    @Instance
    public static Mineralogy instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        File configFile = event.getSuggestedConfigurationFile();
        if (configFile.isFile()) {
            LegacyOreConfigMigrator.migrate(configFile, LOGGER);
        }

        MineralogyConfig.preInit(event);
        MinIoC.getInstance();

        zone.moddev.mc.mineralogy.init.Blocks.init();
        zone.moddev.mc.mineralogy.init.Items.init();
        zone.moddev.mc.mineralogy.init.Ores.Init();
        PatchHandler.getInstance().init(MineralogyConfig.patchUpdate());
        MineralogyFluids.register();
        DocumentationExporter.exportBundledGuide();

        if (event.getSide().isClient()) {
            MineralogyFluids.registerClientModels();
        }
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        if (MineralogyConfig.smeltableGravel()) {
            GameRegistry.addSmelting(Blocks.GRAVEL, new ItemStack(Blocks.STONE), 0.1F);
        }
        if (event.getSide().isClient()) {
            registerItemRenders();
        }
    }

    private void registerItemRenders() {
        for (String name : MineralogyRegistry.MineralogyItemRegistry.keySet()) {
            Item item = MineralogyRegistry.MineralogyItemRegistry.get(name);
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0,
                    new ModelResourceLocation(MODID + ":" + name, "inventory"));
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        IDynamicTabProvider tabs = MinIoC.getInstance().resolve(IDynamicTabProvider.class);
        if (tabs == null) {
            return;
        }
        try {
            tabs.setTabIcons();
            if (MineralogyConfig.groupCreativeTabItemsByType()) {
                setBlockIcon(tabs, "Rock", "basalt");
                setBlockIcon(tabs, "Stair", "basalt_stairs");
                setBlockIcon(tabs, "Slab", "basalt_slab");
                setBlockIcon(tabs, "Wall", "basalt_wall");
                Item sulfur = MineralogyRegistry.MineralogyItemRegistry.get(Constants.SULFUR);
                if (sulfur != null) {
                    tabs.setIcon("Item", new ItemStack(sulfur));
                }
            } else {
                setBlockIcon(tabs, MODID, "basalt");
            }
        } catch (TabNotFoundException ex) {
            LOGGER.warn("Unable to configure Mineralogy creative-tab icons", ex);
        }
    }

    private static void setBlockIcon(IDynamicTabProvider tabs, String tab, String name)
            throws TabNotFoundException {
        BlockItemPair pair = MineralogyRegistry.MineralogyBlockRegistry.get(name);
        if (pair != null && pair.PairedItem != null) {
            tabs.setIcon(tab, new ItemStack(pair.PairedItem));
        }
    }
}
