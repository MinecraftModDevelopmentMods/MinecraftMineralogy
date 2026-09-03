package zone.moddev.mc.mineralogy.blocks;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import zone.moddev.mc.mineralogy.MineralogyConfig;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public class Rock extends Block implements NamedMineralogyBlock {
	private final String registryPath;

	public Rock(boolean isStoneEquivalent, float hardness, float blastResistance, int toolHardnessLevel,
			SoundType sound, String name) {
		super(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(hardness, blastResistance).sound(sound)
				.requiresCorrectToolForDrops());

		this.registryPath = name;
		this.isStoneEquivalent = isStoneEquivalent;
		this.toolHardnessLevel = toolHardnessLevel;
	}

	@Override
	public String mineralogyRegistryPath() {
		return registryPath;
	}

	public final boolean isStoneEquivalent;
	private final int toolHardnessLevel;

	public boolean isReplaceableOreGen(BlockState state, LevelReader world, BlockPos pos,
			Predicate<BlockState> target) {
		return isStoneEquivalent;
	}
@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		if (isStoneEquivalent && MineralogyConfig.dropCobblestone()) {
			return Collections.singletonList(new ItemStack(Blocks.COBBLESTONE));
		}
		return super.getDrops(state, builder);
	}

	protected static boolean hasSilkTouch(Builder builder) {
		ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
		return tool != null && !tool.isEmpty()
				&& EnchantmentHelper.getItemEnchantmentLevel(builder.getLevel().registryAccess()
						.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.SILK_TOUCH), tool) > 0;
	}

	protected static int getFortuneLevel(Builder builder) {
		ItemStack tool = builder.getOptionalParameter(LootContextParams.TOOL);
		return tool == null || tool.isEmpty() ? 0 : EnchantmentHelper.getItemEnchantmentLevel(
				builder.getLevel().registryAccess().registryOrThrow(Registries.ENCHANTMENT)
						.getHolderOrThrow(Enchantments.FORTUNE), tool);
	}
}
