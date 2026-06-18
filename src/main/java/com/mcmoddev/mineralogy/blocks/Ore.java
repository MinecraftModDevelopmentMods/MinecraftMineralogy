package com.mcmoddev.mineralogy.blocks;

import java.util.Collections;
import java.util.List;

import com.mcmoddev.mineralogy.Mineralogy;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.loot.LootContext.Builder;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.registries.ForgeRegistries;

public class Ore extends Block {
	private final String dropItemName;
	private final int dropAdduct;
	private final int dropRange;
	private final int pickLevel;

	public Ore(String name, String dropItemName, int minNumberDropped, int maxNumberDropped, int pickLevel) {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F, 5.0F).sound(SoundType.STONE));
		this.setRegistryName(Mineralogy.MODID, name);

		this.dropItemName = dropItemName;
		this.dropAdduct = minNumberDropped;
		this.dropRange = (maxNumberDropped - minNumberDropped) + 1;
		this.pickLevel = pickLevel;
	}

	@Override
	public int getExpDrop(BlockState state, IWorldReader world, BlockPos pos, int fortune, int silktouch) {
		return 0;
	}

	public IItemProvider getItemDropped() {
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Mineralogy.MODID, dropItemName));
		return item == null ? this : item;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		return Collections.singletonList(new ItemStack(getItemDropped(),
				builder.getWorld().rand.nextInt(dropRange) + dropAdduct));
	}

	@Override
	public ToolType getHarvestTool(BlockState state) {
		return ToolType.PICKAXE;
	}

	@Override
	public int getHarvestLevel(BlockState state) {
		return pickLevel;
	}
}
