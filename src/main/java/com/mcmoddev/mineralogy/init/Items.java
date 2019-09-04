package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.blocks.Rock;
import com.mcmoddev.mineralogy.data.MaterialData;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.BlockItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(Mineralogy.MODID)
public class Items {
    public static final BlockItem basalt = null;
    
    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
    	    	
    	event.getRegistry().registerAll(
// TODO: make a mineralogy tab
    			MaterialData.ANDESITE.getBlockItem(Blocks.andesite),
    			MaterialData.BASALT.getBlockItem(Blocks.basalt),
    			MaterialData.DIORITE.getBlockItem(Blocks.diorite),		
    			MaterialData.GRANITE.getBlockItem(Blocks.granite),
    			MaterialData.RHYOLITE.getBlockItem(Blocks.rhyolite),
    			MaterialData.PEGMATITE.getBlockItem(Blocks.pegmatite),
    			MaterialData.SHALE.getBlockItem(Blocks.shale),
    			MaterialData.CONGLOMERATE.getBlockItem(Blocks.conglomerate),
    			MaterialData.DOLOMITE.getBlockItem(Blocks.dolomite),
    			MaterialData.LIMESTONE.getBlockItem(Blocks.limestone),
    			MaterialData.MARBLE.getBlockItem(Blocks.marble),
    			MaterialData.SLATE.getBlockItem(Blocks.slate),
    			MaterialData.SCHIST.getBlockItem(Blocks.schist),
    			MaterialData.GNEISS.getBlockItem(Blocks.gneiss),
    			MaterialData.PHYLLITE.getBlockItem(Blocks.phyllite),
    			MaterialData.AMPHIBOLITE.getBlockItem(Blocks.amphibolite),    			
    			MaterialData.ANDESITE.getBlockItem(Blocks.andesite_smooth),
    			MaterialData.BASALT.getBlockItem(Blocks.basalt_smooth),
    			MaterialData.DIORITE.getBlockItem(Blocks.diorite_smooth),		
    			MaterialData.GRANITE.getBlockItem(Blocks.granite_smooth),
    			MaterialData.RHYOLITE.getBlockItem(Blocks.rhyolite_smooth),
    			MaterialData.PEGMATITE.getBlockItem(Blocks.pegmatite_smooth),
    			MaterialData.SHALE.getBlockItem(Blocks.shale_smooth),
    			MaterialData.CONGLOMERATE.getBlockItem(Blocks.conglomerate_smooth),
    			MaterialData.DOLOMITE.getBlockItem(Blocks.dolomite_smooth),
    			MaterialData.LIMESTONE.getBlockItem(Blocks.limestone_smooth),
    			MaterialData.MARBLE.getBlockItem(Blocks.marble_smooth),
    			MaterialData.SLATE.getBlockItem(Blocks.slate_smooth),
    			MaterialData.SCHIST.getBlockItem(Blocks.schist_smooth),
    			MaterialData.GNEISS.getBlockItem(Blocks.gneiss_smooth),
    			MaterialData.PHYLLITE.getBlockItem(Blocks.phyllite_smooth),
    			MaterialData.AMPHIBOLITE.getBlockItem(Blocks.amphibolite_smooth),
    			MaterialData.ANDESITE.getBlockItem(Blocks.andesite_stairs),
    			MaterialData.BASALT.getBlockItem(Blocks.basalt_stairs),
    			MaterialData.DIORITE.getBlockItem(Blocks.diorite_stairs),		
    			MaterialData.GRANITE.getBlockItem(Blocks.granite_stairs),
    			MaterialData.RHYOLITE.getBlockItem(Blocks.rhyolite_stairs),
    			MaterialData.PEGMATITE.getBlockItem(Blocks.pegmatite_stairs),
    			MaterialData.SHALE.getBlockItem(Blocks.shale_stairs),
    			MaterialData.CONGLOMERATE.getBlockItem(Blocks.conglomerate_stairs),
    			MaterialData.DOLOMITE.getBlockItem(Blocks.dolomite_stairs),
    			MaterialData.LIMESTONE.getBlockItem(Blocks.limestone_stairs),
    			MaterialData.MARBLE.getBlockItem(Blocks.marble_stairs),
    			MaterialData.SLATE.getBlockItem(Blocks.slate_stairs),
    			MaterialData.SCHIST.getBlockItem(Blocks.schist_stairs),
    			MaterialData.GNEISS.getBlockItem(Blocks.gneiss_stairs),
    			MaterialData.PHYLLITE.getBlockItem(Blocks.phyllite_stairs),
    			MaterialData.AMPHIBOLITE.getBlockItem(Blocks.amphibolite_stairs),    			
    			MaterialData.ANDESITE.getBlockItem(Blocks.andesite_smooth_stairs),
    			MaterialData.BASALT.getBlockItem(Blocks.basalt_smooth_stairs),
    			MaterialData.DIORITE.getBlockItem(Blocks.diorite_smooth_stairs),		
    			MaterialData.GRANITE.getBlockItem(Blocks.granite_smooth_stairs),
    			MaterialData.RHYOLITE.getBlockItem(Blocks.rhyolite_smooth_stairs),
    			MaterialData.PEGMATITE.getBlockItem(Blocks.pegmatite_smooth_stairs),
    			MaterialData.SHALE.getBlockItem(Blocks.shale_smooth_stairs),
    			MaterialData.CONGLOMERATE.getBlockItem(Blocks.conglomerate_smooth_stairs),
    			MaterialData.DOLOMITE.getBlockItem(Blocks.dolomite_smooth_stairs),
    			MaterialData.LIMESTONE.getBlockItem(Blocks.limestone_smooth_stairs),
    			MaterialData.MARBLE.getBlockItem(Blocks.marble_smooth_stairs),
    			MaterialData.SLATE.getBlockItem(Blocks.slate_smooth_stairs),
    			MaterialData.SCHIST.getBlockItem(Blocks.schist_smooth_stairs),
    			MaterialData.GNEISS.getBlockItem(Blocks.gneiss_smooth_stairs),
    			MaterialData.PHYLLITE.getBlockItem(Blocks.phyllite_smooth_stairs),
    			MaterialData.AMPHIBOLITE.getBlockItem(Blocks.amphibolite_smooth_stairs)
        );
    }
}


