package zone.moddev.mc.mineralogy.init;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.blocks.Ore;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.blocks.RockRelief;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.blocks.RockStairs;
import zone.moddev.mc.mineralogy.blocks.RockWall;

import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class MineralogyItemGroups {
	private static CreativeModeTab mineralogy;
	private static CreativeModeTab rocks;
	private static CreativeModeTab stairs;
	private static CreativeModeTab slabs;
	private static CreativeModeTab walls;
	private static CreativeModeTab items;

	private MineralogyItemGroups() {
		throw new IllegalAccessError("Not an instantiable class");
	}

	@SubscribeEvent
	public static void registerTabs(RegisterEvent event) {
		event.register(Registries.CREATIVE_MODE_TAB, helper -> {
			if (MineralogyConfig.groupCreativeTabItemsByType()) {
				rocks = register(helper, "rock", "basalt");
				stairs = register(helper, "stair", "basalt_stairs");
				slabs = register(helper, "slab", "basalt_slab");
				walls = register(helper, "wall", "basalt_wall");
				items = register(helper, "item", "sulfur_dust");
			} else {
				mineralogy = register(helper, "mineralogy", "basalt");
			}
		});
	}

	@SubscribeEvent
	public static void buildTabContents(BuildCreativeModeTabContentsEvent event) {
		boolean grouped = MineralogyConfig.groupCreativeTabItemsByType();
		CreativeModeTab tab = event.getTab();
		if (!grouped && CreativeModeTabs.INGREDIENTS.equals(event.getTabKey())
				&& MineralogyConfig.isCreativeVisible("mineral_fertilizer")
				&& Items.mineral_fertilizer != null) {
			event.accept(Items.mineral_fertilizer);
			return;
		}

		for (Item item : ForgeRegistries.ITEMS.getValues()) {
			ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
			if (id == null || !Mineralogy.MODID.equals(id.getNamespace())) {
				continue;
			}
			if (!MineralogyConfig.isCreativeVisible(id.getPath())) {
				continue;
			}

			if (!grouped && tab == mineralogy && item != Items.mineral_fertilizer) {
				event.accept(item);
			} else if (grouped && tab == targetTab(item)) {
				event.accept(item);
			}
		}
	}

	private static CreativeModeTab targetTab(Item item) {
		if (!(item instanceof BlockItem)) {
			return items;
		}

		Block block = ((BlockItem) item).getBlock();
		if (block instanceof RockRelief) {
			return items;
		}
		if (block instanceof RockStairs) {
			return stairs;
		}
		if (block instanceof RockSlab) {
			return slabs;
		}
		if (block instanceof RockWall) {
			return walls;
		}
		if (block instanceof Rock || block instanceof Ore) {
			return rocks;
		}
		return items;
	}

	private static CreativeModeTab register(RegisterEvent.RegisterHelper<CreativeModeTab> helper, String name,
			String iconItemName) {
		CreativeModeTab tab = CreativeModeTab.builder()
				.title(Component.translatable("itemGroup." + Mineralogy.MODID + "." + name))
				.icon(() -> icon(iconItemName))
				.withSearchBar()
				.build();
		helper.register(new ResourceLocation(Mineralogy.MODID, name), tab);
		return tab;
	}

	private static ItemStack icon(String itemName) {
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Mineralogy.MODID, itemName));
		return new ItemStack(item == null ? net.minecraft.world.item.Items.IRON_PICKAXE : item);
	}
}
