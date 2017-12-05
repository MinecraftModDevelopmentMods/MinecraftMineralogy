package com.mcmoddev.mineralogy.data;

import com.mcmoddev.mineralogy.RockType;

public class MaterialType {
	public String materialName;
	public RockType rockType;
	public double hardness;
	public double blastResistance;
	public int toolHardnessLevel;
	
	/**
	 * @param materialName
	 * 			Name of the block (should start with capital letter)			
	 * 
	 * @param type
	 *            Igneous, sedimentary, or metamorphic
	 * 
	 * @param hardness
	 *            How hard (time duration) the block is to pick. For reference, dirt
	 *            is 0.5, stone is 1.5, ores are 3, and obsidian is 50
	 * @param blastResistance
	 *            how resistant the block is to explosions. For reference, dirt is
	 *            0, stone is 10, and blast-proof materials are 2000
	 * @param toolHardnessLevel
	 *            0 for wood tools, 1 for stone, 2 for iron, 3 for diamond
	 */
	public MaterialType(String materialName, RockType rockType, 
			double hardness, double blastResistance, int toolHardnessLevel) {
		this.materialName = materialName;
		this.rockType = rockType;
		this.hardness = hardness;
		this.blastResistance = blastResistance;
		this.toolHardnessLevel = toolHardnessLevel;		
	}

	@Override
	public String toString() {
		return materialName;
	}
}