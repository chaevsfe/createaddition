package com.mrh0.createaddition.blocks.liquid_blaze_burner;

import java.util.List;
import java.util.Optional;

import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.network.IObserveBlockEntity;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.mrh0.createaddition.network.TimeRemainingPacketPayload;
import com.mrh0.createaddition.recipe.FluidRecipeWrapper;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.zurrtum.create.AllFluidItemInventory;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.fluids.tank.FluidTankBlock;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.infrastructure.fluids.FluidItemInventory;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HEAT_LEVEL;

public class LiquidBlazeBurnerBlockEntity extends SmartBlockEntity implements IObserveBlockEntity {

	public static final int BUCKET_AMOUNT = 81000;
	public static final int BURN_CONSUMPTION = 8100;

	protected FuelType activeFuel;
	protected int remainingBurnTime;
	public LerpedFloat headAnimation;
	public LerpedFloat headAngle;
	protected boolean isCreative;
	public boolean goggles;
	protected boolean hat;

	protected SmartFluidTankBehaviour tank;

	private Optional<RecipeHolder<LiquidBurningRecipe>> recipeCache = Optional.empty();
	private Fluid lastFluid = null;
	private boolean first = true;

	public LiquidBlazeBurnerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		activeFuel = FuelType.NONE;
		remainingBurnTime = 0;
		headAnimation = LerpedFloat.linear();
		headAngle = LerpedFloat.angular();
		isCreative = false;
		goggles = false;

