package zone.moddev.mc.mineralogy.blocks;

import java.util.Collections;
import java.util.List;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.storage.loot.LootContext.Builder;

public class DoubleSlab extends Block implements NamedMineralogyBlock {
	private final Block drops;
	private final int toolHardnessLevel;
	private final String registryPath;

	public DoubleSlab(float hardness, float blastResistance, int toolHardnessLevel, SoundType sound, Block drops,
			String name) {
		super(BlockBehaviour.Properties.of(Material.STONE).strength(hardness, blastResistance).sound(sound)
				.requiresCorrectToolForDrops());
		this.drops = drops;
		this.toolHardnessLevel = toolHardnessLevel;
		this.registryPath = name;
	}

	@Override
	public String mineralogyRegistryPath() {
		return registryPath;
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
		return new ItemStack(drops);
	}

	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		return Collections.singletonList(new ItemStack(drops, 2));
	}
}
