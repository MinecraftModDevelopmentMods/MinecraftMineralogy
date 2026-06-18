package com.mcmoddev.mineralogy.client;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.blocks.DryWall;
import com.mcmoddev.mineralogy.blocks.RockSaltLamp;
import com.mcmoddev.mineralogy.blocks.RockSaltStreetLamp;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {
	private ClientSetup() {
	}

	@SubscribeEvent
	public static void clientSetup(FMLClientSetupEvent event) {
		RenderType cutout = RenderType.getCutout();
		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			if (block instanceof DryWall || block instanceof RockSaltLamp || block instanceof RockSaltStreetLamp) {
				RenderTypeLookup.setRenderLayer(block, cutout);
			}
		}
	}
}
