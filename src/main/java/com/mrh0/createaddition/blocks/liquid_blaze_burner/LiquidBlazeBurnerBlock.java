package com.mrh0.createaddition.blocks.liquid_blaze_burner;

import com.mojang.serialization.MapCodec;
import com.mrh0.createaddition.index.CABlockEntities;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.fluids.FluidInventory;
import com.zurrtum.create.infrastructure.fluids.FluidInventoryProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HEAT_LEVEL;

public class LiquidBlazeBurnerBlock extends HorizontalDirectionalBlock implements IBE<LiquidBlazeBurnerBlockEntity>, IWrenchable, FluidInventoryProvider<LiquidBlazeBurnerBlockEntity> {

	public static final MapCodec<LiquidBlazeBurnerBlock> CODEC = simpleCodec(LiquidBlazeBurnerBlock::new);

	public LiquidBlazeBurnerBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE));
	}

	@Override
	protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
		return CODEC;
	}

	public static BlazeBurnerBlock.HeatLevel getHeatLevelOf(BlockState blockState) {
		return blockState.hasProperty(HEAT_LEVEL) ? blockState.getValue(HEAT_LEVEL)
				: BlazeBurnerBlock.HeatLevel.NONE;
	}

	public static int getLight(BlockState state) {
		BlazeBurnerBlock.HeatLevel level = state.getValue(HEAT_LEVEL);
		return switch (level) {
			case NONE -> 0;
			case SMOULDERING -> 8;
			default -> 15;
		};
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(HEAT_LEVEL, FACING);
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (world.isClientSide()) return;
		BlockEntity blockEntity = world.getBlockEntity(pos.above());
		if (!(blockEntity instanceof BasinBlockEntity basin)) return;
		basin.notifyChangeOfContents();
	}

	@Override
	public Class<LiquidBlazeBurnerBlockEntity> getBlockEntityClass() {
		return LiquidBlazeBurnerBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends LiquidBlazeBurnerBlockEntity> getBlockEntityType() {
		return CABlockEntities.LIQUID_BLAZE_BURNER;
	}

	@Override
	public FluidInventory getFluidInventory(LevelAccessor world, BlockPos pos, BlockState state,
		LiquidBlazeBurnerBlockEntity blockEntity, @Nullable Direction context) {
		return blockEntity.getTank().getCapability();
	}

	@Override
	public Item asItem() {
		return AllItems.BLAZE_BURNER;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
		InteractionHand hand, BlockHitResult hitResult) {
		BlazeBurnerBlock.HeatLevel heat = state.getValue(HEAT_LEVEL);

		if (stack.is(AllItems.GOGGLES) && heat != BlazeBurnerBlock.HeatLevel.NONE)
			return onBlockEntityUseItemOn(level, pos, be -> {
				if (be.goggles) return InteractionResult.TRY_WITH_EMPTY_HAND;
				be.goggles = true;
				be.notifyUpdate();
				return InteractionResult.SUCCESS;
			});

		if (stack.isEmpty() && heat != BlazeBurnerBlock.HeatLevel.NONE)
			return onBlockEntityUseItemOn(level, pos, be -> {
				if (!be.goggles) return InteractionResult.TRY_WITH_EMPTY_HAND;
				be.goggles = false;
				be.notifyUpdate();
				return InteractionResult.SUCCESS;
			});

		boolean doNotConsume = player.isCreative();
		boolean forceOverflow = !FakePlayerHandler.has(player);

		InteractionResult res = tryInsert(state, level, pos, stack, doNotConsume, forceOverflow, false);
		if (res instanceof InteractionResult.Success success) {
			ItemStack leftover = success.heldItemTransformedTo();
			if (!level.isClientSide() && !doNotConsume && leftover != null && !leftover.isEmpty()) {
				if (stack.isEmpty()) {
					player.setItemInHand(hand, leftover);
				} else if (!player.getInventory().add(leftover)) {
					player.drop(leftover, false);
				}
			}
		}

		if (res.consumesAction()) return res;
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	public static InteractionResult tryInsert(BlockState state, Level level, BlockPos pos,
		ItemStack stack, boolean doNotConsume, boolean forceOverflow, boolean simulate) {
		if (!state.hasBlockEntity()) return InteractionResult.FAIL;

		BlockEntity be = level.getBlockEntity(pos);
		if (!(be instanceof LiquidBlazeBurnerBlockEntity burnerBE))
			return InteractionResult.FAIL;

		if (burnerBE.isCreativeFuel(stack)) {
			if (!simulate) burnerBE.applyCreativeFuel();
			return InteractionResult.SUCCESS.heldItemTransformedTo(ItemStack.EMPTY);
		}
		if (!burnerBE.tryUpdateFuel(stack, forceOverflow, simulate)) return InteractionResult.FAIL;

		if (!doNotConsume) {
			ItemStackTemplate container = stack.getItem().getCraftingRemainder();
			if (!level.isClientSide()) {
				stack.shrink(1);
			}
			return InteractionResult.SUCCESS.heldItemTransformedTo(container != null ? container.create() : ItemStack.EMPTY);
		}
		return InteractionResult.SUCCESS.heldItemTransformedTo(ItemStack.EMPTY);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
		return AllShapes.HEATER_BLOCK_SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter getter, BlockPos pos,
		CollisionContext context) {
		if (context == CollisionContext.empty()) return AllShapes.HEATER_BLOCK_SPECIAL_COLLISION_SHAPE;
		return getShape(state, getter, pos, context);
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
		return Math.max(0, state.getValue(HEAT_LEVEL).ordinal() - 1);
	}

	@Override
	protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
		return false;
	}

	@Override
	public void animateTick(BlockState state, Level world, BlockPos pos, RandomSource random) {
		if (random.nextInt(10) != 0) return;
		if (!state.getValue(HEAT_LEVEL).isAtLeast(BlazeBurnerBlock.HeatLevel.SMOULDERING)) return;
		world.playLocalSound((float) pos.getX() + 0.5F, (float) pos.getY() + 0.5F,
			(float) pos.getZ() + 0.5F, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS,
			0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
	}
}
