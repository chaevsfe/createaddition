package com.mrh0.createaddition.blocks.barbed_wire;

import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.index.CADamageTypes;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BarbedWireBlock extends Block implements SimpleWaterloggedBlock {
	public static final BooleanProperty VERTICAL = BooleanProperty.create("vertical");
	public static final EnumProperty<Direction> HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public BarbedWireBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.defaultBlockState().setValue(VERTICAL, false).setValue(HORIZONTAL_FACING, Direction.NORTH).setValue(WATERLOGGED, false));
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state) {
		return !state.getValue(WATERLOGGED);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean isPrimary) {
		double delta = Math.abs(entity.getX() - entity.xOld) + Math.abs(entity.getY() - entity.yOld) + Math.abs(entity.getZ() - entity.zOld);
		if((entity instanceof LivingEntity) && delta > 0d && level instanceof ServerLevel serverLevel) {
			if(entity.hurtServer(serverLevel, CADamageTypes.barbedWire(level), CACommonConfig.COMMON.BARBED_WIRE_DAMAGE.getF()))
				entity.playSound(SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH, 1f, 1f);
		}
		entity.makeStuckInBlock(state, new Vec3(0.25D, 0.05D, 0.25D));
	}

	@Override
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if(!stack.is(ConventionalItemTags.SHEAR_TOOLS))
			return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
		if(!level.isClientSide()) {
			level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1f, 1f);
			level.destroyBlock(pos, false, player);
			Block.popResource(level, pos, new ItemStack(this));
			stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(VERTICAL, HORIZONTAL_FACING, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext c) {
		FluidState fluidstate = c.getLevel().getFluidState(c.getClickedPos());
		if(c.getPlayer() == null) return defaultBlockState();
		if(c.getClickedFace().getAxis() == Axis.Y)
			return defaultBlockState()
					.setValue(HORIZONTAL_FACING, c.getPlayer().isShiftKeyDown() ? c.getHorizontalDirection().getClockWise() : c.getHorizontalDirection())
					.setValue(VERTICAL, false)
					.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
		else
			return defaultBlockState()
					.setValue(HORIZONTAL_FACING, c.getClickedFace().getOpposite())
					.setValue(VERTICAL, true)
					.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
	}
}
