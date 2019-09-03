package com.mcmoddev.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

public class RockStairs extends net.minecraft.block.StairsBlock {

	public RockStairs(Block materialBlock, float hardness, float blastResistance, int toolHardnessLevel,
			SoundType sound, String name) {
		super(materialBlock.getDefaultState(), Block.Properties.create(Material.ROCK).harvestTool(ToolType.PICKAXE)
				.hardnessAndResistance(hardness, blastResistance).sound(sound));
		
		this.setRegistryName(name);
	}
}
