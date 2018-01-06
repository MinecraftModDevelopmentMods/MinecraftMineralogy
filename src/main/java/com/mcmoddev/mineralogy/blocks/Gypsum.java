package com.mcmoddev.mineralogy.blocks;

import java.util.Random;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.ioc.MinIoC;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class Gypsum extends Rock {

	private final Random prng = new Random();
	private static final String ITEM_NAME = "gypsum";

	public Gypsum(CreativeTabs tab) {
		super(false, (float) 0.75, (float) 1, 0, SoundType.GROUND, tab);
		this.setUnlocalizedName(Mineralogy.MODID + "_" + ITEM_NAME);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
			int fortune) {
		Item dustGypsum = MinIoC.getInstance().resolve(Item.class, "dustGypsum", Mineralogy.MODID);
	
		drops.add(new ItemStack(dustGypsum, prng.nextInt(3) + 1));
		
		super.getDrops(drops, world, pos, state, fortune);
	}
}