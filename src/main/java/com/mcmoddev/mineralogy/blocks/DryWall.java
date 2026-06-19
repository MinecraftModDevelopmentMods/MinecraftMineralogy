package com.mcmoddev.mineralogy.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;

public class DryWall extends IronBarsBlock {
	private final int toolHardnessLevel;

	public DryWall(String color) {
		super(BlockBehaviour.Properties.of(Material.STONE).strength(0.75F, 1.0F).sound(SoundType.STONE)
				.requiresCorrectToolForDrops());
		this.toolHardnessLevel = 0;
		this.setRegistryName("drywall_" + color);
	}
}
