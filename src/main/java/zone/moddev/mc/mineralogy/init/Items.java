package zone.moddev.mc.mineralogy.init;

import java.util.ArrayList;
import java.util.List;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
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

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

@Mod.EventBusSubscriber(modid = Mineralogy.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
@ObjectHolder(Mineralogy.MODID)
public class Items {
	public static final ItemBlock basalt = null;
	public static final Item sulfur_dust = null;
	public static final Item phosphorous_dust = null;
	public static final Item nitrate_dust = null;
	public static final Item gypsum_dust = null;
	public static final Item chalk_dust = null;
	public static final Item rock_salt_dust = null;
	public static final Item salt_dust = null;
	public static final Item mineral_fertilizer = null;

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(
				createItem("sulfur_dust"),
				createItem("phosphorous_dust"),
				createItem("nitrate_dust"),
				createItem("gypsum_dust"),
				createItem("chalk_dust"),
				createItem("rock_salt_dust"),
				createItem("salt_dust"),
				createFertilizer()
		);

		List<Item> blockItems = new ArrayList<Item>();

		for (Block block : ForgeRegistries.BLOCKS.getValues()) {
			if (isMineralogyBlockItem(block)) {
				blockItems.add(createBlockItem(block));
			}
		}

		event.getRegistry().registerAll(blockItems.toArray(new Item[blockItems.size()]));
	}

	private static boolean isMineralogyBlockItem(Block block) {
		ResourceLocation registryName = block.getRegistryName();

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

	private static ItemBlock createBlockItem(Block block) {
		ResourceLocation name = block.getRegistryName();
		Item.Properties properties = new Item.Properties();
		if (name != null && MineralogyConfig.isCreativeVisible(name.getPath())) {
			properties.group(MineralogyItemGroups.forBlock(block));
		}
		if (block instanceof RockFurnace) {
			properties.maxStackSize(1);
		} else if (block instanceof RockSaltStreetLamp) {
			properties.maxStackSize(16);
		}

		ItemBlock item = new ItemBlock(block, properties);
		item.setRegistryName(block.getRegistryName());
		return item;
	}

	private static boolean isUnlitRockFurnace(Block block) {
		ResourceLocation registryName = block.getRegistryName();
		return block instanceof RockFurnace
				&& registryName != null
				&& !registryName.getPath().startsWith("lit_");
	}

	private static Item createItem(String name) {
		Item.Properties properties = new Item.Properties();
		if (MineralogyConfig.isCreativeVisible(name)) properties.group(MineralogyItemGroups.forItem());
		Item item = new Item(properties);
		item.setRegistryName(Mineralogy.MODID, name);
		return item;
	}

	private static Item createFertilizer() {
		MineralFertilizer item = new MineralFertilizer(MineralogyConfig.isCreativeVisible("mineral_fertilizer")
				? MineralogyItemGroups.forFertilizer() : null);
		item.setRegistryName(Mineralogy.MODID, "mineral_fertilizer");
		return item;
	}

	private Items() {
		throw new IllegalAccessError("Not an instantiable class");
	}
}
