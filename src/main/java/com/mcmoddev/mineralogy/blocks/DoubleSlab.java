package com.mcmoddev.mineralogy.blocks;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.loot.LootContext.Builder;
import net.minecraftforge.common.ToolType;

public class DoubleSlab extends Block {
	private final Block drops;
	private final int toolHardnessLevel;

	public DoubleSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, Block drops,
			String name) {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(hardness, blastResistance).sound(sound));
		this.drops = drops;
		this.toolHardnessLevel = toolHardnessLevel;
		this.setRegistryName(name);
	}

	@Override
	public ItemStack getItem(IBlockReader world, BlockPos pos, BlockState state) {
		return new ItemStack(drops);
	}

	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		return Collections.singletonList(new ItemStack(drops, 2));
	}

	@Override
	public ToolType getHarvestTool(BlockState state) {
		return ToolType.PICKAXE;
	}

	@Override
	public int getHarvestLevel(BlockState state) {
		return toolHardnessLevel;
	}
}
