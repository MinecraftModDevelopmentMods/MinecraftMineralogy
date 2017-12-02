package cyano.mineralogy.blocks;

import java.util.Random;

import cyano.mineralogy.Mineralogy;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

public class DryWall extends net.minecraft.block.BlockPane {

	private static final String ITEM_NAME = "drywall";

	public DryWall(String color) {
		super(Material.ROCK, true);
		this.setUnlocalizedName(Mineralogy.MODID + "_" + ITEM_NAME + " _" + color);
		this.useNeighborBrightness = true;
	}

	@Override
	public Item getItemDropped(IBlockState bs, Random prng, int enchantmentLevel) {
		return Item.getItemFromBlock(this);
	}
}
