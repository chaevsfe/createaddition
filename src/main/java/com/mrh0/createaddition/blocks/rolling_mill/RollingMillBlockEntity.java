package com.mrh0.createaddition.blocks.rolling_mill;

import java.util.List;
import java.util.Optional;

import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import com.zurrtum.create.infrastructure.transfer.SlotRangeCache;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RollingMillBlockEntity extends KineticBlockEntity {
	public ItemStackHandler inputInv;
	public ItemStackHandler outputInv;
	public RollingMillInventoryHandler capability;
	public int timer;
	private RollingRecipe lastRecipe;

	public RollingMillBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		inputInv = new ItemStackHandler(1);
		outputInv = new ItemStackHandler(9);
		capability = new RollingMillInventoryHandler();
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
		behaviours.add(new DirectBeltInputBehaviour(this));
		super.addBehaviours(behaviours);
	}

	@Override
	public void tick() {
		super.tick();

		if (getSpeed() == 0) return;
		for (int i = 0; i < outputInv.getContainerSize(); i++) {
			ItemStack stack = outputInv.getItem(i);
			if (stack.getCount() == outputInv.getMaxStackSize(stack))
				return;
		}

		if (timer > 0) {
			timer -= getProcessingSpeed();

			if (level.isClientSide()) {
				spawnParticles();
				return;
			}
			if (timer <= 0) process();
			return;
		}

		if (level.isClientSide()) return;
		if (inputInv.getItem(0).isEmpty()) return;

		SingleRecipeInput input = new SingleRecipeInput(inputInv.getItem(0));
		if (lastRecipe == null || !lastRecipe.matches(input, level)) {
			Optional<RecipeHolder<RollingRecipe>> recipe = find(input, level);
			if (recipe.isEmpty()) {
				timer = 100;
				sendData();
			} else {
				lastRecipe = recipe.get().value();
				timer = CACommonConfig.COMMON.ROLLING_MILL_PROCESSING_DURATION.get();
				sendData();
			}
			return;
		}

		timer = CACommonConfig.COMMON.ROLLING_MILL_PROCESSING_DURATION.get();
		sendData();
	}

	@Override
	public void destroy() {
		super.destroy();
		Containers.dropContents(level, worldPosition, inputInv);
		Containers.dropContents(level, worldPosition, outputInv);
	}

	private void process() {
		SingleRecipeInput input = new SingleRecipeInput(inputInv.getItem(0));

		if (lastRecipe == null || !lastRecipe.matches(input, level)) {
			Optional<RecipeHolder<RollingRecipe>> recipe = find(input, level);
			if (recipe.isEmpty()) return;
			lastRecipe = recipe.get().value();
		}

		ItemStack stackInSlot = inputInv.getItem(0);
		stackInSlot.shrink(1);
		inputInv.setItem(0, stackInSlot);
		insertOutput(lastRecipe.getResultStack().copy());

		sendData();
		setChanged();
	}

	private void insertOutput(ItemStack stack) {
		for (int i = 0; i < outputInv.getContainerSize() && !stack.isEmpty(); i++) {
			ItemStack slotStack = outputInv.getItem(i);
			if (slotStack.isEmpty()) continue;
			if (!ItemStack.isSameItemSameComponents(slotStack, stack)) continue;
			int move = Math.min(stack.getCount(), slotStack.getMaxStackSize() - slotStack.getCount());
			if (move <= 0) continue;
			slotStack.grow(move);
			stack.shrink(move);
		}
		for (int i = 0; i < outputInv.getContainerSize() && !stack.isEmpty(); i++) {
			if (outputInv.getItem(i).isEmpty()) {
				outputInv.setItem(i, stack.copy());
				stack.setCount(0);
			}
		}
	}

	public int insertInput(ItemStack stack) {
		if (stack.isEmpty() || !canProcess(stack)) return 0;
		ItemStack current = inputInv.getItem(0);
		if (current.isEmpty()) {
			int insert = Math.min(stack.getCount(), stack.getMaxStackSize());
			inputInv.setItem(0, stack.copyWithCount(insert));
			setChanged();
			return insert;
		}
		if (!ItemStack.isSameItemSameComponents(current, stack)) return 0;
		int insert = Math.min(stack.getCount(), current.getMaxStackSize() - current.getCount());
		if (insert <= 0) return 0;
		current.grow(insert);
		setChanged();
		return insert;
	}

	public void spawnParticles() {
		ItemStack stackInSlot = inputInv.getItem(0);
		if (stackInSlot.isEmpty()) return;

		ItemParticleOption data = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(stackInSlot));
		float angle = level.getRandom().nextFloat() * 360;
		Vec3 offset = new Vec3(0, 0, 0.5f);
		offset = VecHelper.rotate(offset, angle, Axis.Y);
		Vec3 target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Axis.Y);

		Vec3 center = offset.add(VecHelper.getCenterOf(worldPosition));
		target = VecHelper.offsetRandomly(target.subtract(offset), level.getRandom(), 1 / 128f);
		level.addParticle(data, center.x, center.y, center.z, target.x, target.y, target.z);
	}

	@Override
	protected void write(ValueOutput view, boolean clientPacket) {
		view.putInt("Timer", timer);
		inputInv.write(view.child("InputInventory"));
		outputInv.write(view.child("OutputInventory"));
		super.write(view, clientPacket);
	}

	@Override
	protected void read(ValueInput view, boolean clientPacket) {
		timer = view.getIntOr("Timer", 0);
		inputInv.read(view.childOrEmpty("InputInventory"));
		outputInv.read(view.childOrEmpty("OutputInventory"));
		super.read(view, clientPacket);
	}

	public int getProcessingSpeed() {
		return Mth.clamp((int) Math.abs(getSpeed() / 16f), 1, 512);
	}

	private boolean canProcess(ItemStack stack) {
		if (level == null) return false;
		SingleRecipeInput input = new SingleRecipeInput(stack);
		if (lastRecipe != null && lastRecipe.matches(input, level))
			return true;
		return find(input, level).isPresent();
	}

	public Optional<RecipeHolder<RollingRecipe>> find(SingleRecipeInput input, Level level) {
		if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();
		return serverLevel.recipeAccess().getRecipeFor(CARecipes.ROLLING_TYPE, input, level);
	}

	public class RollingMillInventoryHandler implements SidedItemInventory {
		private static final int[] SLOTS = SlotRangeCache.get(10);

		@Override
		public int getContainerSize() {
			return 10;
		}

		@Override
		public int[] getSlotsForFace(Direction side) {
			return SLOTS;
		}

		@Override
		public boolean canPlaceItem(int slot, ItemStack stack) {
			return slot == 0 && canProcess(stack);
		}

		@Override
		public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction dir) {
			return slot == 0;
		}

		@Override
		public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction dir) {
			return slot != 0;
		}

		@Override
		public ItemStack getItem(int slot) {
			if (slot >= 10) return ItemStack.EMPTY;
			return slot == 0 ? inputInv.getItem(0) : outputInv.getItem(slot - 1);
		}

		@Override
		public void setItem(int slot, ItemStack stack) {
			if (slot >= 10) return;
			if (slot == 0) inputInv.setItem(0, stack);
			else outputInv.setItem(slot - 1, stack);
		}

		@Override
		public void setChanged() {
			RollingMillBlockEntity.this.setChanged();
		}
	}
}
