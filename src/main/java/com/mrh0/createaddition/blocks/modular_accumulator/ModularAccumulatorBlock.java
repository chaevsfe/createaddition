package com.mrh0.createaddition.blocks.modular_accumulator;

import com.mrh0.createaddition.index.CABlockEntities;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.blockEntity.ComparatorUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ModularAccumulatorBlock extends Block implements IWrenchable, IBE<ModularAccumulatorBlockEntity> {

	public static final BooleanProperty TOP = BooleanProperty.create("top");
	public static final BooleanProperty BOTTOM = BooleanProperty.create("bottom");

	public static ModularAccumulatorBlock regular(Properties props) {
		return new ModularAccumulatorBlock(props);
	}

	public static boolean isAccumulator(BlockState state) {
		return state.getBlock() instanceof ModularAccumulatorBlock;
	}

	protected ModularAccumulatorBlock(Properties props) {
		super(props);
		registerDefaultState(defaultBlockState().setValue(TOP, true)
			.setValue(BOTTOM, true));
	}

	@Override
	public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
		if (oldState.getBlock() == state.getBlock()) return;
		if (moved) return;
		withBlockEntityDo(world, pos, ModularAccumulatorBlockEntity::updateConnectivity);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(TOP, BOTTOM);
	}

	@Override
	public InteractionResult onWrenched(BlockState state, UseOnContext context) {
		return InteractionResult.SUCCESS;
	}

	static final VoxelShape CAMPFIRE_SMOKE_CLIP = Block.box(0, 4, 0, 16, 16, 16);

	@Override
	public VoxelShape getCollisionShape(BlockState pState, BlockGetter pLevel, BlockPos pPos,
		CollisionContext pContext) {
		if (pContext == CollisionContext.empty())
			return CAMPFIRE_SMOKE_CLIP;
		return pState.getShape(pLevel, pPos);
	}

	@Override
	public VoxelShape getBlockSupportShape(BlockState pState, BlockGetter pReader, BlockPos pPos) {
		return Shapes.block();
	}

	@Override
	public BlockState updateShape(BlockState pState, LevelReader pLevel, ScheduledTickAccess tickView,
		BlockPos pCurrentPos, Direction pDirection, BlockPos pNeighborPos, BlockState pNeighborState,
		RandomSource random) {
		return pState;
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos pos, Direction direction) {
		return getBlockEntityOptional(level, pos).map(ModularAccumulatorBlockEntity::getControllerBE)
			.map(te -> ComparatorUtil.fractionToRedstoneLevel(te.getFillState()))
			.orElse(0);
	}

	@Override
	public Class<ModularAccumulatorBlockEntity> getBlockEntityClass() {
		return ModularAccumulatorBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ModularAccumulatorBlockEntity> getBlockEntityType() {
		return CABlockEntities.MODULAR_ACCUMULATOR;
	}
}
