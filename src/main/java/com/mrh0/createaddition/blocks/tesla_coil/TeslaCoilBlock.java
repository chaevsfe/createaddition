package com.mrh0.createaddition.blocks.tesla_coil;

import com.mrh0.createaddition.index.CABlockEntities;
import com.mrh0.createaddition.shapes.CAShapes;
import com.zurrtum.create.catnip.math.VoxelShaper;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TeslaCoilBlock extends Block implements IBE<TeslaCoilBlockEntity>, IWrenchable, RedStoneConnectBlock {

	public static final VoxelShaper TESLA_COIL_SHAPE = CAShapes.shape(0, 0, 0, 16, 10, 16).add(1, 0, 1, 15, 12, 15).forDirectional();
	public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

	public TeslaCoilBlock(Properties props) {
		super(props);
		registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return TESLA_COIL_SHAPE.get(state.getValue(FACING).getOpposite());
	}

	@Override
	public Class<TeslaCoilBlockEntity> getBlockEntityClass() {
		return TeslaCoilBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends TeslaCoilBlockEntity> getBlockEntityType() {
		return CABlockEntities.TESLA_COIL;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, POWERED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext c) {
		return this.defaultBlockState().setValue(FACING, c.isSecondaryUseActive() ? c.getClickedFace() : c.getClickedFace().getOpposite());
	}

	public void setPowered(Level world, BlockPos pos, boolean powered) {
		world.setBlock(pos, defaultBlockState().setValue(FACING, world.getBlockState(pos).getValue(FACING)).setValue(POWERED, powered), 3);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, @Nullable Direction side) {
		return true;
	}
}
