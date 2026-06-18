package com.mcmoddev.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraftforge.common.ToolType;

public class RockStairs extends StairsBlock {

	public RockStairs(Block materialBlock, float hardness, float blastResistance, int toolHardnessLevel,
			SoundType sound, String name) {
		super(materialBlock.getDefaultState(), Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(hardness, blastResistance).sound(sound));
		
		this.setRegistryName(name);
		this.toolHardnessLevel = toolHardnessLevel;
	}

	private final int toolHardnessLevel;

	@Override
	public ToolType getHarvestTool(BlockState state) {
		return ToolType.PICKAXE;
	}

	@Override
	public int getHarvestLevel(BlockState state) {
		return toolHardnessLevel;
	}
}
