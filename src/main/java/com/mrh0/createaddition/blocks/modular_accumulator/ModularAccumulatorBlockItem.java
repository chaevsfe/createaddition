package com.mrh0.createaddition.blocks.modular_accumulator;

import com.mrh0.createaddition.index.CABlockEntities;
import com.zurrtum.create.foundation.item.ItemPlacementSoundContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class ModularAccumulatorBlockItem extends BlockItem {

	public ModularAccumulatorBlockItem(Block block, Properties props) {
		super(block, props);
	}

	@Override
	public InteractionResult place(BlockPlaceContext ctx) {
		InteractionResult initialResult = super.place(ctx);
		if (!initialResult.consumesAction()) return initialResult;
		tryMultiPlace(ctx);
		return initialResult;
	}

	private void tryMultiPlace(BlockPlaceContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null) return;
		if (player.isShiftKeyDown()) return;
		Direction face = ctx.getClickedFace();
		if (!face.getAxis().isVertical()) return;
		ItemStack stack = ctx.getItemInHand();
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockPos placedOnPos = pos.relative(face.getOpposite());
		BlockState placedOnState = world.getBlockState(placedOnPos);

		if (!ModularAccumulatorBlock.isAccumulator(placedOnState)) return;
		ModularAccumulatorBlockEntity accumulatorAt = CAConnectivityHandler.partAt(CABlockEntities.MODULAR_ACCUMULATOR, world, placedOnPos);
		if (accumulatorAt == null) return;
		ModularAccumulatorBlockEntity controllerBE = accumulatorAt.getControllerBE();
		if (controllerBE == null) return;

		int width = controllerBE.width;
		if (width == 1) return;

		int blocksToPlace = 0;
		BlockPos startPos = face == Direction.DOWN ? controllerBE.getBlockPos()
			.below()
			: controllerBE.getBlockPos()
				.above(controllerBE.height);

		if (startPos.getY() != pos.getY()) return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (ModularAccumulatorBlock.isAccumulator(blockState)) continue;
				if (!blockState.canBeReplaced()) return;
				blocksToPlace++;
			}
		}

		if (!player.isCreative() && stack.getCount() < blocksToPlace) return;

		ItemPlacementSoundContext context = new ItemPlacementSoundContext(ctx, 0.1f, 1.5f, SILENCED_METAL.getPlaceSound());
		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (ModularAccumulatorBlock.isAccumulator(blockState)) continue;
				super.place(context.offset(offsetPos, face));
			}
		}
	}

	public static final SoundType SILENCED_METAL =
		new SoundType(0.1F, 1.5F, SoundEvents.METAL_BREAK, SoundEvents.METAL_STEP,
			SoundEvents.METAL_PLACE, SoundEvents.METAL_HIT, SoundEvents.METAL_FALL);
}
