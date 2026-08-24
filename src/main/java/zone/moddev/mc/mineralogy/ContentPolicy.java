package zone.moddev.mc.mineralogy;

import net.minecraftforge.common.config.Configuration;

/**
 * Compatibility-safe switches for optional Mineralogy content.
 *
 * <p>These switches deliberately control recipes and creative visibility,
 * never registry or Ore Dictionary participation. That keeps established
 * worlds, inventories, and integrations loadable when an option is changed.</p>
 */
public final class ContentPolicy {
    public static final String CATEGORY = "options";
    public static final String ENABLE_DRYWALLS = "ENABLE_DRYWALLS";
    public static final String ENABLE_ROCK_SALT_LAMPS = "ENABLE_ROCK_SALT_LAMPS";
    public static final String ENABLE_MINERAL_DUSTS = "ENABLE_MINERAL_DUSTS";
    public static final String ENABLE_MINERAL_FERTILIZER = "ENABLE_MINERAL_FERTILIZER";

    private final boolean drywalls;
    private final boolean rockSaltLamps;
    private final boolean mineralDusts;
    private final boolean mineralFertilizer;

    public ContentPolicy(boolean drywalls, boolean rockSaltLamps,
            boolean mineralDusts, boolean mineralFertilizer) {
        this.drywalls = drywalls;
        this.rockSaltLamps = rockSaltLamps;
        this.mineralDusts = mineralDusts;
        this.mineralFertilizer = mineralFertilizer;
    }

    public static ContentPolicy defaults() {
        return new ContentPolicy(true, true, true, true);
    }

    public static ContentPolicy read(Configuration config) {
        return new ContentPolicy(
                config.getBoolean(ENABLE_DRYWALLS, CATEGORY, true,
                        "Show Mineralogy drywalls and add their crafting recipes."),
                config.getBoolean(ENABLE_ROCK_SALT_LAMPS, CATEGORY, true,
                        "Show rock salt lamps and add their crafting recipes."),
                config.getBoolean(ENABLE_MINERAL_DUSTS, CATEGORY, true,
                        "Show sulfur, phosphorous, and nitrate dust content and add its recipes."),
                config.getBoolean(ENABLE_MINERAL_FERTILIZER, CATEGORY, true,
                        "Show mineral fertilizer and add its crafting recipe."));
    }

    public boolean drywallsEnabled() {
        return drywalls;
    }

    public boolean rockSaltLampsEnabled() {
        return rockSaltLamps;
    }

    public boolean mineralDustsEnabled() {
        return mineralDusts;
    }

    public boolean mineralFertilizerEnabled() {
        return mineralFertilizer;
    }
}
