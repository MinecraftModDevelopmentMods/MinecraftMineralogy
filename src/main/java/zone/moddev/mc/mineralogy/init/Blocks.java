package zone.moddev.mc.mineralogy.init;

import zone.moddev.mc.mineralogy.Constants;
import zone.moddev.mc.mineralogy.ConstructionRecipeHelper;
import zone.moddev.mc.mineralogy.Mineralogy;
import zone.moddev.mc.mineralogy.MineralogyConfig;
import zone.moddev.mc.mineralogy.blocks.Chalk;
import zone.moddev.mc.mineralogy.blocks.Chert;
import zone.moddev.mc.mineralogy.blocks.DoubleSlab;
import zone.moddev.mc.mineralogy.blocks.DryWall;
import zone.moddev.mc.mineralogy.blocks.Gypsum;
import zone.moddev.mc.mineralogy.blocks.Rock;
import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.blocks.RockRelief;
import zone.moddev.mc.mineralogy.blocks.RockSalt;
import zone.moddev.mc.mineralogy.blocks.RockSaltLamp;
import zone.moddev.mc.mineralogy.blocks.RockSaltStreetLamp;
import zone.moddev.mc.mineralogy.blocks.RockSlab;
import zone.moddev.mc.mineralogy.blocks.RockStairs;
import zone.moddev.mc.mineralogy.blocks.RockWall;
import zone.moddev.mc.mineralogy.data.Material;
import zone.moddev.mc.mineralogy.data.MaterialData;
import zone.moddev.mc.mineralogy.ioc.MinIoC;
import zone.moddev.mc.mineralogy.lib.interfaces.IDynamicTabProvider;
import zone.moddev.mc.mineralogy.tileentity.TileEntityRockFurnace;
import zone.moddev.mc.mineralogy.util.BlockItemPair;
import zone.moddev.mc.mineralogy.util.RecipeHelper;
import zone.moddev.mc.mineralogy.util.RegistrationHelper;

