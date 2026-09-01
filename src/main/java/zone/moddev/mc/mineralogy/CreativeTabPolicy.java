package zone.moddev.mc.mineralogy;

import zone.moddev.mc.mineralogy.blocks.Ore;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.blocks.RockRelief;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.blocks.RockStairs;
import zone.moddev.mc.mineralogy.blocks.RockWall;

/** Target-native five-way creative classification. */
public final class CreativeTabPolicy {
    public enum ContentGroup { ROCK, STAIR, SLAB, WALL, ITEM }
    private final boolean grouped;
    public CreativeTabPolicy(boolean grouped) { this.grouped = grouped; }
    public boolean groupTabsByType() { return grouped; }
    public ContentGroup groupFor(Class<?> type) {
        if (RockRelief.class.isAssignableFrom(type)) return ContentGroup.ITEM;
        if (RockStairs.class.isAssignableFrom(type)) return ContentGroup.STAIR;
        if (RockSlab.class.isAssignableFrom(type)) return ContentGroup.SLAB;
        if (RockWall.class.isAssignableFrom(type)) return ContentGroup.WALL;
        if (Rock.class.isAssignableFrom(type) || Ore.class.isAssignableFrom(type)) return ContentGroup.ROCK;
        return ContentGroup.ITEM;
    }
}
