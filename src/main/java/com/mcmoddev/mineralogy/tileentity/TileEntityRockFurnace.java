package com.mcmoddev.mineralogy.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mcmoddev.mineralogy.blocks.RockFurnace;
import com.mcmoddev.mineralogy.init.TileEntities;
import com.mcmoddev.mineralogy.inventory.ContainerRockFurnace;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.IRecipeHolder;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.FurnaceFuelSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.LockableTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IIntArray;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class TileEntityRockFurnace extends LockableTileEntity
		implements ISidedInventory, IRecipeHolder, IRecipeHelperPopulator, ITickableTileEntity {
	private static final int[] SLOTS_TOP = new int[] { 0 };
	private static final int[] SLOTS_BOTTOM = new int[] { 2, 1 };
	private static final int[] SLOTS_SIDES = new int[] { 1 };

	private NonNullList<ItemStack> furnaceItemStacks = NonNullList.withSize(3, ItemStack.EMPTY);
	private int furnaceBurnTime;
	private int currentItemBurnTime;
	private int cookTime;
	private int totalCookTime = 200;
	private ITextComponent furnaceCustomName;
	private final Map<ResourceLocation, Integer> recipeUseCounts = new HashMap<ResourceLocation, Integer>();
	private final IIntArray furnaceData = new IIntArray() {
		@Override
		public int get(int index) {
			return getField(index);
		}

		@Override
		public void set(int index, int value) {
			setField(index, value);
		}

		@Override
		public int size() {
			return 4;
		}
	};
	private final LazyOptional<? extends IItemHandlerModifiable>[] handlers =
			SidedInvWrapper.create(this, Direction.UP, Direction.DOWN, Direction.NORTH);
	private final float fallbackBurnModifier;

	public TileEntityRockFurnace() {
		this(1.0F);
	}

	public TileEntityRockFurnace(float burnModifier) {
		super(TileEntities.rock_furnace);
		this.fallbackBurnModifier = burnModifier;
	}

	@Override
	public int getSizeInventory() {
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
	public ItemStack getStackInSlot(int index) {
		return furnaceItemStacks.get(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStackHelper.getAndSplit(furnaceItemStacks, index, count);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(furnaceItemStacks, index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		ItemStack previous = furnaceItemStacks.get(index);
		boolean sameStack = !stack.isEmpty() && stack.isItemEqual(previous)
				&& ItemStack.areItemStackTagsEqual(stack, previous);
		furnaceItemStacks.set(index, stack);

		if (stack.getCount() > getInventoryStackLimit()) {
			stack.setCount(getInventoryStackLimit());
		}

		if (index == 0 && !sameStack) {
			totalCookTime = getCookTime(getSmeltingRecipe());
			cookTime = 0;
			markDirty();
		}
	}

	@Override
	public ITextComponent getName() {
		return hasCustomName() ? furnaceCustomName : getDefaultName();
	}

	@Override
	public boolean hasCustomName() {
		return furnaceCustomName != null;
	}

	@Override
	public ITextComponent getCustomName() {
		return furnaceCustomName;
	}

	@Override
	protected ITextComponent getDefaultName() {
		return new TranslationTextComponent("container.furnace");
	}

	public void setCustomInventoryName(ITextComponent name) {
		furnaceCustomName = name;
	}

	@Override
	public void read(BlockState state, CompoundNBT compound) {
		super.read(state, compound);
		furnaceItemStacks = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(compound, furnaceItemStacks);
		furnaceBurnTime = compound.getInt("BurnTime");
		cookTime = compound.getInt("CookTime");
		totalCookTime = compound.getInt("CookTimeTotal");
		currentItemBurnTime = getItemBurnTime(furnaceItemStacks.get(1));

		if (compound.contains("CustomName", 8)) {
			furnaceCustomName = ITextComponent.Serializer.getComponentFromJson(compound.getString("CustomName"));
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		compound.putInt("BurnTime", furnaceBurnTime);
		compound.putInt("CookTime", cookTime);
		compound.putInt("CookTimeTotal", totalCookTime);
		ItemStackHelper.saveAllItems(compound, furnaceItemStacks);

		if (hasCustomName()) {
			compound.putString("CustomName", ITextComponent.Serializer.toJson(furnaceCustomName));
		}

		return compound;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	public boolean isBurning() {
		return furnaceBurnTime > 0;
	}

	@Override
	public void tick() {
		boolean wasBurning = isBurning();
		boolean dirty = false;

		if (isBurning()) {
			--furnaceBurnTime;
		}

		if (!world.isRemote) {
			ItemStack fuel = furnaceItemStacks.get(1);

			if (isBurning() || !fuel.isEmpty() && !furnaceItemStacks.get(0).isEmpty()) {
				FurnaceRecipe recipe = getSmeltingRecipe();

				if (!isBurning() && canSmelt(recipe)) {
					furnaceBurnTime = (int) (getItemBurnTime(fuel) * getBurnModifier());
					currentItemBurnTime = furnaceBurnTime;

					if (isBurning()) {
						dirty = true;
						if (fuel.hasContainerItem()) {
							furnaceItemStacks.set(1, fuel.getContainerItem());
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
				cookTime = MathHelper.clamp(cookTime - 2, 0, totalCookTime);
			}

			if (wasBurning != isBurning()) {
				dirty = true;
				RockFurnace.setState(isBurning(), world, pos);
			}
		}

		if (dirty) {
			markDirty();
		}
	}

	private int getCookTime(@Nullable FurnaceRecipe recipe) {
		return recipe != null ? recipe.getCookTime() : 200;
	}

	@Nullable
	private FurnaceRecipe getSmeltingRecipe() {
		return world.getRecipeManager().getRecipe(IRecipeType.SMELTING, this, world).orElse(null);
	}

	private boolean canSmelt(@Nullable IRecipe<?> recipe) {
		if (furnaceItemStacks.get(0).isEmpty() || recipe == null) {
			return false;
		}

		ItemStack result = recipe.getRecipeOutput();
		if (result.isEmpty()) {
			return false;
		}

		ItemStack output = furnaceItemStacks.get(2);
		if (output.isEmpty()) {
			return true;
		}
		if (!output.isItemEqual(result)) {
			return false;
		}
		if (output.getCount() + result.getCount() <= getInventoryStackLimit()
				&& output.getCount() + result.getCount() <= output.getMaxStackSize()) {
			return true;
		}

		return output.getCount() + result.getCount() <= result.getMaxStackSize();
	}

	private void smeltItem(@Nullable IRecipe<?> recipe) {
		if (!canSmelt(recipe)) {
			return;
		}

		ItemStack input = furnaceItemStacks.get(0);
		ItemStack result = recipe.getRecipeOutput();
		ItemStack output = furnaceItemStacks.get(2);

		if (output.isEmpty()) {
			furnaceItemStacks.set(2, result.copy());
		} else if (output.getItem() == result.getItem()) {
			output.grow(result.getCount());
		}

		canUseRecipe(world, null, recipe);

		if (input.getItem() == Blocks.WET_SPONGE.asItem()
				&& !furnaceItemStacks.get(1).isEmpty()
				&& furnaceItemStacks.get(1).getItem() == Items.BUCKET) {
			furnaceItemStacks.set(1, new ItemStack(Items.WATER_BUCKET));
		}

		input.shrink(1);
	}

	private static int getItemBurnTime(ItemStack stack) {
		if (stack.isEmpty()) {
			return 0;
		}

		Integer vanillaBurnTime = AbstractFurnaceTileEntity.getBurnTimes().get(stack.getItem());
		int defaultBurnTime = vanillaBurnTime == null ? 0 : vanillaBurnTime.intValue();
		return ForgeEventFactory.getItemBurnTime(stack, defaultBurnTime);
	}

	public static boolean isItemFuel(ItemStack stack) {
		return getItemBurnTime(stack) > 0;
	}

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		if (world.getTileEntity(pos) != this) {
			return false;
		}
		return player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
				(double) pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(PlayerEntity player) {
	}

	@Override
	public void closeInventory(PlayerEntity player) {
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
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
	public boolean canInsertItem(int index, ItemStack stack, Direction direction) {
		return isItemValidForSlot(index, stack);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, Direction direction) {
		if (direction == Direction.DOWN && index == 1) {
			Item item = stack.getItem();
			return item == Items.WATER_BUCKET || item == Items.BUCKET;
		}

		return true;
	}

	@Override
	protected Container createMenu(int windowId, PlayerInventory playerInventory) {
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
	public void clear() {
		furnaceItemStacks.clear();
	}

	@Override
	public void fillStackedContents(RecipeItemHelper helper) {
		for (ItemStack stack : furnaceItemStacks) {
			helper.accountStack(stack);
		}
	}

	@Override
	public void setRecipeUsed(IRecipe<?> recipe) {
		ResourceLocation id = recipe.getId();
		Integer count = recipeUseCounts.get(id);
		recipeUseCounts.put(id, count == null ? 1 : count + 1);
	}

	@Override
	public IRecipe<?> getRecipeUsed() {
		return null;
	}

	public Map<ResourceLocation, Integer> getRecipeUseCounts() {
		return recipeUseCounts;
	}

	@Override
	public boolean canUseRecipe(World world, ServerPlayerEntity player, IRecipe<?> recipe) {
		if (recipe == null) {
			return false;
		}
		setRecipeUsed(recipe);
		return true;
	}

	@Override
	public void onCrafting(PlayerEntity player) {
		if (!world.getGameRules().getBoolean(GameRules.DO_LIMITED_CRAFTING)) {
			List<IRecipe<?>> recipes = new ArrayList<IRecipe<?>>();
			for (ResourceLocation id : recipeUseCounts.keySet()) {
				IRecipe<?> recipe = player.world.getRecipeManager().getRecipe(id).orElse(null);
				if (recipe != null) {
					recipes.add(recipe);
				}
			}
			player.unlockRecipes(recipes);
		}

		recipeUseCounts.clear();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if (!removed && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
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
	public void remove() {
		super.remove();
		for (LazyOptional<? extends IItemHandlerModifiable> handler : handlers) {
			handler.invalidate();
		}
	}

	private float getBurnModifier() {
		BlockState state = getBlockState();
		Block block = state.getBlock();
		return block instanceof RockFurnace ? ((RockFurnace) block).getBurnModifier() : fallbackBurnModifier;
	}
}
