package zone.moddev.mc.mineralogy.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;

public class RockStairs extends StairBlock implements NamedMineralogyBlock {
	private final String registryPath;

	public RockStairs(Block materialBlock, float hardness, float blastResistance, int toolHardnessLevel,
			SoundType sound, String name) {
		super(materialBlock.defaultBlockState(), BlockBehaviour.Properties.of(Material.STONE)
				.strength(hardness, blastResistance).sound(sound).requiresCorrectToolForDrops());

		this.registryPath = name;
		this.toolHardnessLevel = toolHardnessLevel;
	}

	@Override
	public String mineralogyRegistryPath() {
		return registryPath;
	}

	private final int toolHardnessLevel;
}
