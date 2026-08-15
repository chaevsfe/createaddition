package com.mrh0.createaddition.blocks.tesla_coil;

import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.energy.AbstractElectricBlockEntity;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CADamageTypes;
import com.mrh0.createaddition.index.CAEffects;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.index.CASounds;
import com.mrh0.createaddition.network.IObserveBlockEntity;
import com.mrh0.createaddition.network.ObservePacketPayload;
import com.mrh0.createaddition.network.TimeRemainingPacketPayload;
import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.infrastructure.items.ItemStackHandler;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import team.reborn.energy.api.EnergyStorage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TeslaCoilBlockEntity extends AbstractElectricBlockEntity implements IObserveBlockEntity {

	private Optional<RecipeHolder<ChargingRecipe>> recipeCache = Optional.empty();

	private final ItemStackHandler inputInv;
	private int chargeAccumulator;
	protected int poweredTimer = 0;

	public TeslaCoilBlockEntity(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
		super(tileEntityTypeIn, pos, state);
		inputInv = new ItemStackHandler(1);
	}

	@Override
	public long getCapacity() {
		return Util.max(CACommonConfig.COMMON.TESLA_COIL_CAPACITY.get(), CACommonConfig.COMMON.TESLA_COIL_CHARGE_RATE.get(), CACommonConfig.COMMON.TESLA_COIL_RECIPE_CHARGE_RATE.get());
	}

	@Override
	public long getMaxIn() {
		return CACommonConfig.COMMON.TESLA_COIL_MAX_INPUT.get();
	}

	@Override
	public long getMaxOut() {
		return 0;
	}

	public BeltProcessingBehaviour processingBehaviour;

	@Override
	public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
		super.addBehaviours(behaviours);
		processingBehaviour =
			new BeltProcessingBehaviour(this).whenItemEnters((s, i) -> TeslaCoilBeltCallbacks.onItemReceived(s, i, this))
				.whileItemHeld((s, i) -> TeslaCoilBeltCallbacks.whenItemHeld(s, i, this));
		behaviours.add(processingBehaviour);
	}

	@Override
	public boolean isEnergyInput(Direction side) {
		return side != getBlockState().getValue(TeslaCoilBlock.FACING).getOpposite();
	}

	@Override
	public boolean isEnergyOutput(Direction side) {
		return false;
	}

	public long getConsumption() {
		return CACommonConfig.COMMON.TESLA_COIL_CHARGE_RATE.get();
	}

	protected float getItemCharge(EnergyStorage energy) {
		if (energy == null) return 0f;
		return (float) energy.getAmount() / (float) energy.getCapacity();
	}

	protected BeltProcessingBehaviour.ProcessingResult onCharge(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
		return chargeCompoundAndStack(transported, handler);
	}

	private void doDmg() {
		localEnergy.internalConsumeEnergy(CACommonConfig.COMMON.TESLA_COIL_HURT_ENERGY_REQUIRED.get());
		BlockPos origin = getBlockPos().relative(getBlockState().getValue(TeslaCoilBlock.FACING).getOpposite());
		List<LivingEntity> ents = getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(origin).inflate(CACommonConfig.COMMON.TESLA_COIL_HURT_RANGE.get()));
		boolean zapped = false;
		for(LivingEntity e : ents) {
			if(e == null) return;

			boolean allChain = true;
			for(EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
				ItemStack armor = e.getItemBySlot(slot);
				if(armor.is(Items.CHAINMAIL_BOOTS)) continue;
				if(armor.is(Items.CHAINMAIL_LEGGINGS)) continue;
				if(armor.is(Items.CHAINMAIL_CHESTPLATE)) continue;
				if(armor.is(Items.CHAINMAIL_HELMET)) continue;
				allChain = false;
				break;
			}
			if(allChain) continue;

			int dmg = CACommonConfig.COMMON.TESLA_COIL_HURT_DMG_MOB.get();
			int time = CACommonConfig.COMMON.TESLA_COIL_HURT_EFFECT_TIME_MOB.get();
			if(e instanceof Player) {
				dmg = CACommonConfig.COMMON.TESLA_COIL_HURT_DMG_PLAYER.get();
				time = CACommonConfig.COMMON.TESLA_COIL_HURT_EFFECT_TIME_PLAYER.get();
			}

			if(dmg > 0 && level instanceof ServerLevel serverLevel) {
				e.hurtServer(serverLevel, CADamageTypes.teslaCoil(level), dmg);
				if (!zapped) {
					if (CACommonConfig.COMMON.AUDIO_ENABLED.get()) level.playSound(null, worldPosition, CASounds.LOUD_ZAP, SoundSource.BLOCKS, 0.6f, 1f);
					zapped = true;
				}
			}
			if(time > 0) e.addEffect(new MobEffectInstance(CAEffects.SHOCKING, time));
		}
	}

	int dmgTick = 0;
	int zapTimer = 200;

	@Override
	public void tick() {
		super.tick();
		if(level == null) return;
		if(level.isClientSide()) return;

		int signal = level.getBestNeighborSignal(getBlockPos());
		if(signal > 0 && localEnergy.getAmount() >= CACommonConfig.COMMON.TESLA_COIL_HURT_ENERGY_REQUIRED.get()) poweredTimer = 10;

		dmgTick++;
		if((dmgTick %= CACommonConfig.COMMON.TESLA_COIL_HURT_FIRE_COOLDOWN.get()) == 0 && localEnergy.getAmount() >= CACommonConfig.COMMON.TESLA_COIL_HURT_ENERGY_REQUIRED.get() && signal > 0) doDmg();

		if(poweredTimer > 0) {
			if (zapTimer == 0) {
				if (CACommonConfig.COMMON.AUDIO_ENABLED.get()) level.playSound(null, worldPosition, CASounds.LITTLE_ZAP, SoundSource.BLOCKS, 0.1f, 1f);
				zapTimer = level.getRandom().nextInt(100, 300);
			}
			zapTimer--;

			if(!isPoweredState()) CABlocks.TESLA_COIL.setPowered(level, getBlockPos(), true);
			poweredTimer--;
		}
		else if(isPoweredState()) CABlocks.TESLA_COIL.setPowered(level, getBlockPos(), false);
	}

	public boolean isPoweredState() {
		return getBlockState().getValue(TeslaCoilBlock.POWERED);
	}

	protected BeltProcessingBehaviour.ProcessingResult chargeCompoundAndStack(TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {

		ItemStack stack = transported.stack;
		if(stack == null) return BeltProcessingBehaviour.ProcessingResult.PASS;
		if(chargeStack(stack, transported, handler)) {
			poweredTimer = 10;
			return BeltProcessingBehaviour.ProcessingResult.HOLD;
		}
		else if(chargeRecipe(stack, transported, handler)) {
			if (energyRemoved > 0) poweredTimer = 10;
			return BeltProcessingBehaviour.ProcessingResult.HOLD;
		}
		return BeltProcessingBehaviour.ProcessingResult.PASS;
	}

	protected boolean chargeStack(ItemStack stack, TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
		ContainerItemContext context = ContainerItemContext.ofSingleSlot(new SingleStackStorage() {
			@Override
			protected ItemStack getStack() {
				return transported.stack;
			}

			@Override
			protected void setStack(ItemStack stack) {
				transported.stack = stack;
			}
		});
		EnergyStorage es = EnergyStorage.ITEM.find(transported.stack, context);
		if (es == null) return false;
		try(Transaction t = Transaction.openOuter()) {
			if(es.insert(1, t) != 1) return false;
		}
		if(localEnergy.getAmount() < transported.stack.getCount()) return false;
		try(Transaction t = Transaction.openOuter()) {
			localEnergy.internalConsumeEnergy(es.insert(Math.min(getConsumption(), localEnergy.getAmount()), t));
			t.commit();
		}
		return true;
	}

	private int energyRemoved = 0;
	private final int[] chargeRateHistory = new int[20];
	private int chargeRateIndex = 0;
	private int chargeRateSamples = 0;

	private boolean chargeRecipe(ItemStack stack, TransportedItemStack transported, TransportedItemStackHandlerBehaviour handler) {
		if(this.getLevel() == null) return false;
		if(!inputInv.getItem(0).is(stack.getItem())) {
			inputInv.setItem(0, stack);
			recipeCache = find(new SingleRecipeInput(stack), this.getLevel());
			chargeAccumulator = 0;
			Arrays.fill(chargeRateHistory, 0);
			chargeRateIndex = 0;
			chargeRateSamples = 0;
		}
		if(recipeCache.isPresent()) {
			ChargingRecipe recipe = recipeCache.get().value();
			energyRemoved = (int) localEnergy.internalConsumeEnergy(Util.min(CACommonConfig.COMMON.TESLA_COIL_RECIPE_CHARGE_RATE.get(), recipe.getEnergy() - chargeAccumulator, recipe.getMaxChargeRate()));
			chargeRateHistory[chargeRateIndex] = energyRemoved;
			chargeRateIndex = (chargeRateIndex + 1) % 20;
			if (chargeRateSamples < 20) chargeRateSamples++;
			chargeAccumulator += energyRemoved;
			if(chargeAccumulator >= recipe.getEnergy()) {
				TransportedItemStack remainingStack = transported.copy();
				TransportedItemStack result = transported.copy();
				result.stack = recipe.getResultStack();
				remainingStack.stack.shrink(1);
				List<TransportedItemStack> outList = new ArrayList<>();
				outList.add(result);
				handler.handleProcessingOnItem(transported, TransportedItemStackHandlerBehaviour.TransportedResult.convertToAndLeaveHeld(outList, remainingStack));
				chargeAccumulator = 0;

				if (CACommonConfig.COMMON.AUDIO_ENABLED.get()) level.playSound(null, worldPosition, CASounds.LITTLE_ZAP, SoundSource.BLOCKS, 0.1f, 1f);
			}
			return true;
		}
		return false;
	}

	public Optional<RecipeHolder<ChargingRecipe>> find(SingleRecipeInput input, Level level) {
		if(!(level instanceof ServerLevel serverLevel)) return Optional.empty();
		return serverLevel.recipeAccess().getRecipeFor(CARecipes.CHARGING_TYPE, input, level);
	}

	@Override
	public void onObserved(ServerPlayer player, ObservePacketPayload pkt) {
		int timeRemaining = 0;
		if(recipeCache.isPresent() && chargeRateSamples > 0) {
			ChargingRecipe recipe = recipeCache.get().value();
			int totalRate = 0;
			for (int rate : chargeRateHistory) totalRate += rate;
			int avgChargeRate = totalRate / chargeRateSamples;
			if (avgChargeRate == 0) {
				TimeRemainingPacketPayload.send(-1, player);
				return;
			}
			timeRemaining = (recipe.getEnergy() - chargeAccumulator) / avgChargeRate;
		}
		TimeRemainingPacketPayload.send(timeRemaining, player);
	}
}
