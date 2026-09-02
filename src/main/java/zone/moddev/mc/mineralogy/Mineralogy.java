package zone.moddev.mc.mineralogy;

import zone.moddev.mc.mineralogy.compat.CobblestoneTagPolicy;
import zone.moddev.mc.mineralogy.documentation.DocumentationExporter;
import zone.moddev.mc.mineralogy.init.MineralogyFluids;
import zone.moddev.mc.mineralogy.migration.LegacyMineralogy6ConfigMigrator;
import zone.moddev.mc.mineralogy.migration.LegacyOreConfigMigrator;
import zone.moddev.mc.mineralogy.patching.LegacyWorldDataHook;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Mineralogy.MODID)
public class Mineralogy {
	public static Mineralogy instance;

	public static final String MODID = "mineralogy";
	public static final String NAME = "Mineralogy";
	public static final String VERSION = getVersion();

	public static final Logger LOGGER = LogManager.getLogger();

	private static String getVersion() {
		Package metadata = Mineralogy.class.getPackage();
		String version = metadata == null ? null : metadata.getImplementationVersion();
		return version == null ? "DEV" : version;
	}

	public Mineralogy(FMLJavaModLoadingContext loadingContext) {
		instance = this;
		var modEventBus = loadingContext.getModEventBus();
		MineralogyConfig.load();
		LegacyMineralogy6ConfigMigrator.migrateGlobalConfig(
				MineralogyConfig.configFile().toAbsolutePath().getParent(), LOGGER);
		LegacyOreConfigMigrator.migrate(MineralogyConfig.configFile(), LOGGER);
		MineralogyConfig.registerRecipeConditions(modEventBus);

		modEventBus.addListener(this::setup);
		MineralogyFluids.register(modEventBus);
		MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,
				LegacyWorldDataHook::onServerAboutToStart);
		MinecraftForge.EVENT_BUS.addListener(CobblestoneTagPolicy::onTagsUpdated);
	}

	private void setup(final FMLCommonSetupEvent event) {
		event.enqueueWork(DocumentationExporter::exportBundledGuide);
	}
}
