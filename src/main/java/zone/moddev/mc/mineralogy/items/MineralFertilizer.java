package zone.moddev.mc.mineralogy.items;

import zone.moddev.mc.mineralogy.init.RegistrationProperties;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class MineralFertilizer extends Item {
	public MineralFertilizer() {
		super(RegistrationProperties.item(new Item.Properties(), "mineral_fertilizer"));
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level world = context.getLevel();
		BlockPos target = context.getClickedPos();
		Player player = context.getPlayer();

		boolean canUse = BoneMealItem.applyBonemeal(context.getItemInHand(), world, target, player);
		if (canUse) {
			// Minecraft 26.2 binds item components after registry objects are
			// constructed, so helper stacks must be created lazily during play.
			ItemStack phantomBonemeal = new ItemStack(Items.BONE_MEAL, 27);
			for (int dx = -2; dx <= 2; dx++) {
				for (int dy = -2; dy <= 2; dy++) {
					for (int dz = -1; dz <= 1; dz++) {
						if ((dx | dy | dz) == 0) {
							continue;
						}
						BoneMealItem.applyBonemeal(phantomBonemeal, world, target.offset(dx, dy, dz), player);
					}
				}
			}
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}
}