//package com.mcmoddev.mineralogy.init;
//
//import com.mcmoddev.mineralogy.Constants;
//import com.mcmoddev.mineralogy.Mineralogy;
//import com.mcmoddev.mineralogy.ioc.MinIoC;
//import com.mcmoddev.mineralogy.items.MineralFertilizer;
//import com.mcmoddev.mineralogy.lib.exceptions.ItemNotFoundException;
//import com.mcmoddev.mineralogy.lib.exceptions.TabNotFoundException;
//import com.mcmoddev.mineralogy.lib.interfaces.IDynamicTabProvider;
//import com.mcmoddev.mineralogy.util.RecipeHelper;
//import com.mcmoddev.mineralogy.util.RegistrationHelper;
//
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//
//public class Items {
//	private static boolean initDone = false;
//		
//	protected Items() {
//		throw new IllegalAccessError("Not a instantiable class");
//	}
//
//	/**
//	 *
//	 */
//	public static void init() {
//		if (initDone) {
//			return;
//		}
//		
//		MinIoC IoC = MinIoC.getInstance();
//		
//		Item gypsumPowder = addDust(Constants.GYPSUM);;
//		Item sulphurPowder = addDust(Constants.SULFUR);;
//		Item phosphorousPowder = addDust(Constants.PHOSPHOROUS);;
//		Item nitratePowder = addDust(Constants.NITRATE);
//		
//		Item mineralFertilizer = RegistrationHelper.registerItem(new MineralFertilizer(), "mineral_fertilizer")
//				.setTranslationKey(Mineralogy.MODID + "." + "mineral_fertilizer");
//		
//		IoC.register(Item.class, gypsumPowder, Constants.DUST_GYPSUM, Mineralogy.MODID);
//		IoC.register(Item.class, sulphurPowder, Constants.SULFUR, Mineralogy.MODID);
//		IoC.register(Item.class, phosphorousPowder, Constants.PHOSPHOROUS, Mineralogy.MODID);
//		IoC.register(Item.class, nitratePowder, Constants.NITRATE, Mineralogy.MODID);
//		IoC.register(Item.class, mineralFertilizer, Constants.FERTILIZER, Mineralogy.MODID);
//		
//		MineralogyRegistry.ItemsToRegister.put(Constants.FERTILIZER, mineralFertilizer);
//		
//		initDone = true;
//	}
//	
//	private static Item addDust(String oreDictionaryName) {
//		String dustName = oreDictionaryName.toLowerCase() + "_" + Constants.DUST;
//
//		Item item = RegistrationHelper.registerItem(new Item(), dustName).setTranslationKey(Mineralogy.MODID + "." + dustName);
//
//		try {
//			MinIoC.getInstance().resolve(IDynamicTabProvider.class).addToTab(item);
//		} catch (TabNotFoundException | ItemNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		MineralogyRegistry.ItemsToRegister.put(Constants.DUST + oreDictionaryName, item);
//		MinIoC.getInstance().register(Item.class, item, Constants.DUST + oreDictionaryName, Mineralogy.MODID);
//		
//		RecipeHelper.addShapelessOreRecipe(Constants.BLOCK.toLowerCase() + oreDictionaryName, new ItemStack(item, 9),
//				Constants.BLOCK.toLowerCase() + oreDictionaryName);
//
//		return item;
//	}
//}
