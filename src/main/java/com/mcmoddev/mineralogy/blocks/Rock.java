package com.mcmoddev.mineralogy.blocks;

import java.util.function.Predicate;

import com.mcmoddev.mineralogy.MineralogyConfig;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class Rock extends Block {

	public Rock(boolean isStoneEquivalent, float hardness, float blastResistance, int toolHardnessLevel,
			SoundType sound, String name) {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(hardness, blastResistance).sound(sound));
		
		this.setRegistryName(name);
		this.isStoneEquivalent = isStoneEquivalent;
		this.toolHardnessLevel = toolHardnessLevel;
	}

	public final boolean isStoneEquivalent;
	private final int toolHardnessLevel;

	@Override
	public boolean isReplaceableOreGen(IBlockState state, IWorldReader world, BlockPos pos,
			Predicate<IBlockState> target) {
		return isStoneEquivalent;
	}

	@Override
	public ToolType getHarvestTool(IBlockState state) {
		return ToolType.PICKAXE;
	}

	@Override
	public int getHarvestLevel(IBlockState state) {
		return toolHardnessLevel;
	}

	@Override
	public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
		super.getDrops(state, drops, world, pos, fortune);

		if (MineralogyConfig.dropCobblestone()) {
			drops.add(new ItemStack(Blocks.COBBLESTONE));
		}
	}
}
