package zone.moddev.mc.mineralogy.fixture;

import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;

/** Standalone entry point for the non-shipping native recipe assertions. */
@Mod(RecipeIntegrationProbe.MODID)
public final class RecipeIntegrationProbe {
    public static final String MODID = "mineralogyrecipeprobe";

    public RecipeIntegrationProbe() {
        ServerStartedEvent.BUS.addListener(this::serverStarted);
    }

    private void serverStarted(ServerStartedEvent event) {
        boolean enabled = Boolean.parseBoolean(System.getProperty("mineralogy.recipeProbe.equivalence", "true"));
        String phase = System.getProperty("mineralogy.recipeProbe.phase", "single");
        RecipeIntegrationAssertions.verify(event, enabled, phase);
        event.getServer().halt(false);
    }
}
