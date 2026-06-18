package com.mcmoddev.mineralogy.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.mcmoddev.mineralogy.MineralogyConfig;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraft.world.storage.loot.LootParameters;
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
	public boolean isReplaceableOreGen(BlockState state, IWorldReader world, BlockPos pos,
			Predicate<BlockState> target) {
		return isStoneEquivalent;
	}

	@Override
	public ToolType getHarvestTool(BlockState state) {
		return ToolType.PICKAXE;
	}

	@Override
	public int getHarvestLevel(BlockState state) {
		return toolHardnessLevel;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		List<ItemStack> drops = new ArrayList<ItemStack>(super.getDrops(state, builder));

		if (MineralogyConfig.dropCobblestone()) {
			drops.add(new ItemStack(Blocks.COBBLESTONE));
		}

		return drops;
	}

	protected static boolean hasSilkTouch(Builder builder) {
		ItemStack tool = builder.get(LootParameters.TOOL);
		return !tool.isEmpty() && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0;
	}

	protected static int getFortuneLevel(Builder builder) {
		ItemStack tool = builder.get(LootParameters.TOOL);
		return tool.isEmpty() ? 0 : EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool);
	}
}
