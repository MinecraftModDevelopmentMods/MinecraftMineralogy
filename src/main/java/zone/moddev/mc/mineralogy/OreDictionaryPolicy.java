package zone.moddev.mc.mineralogy;

import net.minecraftforge.common.config.Configuration;

/**
 * Recipe-compatibility policy for Mineralogy's Ore Dictionary identities.
 */
final class OreDictionaryPolicy {
    static final String CATEGORY = "options";
    static final String COBBLESTONE_EQUIVILENT = "COBBLESTONE_EQUIVILENT";

    private final boolean cobblestoneEquivalent;

    OreDictionaryPolicy(boolean cobblestoneEquivalent) {
        this.cobblestoneEquivalent = cobblestoneEquivalent;
    }

    static OreDictionaryPolicy defaults() {
        return new OreDictionaryPolicy(true);
    }

    static OreDictionaryPolicy read(Configuration config) {
        return new OreDictionaryPolicy(config.getBoolean(
                COBBLESTONE_EQUIVILENT,
                CATEGORY,
                true,
                "Treat raw Mineralogy rock blocks as cobblestone for recipes."));
    }

    boolean cobblestoneEquivalentEnabled() {
        return cobblestoneEquivalent;
    }
}
