package zone.moddev.mc.mineralogy.blocks;

import java.util.Collections;
import java.util.List;

import zone.moddev.mc.mineralogy.Mineralogy;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
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

		Item dust = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(Mineralogy.MODID, "chalk_dust"));
		if (dust != null) {
			return Collections.singletonList(new ItemStack(dust, 4));
		}
		return super.getDrops(state, builder);
	}
}
