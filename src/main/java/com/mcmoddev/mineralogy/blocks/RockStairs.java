package com.mcmoddev.mineralogy.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;

public class RockStairs extends StairBlock {

	public RockStairs(Block materialBlock, float hardness, float blastResistance, int toolHardnessLevel,
			SoundType sound, String name) {
		super(materialBlock.defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE)
				.strength(hardness, blastResistance).sound(sound).requiresCorrectToolForDrops());
		
		this.setRegistryName(name);
		this.toolHardnessLevel = toolHardnessLevel;
	}

	private final int toolHardnessLevel;
}
