package zone.moddev.mc.mineralogy.blocks;

import java.util.Random;

import zone.moddev.mc.mineralogy.Mineralogy;

import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;

public class Ore extends BlockOre {
	private final String dropItemName;
	private final int dropAdduct;
	private final int dropRange;
	private final int pickLevel;

	public Ore(String name, String dropItemName, int minNumberDropped, int maxNumberDropped, int pickLevel) {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F, 5.0F).sound(SoundType.STONE));
		this.setRegistryName(Mineralogy.MODID, name);

		this.dropItemName = dropItemName;
		this.dropAdduct = minNumberDropped;
		this.dropRange = (maxNumberDropped - minNumberDropped) + 1;
		this.pickLevel = pickLevel;
	}

	@Override
	public int getExpDrop(IBlockState state, IWorldReader world, BlockPos pos, int fortune) {
		return 0;
	}

	@Override
	public int quantityDropped(IBlockState state, Random random) {
		return random.nextInt(dropRange) + dropAdduct;
	}

	@Override
	public int getItemsToDropCount(IBlockState state, int fortune, World world, BlockPos pos, Random random) {
		return quantityDropped(state, random);
	}

	@Override
	public IItemProvider getItemDropped(IBlockState state, World world, BlockPos pos, int fortune) {
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Mineralogy.MODID, dropItemName));
		return item == null ? this : item;
	}

	@Override
	public ToolType getHarvestTool(IBlockState state) {
		return ToolType.PICKAXE;
	}

	@Override
	public int getHarvestLevel(IBlockState state) {
		return pickLevel;
	}
}
