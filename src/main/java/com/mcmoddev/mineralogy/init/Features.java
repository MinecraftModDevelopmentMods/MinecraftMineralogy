package com.mcmoddev.mineralogy.init;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.worldgen.StoneReplacer;

import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class Features {
	@SubscribeEvent
	public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
		event.getRegistry().register(StoneReplacer.FEATURE);
	}

	private Features() {
		throw new IllegalAccessError("Not an instantiable class");
	}
}
