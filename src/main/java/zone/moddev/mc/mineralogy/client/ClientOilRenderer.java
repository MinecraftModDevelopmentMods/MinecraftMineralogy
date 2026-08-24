package zone.moddev.mc.mineralogy.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.IFluidState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.fluids.MineralogyFluids;

/** Supplies the custom sprites missing from Forge 25's transitional fluid renderer. */
@OnlyIn(Dist.CLIENT)
public final class ClientOilRenderer {
    private static final ResourceLocation STILL = new ResourceLocation(Mineralogy.MODID, "blocks/crude_oil_still");
    private static final ResourceLocation FLOW = new ResourceLocation(Mineralogy.MODID, "blocks/crude_oil_flow");
    private static volatile TextureAtlasSprite[] sprites;

    private ClientOilRenderer() {
    }

    public static void register() {
        MinecraftForge.EVENT_BUS.addListener(ClientOilRenderer::onTexturePre);
        MinecraftForge.EVENT_BUS.addListener(ClientOilRenderer::onTexturePost);
    }

    private static void onTexturePre(TextureStitchEvent.Pre event) {
        event.getMap().registerSprite(Minecraft.getInstance().getResourceManager(), STILL);
        event.getMap().registerSprite(Minecraft.getInstance().getResourceManager(), FLOW);
    }

    private static void onTexturePost(TextureStitchEvent.Post event) {
        sprites = new TextureAtlasSprite[] { event.getMap().getSprite(STILL), event.getMap().getSprite(FLOW) };
    }

    public static boolean useOpaqueFluidPath(IFluidState state, boolean vanillaValue) {
        return isOil(state) || vanillaValue;
    }

    public static TextureAtlasSprite[] overrideSprites(IFluidState state, TextureAtlasSprite[] vanillaSprites) {
        TextureAtlasSprite[] current = sprites;
        return isOil(state) && current != null ? current : vanillaSprites;
    }

    public static int overrideColor(IFluidState state, int vanillaColor) {
        return isOil(state) ? 0x2B2118 : vanillaColor;
    }

    private static boolean isOil(IFluidState state) {
        return state != null && MineralogyFluids.CRUDE_OIL != null
                && state.getFluid().isEquivalentTo(MineralogyFluids.CRUDE_OIL);
    }
}
