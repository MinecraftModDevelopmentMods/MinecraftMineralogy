package zone.moddev.mc.mineralogy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.junit.BeforeClass;
import org.junit.Test;
import zone.moddev.mc.mineralogy.patching.PatchHandler;

public class LegacyPatchContractTest {
    @BeforeClass
    public static void bootstrapMinecraft() {
        MinecraftTestBootstrap.registerVanilla();
    }

    @Test
    public void legacyPatchBlocksRetainHistoricalRegistryNames() throws Exception {
        Method factory = PatchHandler.class.getDeclaredMethod(
                "legacyBlock", String.class, net.minecraft.block.state.IBlockState.class);
        factory.setAccessible(true);

        Block saprolite = (Block) factory.invoke(null, "saprolite", Blocks.STONE.getDefaultState());
        Block pummice = (Block) factory.invoke(null, "pummice", Blocks.STONE.getDefaultState());

        assertNotNull(saprolite.getRegistryName());
        assertNotNull(pummice.getRegistryName());
        assertEquals(new ResourceLocation("mineralogy", "saprolite"), saprolite.getRegistryName());
        assertEquals(new ResourceLocation("mineralogy", "pummice"), pummice.getRegistryName());
    }
}
