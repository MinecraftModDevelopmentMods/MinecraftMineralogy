package zone.moddev.mc.mineralogy.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.block.state.BlockState;

/**
*
* @author Jasmine Iwanek
*
*/
public class RockWall extends WallBlock {
	public RockWall(Block materialBlock, float hardness, float blastResistance, int toolHardnessLevel,
	 			SoundType sound, String name) {
		super(BlockBehaviour.Properties.of(Material.STONE).strength(hardness, blastResistance).sound(sound)
				.requiresCorrectToolForDrops());

		this.setRegistryName(name);
		this.toolHardnessLevel = toolHardnessLevel;
	 }

	private final int toolHardnessLevel;
}
