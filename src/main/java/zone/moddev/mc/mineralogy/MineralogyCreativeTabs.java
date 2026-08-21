package zone.moddev.mc.mineralogy;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

/** Lazily creates the legacy single tab or the five Mineralogy 6 grouped tabs. */
final class MineralogyCreativeTabs {
    private static CreativeTabPolicy policy = CreativeTabPolicy.defaults();

    private MineralogyCreativeTabs() {
    }

    static void configure(CreativeTabPolicy configuredPolicy) {
        policy = configuredPolicy;
    }

    /**
     * Tab used by legacy constructors before central registration applies the
     * exact block classification. Keeping this lazy avoids an empty main tab
     * when grouped tabs are enabled.
     */
    static CreativeTabs constructionTab() {
        return policy.groupTabsByType() ? ItemGroup.TAB : MainGroup.TAB;
    }

    static CreativeTabs forBlock(Block block) {
        if (!policy.groupTabsByType()) {
            return MainGroup.TAB;
        }
        switch (policy.groupFor(block)) {
        case ROCK:
            return RockGroup.TAB;
        case STAIR:
            return StairGroup.TAB;
        case SLAB:
            return SlabGroup.TAB;
        case WALL:
            return WallGroup.TAB;
        case ITEM:
        default:
            return ItemGroup.TAB;
        }
    }

    static CreativeTabs forItem(CreativeTabs ungroupedTab) {
        return policy.groupTabsByType() ? ItemGroup.TAB : ungroupedTab;
    }

    private static CreativeTabs create(final String label, final String iconName) {
        CreativeTabs tab = new CreativeTabs(label) {
            @Override
            public Item getTabIconItem() {
                Item icon = Item.getByNameOrId(iconName);
                return icon == null ? Items.IRON_PICKAXE : icon;
            }

            @Override
            public boolean hasSearchBar() {
                return true;
            }
        };
        tab.setBackgroundImageName("item_search.png");
        return tab;
    }

    private static final class MainGroup {
        private static final CreativeTabs TAB = create("mineralogyTab", "minecraft:stone");
    }

    private static final class RockGroup {
        private static final CreativeTabs TAB = create("mineralogy.rock", "mineralogy:basalt");
    }

    private static final class StairGroup {
        private static final CreativeTabs TAB = create("mineralogy.stair", "mineralogy:basalt_stairs");
    }

    private static final class SlabGroup {
        private static final CreativeTabs TAB = create("mineralogy.slab", "mineralogy:basalt_slab");
    }

    private static final class WallGroup {
        private static final CreativeTabs TAB = create("mineralogy.wall", "mineralogy:basalt_wall");
    }

    private static final class ItemGroup {
        private static final CreativeTabs TAB = create("mineralogy.item", "mineralogy:sulfur_dust");
    }
}
