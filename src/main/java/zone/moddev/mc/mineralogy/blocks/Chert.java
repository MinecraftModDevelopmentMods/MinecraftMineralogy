package zone.moddev.mc.mineralogy.blocks;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.SoundType;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootContext.Builder;

public class Chert extends Rock {
	private final Random prng = new Random();

	public Chert() {
		super(false, 1.5F, 10.0F, 1, SoundType.STONE, "chert");
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		if (prng.nextInt(10) == 0) {
			return Collections.singletonList(
					new ItemStack(Items.FLINT, 1 + Math.max(0, getFortuneLevel(builder))));
		}
		return super.getDrops(state, builder);
	}
}
