package zone.moddev.mc.mineralogy.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.WallBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraftforge.common.ToolType;

/**
*
* @author Jasmine Iwanek
*
*/
public class RockWall extends WallBlock {
	public RockWall(Block materialBlock, float hardness, float blastResistance, int toolHardnessLevel,
	 			SoundType sound, String name) {
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(hardness, blastResistance).sound(sound));

		this.setRegistryName(name);
		this.toolHardnessLevel = toolHardnessLevel;
	 }

	private final int toolHardnessLevel;

	@Override
	public ToolType getHarvestTool(BlockState state) {
		return ToolType.PICKAXE;
	}

	@Override
	public int getHarvestLevel(BlockState state) {
		return toolHardnessLevel;
	}
}
