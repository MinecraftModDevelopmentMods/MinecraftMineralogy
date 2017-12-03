package com.mcmoddev.mineralogy.blocks;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.mcmoddev.mineralogy.Mineralogy;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class Chert extends Rock {

	private final Random prng = new Random();
	private static final String ITEM_NAME = "chert";

	public Chert(CreativeTabs tab) {
		super(false, (float) 1.5, (float) 10, 1, SoundType.STONE, tab);
		this.setUnlocalizedName(Mineralogy.MODID + "_" + ITEM_NAME);
	}

	@Deprecated
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		if (prng.nextInt(10) == 0) {
			return Arrays.asList(new ItemStack(Items.FLINT, 1 + Math.max(0, fortune)));
		} else {
			return super.getDrops(world, pos, state, fortune);
		}
	}
}