import net.minecraft.block.SoundType;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Blocks {
    private static boolean initDone = false;

    protected Blocks() {
        throw new IllegalAccessError("Not a instantiable class");
    }

    /**
     *
     */
    public static void init() {
        if (initDone) {
            return;
        }

        MinIoC IoC =  MinIoC.getInstance();

        BlockItemPair blockChert;
        BlockItemPair blockGypsum;
        BlockItemPair blockChalk;
        BlockItemPair blockRocksalt;
        BlockItemPair blockPumice;
        BlockItemPair[] drywalls = new BlockItemPair[16];

        GameRegistry.registerTileEntity(TileEntityRockFurnace.class, "rockfurnace");

        MaterialData.toArray().forEach(material -> addStoneType(material));

        blockChert = RegistrationHelper.registerBlock(new Chert(), Constants.CHERT, Constants.BLOCK_CHERT);

        blockGypsum = RegistrationHelper.registerBlock(new Gypsum(), Constants.GYPSUM.toLowerCase(), Constants.BLOCK_GYPSUM);

        blockChalk = RegistrationHelper.registerBlock(new Chalk(), Constants.CHALK.toLowerCase(), Constants.BLOCK_CHALK);

        blockRocksalt = RegistrationHelper.registerBlock(new RockSalt(), Constants.ROCKSALT.toLowerCase(), Constants.BLOCK_ROCKSALT);

        addStoneType(MaterialData.ROCK_SALT, blockRocksalt);

        IoC.register(BlockItemPair.class, blockGypsum, Constants.BLOCK_GYPSUM, Mineralogy.MODID);
        IoC.register(BlockItemPair.class, blockChalk, Constants.BLOCK_CHALK, Mineralogy.MODID);
        IoC.register(BlockItemPair.class, blockRocksalt, Constants.BLOCK_ROCKSALT, Mineralogy.MODID);

        blockPumice = RegistrationHelper.registerBlock(new Rock(false, 0.5F, 5F, 0, SoundType.STONE), Constants.PUMICE, Constants.BLOCK_PUMICE);

        IoC.register(BlockItemPair.class, blockPumice, Constants.BLOCK_PUMICE, Mineralogy.MODID);

        RegistrationHelper.registerBlock(new RockSaltLamp(), "rocksaltlamp", "lampRocksalt");
        RegistrationHelper.registerBlock(new RockSaltStreetLamp(), "rocksaltstreetlamp", "lampRocksaltStreet", 16);

        IDynamicTabProvider tabProvider = IoC.resolve(IDynamicTabProvider.class);

        for (int i = 0; i < 16; i++) {
            if (MineralogyConfig.groupCreativeTabItemsByType())
                tabProvider.setTabItemMapping("Item", Constants.DRYWALL + "_" + Constants.colorSuffixes[i]);

            drywalls[i] = RegistrationHelper.registerBlock(new DryWall(Constants.colorSuffixes[i]), Constants.DRYWALL + "_" + Constants.colorSuffixes[i],
                    Constants.DRYWALL + Constants.colorSuffixesTwo[i]);

            IoC.register(BlockItemPair.class, drywalls[i], Constants.DRYWALL + Constants.colorSuffixesTwo[i], Mineralogy.MODID);

            if (MineralogyConfig.contentPolicy().drywallsEnabled()) {
                RecipeHelper.addShapelessOreRecipe(Constants.DRYWALL + "_" + Constants.colorSuffixes[i], new ItemStack(drywalls[i].PairedItem, 1),
                        Constants.DRYWALL_WHITE,
                        Ingredient.fromStacks(new ItemStack(Items.DYE, 1, i)));
            }
        }

        initDone = true;
    }

    private static void generateReliefs(String materialName, double hardness, double blastResistance,
            int toolHardnessLevel, final BlockItemPair rock) {

        String oreDictName = "stone" + materialName.substring(0, 1).toUpperCase() + materialName.substring(1) + "Smooth";

        final BlockItemPair  blankRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_blank", Constants.RELIEF + "Blank" + materialName);
        RecipeHelper.addShapedOreRecipe(materialName + "_relief_blank", new ItemStack(blankRelief.PairedItem, 16), "xxx", "xxx", "xxx", 'x', oreDictName);

        final BlockItemPair axeRelief =  RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_axe", Constants.RELIEF + "Axe" + materialName);
        RecipeHelper.addShapelessOreRecipe(materialName + "_relief_axe", new ItemStack(axeRelief.PairedItem, 8), Constants.RELIEF + "Blank" + materialName, Constants.RELIEF + "Blank" + materialName, Constants.RELIEF + "Blank" + materialName, Constants.RELIEF + "Blank" + materialName, Constants.RELIEF + "Blank" + materialName, Constants.RELIEF + "Blank" + materialName, Constants.RELIEF + "Blank" + materialName, Constants.RELIEF + "Blank" + materialName, Items.STONE_AXE);

        final BlockItemPair crossRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_cross", Constants.RELIEF  + "Cross" + materialName);
        RecipeHelper.addShapedOreRecipe(materialName + "_relief_cross", new ItemStack(crossRelief.PairedItem, 4), "x x", "   ", "x x", 'x', Constants.RELIEF + "Blank" + materialName);

        final BlockItemPair hammerRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_hammer", Constants.RELIEF  + "Hammer" + materialName);
        RecipeHelper.addShapedOreRecipe(materialName + "_relief_hammer", new ItemStack(hammerRelief.PairedItem, 7), "zxz","zyz","zzz",'x', oreDictName,'y', Items.STICK,'z', Constants.RELIEF + "Blank" + materialName);

        final BlockItemPair hoeRelief =  RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_hoe", Constants.RELIEF + "Hoe" + materialName);
        RecipeHelper.addShapelessOreRecipe(materialName + "_relief_hoe", new ItemStack(hoeRelief.PairedItem, 8), Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName, Items.STONE_HOE);

        final BlockItemPair horizontalRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_horizontal", Constants.RELIEF  + "Horizontal" + materialName);
        RecipeHelper.addShapedOreRecipe(materialName + "_relief_horizontal", new ItemStack(horizontalRelief.PairedItem, 3), "xxx", 'x', Constants.RELIEF + "Blank" + materialName);

        final BlockItemPair leftRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_left", Constants.RELIEF  + "Left" + materialName);
        RecipeHelper.addShapedOreRecipe(materialName + "_relief_left", new ItemStack(leftRelief.PairedItem, 3),"x  "," x ","  x",'x', Constants.RELIEF + "Blank" + materialName);

        final BlockItemPair pickaxeRelief =  RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_pickaxe", Constants.RELIEF + "Pickaxe" + materialName);
        RecipeHelper.addShapelessOreRecipe(materialName + "_relief_pickaxe", new ItemStack(pickaxeRelief.PairedItem, 8), Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName, Items.STONE_PICKAXE);

        final BlockItemPair plusRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_plus", Constants.RELIEF  + "Plus" + materialName);
        RecipeHelper.addShapedOreRecipe(materialName + "_relief_plus", new ItemStack(plusRelief.PairedItem, 5), " x ","xxx"," x ", 'x', Constants.RELIEF + "Blank" + materialName);

        final BlockItemPair rightRelief =  RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_right", Constants.RELIEF + "Right" + materialName);
        RecipeHelper.addShapedOreRecipe(materialName + "_relief_right", new ItemStack(rightRelief.PairedItem, 3),"  x"," x ","x  ",'x', Constants.RELIEF  + "Left" + materialName);

        final BlockItemPair swordRelief =  RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_sword", Constants.RELIEF + "Sword" + materialName);
        RecipeHelper.addShapelessOreRecipe(materialName + "_relief_sword", new ItemStack(swordRelief.PairedItem, 8), Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName,Constants.RELIEF + "Blank" + materialName, Items.STONE_SWORD);

        final BlockItemPair iRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_i", Constants.RELIEF  + "I" + materialName);
        RecipeHelper.addShapedOreRecipe(materialName + "_relief_i", new ItemStack(iRelief.PairedItem, 7), "xxx"," x ","xxx", 'x', Constants.RELIEF + "Blank" + materialName);

        final BlockItemPair verticalRelief = RegistrationHelper.registerBlock(new RockRelief((float)hardness, (float)blastResistance / 2, toolHardnessLevel, SoundType.STONE), materialName + "_relief_vertical", Constants.RELIEF  + "Vertical" + materialName);
        RecipeHelper.addShapedOreRecipe(materialName + "_relief_vertical", new ItemStack(verticalRelief.PairedItem, 3), "x","x","x", 'x', Constants.RELIEF + "Blank" + materialName);
    }

    protected static void addStoneType(Material materialType, BlockItemPair rockPair) {

        String name = materialType.materialName.toLowerCase();
        String oreDictName = "stone" + materialType.materialName;
        float burnModifier = (float) (1 + ((materialType.hardness - 3) / 10));

        BlockItemPair rockFurnacePair = null;
        BlockItemPair rockStairPair = null;
        BlockItemPair rockSlabPair = null;
        BlockItemPair rockWallPair = null;
        BlockItemPair brickPair = null;
        BlockItemPair brickFurnacePair = null;
        BlockItemPair brickStairPair = null;
        BlockItemPair brickSlabPair = null;
        BlockItemPair brickWallPair = null;
        BlockItemPair smoothPair = null;
        BlockItemPair smoothFurnacePair = null;
        BlockItemPair smoothStairPair = null;
        BlockItemPair smoothSlabPair = null;
        BlockItemPair smoothWallPair = null;
        BlockItemPair smoothBrickPair = null;
        BlockItemPair smoothBrickFurnacePair = null;
        BlockItemPair smoothBrickStairPair = null;
        BlockItemPair smoothBrickSlabPair = null;
        BlockItemPair smoothBrickWallPair = null;

        RecipeHelper.addShapelessOreRecipe(name + "_" + Constants.COBBLESTONE.toUpperCase(), new ItemStack(net.minecraft.init.Blocks.COBBLESTONE, 4),
                oreDictName,
                oreDictName,
                Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.GRAVEL)),
                Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.GRAVEL)));

        GameRegistry.addSmelting(rockPair.PairedItem, new ItemStack(net.minecraft.init.Blocks.STONE), 0.1F);

        // no point in ore dicting these recipes I think
        if (MineralogyConfig.generateRockStairs()) {
            rockStairPair = RegistrationHelper.registerBlock(new RockStairs(rockPair.PairedBlock, (float) materialType.hardness,
                    (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE), name + "_" + Constants.STAIRS,
                    Constants.STAIRS + materialType.materialName);
            RecipeHelper.addShapedOreRecipe(name + "_" + Constants.STAIRS, new ItemStack(rockStairPair.PairedItem, 4), "x  ", "xx ", "xxx",
                    'x', oreDictName);
        }

        if (MineralogyConfig.generateRockSlab()) {
            rockSlabPair = RegistrationHelper.registerBlock(
                    new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_double_" + Constants.SLAB),
                    name + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName, true, 64, true);
            RecipeHelper.addExactHorizontalThreeRecipe(name + "_" + Constants.SLAB,
                    new ItemStack(rockSlabPair.PairedItem, 6), new ItemStack(rockPair.PairedItem, 1, 0));

            RegistrationHelper.registerBlock(
                    new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, rockSlabPair.PairedBlock, rockPair.PairedBlock),
                    name + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName, false, 64, false);

            if (MineralogyConfig.generateRockFurnace()) {
                rockFurnacePair = RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                        (float) materialType.blastResistance, materialType.toolHardnessLevel, false, burnModifier), name + "_" + Constants.FURNACE,
                        Constants.FURNACE + materialType.materialName, true, 1, false);
                RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                        (float) materialType.blastResistance, materialType.toolHardnessLevel, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_" + Constants.FURNACE,
                        Constants.FURNACE + "Lit" + materialType.materialName, false, 1, true);

                RecipeHelper.addShapedOreRecipe(name + "_" + Constants.FURNACE, new ItemStack(rockFurnacePair.PairedItem, 1), "xxx", "xyx", "xxx",
                        'x', Constants.SLAB + materialType.materialName, 'y', net.minecraft.init.Blocks.FURNACE);
            }
        }

        if (MineralogyConfig.generateRockWall()) {
            rockWallPair = RegistrationHelper.registerBlock(
                    new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                    name + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
            RecipeHelper.addShapedOreRecipe(name + "_" + Constants.WALL, new ItemStack(rockWallPair.PairedItem, 6), "xxx", "xxx", 'x',
                    oreDictName);
        }

        if (MineralogyConfig.generateBrick()) {
            brickPair = RegistrationHelper.registerBlock(
                    new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                    name + "_" + Constants.BRICK, "stone" + materialType.materialName + "Brick");
            RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK, new ItemStack(brickPair.PairedItem, 4), "xx", "xx", 'x',
                    oreDictName);

            if (MineralogyConfig.generateBrickStairs()) {
                brickStairPair = RegistrationHelper.registerBlock(
                        new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
                                materialType.toolHardnessLevel, SoundType.STONE),
                        name + "_" + Constants.BRICK + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "Brick");

                RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.STAIRS, new ItemStack(brickStairPair.PairedItem, 4),
                        "x  ", "xx ", "xxx", 'x', "stone" + materialType.materialName + "Brick");
            }

            if (MineralogyConfig.generateBrickSlab()) {
                brickSlabPair = RegistrationHelper.registerBlock(
                        new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_" + Constants.BRICK + "_double_" + Constants.SLAB),
                        name + "_" + Constants.BRICK + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "Brick", true, 64, true);
                RecipeHelper.addExactHorizontalThreeRecipe(name + "_" + Constants.BRICK + "_" + Constants.SLAB,
                        new ItemStack(brickSlabPair.PairedItem, 6), new ItemStack(brickPair.PairedItem, 1, 0));

                RegistrationHelper.registerBlock(
                        new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, brickSlabPair.PairedBlock, brickPair.PairedBlock),
                        name + "_" + Constants.BRICK + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName + "Brick", false, 64, false);

                if (MineralogyConfig.generateBrickFurnace()) {
                    brickFurnacePair = RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                    (float) materialType.blastResistance, materialType.toolHardnessLevel, false, burnModifier), name + "_" + Constants.BRICK + "_" + Constants.FURNACE,
                            Constants.FURNACE + materialType.materialName, true, 1, false);
                    RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                    (float) materialType.blastResistance, materialType.toolHardnessLevel, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_" + Constants.BRICK + "_" + Constants.FURNACE,
                            Constants.FURNACE + "Lit" +  materialType.materialName, false, 1, false);

                    RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.FURNACE, new ItemStack(brickFurnacePair.PairedItem, 1), "xxx", "xyx", "xxx",
                            'x', Constants.SLAB + materialType.materialName + "Brick", 'y', net.minecraft.init.Blocks.FURNACE);
                }
            }

            if (MineralogyConfig.generateBrickWall()) {
                brickWallPair = RegistrationHelper.registerBlock(
                        new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                        name + "_" + Constants.BRICK + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
                RecipeHelper.addShapedOreRecipe(name + "_" + Constants.BRICK + "_" + Constants.WALL, new ItemStack(brickWallPair.PairedItem, 6), "xxx", "xxx", 'x',
                        "stone" + materialType.materialName + "Brick");
            }
        }

        if (MineralogyConfig.generateSmooth()) {
            smoothPair = RegistrationHelper.registerBlock(
                    new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                    name + "_" + Constants.SMOOTH, "stone" + materialType.materialName + "Smooth");
            RecipeHelper.addShapelessOreRecipe(name + "_" + Constants.SMOOTH, new ItemStack(smoothPair.PairedItem, 1),
                    oreDictName,
                    Ingredient.fromStacks(new ItemStack(net.minecraft.init.Blocks.SAND, 1)));

            if(MineralogyConfig.generateReliefs()) {
                generateReliefs(name, materialType.hardness, materialType.blastResistance, materialType.toolHardnessLevel, smoothPair);
            }

            if (MineralogyConfig.generateSmoothStairs()) {
                smoothStairPair = RegistrationHelper.registerBlock(
                        new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
                                materialType.toolHardnessLevel, SoundType.STONE),
                        name + "_" + Constants.SMOOTH + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "Smooth");
                RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.STAIRS, new ItemStack(smoothStairPair.PairedItem, 4),
                        "x  ", "xx ", "xxx", 'x', "stone" + materialType.materialName + "Smooth");
            }

            if (MineralogyConfig.generateSmoothSlab()) {
                smoothSlabPair = RegistrationHelper.registerBlock(
                        new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_" + Constants.SMOOTH + "_double_" + Constants.SLAB),
                        name + "_" + Constants.SMOOTH + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "Smooth", true, 64, true);
                RecipeHelper.addExactHorizontalThreeRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.SLAB,
                        new ItemStack(smoothSlabPair.PairedItem, 6), new ItemStack(smoothPair.PairedItem, 1, 0));
                RegistrationHelper.registerBlock(
                        new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, smoothSlabPair.PairedBlock, smoothPair.PairedBlock),
                        name + "_" + Constants.SMOOTH + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName + "Smooth", false, 64, false);

                if (MineralogyConfig.generateSmoothFurnace()) {
                    smoothFurnacePair = RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                    (float) materialType.blastResistance, materialType.toolHardnessLevel, false, burnModifier), name + "_" + Constants.SMOOTH + "_" + Constants.FURNACE,
                            Constants.FURNACE + materialType.materialName, true, 1, false);
                    RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                    (float) materialType.blastResistance, materialType.toolHardnessLevel, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_" + Constants.SMOOTH + "_" + Constants.FURNACE,
                            Constants.FURNACE + "Lit" +  materialType.materialName, false, 1, false);

                    RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.FURNACE, new ItemStack(smoothFurnacePair.PairedItem, 1), "xxx", "xyx", "xxx",
                            'x', Constants.SLAB + materialType.materialName + "Smooth", 'y', net.minecraft.init.Blocks.FURNACE);
                }
            }

            if (MineralogyConfig.generateSmoothWall()) {
                smoothWallPair = RegistrationHelper.registerBlock(
                        new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                        name + "_" + Constants.SMOOTH + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
                RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.WALL, new ItemStack(smoothWallPair.PairedItem, 6), "xxx", "xxx", 'x',
                        "stone" + materialType.materialName + "Smooth");
            }

            if (MineralogyConfig.generateSmoothBrick()) {
                smoothBrickPair = RegistrationHelper.registerBlock(
                        new Rock(false, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                        name + "_" + Constants.SMOOTH + "_" + Constants.BRICK, "stone" + materialType.materialName + "SmoothBrick");
                RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK, new ItemStack(smoothBrickPair.PairedItem, 4),
                        "xx", "xx", 'x', "stone" + materialType.materialName + "Smooth");

                if (MineralogyConfig.generateSmoothBrickStairs()) {
                    smoothBrickStairPair = RegistrationHelper.registerBlock(
                            new RockStairs(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance,
                                    materialType.toolHardnessLevel, SoundType.STONE),
                            name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.STAIRS, Constants.STAIRS + materialType.materialName + "SmoothBrick");
                    RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.STAIRS,
                            new ItemStack(smoothBrickStairPair.PairedItem, 4), "x  ", "xx ", "xxx", 'x',
                            "stone" + materialType.materialName + "SmoothBrick");
                }

                if (MineralogyConfig.generateSmoothBrickSlab()) {
                    smoothBrickSlabPair = RegistrationHelper.registerBlock(
                            new RockSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_double_" + Constants.SLAB),
                            name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.SLAB, Constants.SLAB + materialType.materialName + "SmoothBrick", true, 64, true);
                    RecipeHelper.addExactHorizontalThreeRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.SLAB,
                            new ItemStack(smoothBrickSlabPair.PairedItem, 6), new ItemStack(smoothBrickPair.PairedItem, 1, 0));
                    RegistrationHelper.registerBlock(
                            new DoubleSlab((float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE, smoothBrickSlabPair.PairedBlock, smoothBrickPair.PairedBlock),
                            name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_double_" + Constants.SLAB, Constants.SLAB + "Double" + materialType.materialName + "SmoothBrick", false, 64, false);

                    if (MineralogyConfig.generateSmoothBrickFurnace()) {
                        smoothBrickFurnacePair = RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                        (float) materialType.blastResistance, materialType.toolHardnessLevel, false, burnModifier), name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.FURNACE,
                                Constants.FURNACE + materialType.materialName, true, 1, false);
                        RegistrationHelper.registerBlock(new RockFurnace((float) materialType.hardness,
                                        (float) materialType.blastResistance, materialType.toolHardnessLevel, true, burnModifier).setLightLevel(0.875F), "lit_" + name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.FURNACE,
                                Constants.FURNACE + "Lit" +  materialType.materialName, false, 1, false);

                        RecipeHelper.addShapedOreRecipe(name+ "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.FURNACE, new ItemStack(smoothBrickFurnacePair.PairedItem, 1), "xxx", "xyx", "xxx",
                                'x', Constants.SLAB + materialType.materialName + "SmoothBrick", 'y', net.minecraft.init.Blocks.FURNACE);
                    }
                }

                if (MineralogyConfig.generateSmoothBrickWall()) {
                    smoothBrickWallPair = RegistrationHelper.registerBlock(
                            new RockWall(rockPair.PairedBlock, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE),
                            name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.WALL, Constants.WALL + materialType.materialName);
                    RecipeHelper.addShapedOreRecipe(name + "_" + Constants.SMOOTH + "_" + Constants.BRICK + "_" + Constants.WALL, new ItemStack(smoothBrickWallPair.PairedItem, 6), "xxx", "xxx", 'x',
                            "stone" + materialType.materialName + "SmoothBrick");
                }
            }
        }

        ConstructionRecipeHelper.registerConvenienceRecipes(name,
                forms(rockPair, rockStairPair, rockSlabPair, rockWallPair),
                forms(brickPair, brickStairPair, brickSlabPair, brickWallPair),
                forms(smoothPair, smoothStairPair, smoothSlabPair, smoothWallPair),
                forms(smoothBrickPair, smoothBrickStairPair, smoothBrickSlabPair, smoothBrickWallPair));
    }

    private static ConstructionRecipeHelper.Forms forms(BlockItemPair fullBlock, BlockItemPair stairs,
            BlockItemPair slab, BlockItemPair wall) {
        if (fullBlock == null) {
            return null;
        }
        return new ConstructionRecipeHelper.Forms(fullBlock.PairedBlock,
                stairs == null ? null : stairs.PairedBlock,
                slab == null ? null : slab.PairedBlock,
                wall == null ? null : wall.PairedBlock);
    }

    protected static void addStoneType(Material materialType) {
        String name = materialType.materialName.toLowerCase();
        final BlockItemPair rockPair = RegistrationHelper.registerBlock(new Rock(true, (float) materialType.hardness, (float) materialType.blastResistance, materialType.toolHardnessLevel, SoundType.STONE), name, "stone" + materialType.materialName);

        addStoneType(materialType, rockPair);
    }
}
