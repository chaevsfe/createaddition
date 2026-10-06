package com.mrh0.createaddition.blocks.servo_motor;

import com.mrh0.createaddition.index.CABlockEntities;
import com.mrh0.createaddition.shapes.CAShapes;
import com.zurrtum.create.api.contraption.transformable.TransformableBlock;
import com.zurrtum.create.catnip.math.VoxelShaper;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.RedStoneConnectBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;

public class ServoMotorBlock extends DirectionalKineticBlock implements IBE<ServoMotorBlockEntity>, RedStoneConnectBlock, TransformableBlock {

	private static final VoxelShaper OCCLUSION_SHAPE = CAShapes.shape(0, 0, 0, 16, 12, 16).forDirectional();

	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final IntegerProperty ROLL = IntegerProperty.create("roll", 0, 3);

	public ServoMotorBlock(Properties properties) {
		super(properties);
		registerDefaultState(defaultBlockState().setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(POWERED, ROLL);
	}

	private static Direction getReferenceTop(Direction facing) {
		return facing.getAxis() == Axis.Y ? Direction.EAST : Direction.UP;
	}

	public static Direction getTop(BlockState state) {
		Direction facing = state.getValue(FACING);
		Direction top = getReferenceTop(facing);
		for (int i = state.getValue(ROLL); i > 0; i--)
			top = top.getClockWise(facing.getAxis());
		return top;
	}

	public static BlockState withOrientation(BlockState state, Direction facing, Direction top) {
		Direction candidate = getReferenceTop(facing);
		for (int roll = 0; roll < 4; roll++) {
			if (candidate == top)
				return state.setValue(FACING, facing).setValue(ROLL, roll);
			candidate = candidate.getClockWise(facing.getAxis());
		}
		return state.setValue(FACING, facing).setValue(ROLL, 0);
	}

	@Override
	public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
		Axis axis = targetedFace.getAxis();
		return withOrientation(originalState,
				originalState.getValue(FACING).getClockWise(axis),
				getTop(originalState).getClockWise(axis));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return withOrientation(state, rot.rotate(state.getValue(FACING)), rot.rotate(getTop(state)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return withOrientation(state, mirror.mirror(state.getValue(FACING)), mirror.mirror(getTop(state)));
	}

	@Override
	public BlockState transform(BlockState state, StructureTransform transform) {
		return withOrientation(state,
				transform.rotateFacing(transform.mirrorFacing(state.getValue(FACING))),
				transform.rotateFacing(transform.mirrorFacing(getTop(state))));
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction preferred = getPreferredFacing(context);
		if ((context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) || preferred == null)
			return super.getStateForPlacement(context);
		return defaultBlockState().setValue(FACING, preferred);
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state) {
		return OCCLUSION_SHAPE.get(state.getValue(FACING));
	}

	@Override
	public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
		return false;
	}

	@Override
	public Axis getRotationAxis(BlockState state) {
		return state.getValue(FACING).getAxis();
	}

	@Override
	public Class<ServoMotorBlockEntity> getBlockEntityClass() {
		return ServoMotorBlockEntity.class;
	}

	@Override
	public BlockEntityType<? extends ServoMotorBlockEntity> getBlockEntityType() {
		return CABlockEntities.SERVO_MOTOR;
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (!player.mayBuild())
			return InteractionResult.FAIL;
		if (player.isShiftKeyDown())
			return InteractionResult.FAIL;
		if (stack.isEmpty()) {
			if (level.isClientSide())
				return InteractionResult.SUCCESS;
			withBlockEntityDo(level, pos, be -> {
				if (be.isRunning()) {
					be.disassemble();
					return;
				}
				be.triggerAssemble();
			});
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	public void setPowered(Level world, BlockPos pos, boolean powered) {
		world.setBlock(pos, world.getBlockState(pos).setValue(POWERED, powered), 3);
	}

	@Override
	public boolean canConnectRedstone(BlockState state, @Nullable Direction side) {
		return true;
	}

	@Override
	public boolean hideStressImpact() {
		return true;
	}

	@Override
	public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block,
			@Nullable Orientation wireOrientation, boolean isMoving) {
		if (!world.isClientSide()) {
			boolean flag = state.getValue(POWERED);
			if (flag != world.hasNeighborSignal(pos)) {
				if (flag) {
					setPowered(world, pos, false);
					world.scheduleTick(pos, this, 4);
				} else {
					setPowered(world, pos, true);
					world.setBlock(pos, state.cycle(POWERED), 2);
				}
			}
		}
	}

	@Override
	protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
		if (state.getValue(POWERED) && !world.hasNeighborSignal(pos))
			world.setBlock(pos, state.cycle(POWERED), 2);
	}
}
