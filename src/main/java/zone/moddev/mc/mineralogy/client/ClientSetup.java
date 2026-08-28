package zone.moddev.mc.mineralogy.client;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.blocks.DryWall;
import zone.moddev.mc.mineralogy.blocks.RockSaltLamp;
import zone.moddev.mc.mineralogy.blocks.RockSaltStreetLamp;
import zone.moddev.mc.mineralogy.fluids.MineralogyFluids;

import net.minecraft.world.level.block.Block;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
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
        RenderType cutout = RenderType.cutout();
        for (Block block : ForgeRegistries.BLOCKS.getValues()) {
            if (block instanceof DryWall || block instanceof RockSaltLamp || block instanceof RockSaltStreetLamp) {
                ItemBlockRenderTypes.setRenderLayer(block, cutout);
            }
        }
        ItemBlockRenderTypes.setRenderLayer(MineralogyFluids.CRUDE_OIL, RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(MineralogyFluids.FLOWING_CRUDE_OIL, RenderType.translucent());
    }
}
