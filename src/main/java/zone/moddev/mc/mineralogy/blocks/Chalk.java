package zone.moddev.mc.mineralogy.blocks;

import java.util.Collections;
import java.util.List;

import zone.moddev.mc.mineralogy.Mineralogy;

import net.minecraft.block.SoundType;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext.Builder;
import net.minecraftforge.registries.ForgeRegistries;

public class Chalk extends Rock {
	public Chalk() {
		super(false, 0.75F, 1.0F, 0, SoundType.STONE, "chalk");
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		if (hasSilkTouch(builder)) {
			return Collections.singletonList(new ItemStack(this));
		}

		Item dust = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Mineralogy.MODID, "chalk_dust"));
		if (dust != null) {
			return Collections.singletonList(new ItemStack(dust, 4));
		}
		return super.getDrops(state, builder);
	}
}
