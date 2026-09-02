package zone.moddev.mc.mineralogy.blocks;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;

public class DoubleSlab extends Block implements NamedMineralogyBlock {
	private final Block drops;
	private final Block fullBlock;
	private final int toolHardnessLevel;
	private final String registryPath;

	public DoubleSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, Block drops,
			String name) {
		this(hardness, blastResistance, toolHardnessLevel, sound, drops, drops, name);
	}

	public DoubleSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, Block drops,
			Block fullBlock, String name) {
		super(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.STONE).strength(hardness, blastResistance).sound(sound)
				.requiresCorrectToolForDrops());
		this.drops = drops;
		this.fullBlock = fullBlock;
		this.toolHardnessLevel = toolHardnessLevel;
		this.registryPath = name;
	}

	@Override
	public String mineralogyRegistryPath() {
		return registryPath;
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos,
			Player player) {
		return new ItemStack(drops);
	}

	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
		if (tool != null && !tool.isEmpty()
				&& EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, tool) > 0) {
			return Collections.singletonList(new ItemStack(fullBlock));
		}
		return Collections.singletonList(new ItemStack(drops, 2));
	}
}
