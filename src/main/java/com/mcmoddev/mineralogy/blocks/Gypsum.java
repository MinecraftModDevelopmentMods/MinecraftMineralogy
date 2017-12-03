package com.mcmoddev.mineralogy.blocks;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.mcmoddev.mineralogy.Mineralogy;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class Gypsum extends Rock {

	private final Random prng = new Random();
	private static final String ITEM_NAME = "gypsum";

	public Gypsum(CreativeTabs tab) {
		super(false, (float) 0.75, (float) 1, 0, SoundType.GROUND, tab);
		this.setUnlocalizedName(Mineralogy.MODID + "_" + ITEM_NAME);
	}

	@Deprecated
	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		return Arrays.asList(new ItemStack(Mineralogy.gypsumPowder, prng.nextInt(3) + 1));
	}
}
