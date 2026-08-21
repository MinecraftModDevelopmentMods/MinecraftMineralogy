package zone.moddev.mc.mineralogy;

import net.minecraft.init.Bootstrap;

/** Initializes vanilla's static registries for isolated Minecraft 1.10 tests. */
public final class MinecraftTestBootstrap {
    private static boolean initialized;

    private MinecraftTestBootstrap() {
    }

    public static synchronized void registerVanilla() {
        if (initialized) {
            return;
        }
        Bootstrap.register();
        initialized = true;
    }
}
