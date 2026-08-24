package zone.moddev.mc.mineralogy.data;

public class Material {
    public String materialName;
    public double hardness;
    public double blastResistance;
    public int toolHardnessLevel;
    public boolean cobbleEquivilent;
    public boolean standardRockType;

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
    public Material(String materialName,
            double hardness, double blastResistance, int toolHardnessLevel,
            boolean cobbleEquivilent) {
        this(materialName, hardness, blastResistance, toolHardnessLevel,cobbleEquivilent, true);
    }

    public Material(String materialName,
            double hardness, double blastResistance, int toolHardnessLevel,
            boolean cobbleEquivilent, boolean standardRockType) {
        this.materialName = materialName;
        this.hardness = hardness;
        this.blastResistance = blastResistance;
        this.toolHardnessLevel = toolHardnessLevel;
        this.cobbleEquivilent = cobbleEquivilent;
        this.standardRockType = standardRockType;
    }

    @Override
    public String toString() {
        return materialName;
    }
}
