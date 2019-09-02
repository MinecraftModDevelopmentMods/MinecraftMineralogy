package com.mcmoddev.mineralogy;

// DON'T FORGET TO UPDATE mcmod.info FILE!!!

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.mcmoddev.mineralogy.init.MineralogyRegistry;
//import com.mcmoddev.mineralogy.ioc.MinIoC;
//import com.mcmoddev.mineralogy.lib.exceptions.TabNotFoundException;
//import com.mcmoddev.mineralogy.lib.interfaces.IDynamicTabProvider;
//import com.mcmoddev.mineralogy.worldgen.StoneReplacer;

//import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
//import net.minecraft.client.renderer.block.model.ModelResourceLocation;
//import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
//import net.minecraftforge.common.MinecraftForge;
//import net.minecraftforge.fml.common.Mod;
//import net.minecraftforge.fml.common.Mod.EventHandler;
//import net.minecraftforge.fml.common.Mod.Instance;
//import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
//import net.minecraftforge.fml.common.event.FMLInitializationEvent;
//import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
//import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.stream.Collectors;

//@Mod(
//		modid = Mineralogy.MODID,
//		name = Mineralogy.NAME,
//		version = Mineralogy.VERSION,
//		acceptedMinecraftVersions = "[1.12,)",
//		certificateFingerprint = "@FINGERPRINT@")
@Mod(Mineralogy.MODID)
public class Mineralogy {

	//@Instance
	public static Mineralogy instance;

	/** ID of this Mod */
	public static final String MODID = "mineralogy";

	/** Display name of this Mod */
	public static final String NAME = "Mineralogy";

	/**
	 * Version number, in Major.Minor.Patch format. The minor number is
	 * increased whenever a change is made that has the potential to break
	 * compatibility with other mods that depend on this one.
	 */
	public static final String VERSION = "4.0.0.0";

	//public static final Logger logger = LogManager.getFormatterLogger(Mineralogy.MODID);
	private static final Logger LOGGER = LogManager.getLogger();
	
	//@Mod.EventHandler
//	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {
//		logger.warn("Invalid fingerprint detected!");
//	}
	// new stuff
	public Mineralogy() {
        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

	private void setup(final FMLCommonSetupEvent event)
    {
        // example preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());
        //
        
        // original preinit code
//        MinecraftForge.EVENT_BUS.register(new MineralogyEventBusSubscriber());
//		MinecraftForge.EVENT_BUS.register(com.mcmoddev.mineralogy.init.Blocks.class);
//		MinecraftForge.EVENT_BUS.register(com.mcmoddev.mineralogy.init.Items.class);
//		MinecraftForge.EVENT_BUS.register(com.mcmoddev.mineralogy.init.Ores.class);
//		MinecraftForge.EVENT_BUS.register(com.mcmoddev.mineralogy.init.Recipes.class);
//		
//		MineralogyConfig.preInit(event);
//
//		com.mcmoddev.mineralogy.init.Blocks.init();
//		com.mcmoddev.mineralogy.init.Items.init();
//		com.mcmoddev.mineralogy.init.Ores.Init();
//		com.mcmoddev.mineralogy.init.Recipes.Init();
        //
 
        
        // original init code
//        if (MineralogyConfig.smeltableGravel())
//			GameRegistry.addSmelting(Blocks.GRAVEL, new ItemStack(Blocks.STONE), 0.1F);
//
//		GameRegistry.registerWorldGenerator(new StoneReplacer(), 10); // register custom chunk generation
//
//		// register renderers
//		if (event.getSide().isClient()) {
//			registerItemRenders();
//		}
        //
        
        
        // original post init
		// process black-lists and white-lists
//		for (String id : MineralogyConfig.igneousWhitelist()) {
//			Block b = getBlock(id);
//			if (b == null)
//				continue;
//			MineralogyRegistry.igneousStones.add(b);
//		}
//		for (String id : MineralogyConfig.metamorphicWhitelist()) {
//			Block b = getBlock(id);
//			if (b == null)
//				continue;
//			MineralogyRegistry.metamorphicStones.add(b);
//		}
//		for (String id : MineralogyConfig.sedimentaryWhitelist()) {
//			Block b = getBlock(id);
//			if (b == null)
//				continue;
//			MineralogyRegistry.sedimentaryStones.add(b);
//		}
//		for (String id : MineralogyConfig.igneousBlacklist()) {
//			Block b = getBlock(id);
//			if (b == null)
//				continue;
//			MineralogyRegistry.igneousStones.remove(b);
//		}
//		for (String id : MineralogyConfig.metamorphicBlacklist()) {
//			Block b = getBlock(id);
//			if (b == null)
//				continue;
//			MineralogyRegistry.metamorphicStones.remove(b);
//		}
//		for (String id : MineralogyConfig.sedimentaryBlacklist()) {
//			Block b = getBlock(id);
//			if (b == null)
//				continue;
//			MineralogyRegistry.sedimentaryStones.remove(b);
//		}
//		
//		MinIoC IoC = MinIoC.getInstance();
//		ItemStack sulphurStack = new ItemStack(IoC.resolve(Item.class, Constants.SULFUR, Mineralogy.MODID));
//		
//		try {
//			IDynamicTabProvider tabProvider = IoC.resolve(IDynamicTabProvider.class);
//			
//			tabProvider.setTabIcons();
//			
//			if (MineralogyConfig.groupCreativeTabItemsByType())
//				tabProvider.setIcon("Item", sulphurStack);
//			
//		} catch (TabNotFoundException e) {
//			e.printStackTrace();
//		}
        //
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
	
    // end new stuff
    


//	private void registerItemRenders() {
//		for (String name : MineralogyRegistry.MineralogyItemRegistry.keySet()) {
//			Item i = MineralogyRegistry.MineralogyItemRegistry.get(name);
//			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(i, 0,
//					new ModelResourceLocation(Mineralogy.MODID + ":" + name, "inventory"));
//		}
//	}

//	private static Block getBlock(String id) {
//		return Block.getBlockFromName(id);
//	}	
}