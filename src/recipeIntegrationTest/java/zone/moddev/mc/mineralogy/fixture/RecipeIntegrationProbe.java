package zone.moddev.mc.mineralogy.fixture;

import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.fml.common.Mod;

/** Standalone entry point for the non-shipping native recipe assertions. */
@Mod(RecipeIntegrationProbe.MODID)
public final class RecipeIntegrationProbe {
    public static final String MODID = "mineralogyrecipeprobe";

    public RecipeIntegrationProbe() {
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(this::serverStarted);
    }

    private void serverStarted(ServerStartedEvent event) {
        boolean enabled = Boolean.parseBoolean(System.getProperty("mineralogy.recipeProbe.equivalence", "true"));
        String phase = System.getProperty("mineralogy.recipeProbe.phase", "single");
        RecipeIntegrationAssertions.verify(event, enabled, phase);
        event.getServer().halt(false);
    }
}
