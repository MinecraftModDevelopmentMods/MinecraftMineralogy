package zone.moddev.mc.mineralogy.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
*
* @author Jasmine Iwanek
*
*/
public class RockWall extends WallBlock implements NamedMineralogyBlock {
	private final String registryPath;
	public RockWall(Block materialBlock, float hardness, float blastResistance, int toolHardnessLevel,
	 			SoundType sound, String name) {
		super(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.STONE).strength(hardness, blastResistance).sound(sound)
				.requiresCorrectToolForDrops());

		this.registryPath = name;
		this.toolHardnessLevel = toolHardnessLevel;
	 }

	@Override
	public String mineralogyRegistryPath() {
		return registryPath;
	}

	private final int toolHardnessLevel;
}
