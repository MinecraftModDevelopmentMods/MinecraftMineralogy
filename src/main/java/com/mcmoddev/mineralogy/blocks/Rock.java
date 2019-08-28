package com.mcmoddev.mineralogy.blocks;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.common.ToolType;

public class Rock extends Block {

	public Rock(boolean isStoneEquivalent, float hardness, float blastResistance, int toolHardnessLevel,
			SoundType sound) {
		super(Block.Properties.create(Material.ROCK).harvestTool(ToolType.PICKAXE)
				.hardnessAndResistance(hardness, blastResistance).sound(sound));
		
		this.isStoneEquivalent = isStoneEquivalent;
	}

	public final boolean isStoneEquivalent;

	@Override
	public boolean isReplaceableOreGen(BlockState state, IWorldReader world, BlockPos pos,
			Predicate<BlockState> target) {
		return isStoneEquivalent;
	}
	
	
	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		// TODO Auto-generated method stub
		return super.getDrops(state, builder);
		
//super.getDrops(drops, world, pos, state, fortune);
//		
//		if (MineralogyConfig.dropCobblestone())
//			drops.add(new ItemStack(Blocks.COBBLESTONE));
	}
}
