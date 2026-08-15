package com.mrh0.createaddition.blocks.rolling_mill;

import com.mrh0.createaddition.index.CABlockEntities;
import com.mrh0.createaddition.shapes.CAShapes;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.foundation.block.EntityControlBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.items.ItemInventoryProvider;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RollingMillBlock extends HorizontalKineticBlock implements IBE<RollingMillBlockEntity>, ItemInventoryProvider<RollingMillBlockEntity>, EntityControlBlock {

	public static final VoxelShape ROLLING_MILL_SHAPE = CAShapes.shape(0,0,0,16,5,16).add(2,0,2,14,16,14).build();

	public RollingMillBlock(Properties properties) {
		super(properties);
	}

	@Override
	public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, RollingMillBlockEntity blockEntity, @Nullable Direction context) {
		return blockEntity.capability;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return ROLLING_MILL_SHAPE;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		withBlockEntityDo(level, pos, rollingMill -> {
			boolean emptyOutput = true;
			for (int slot = 0; slot < rollingMill.outputInv.getContainerSize(); slot++) {
				ItemStack stackInSlot = rollingMill.outputInv.getItem(slot);
				if (!stackInSlot.isEmpty())
					emptyOutput = false;
				player.getInventory().placeItemBackInInventory(stackInSlot);
				rollingMill.outputInv.setItem(slot, ItemStack.EMPTY);
			}

			if (emptyOutput) {
				for (int slot = 0; slot < rollingMill.inputInv.getContainerSize(); slot++) {
					player.getInventory().placeItemBackInInventory(rollingMill.inputInv.getItem(slot));
					rollingMill.inputInv.setItem(slot, ItemStack.EMPTY);
				}
			}

			rollingMill.setChanged();
			rollingMill.sendData();
		});

		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.isClientSide()) return InteractionResult.SUCCESS;
		return InteractionResult.PASS;
	}

	@Override
	public void onEntityMovement(Level level, Entity entity) {
		if (level.isClientSide() || !(entity instanceof ItemEntity itemEntity) || !entity.isAlive())
			return;

		BlockPos pos = entity.blockPosition();
		RollingMillBlockEntity rollingMill = getBlockEntity(level, pos);
		if (rollingMill == null) {
			rollingMill = getBlockEntity(level, pos.below());
			if (rollingMill == null)
				return;
		}

		ItemStack stack = itemEntity.getItem();
		int insert = rollingMill.insertInput(stack);
		if (insert == stack.getCount()) {
			itemEntity.discard();
		} else if (insert != 0) {
			stack.shrink(insert);
			itemEntity.setItem(stack);
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction preferredSide = getPreferredHorizontalFacing(context);
		if (preferredSide != null)
			return defaultBlockState().setValue(HORIZONTAL_FACING, preferredSide);
		return super.getStateForPlacement(context);
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.getValue(HORIZONTAL_FACING)
			.getAxis();
	}

	@Override
	public BlockEntityType<? extends RollingMillBlockEntity> getBlockEntityType() {
		return CABlockEntities.ROLLING_MILL;
	}

	@Override
	public Class<RollingMillBlockEntity> getBlockEntityClass() {
		return RollingMillBlockEntity.class;
	}
}
