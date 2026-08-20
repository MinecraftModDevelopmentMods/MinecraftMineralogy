package zone.moddev.mc.mineralogy;

import net.minecraft.block.Block;
import net.minecraftforge.common.config.Configuration;
import zone.moddev.mc.mineralogy.blocks.Ore;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.blocks.RockRelief;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.blocks.RockStairs;
import zone.moddev.mc.mineralogy.blocks.RockWall;

/** Configuration and classification policy for Mineralogy's creative tabs. */
final class CreativeTabPolicy {
    static final String CATEGORY = "options";
    static final String GROUP_TABS_BY_TYPE = "GROUP_TABS_BY_TYPE";

    enum ContentGroup {
        ROCK,
        STAIR,
        SLAB,
        WALL,
        ITEM
    }

    private final boolean groupTabsByType;

    CreativeTabPolicy(boolean groupTabsByType) {
        this.groupTabsByType = groupTabsByType;
    }

    static CreativeTabPolicy defaults() {
        return new CreativeTabPolicy(false);
    }

    static CreativeTabPolicy read(Configuration config) {
        return new CreativeTabPolicy(config.getBoolean(
                GROUP_TABS_BY_TYPE,
                CATEGORY,
                false,
                "Split Mineralogy's creative inventory entries into tabs by content type."));
    }

    boolean groupTabsByType() {
        return groupTabsByType;
    }

    ContentGroup groupFor(Block block) {
        return groupFor(block.getClass());
    }

    ContentGroup groupFor(Class<?> blockType) {
        // Reliefs extend RockSlab on this target, so this check must stay first.
        if (RockRelief.class.isAssignableFrom(blockType)) {
            return ContentGroup.ITEM;
        }
        if (RockStairs.class.isAssignableFrom(blockType)) {
            return ContentGroup.STAIR;
        }
        if (RockSlab.class.isAssignableFrom(blockType)) {
            return ContentGroup.SLAB;
        }
        if (RockWall.class.isAssignableFrom(blockType)) {
            return ContentGroup.WALL;
        }
        if (Rock.class.isAssignableFrom(blockType) || Ore.class.isAssignableFrom(blockType)) {
            return ContentGroup.ROCK;
        }
        return ContentGroup.ITEM;
    }
}
