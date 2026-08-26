package zone.moddev.mc.mineralogy.data;

import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.blocks.RockStairs;
import zone.moddev.mc.mineralogy.blocks.RockWall;
import zone.moddev.mc.mineralogy.init.MineralogyItemGroups;

import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ResourceLocation;

public class Material {
	public String materialName;
	public double hardness;
	public double blastResistance;
	public int toolHardnessLevel;
	public boolean cobbleEquivilent;
	public boolean standardRockType;
	private Rock rock;

	/**
	 * @param materialName
	 * 			Name of the block (should start with capital letter)
	 *
	 * @param hardness
	 *            How hard (time duration) the block is to pick. For reference, dirt
	 *            is 0.5, stone is 1.5, ores are 3, and obsidian is 50
	 * @param blastResistance
	 *            how resistant the block is to explosions. For reference, dirt is
	 *            0, stone is 10, and blast-proof materials are 2000
	 * @param toolHardnessLevel
	 *            0 for wood tools, 1 for stone, 2 for iron, 3 for diamond
	 * @param cobbleEquivilent
	 *            is material equivalent to cobblestone
	 */
	public Material(String materialName, double hardness, double blastResistance, int toolHardnessLevel,
			boolean cobbleEquivilent) {
		this(materialName, hardness, blastResistance, toolHardnessLevel, cobbleEquivilent, true);
	}

	public Material(String materialName, double hardness, double blastResistance, int toolHardnessLevel,
			boolean cobbleEquivilent, boolean standardRockType) {
		this.materialName = materialName;
		this.hardness = hardness;
		this.blastResistance = blastResistance;
		this.toolHardnessLevel = toolHardnessLevel;
		this.cobbleEquivilent = cobbleEquivilent;
		this.standardRockType = standardRockType;
	}

	public String id() {
		return materialName.toLowerCase();
	}

	public Rock toRock(boolean isSmooth, boolean isBrick) {
		String name = this.materialName.toLowerCase();

		if (isSmooth)
			name = name + "_smooth";

		if (isBrick)
			name = name + "_brick";

		rock = new Rock(true, (float)this.hardness, (float)this.blastResistance, (int)this.toolHardnessLevel, SoundType.STONE, name);

		return rock;
	}

	public void setRock(Rock rock) {
		this.rock = rock;
	}

	public RockWall toRockWall(boolean isSmooth, boolean isBrick) {
		String name = this.materialName.toLowerCase();

		if (isSmooth)
			name = name + "_smooth";

		if (isBrick)
			name = name + "_brick";

		return new RockWall(rock, (float)this.hardness, (float)this.blastResistance, (int)this.toolHardnessLevel, SoundType.STONE, name + "_wall");
	}

	public RockStairs toRockStairs(boolean isSmooth, boolean isBrick) {
		String name = this.materialName.toLowerCase();

		if (isSmooth)
			name = name + "_smooth";

		if (isBrick)
			name = name + "_brick";

		return new RockStairs(rock, (float)this.hardness, (float)this.blastResistance, (int)this.toolHardnessLevel, SoundType.STONE, name + "_stairs");
	}

	public BlockItem getBlockItem(Rock blockHandle) {
		BlockItem blockItem = new BlockItem(blockHandle,
				new Item.Properties().group(MineralogyItemGroups.forBlock(blockHandle)));

		blockItem.setRegistryName(Mineralogy.MODID, blockHandle.getRegistryName().getPath());

		return blockItem;
	}

	public BlockItem getBlockItem(RockStairs blockHandle) {
		BlockItem blockItem = new BlockItem(blockHandle,
				new Item.Properties().group(MineralogyItemGroups.forBlock(blockHandle)));

		blockItem.setRegistryName(Mineralogy.MODID, blockHandle.getRegistryName().getPath());

		return blockItem;
	}

	public BlockItem getBlockItem(RockWall blockHandle) {
		BlockItem blockItem = new BlockItem(blockHandle,
				new Item.Properties().group(MineralogyItemGroups.forBlock(blockHandle)));

		blockItem.setRegistryName(Mineralogy.MODID, blockHandle.getRegistryName().getPath());

		return blockItem;
	}

	@Override
	public String toString() {
		return materialName;
	}
}
