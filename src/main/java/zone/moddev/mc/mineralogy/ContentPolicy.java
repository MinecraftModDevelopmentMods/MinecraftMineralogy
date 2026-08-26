package zone.moddev.mc.mineralogy;

/** Visibility and recipe policy for compatibility-safe optional content. */
public final class ContentPolicy {
    public static final String ENABLE_DRYWALLS = "ENABLE_DRYWALLS";
    public static final String ENABLE_ROCK_SALT_LAMPS = "ENABLE_ROCK_SALT_LAMPS";
    public static final String ENABLE_MINERAL_DUSTS = "ENABLE_MINERAL_DUSTS";
    public static final String ENABLE_MINERAL_FERTILIZER = "ENABLE_MINERAL_FERTILIZER";

    private final boolean drywalls;
    private final boolean lamps;
    private final boolean dusts;
    private final boolean fertilizer;

    public ContentPolicy(boolean drywalls, boolean lamps, boolean dusts, boolean fertilizer) {
        this.drywalls = drywalls;
        this.lamps = lamps;
        this.dusts = dusts;
        this.fertilizer = fertilizer;
    }

    public static ContentPolicy defaults() { return new ContentPolicy(true, true, true, true); }
    public boolean drywallsEnabled() { return drywalls; }
    public boolean rockSaltLampsEnabled() { return lamps; }
    public boolean mineralDustsEnabled() { return dusts; }
    public boolean mineralFertilizerEnabled() { return fertilizer; }
}
