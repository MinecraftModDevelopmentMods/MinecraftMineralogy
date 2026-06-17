package com.mcmoddev.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.ToolType;

public class DryWall extends BlockPane {
	private final int toolHardnessLevel;

	public DryWall(String color) {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.75F, 1.0F).sound(SoundType.STONE));
		this.toolHardnessLevel = 0;
		this.setRegistryName("drywall_" + color);
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
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT;
	}
}
