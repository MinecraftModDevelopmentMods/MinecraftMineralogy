package zone.moddev.mc.mineralogy.blocks;

import java.util.Collections;
import java.util.List;

import zone.moddev.mc.mineralogy.Mineralogy;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraftforge.registries.ForgeRegistries;

public class Ore extends Block implements NamedMineralogyBlock {
	private final String registryPath;
	private final String dropItemName;
	private final int dropAdduct;
	private final int dropRange;
	private final int pickLevel;

	public Ore(String name, String dropItemName, int minNumberDropped, int maxNumberDropped, int pickLevel) {
		super(BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.STONE).strength(1.5F, 5.0F).sound(SoundType.STONE)
				.requiresCorrectToolForDrops());
		this.registryPath = name;
		this.dropItemName = dropItemName;
		this.dropAdduct = minNumberDropped;
		this.dropRange = (maxNumberDropped - minNumberDropped) + 1;
		this.pickLevel = pickLevel;
	}

	@Override
	public String mineralogyRegistryPath() {
		return registryPath;
	}

	@Override
	public int getExpDrop(BlockState state, LevelReader world, RandomSource random, BlockPos pos,
			int fortune, int silktouch) {
		return 0;
	}

	public ItemLike getItemDropped() {
		Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(Mineralogy.MODID, dropItemName));
		return item == null ? this : item;
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder) {
		return Collections.singletonList(new ItemStack(getItemDropped(),
				builder.getLevel().random.nextInt(dropRange) + dropAdduct));
	}
}
