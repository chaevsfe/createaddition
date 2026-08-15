package com.mrh0.createaddition.blocks.portable_energy_interface;

import com.mrh0.createaddition.index.CABlockEntities;
import com.zurrtum.create.AllShapes;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.WrenchableDirectionalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PortableEnergyInterfaceBlock extends WrenchableDirectionalBlock implements IBE<PortableEnergyInterfaceBlockEntity> {

	public PortableEnergyInterfaceBlock(Properties properties) {
		super(properties);
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, @Nullable Orientation orientation, boolean movedByPiston) {
		withBlockEntityDo(world, pos, PortableEnergyInterfaceBlockEntity::neighbourChanged);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction direction = context.getNearestLookingDirection();
		if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
			direction = direction.getOpposite();
		}

		return defaultBlockState().setValue(FACING, direction.getOpposite());
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AllShapes.PORTABLE_STORAGE_INTERFACE.get(state.getValue(FACING));
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos, Direction direction) {
		return getBlockEntityOptional(worldIn, pos).map(te -> te.isConnected() ? 15 : 0).orElse(0);
	}

	@Override
	public Class<PortableEnergyInterfaceBlockEntity> getBlockEntityClass() {
		return PortableEnergyInterfaceBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends PortableEnergyInterfaceBlockEntity> getBlockEntityType() {
		return CABlockEntities.PORTABLE_ENERGY_INTERFACE;
	}
}
