//package com.mcmoddev.mineralogy.blocks;
//
//import java.util.Random;
//
//import com.mcmoddev.mineralogy.Mineralogy;
//
//import net.minecraft.block.SoundType;
//import net.minecraft.block.state.IBlockState;
//import net.minecraft.init.Items;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.NonNullList;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.world.IBlockAccess;
//
//public class Chert extends Rock {
//
//	private final Random prng = new Random();
//	private static final String ITEM_NAME = "chert";
//
//	public Chert() {
//		super(false, (float) 1.5, (float) 10, 1, SoundType.STONE);
//		this.setTranslationKey(Mineralogy.MODID + "_" + ITEM_NAME);
//	}
//
//	@Override
//	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
//			int fortune) {
//	
//		super.getDrops(drops, world, pos, state, fortune);
//		
//		if (prng.nextInt(10) == 0)
//			drops.add(new ItemStack(Items.FLINT, 1 + Math.max(0, fortune)));
//		}
//}
