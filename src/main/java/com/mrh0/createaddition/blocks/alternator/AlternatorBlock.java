package com.mrh0.createaddition.blocks.alternator;

import com.mrh0.createaddition.index.CABlockEntities;
import com.mrh0.createaddition.shapes.CAShapes;
import com.zurrtum.create.catnip.math.VoxelShaper;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AlternatorBlock extends DirectionalKineticBlock implements IBE<AlternatorBlockEntity> {

	public static final VoxelShaper ALTERNATOR_SHAPE = CAShapes.shape(0, 3, 0, 16, 13, 16).add(2, 2, 2, 14, 14, 14).forDirectional();

	public AlternatorBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return ALTERNATOR_SHAPE.get(state.getValue(FACING));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction preferred = getPreferredFacing(context);
		if ((context.getPlayer() != null && context.getPlayer()
			.isShiftKeyDown()) || preferred == null)
			return super.getStateForPlacement(context);
		return defaultBlockState().setValue(FACING, preferred);
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return face.getAxis() == state.getValue(FACING).getAxis();
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING)
			.getAxis();
	}

	@Override
	public IRotate.SpeedLevel getMinimumRequiredSpeedLevel() {
		return IRotate.SpeedLevel.MEDIUM;
	}

	@Override
	public BlockEntityType<? extends AlternatorBlockEntity> getBlockEntityType() {
		return CABlockEntities.ALTERNATOR;
	}

	@Override
	public Class<AlternatorBlockEntity> getBlockEntityClass() {
		return AlternatorBlockEntity.class;
	}
}
