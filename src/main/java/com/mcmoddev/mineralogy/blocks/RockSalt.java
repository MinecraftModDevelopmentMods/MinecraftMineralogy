package com.mcmoddev.mineralogy.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.mcmoddev.mineralogy.Mineralogy;
import com.mcmoddev.mineralogy.ioc.MinIoC;

public class RockSalt extends Rock {
	private final String itemName = "rock_salt";
	public RockSalt() {
		super(false,(float)1.5, (float)10, 0, SoundType.STONE);
		this.setTranslationKey(Mineralogy.MODID + "_" + itemName);
	}

	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
			int fortune) {
		Item dustRocksalt = MinIoC.getInstance().resolve(Item.class, "dustRocksalt", Mineralogy.MODID);
	
		drops.add(new ItemStack(dustRocksalt, 4));
		
		super.getDrops(drops, world, pos, state, fortune);
	}
}
