package zone.moddev.mc.mineralogy.items;

import java.util.function.Supplier;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

/**
 * Retains Mineralogy's bucket class name for compatibility while using Forge's
 * current fluid-type-aware bucket behavior.
 */
public class MineralogyBucketItem extends BucketItem {
	public MineralogyBucketItem(Supplier<? extends Fluid> fluid, Item.Properties properties) {
		super(fluid, properties);
	}
}
