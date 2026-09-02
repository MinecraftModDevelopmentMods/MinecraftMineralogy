package zone.moddev.mc.mineralogy.init;

import java.util.ArrayList;
import java.util.List;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.blocks.DryWall;
import zone.moddev.mc.mineralogy.blocks.Ore;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.blocks.RockRelief;
import zone.moddev.mc.mineralogy.blocks.RockSaltLamp;
import zone.moddev.mc.mineralogy.blocks.RockSaltStreetLamp;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.blocks.RockStairs;
import zone.moddev.mc.mineralogy.blocks.RockWall;
import zone.moddev.mc.mineralogy.items.MineralFertilizer;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegisterEvent;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Items {
	public static BlockItem basalt;
	public static Item sulfur_dust;
	public static Item phosphorous_dust;
	public static Item nitrate_dust;
	public static Item gypsum_dust;
	public static Item chalk_dust;
	public static Item rock_salt_dust;
	public static Item salt_dust;
	public static Item mineral_fertilizer;

	@SubscribeEvent
	public static void registerItems(RegisterEvent event) {
		if (!ForgeRegistries.Keys.ITEMS.equals(event.getRegistryKey())) {
			return;
		}
		IForgeRegistry<Item> registry = event.getForgeRegistry();

		sulfur_dust = register(registry, "sulfur_dust", createItem());
		phosphorous_dust = register(registry, "phosphorous_dust", createItem());
		nitrate_dust = register(registry, "nitrate_dust", createItem());
		gypsum_dust = register(registry, "gypsum_dust", createItem());
		chalk_dust = register(registry, "chalk_dust", createItem());
		rock_salt_dust = register(registry, "rock_salt_dust", createItem());
		salt_dust = register(registry, "salt_dust", createItem());
		mineral_fertilizer = register(registry, "mineral_fertilizer", createFertilizer());

		List<Item> blockItems = new ArrayList<Item>();

		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			if (isMineralogyBlockItem(block)) {
				blockItems.add(createBlockItem(block));
			}
		}

		for (Item item : blockItems) {
			ResourceLocation name = ForgeRegistries.BLOCKS.getKey(((BlockItem) item).getBlock());
			registry.register(name, item);
			if ("basalt".equals(name.getPath())) {
				basalt = (BlockItem) item;
			}
		}
	}

	private static boolean isMineralogyBlockItem(Block block) {
		ResourceLocation registryName = ForgeRegistries.BLOCKS.getKey(block);

		return registryName != null
				&& Mineralogy.MODID.equals(registryName.getNamespace())
				&& (block instanceof Rock
						|| block instanceof RockStairs
						|| block instanceof RockWall
						|| block instanceof RockSlab
						|| isUnlitRockFurnace(block)
						|| block instanceof RockRelief
						|| block instanceof Ore
						|| block instanceof DryWall
						|| block instanceof RockSaltLamp
						|| block instanceof RockSaltStreetLamp);
	}

	private static BlockItem createBlockItem(Block block) {
		Item.Properties properties = new Item.Properties();
		if (block instanceof RockFurnace) {
			properties.stacksTo(1);
		} else if (block instanceof RockSaltStreetLamp) {
			properties.stacksTo(16);
		}

		return new BlockItem(block, properties);
	}

	private static boolean isUnlitRockFurnace(Block block) {
		ResourceLocation registryName = ForgeRegistries.BLOCKS.getKey(block);
		return block instanceof RockFurnace
				&& registryName != null
				&& !registryName.getPath().startsWith("lit_");
	}

	private static Item createItem() {
		return new Item(new Item.Properties());
	}

	private static Item createFertilizer() {
		return new MineralFertilizer();
	}

	private static <T extends Item> T register(IForgeRegistry<Item> registry, String path, T item) {
		registry.register(ResourceLocation.fromNamespaceAndPath(Mineralogy.MODID, path), item);
		return item;
	}

	private Items() {
		throw new IllegalAccessError("Not an instantiable class");
	}
}
