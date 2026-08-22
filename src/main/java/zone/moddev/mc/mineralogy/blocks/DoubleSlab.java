package zone.moddev.mc.mineralogy.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DoubleSlab extends net.minecraft.block.Block {
    private final net.minecraft.block.Block drops;
    private final net.minecraft.block.Block silkTouchDrop;
    private final int silkTouchDropCount;

    public DoubleSlab(float hardness, float blastResistance, int toolHardnessLevel,
            SoundType sound, net.minecraft.block.Block drops) {
        this(hardness, blastResistance, toolHardnessLevel, sound, drops, drops, 2);
    }

    public DoubleSlab(float hardness, float blastResistance, int toolHardnessLevel,
            SoundType sound, net.minecraft.block.Block drops, net.minecraft.block.Block fullBlock) {
        this(hardness, blastResistance, toolHardnessLevel, sound, drops, fullBlock, 1);
    }

    private DoubleSlab(float hardness, float blastResistance, int toolHardnessLevel,
            SoundType sound, net.minecraft.block.Block drops, net.minecraft.block.Block silkTouchDrop,
            int silkTouchDropCount) {
        super(Material.ROCK);
        this.setHardness(hardness); // dirt is 0.5, grass is 0.6, stone is 1.5,iron ore is 3, obsidian is 50
        this.setResistance(blastResistance); // dirt is 0, iron ore is 5, stone is 10, obsidian is 2000
        this.setSoundType(sound); // sound for stone
        this.setHarvestLevel("pickaxe", toolHardnessLevel);
        this.drops = drops;
        this.silkTouchDrop = silkTouchDrop;
        this.silkTouchDropCount = silkTouchDropCount;
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(drops);
    }

    @Override
    protected boolean canSilkHarvest() {
        return true;
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(silkTouchDrop, silkTouchDropCount);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state,
            int fortune) {

        //super.getDrops(drops, world, pos, state, fortune);
        drops.add(new ItemStack(this.drops, 2));
    }
}
