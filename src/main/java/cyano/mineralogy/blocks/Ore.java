package cyano.mineralogy.blocks;

import cyano.mineralogy.Mineralogy;
import net.minecraft.block.BlockOre;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

public class Ore extends BlockOre {

	private final Item dropItem;
	private final int dropAdduct;
	private final int dropRange;

	public Ore(String name, Item oreDrop, int minNumberDropped, int maxNumberDropped, int pickLevel) {
		super();
		this.setSoundType(SoundType.STONE); // sound for stone
		this.setUnlocalizedName(Mineralogy.MODID +"_"+ name);
		this.setHardness((float)1.5); // dirt is 0.5, grass is 0.6, stone is 1.5,iron ore is 3, obsidian is 50
		this.setResistance((float)5); // dirt is 0, iron ore is 5, stone is 10, obsidian is 2000
		this.setHarvestLevel("pickaxe", pickLevel);
		this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		dropItem = oreDrop;
		dropAdduct = minNumberDropped;
		dropRange = (maxNumberDropped - minNumberDropped) + 1;
	}

	@Override
	public int getExpDrop(final IBlockState state, IBlockAccess world, final BlockPos pos, final int fortune) {
		return 0; // XP comes from smelting
	}

	@Override public int quantityDropped(Random random) {
		return random.nextInt(dropRange) + dropAdduct;
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		return quantityDroppedWithBonus(fortune, random);
	}

	@Override
	public int quantityDroppedWithBonus(int fortune, Random random) {
		return this.quantityDropped(random);
	}

	@Override public Item getItemDropped(IBlockState state, Random random, int fortune) {
		return dropItem;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}
}