		headAngle.startWithValue((AngleHelper.horizontalAngle(state.getValueOrElse(LiquidBlazeBurnerBlock.FACING,
			Direction.SOUTH)) + 180) % 360);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
		tank = SmartFluidTankBehaviour.single(this, maxLiquidCapacity())
			.whenFluidUpdates(this::onFluidStackChanged);
		behaviours.add(tank);
	}

	public static int maxLiquidCapacity() {
		return CACommonConfig.COMMON.LIQUID_BLAZE_BURNER_MAX_LIQUID_CAPACITY.get() * 81;
	}

	public static int maxHeatCapacity() {
		return CACommonConfig.COMMON.LIQUID_BLAZE_BURNER_MAX_HEAT_CAPACITY.get();
	}

	public SmartFluidTankBehaviour getTank() {
		return tank;
	}

	public BlazeBurnerBlock.HeatLevel getHeatLevelForRender() {
		return getHeatLevelFromBlock();
	}

	protected void onFluidStackChanged() {
		if (!hasLevel()) return;
		update(tank.getPrimaryHandler().getStack(0));
	}

	private void update(FluidStack stack) {
		if (level == null) return;
		if (level.isClientSide()) return;
		if (stack.getFluid() != lastFluid) recipeCache = find(stack, level);
		lastFluid = stack.getFluid();
	}

	public Optional<RecipeHolder<LiquidBurningRecipe>> find(@Nullable FluidStack stack, @Nullable Level level) {
		if (stack == null || stack.isEmpty() || level == null) return Optional.empty();
		if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();
		return serverLevel.recipeAccess().getRecipeFor(CARecipes.LIQUID_BURNING_TYPE,
			new FluidRecipeWrapper(new FluidStack(stack.getFluid(), BUCKET_AMOUNT)), level);
	}

	public void burningTick() {
		if (level == null) return;
		if (level.isClientSide()) return;

		if (first) update(tank.getPrimaryHandler().getStack(0));
		first = false;

		if (tank.getPrimaryHandler().getStack(0).getAmount() < BURN_CONSUMPTION) return;
		if (remainingBurnTime > maxHeatCapacity()) return;
		if (recipeCache.isEmpty()) return;

		try {
			var recipe = recipeCache.get().value();
			var burnTime = recipe.getBurnTime() / 10;
			var fuelType = recipe.isSuperheated() ? FuelType.SPECIAL : FuelType.NORMAL;
			remainingBurnTime = activeFuel == fuelType ? remainingBurnTime + burnTime : burnTime;
			activeFuel = fuelType;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		FluidStack contained = tank.getPrimaryHandler().getStack(0);
		tank.getPrimaryHandler().extract(new FluidStack(contained.getFluid(), BURN_CONSUMPTION));

		BlazeBurnerBlock.HeatLevel prev = getHeatLevelFromBlock();
		playSound();
		updateBlockState();

		if (prev != getHeatLevelFromBlock()) {
			level.playSound(null, worldPosition, SoundEvents.BLAZE_AMBIENT, SoundSource.BLOCKS,
				.125f + level.getRandom().nextFloat() * .125f, 1.15f - level.getRandom().nextFloat() * .25f);

			spawnParticleBurst(activeFuel == FuelType.SPECIAL);
		}
	}

	public FuelType getActiveFuel() {
		return activeFuel;
	}

	public int getRemainingBurnTime() {
		return remainingBurnTime;
	}

	public boolean isCreative() {
		return isCreative;
	}

	@Override
	public void tick() {
		super.tick();

		if (level == null) return;
		if (level.isClientSide()) {
			if (!isVirtual()) spawnParticles(getHeatLevelForRender(), 1);
			return;
		}

		burningTick();

		if (isCreative) return;

		if (remainingBurnTime > 0) remainingBurnTime--;
		if (remainingBurnTime > 0) return;

		if (activeFuel == FuelType.SPECIAL) {
			activeFuel = FuelType.NORMAL;
			remainingBurnTime = maxHeatCapacity() / 2;
		} else activeFuel = FuelType.NONE;

		updateBlockState();
	}

	@Override
	public void write(ValueOutput view, boolean clientPacket) {
		if (!isCreative) {
			view.putInt("fuelLevel", activeFuel.ordinal());
			view.putInt("burnTimeRemaining", remainingBurnTime);
		} else view.putBoolean("isCreative", true);
		if (goggles) view.putBoolean("Goggles", true);
		if (hat) view.putBoolean("TrainHat", true);
		super.write(view, clientPacket);
	}

	@Override
	protected void read(ValueInput view, boolean clientPacket) {
		activeFuel = FuelType.values()[view.getIntOr("fuelLevel", 0)];
		remainingBurnTime = view.getIntOr("burnTimeRemaining", 0);
		isCreative = view.getBooleanOr("isCreative", false);
		goggles = view.getBooleanOr("Goggles", false);
		hat = view.getBooleanOr("TrainHat", false);
		super.read(view, clientPacket);
	}

	public BlazeBurnerBlock.HeatLevel getHeatLevelFromBlock() {
		return LiquidBlazeBurnerBlock.getHeatLevelOf(getBlockState());
	}

	public void updateBlockState() {
		setBlockHeat(getHeatLevelFromFuelType(activeFuel));
	}

	protected void setBlockHeat(BlazeBurnerBlock.HeatLevel heat) {
		if (level == null) return;
		if (getHeatLevelFromBlock() == heat) return;
		level.setBlockAndUpdate(worldPosition, getBlockState().setValue(HEAT_LEVEL, heat));
		notifyUpdate();
	}

	private boolean tryUpdateLiquid(ItemStack itemStack, boolean simulate) {
		if (level == null) return false;
		FluidItemInventory itemInventory = AllFluidItemInventory.of(itemStack);
		if (itemInventory == null) return false;
		try (itemInventory) {
			FluidStack stack = itemInventory.getStack(0);
			if (stack.isEmpty()) return false;
			if (find(stack, level).isEmpty()) return false;

			FluidStack toFill = new FluidStack(stack.getFluid(), BUCKET_AMOUNT);
			if (tank.getPrimaryHandler().countSpace(toFill) < BUCKET_AMOUNT) return false;

			if (!simulate) {
				tank.getPrimaryHandler().insert(toFill);
				level.playSound(null, getBlockPos(), SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS,
					.125f + level.getRandom().nextFloat() * .125f, .75f - level.getRandom().nextFloat() * .25f);
			}
			return true;
		}
	}

	protected boolean tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate) {
		if (isCreative) return false;

		FuelType newFuel = FuelType.NONE;
		int newBurnTime;

		if (tryUpdateLiquid(itemStack, simulate)) return true;

		if (itemStack.is(AllItemTags.BLAZE_BURNER_FUEL_SPECIAL)) {
			newBurnTime = 3200;
			newFuel = FuelType.SPECIAL;
		} else {
			newBurnTime = level == null ? 0 : level.fuelValues().burnDuration(itemStack);
			if (newBurnTime > 0)
				newFuel = FuelType.NORMAL;
			else if (itemStack.is(AllItemTags.BLAZE_BURNER_FUEL_REGULAR)) {
				newBurnTime = 1600;
				newFuel = FuelType.NORMAL;
			}
		}

		if (newFuel == FuelType.NONE) return false;
		if (newFuel.ordinal() < activeFuel.ordinal()) return false;
		if (activeFuel == FuelType.SPECIAL && remainingBurnTime > 20) return false;

		if (newFuel == activeFuel) {
			if (remainingBurnTime + newBurnTime > maxHeatCapacity() && !forceOverflow) return false;
			newBurnTime = Mth.clamp(remainingBurnTime + newBurnTime, 0, maxHeatCapacity());
		}

		if (simulate) return true;

		activeFuel = newFuel;
		remainingBurnTime = newBurnTime;

		if (level == null) return false;
		if (level.isClientSide()) {
			spawnParticleBurst(activeFuel == FuelType.SPECIAL);
			return true;
		}

		BlazeBurnerBlock.HeatLevel prev = getHeatLevelFromBlock();
		playSound();
		updateBlockState();

		if (prev != getHeatLevelFromBlock())
			level.playSound(null, worldPosition, SoundEvents.BLAZE_AMBIENT, SoundSource.BLOCKS,
				.125f + level.getRandom().nextFloat() * .125f, 1.15f - level.getRandom().nextFloat() * .25f);

		return true;
	}

	protected void applyCreativeFuel() {
		activeFuel = FuelType.NONE;
		remainingBurnTime = 0;
		isCreative = true;

		BlazeBurnerBlock.HeatLevel next = getHeatLevelFromBlock().nextActiveLevel();

		if (level.isClientSide()) {
			spawnParticleBurst(next.isAtLeast(BlazeBurnerBlock.HeatLevel.SEETHING));
			return;
		}

		playSound();
		if (next == BlazeBurnerBlock.HeatLevel.FADING)
			next = next.nextActiveLevel();
		setBlockHeat(next);
	}

	public boolean isCreativeFuel(ItemStack stack) {
		return stack.is(AllItems.CREATIVE_BLAZE_CAKE);
	}

	public boolean isValidBlockAbove() {
		if (isVirtual()) return false;
		BlockState blockState = level.getBlockState(worldPosition.above());
		return BasinBlock.isBasin(level, worldPosition.above()) || blockState.getBlock() instanceof FluidTankBlock;
	}

	protected void playSound() {
		level.playSound(null, worldPosition, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS,
			.125f + level.getRandom().nextFloat() * .125f, .75f - level.getRandom().nextFloat() * .25f);
	}

	protected BlazeBurnerBlock.HeatLevel getHeatLevelFromFuelType(FuelType fuel) {
		BlazeBurnerBlock.HeatLevel level = BlazeBurnerBlock.HeatLevel.SMOULDERING;
		switch (activeFuel) {
			case SPECIAL:
				level = BlazeBurnerBlock.HeatLevel.SEETHING;
				break;
			case NORMAL:
				boolean lowPercent = (double) remainingBurnTime / maxHeatCapacity() < 0.0125;
				level = lowPercent ? BlazeBurnerBlock.HeatLevel.FADING : BlazeBurnerBlock.HeatLevel.KINDLED;
				break;
			case NONE:
			default:
				break;
		}
		return level;
	}

	protected void spawnParticles(BlazeBurnerBlock.HeatLevel heatLevel, double burstMult) {
		if (level == null) return;
		if (heatLevel == BlazeBurnerBlock.HeatLevel.NONE) return;

		RandomSource r = level.getRandom();

		Vec3 c = VecHelper.getCenterOf(worldPosition);
		Vec3 v = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .125f)
			.multiply(1, 0, 1));

		if (r.nextInt(3) == 0) level.addParticle(ParticleTypes.LARGE_SMOKE, v.x, v.y, v.z, 0, 0, 0);
		if (r.nextInt(2) != 0) return;

		boolean empty = level.getBlockState(worldPosition.above())
			.getCollisionShape(level, worldPosition.above())
			.isEmpty();

		double yMotion = empty ? .0625f : r.nextDouble() * .0125f;
		Vec3 v2 = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .5f)
			.multiply(1, .25f, 1)
			.normalize()
			.scale((empty ? .25f : .5) + r.nextDouble() * .125f))
			.add(0, .5, 0);

		if (heatLevel.isAtLeast(BlazeBurnerBlock.HeatLevel.SEETHING)) {
			level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
		} else if (heatLevel.isAtLeast(BlazeBurnerBlock.HeatLevel.FADING)) {
			level.addParticle(ParticleTypes.FLAME, v2.x, v2.y, v2.z, 0, yMotion, 0);
		}
	}

	public void spawnParticleBurst(boolean soulFlame) {
		Vec3 c = VecHelper.getCenterOf(worldPosition);
		RandomSource r = level.getRandom();
		for (int i = 0; i < 20; i++) {
			Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, r, .5f)
				.multiply(1, .25f, 1)
				.normalize();
			Vec3 v = c.add(offset.scale(.5 + r.nextDouble() * .125f))
				.add(0, .125, 0);
			Vec3 m = offset.scale(1 / 32f);

			level.addParticle(soulFlame ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, v.x, v.y, v.z, m.x, m.y, m.z);
		}
	}

	public enum FuelType {
		NONE, NORMAL, SPECIAL
	}

	@Override
	public void onObserved(ServerPlayer player, ObservePacketPayload pack) {
		TimeRemainingPacketPayload.send(remainingBurnTime, player);
	}
}
