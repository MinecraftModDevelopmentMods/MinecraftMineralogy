package zone.moddev.mc.mineralogy;

import zone.moddev.mc.mineralogy.documentation.DocumentationExporter;
import zone.moddev.mc.mineralogy.init.MineralogyFluids;
import zone.moddev.mc.mineralogy.patching.LegacyWorldDataHook;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Mineralogy.MODID)
public class Mineralogy {
	public static Mineralogy instance;

	public static final String MODID = "mineralogy";
	public static final String NAME = "Mineralogy";
	public static final String VERSION = getVersion();

	private static String getVersion() {
		Package metadata = Mineralogy.class.getPackage();
		String version = metadata == null ? null : metadata.getImplementationVersion();
		return version == null ? "DEV" : version;
	}

	public Mineralogy() {
		instance = this;
		MineralogyConfig.register();
		MineralogyConfig.registerRecipeConditions();
		MineralogyConfig.registerAdvancementPredicates();
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
		MineralogyFluids.register(FMLJavaModLoadingContext.get().getModEventBus());
		MinecraftForge.EVENT_BUS.addListener(LegacyWorldDataHook::onServerAboutToStart);
	}

	private void setup(final FMLCommonSetupEvent event) {
		MineralogyConfig.bake();
		event.enqueueWork(DocumentationExporter::exportBundledGuide);
	}
}
