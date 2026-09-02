package zone.moddev.mc.mineralogy.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.init.TileEntities;
import zone.moddev.mc.mineralogy.inventory.ContainerRockFurnace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceFuelSlot;
import net.minecraft.world.inventory.RecipeHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class TileEntityRockFurnace extends BaseContainerBlockEntity
		implements WorldlyContainer, RecipeHolder, StackedContentsCompatible {
	private static final int[] SLOTS_TOP = new int[] { 0 };
	private static final int[] SLOTS_BOTTOM = new int[] { 2, 1 };
	private static final int[] SLOTS_SIDES = new int[] { 1 };

	private NonNullList<ItemStack> furnaceItemStacks = NonNullList.withSize(3, ItemStack.EMPTY);
	private int furnaceBurnTime;
	private int currentItemBurnTime;
	private int cookTime;
	private int totalCookTime = 200;
	private final Map<ResourceLocation, Integer> recipeUseCounts = new HashMap<ResourceLocation, Integer>();
	private final ContainerData furnaceData = new ContainerData() {
		@Override
		public int get(int index) {
			return getField(index);
		}

		@Override
		public void set(int index, int value) {
			setField(index, value);
		}

		@Override
		public int getCount() {
			return 4;
		}
	};
	private LazyOptional<? extends IItemHandlerModifiable>[] handlers = createHandlers();
	private final float fallbackBurnModifier;

	public TileEntityRockFurnace(BlockPos pos, BlockState state) {
		this(pos, state, 1.0F);
	}

	public TileEntityRockFurnace(BlockPos pos, BlockState state, float burnModifier) {
		super(TileEntities.rock_furnace, pos, state);
		this.fallbackBurnModifier = burnModifier;
	}

	@Override
	public int getContainerSize() {
		return furnaceItemStacks.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack stack : furnaceItemStacks) {
			if (!stack.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public ItemStack getItem(int index) {
		return furnaceItemStacks.get(index);
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		return ContainerHelper.removeItem(furnaceItemStacks, index, count);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return ContainerHelper.takeItem(furnaceItemStacks, index);
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		ItemStack previous = furnaceItemStacks.get(index);
		boolean sameStack = !stack.isEmpty() && ItemStack.isSameItemSameTags(stack, previous);
		furnaceItemStacks.set(index, stack);

		if (stack.getCount() > getMaxStackSize()) {
			stack.setCount(getMaxStackSize());
		}

		if (index == 0 && !sameStack) {
			totalCookTime = getCookTime(getSmeltingRecipe());
			cookTime = 0;
			setChanged();
		}
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.furnace");
	}

	public void setCustomInventoryName(Component name) {
		setCustomName(name);
	}

	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		furnaceItemStacks = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(compound, furnaceItemStacks);
		furnaceBurnTime = compound.getInt("BurnTime");
		cookTime = compound.getInt("CookTime");
		totalCookTime = compound.getInt("CookTimeTotal");
		currentItemBurnTime = getItemBurnTime(furnaceItemStacks.get(1));
	}

	@Override
	protected void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		compound.putInt("BurnTime", furnaceBurnTime);
		compound.putInt("CookTime", cookTime);
		compound.putInt("CookTimeTotal", totalCookTime);
		ContainerHelper.saveAllItems(compound, furnaceItemStacks);
	}

	public boolean isBurning() {
		return furnaceBurnTime > 0;
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, TileEntityRockFurnace furnace) {
		furnace.tickServer(level, pos);
	}

	private void tickServer(Level level, BlockPos pos) {
		boolean wasBurning = isBurning();
		boolean dirty = false;

		if (isBurning()) {
			--furnaceBurnTime;
		}

		ItemStack fuel = furnaceItemStacks.get(1);
		if (isBurning() || !fuel.isEmpty() && !furnaceItemStacks.get(0).isEmpty()) {
			SmeltingRecipe recipe = getSmeltingRecipe();

			if (!isBurning() && canSmelt(recipe)) {
				furnaceBurnTime = (int) (getItemBurnTime(fuel) * getBurnModifier());
				currentItemBurnTime = furnaceBurnTime;

				if (isBurning()) {
					dirty = true;
					ItemStack container = fuel.getCraftingRemainingItem();
					if (!container.isEmpty()) {
						furnaceItemStacks.set(1, container);
					} else if (!fuel.isEmpty()) {
						fuel.shrink(1);
						if (fuel.isEmpty()) {
							furnaceItemStacks.set(1, ItemStack.EMPTY);
						}
					}
				}
			}

			if (isBurning() && canSmelt(recipe)) {
				++cookTime;
				if (cookTime == totalCookTime) {
					cookTime = 0;
					totalCookTime = getCookTime(recipe);
					smeltItem(recipe);
					dirty = true;
				}
			} else {
				cookTime = 0;
			}
		} else if (!isBurning() && cookTime > 0) {
			cookTime = Mth.clamp(cookTime - 2, 0, totalCookTime);
		}

		if (wasBurning != isBurning()) {
			dirty = true;
			RockFurnace.setState(isBurning(), level, pos);
		}

		if (dirty) {
			setChanged();
		}
	}

	private int getCookTime(@Nullable SmeltingRecipe recipe) {
		return recipe != null ? recipe.getCookingTime() : 200;
	}

	@Nullable
	private SmeltingRecipe getSmeltingRecipe() {
		return level == null ? null
				: level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, this, level).orElse(null);
	}

	private boolean canSmelt(@Nullable Recipe<?> recipe) {
		if (furnaceItemStacks.get(0).isEmpty() || recipe == null) {
			return false;
		}

		if (level == null) {
			return false;
		}
		ItemStack result = recipe.getResultItem(level.registryAccess());
		if (result.isEmpty()) {
			return false;
		}

		ItemStack output = furnaceItemStacks.get(2);
		if (output.isEmpty()) {
			return true;
		}
		if (!ItemStack.isSameItem(output, result)) {
			return false;
		}
		if (output.getCount() + result.getCount() <= getMaxStackSize()
				&& output.getCount() + result.getCount() <= output.getMaxStackSize()) {
			return true;
		}

		return output.getCount() + result.getCount() <= result.getMaxStackSize();
	}

	private void smeltItem(@Nullable Recipe<?> recipe) {
		if (!canSmelt(recipe)) {
			return;
		}

		ItemStack input = furnaceItemStacks.get(0);
		ItemStack result = recipe.getResultItem(level.registryAccess());
		ItemStack output = furnaceItemStacks.get(2);

		if (output.isEmpty()) {
			furnaceItemStacks.set(2, result.copy());
		} else if (output.getItem() == result.getItem()) {
			output.grow(result.getCount());
		}

		setRecipeUsed(recipe);

		if (input.getItem() == Blocks.WET_SPONGE.asItem()
				&& !furnaceItemStacks.get(1).isEmpty()
				&& furnaceItemStacks.get(1).getItem() == Items.BUCKET) {
			furnaceItemStacks.set(1, new ItemStack(Items.WATER_BUCKET));
		}

		input.shrink(1);
	}

	private static int getItemBurnTime(ItemStack stack) {
		return stack.isEmpty() ? 0 : ForgeHooks.getBurnTime(stack, RecipeType.SMELTING);
	}

	public static boolean isItemFuel(ItemStack stack) {
		return getItemBurnTime(stack) > 0;
	}

	@Override
	public boolean stillValid(Player player) {
		if (level == null || level.getBlockEntity(worldPosition) != this) {
			return false;
		}
		return player.distanceToSqr((double) worldPosition.getX() + 0.5D,
				(double) worldPosition.getY() + 0.5D,
				(double) worldPosition.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		if (index == 2) {
			return false;
		}
		if (index != 1) {
			return true;
		}

		ItemStack fuel = furnaceItemStacks.get(1);
		return isItemFuel(stack) || FurnaceFuelSlot.isBucket(stack) && fuel.getItem() != Items.BUCKET;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side == Direction.DOWN ? SLOTS_BOTTOM : side == Direction.UP ? SLOTS_TOP : SLOTS_SIDES;
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
		return canPlaceItem(index, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
		if (direction == Direction.DOWN && index == 1) {
			Item item = stack.getItem();
			return item == Items.WATER_BUCKET || item == Items.BUCKET;
		}

		return true;
	}

	@Override
	protected AbstractContainerMenu createMenu(int windowId, Inventory playerInventory) {
		return new ContainerRockFurnace(windowId, playerInventory, this, furnaceData);
	}

	public int getField(int id) {
		switch (id) {
			case 0:
				return furnaceBurnTime;
			case 1:
				return currentItemBurnTime;
			case 2:
				return cookTime;
			case 3:
				return totalCookTime;
			default:
				return 0;
		}
	}

	public void setField(int id, int value) {
		switch (id) {
			case 0:
				furnaceBurnTime = value;
				break;
			case 1:
				currentItemBurnTime = value;
				break;
			case 2:
				cookTime = value;
				break;
			case 3:
				totalCookTime = value;
				break;
			default:
				break;
		}
	}

	@Override
	public void clearContent() {
		furnaceItemStacks.clear();
	}

	@Override
	public void fillStackedContents(StackedContents helper) {
		for (ItemStack stack : furnaceItemStacks) {
			helper.accountStack(stack);
		}
	}

	@Override
	public void setRecipeUsed(Recipe<?> recipe) {
		if (recipe == null) {
			return;
		}
		ResourceLocation id = recipe.getId();
		Integer count = recipeUseCounts.get(id);
		recipeUseCounts.put(id, count == null ? 1 : count + 1);
	}

	@Override
	public Recipe<?> getRecipeUsed() {
		return null;
	}

	public Map<ResourceLocation, Integer> getRecipeUseCounts() {
		return recipeUseCounts;
	}

	@Override
	public boolean setRecipeUsed(Level level, ServerPlayer player, Recipe<?> recipe) {
		setRecipeUsed(recipe);
		return recipe != null;
	}

	public void onCrafting(Player player) {
		if (!player.level().getGameRules().getBoolean(GameRules.RULE_LIMITED_CRAFTING)) {
			List<Recipe<?>> recipes = new ArrayList<Recipe<?>>();
			for (ResourceLocation id : recipeUseCounts.keySet()) {
				Recipe<?> recipe = player.level().getRecipeManager().byKey(id).orElse(null);
				if (recipe != null) {
					recipes.add(recipe);
				}
			}
			player.awardRecipes(recipes);
		}

		recipeUseCounts.clear();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (!isRemoved() && facing != null && capability == ForgeCapabilities.ITEM_HANDLER) {
			if (facing == Direction.UP) {
				return handlers[0].cast();
			}
			if (facing == Direction.DOWN) {
				return handlers[1].cast();
			}
			return handlers[2].cast();
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		for (LazyOptional<? extends IItemHandlerModifiable> handler : handlers) {
			handler.invalidate();
		}
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		handlers = createHandlers();
	}

	private LazyOptional<? extends IItemHandlerModifiable>[] createHandlers() {
		return SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);
	}

	private float getBurnModifier() {
		BlockState state = getBlockState();
		Block block = state.getBlock();
		return block instanceof RockFurnace ? ((RockFurnace) block).getBurnModifier() : fallbackBurnModifier;
	}
}
