package zone.moddev.mc.mineralogy.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import zone.moddev.mc.mineralogy.blocks.RockFurnace;
import zone.moddev.mc.mineralogy.init.TileEntities;
import zone.moddev.mc.mineralogy.inventory.ContainerRockFurnace;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.IRecipeHelperPopulator;
import net.minecraft.inventory.IRecipeHolder;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeItemHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.crafting.VanillaRecipeTypes;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class TileEntityRockFurnace extends TileEntityLockable
		implements ISidedInventory, IRecipeHolder, IRecipeHelperPopulator, ITickable {
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
	private final LazyOptional<? extends IItemHandlerModifiable>[] handlers =
			SidedInvWrapper.create(this, EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH);
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
		return hasCustomName() ? furnaceCustomName : new TextComponentTranslation("container.furnace");
	}

	@Override
	public boolean hasCustomName() {
		return furnaceCustomName != null;
	}

	@Override
	public ITextComponent getCustomName() {
		return furnaceCustomName;
	}

	public void setCustomInventoryName(ITextComponent name) {
		furnaceCustomName = name;
	}

	@Override
	public void read(NBTTagCompound compound) {
		super.read(compound);
		furnaceItemStacks = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
		ItemStackHelper.loadAllItems(compound, furnaceItemStacks);
		furnaceBurnTime = compound.getInt("BurnTime");
		cookTime = compound.getInt("CookTime");
		totalCookTime = compound.getInt("CookTimeTotal");
		currentItemBurnTime = getItemBurnTime(furnaceItemStacks.get(1));

		if (compound.contains("CustomName", 8)) {
			furnaceCustomName = ITextComponent.Serializer.fromJson(compound.getString("CustomName"));
		}
	}

	@Override
	public NBTTagCompound write(NBTTagCompound compound) {
		super.write(compound);
		compound.setInt("BurnTime", furnaceBurnTime);
		compound.setInt("CookTime", cookTime);
		compound.setInt("CookTimeTotal", totalCookTime);
		ItemStackHelper.saveAllItems(compound, furnaceItemStacks);

		if (hasCustomName()) {
			compound.setString("CustomName", ITextComponent.Serializer.toJson(furnaceCustomName));
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

	public static boolean isBurning(IInventory inventory) {
		return inventory.getField(0) > 0;
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
		return recipe != null ? recipe.getCookingTime() : 200;
	}

	@Nullable
	private FurnaceRecipe getSmeltingRecipe() {
		IRecipe recipe = world.getRecipeManager().getRecipe(this, world, VanillaRecipeTypes.SMELTING);
		return recipe instanceof FurnaceRecipe ? (FurnaceRecipe) recipe : null;
	}

	private boolean canSmelt(@Nullable IRecipe recipe) {
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

	private void smeltItem(@Nullable IRecipe recipe) {
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

		Integer vanillaBurnTime = TileEntityFurnace.getBurnTimes().get(stack.getItem());
		int defaultBurnTime = vanillaBurnTime == null ? 0 : vanillaBurnTime.intValue();
		return ForgeEventFactory.getItemBurnTime(stack, defaultBurnTime);
	}

	public static boolean isItemFuel(ItemStack stack) {
		return getItemBurnTime(stack) > 0;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		if (world.getTileEntity(pos) != this) {
			return false;
		}
		return player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
				(double) pos.getZ() + 0.5D) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void closeInventory(EntityPlayer player) {
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
		return isItemFuel(stack) || SlotFurnaceFuel.isBucket(stack) && fuel.getItem() != Items.BUCKET;
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return side == EnumFacing.DOWN ? SLOTS_BOTTOM : side == EnumFacing.UP ? SLOTS_TOP : SLOTS_SIDES;
	}

	@Override
	public boolean canInsertItem(int index, ItemStack stack, EnumFacing direction) {
		return isItemValidForSlot(index, stack);
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		if (direction == EnumFacing.DOWN && index == 1) {
			Item item = stack.getItem();
			return item == Items.WATER_BUCKET || item == Items.BUCKET;
		}

		return true;
	}

	@Override
	public String getGuiID() {
		return "minecraft:furnace";
	}

	@Override
	public Container createContainer(InventoryPlayer playerInventory, EntityPlayer player) {
		return new ContainerRockFurnace(playerInventory, this);
	}

	@Override
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

	@Override
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
	public int getFieldCount() {
		return 4;
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
	public void setRecipeUsed(IRecipe recipe) {
		ResourceLocation id = recipe.getId();
		Integer count = recipeUseCounts.get(id);
		recipeUseCounts.put(id, count == null ? 1 : count + 1);
	}

	@Override
	public IRecipe getRecipeUsed() {
		return null;
	}

	public Map<ResourceLocation, Integer> getRecipeUseCounts() {
		return recipeUseCounts;
	}

	@Override
	public boolean canUseRecipe(World world, EntityPlayerMP player, IRecipe recipe) {
		if (recipe == null) {
			return false;
		}
		setRecipeUsed(recipe);
		return true;
	}

	@Override
	public void onCrafting(EntityPlayer player) {
		if (!world.getGameRules().getBoolean("doLimitedCrafting")) {
			List<IRecipe> recipes = new ArrayList<IRecipe>();
			for (ResourceLocation id : recipeUseCounts.keySet()) {
				IRecipe recipe = player.world.getRecipeManager().getRecipe(id);
				if (recipe != null) {
					recipes.add(recipe);
				}
			}
			player.unlockRecipes(recipes);
		}

		recipeUseCounts.clear();
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (!removed && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (facing == EnumFacing.UP) {
				return handlers[0].cast();
			}
			if (facing == EnumFacing.DOWN) {
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
		Block block = getBlockState().getBlock();
		return block instanceof RockFurnace ? ((RockFurnace) block).getBurnModifier() : fallbackBurnModifier;
	}
}
