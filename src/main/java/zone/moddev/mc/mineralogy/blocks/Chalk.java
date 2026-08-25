package zone.moddev.mc.mineralogy.blocks;

import zone.moddev.mc.mineralogy.Mineralogy;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

public class Chalk extends Rock {
	public Chalk() {
		super(false, 0.75F, 1.0F, 0, SoundType.STONE, "chalk");
	}

	@Override
	public boolean canSilkHarvest(IBlockState state, IWorldReader world, BlockPos pos, EntityPlayer player) {
		return true;
	}

	@Override
	public void getDrops(IBlockState state, NonNullList<ItemStack> drops, World world, BlockPos pos, int fortune) {
		Item dust = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Mineralogy.MODID, "chalk_dust"));
		if (dust != null) {
			drops.clear();
			drops.add(new ItemStack(dust, 4));
		}
	}
}
