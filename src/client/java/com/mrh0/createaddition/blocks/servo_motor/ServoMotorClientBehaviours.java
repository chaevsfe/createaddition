package com.mrh0.createaddition.blocks.servo_motor;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.content.contraptions.DirectionalExtenderScrollOptionSlot;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsBoard;
import com.zurrtum.create.client.foundation.blockEntity.ValueSettingsFormatter;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.KineticScrollValueBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.IControlContraption.RotationMode;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class ServoMotorClientBehaviours {

	public static final BehaviourType<ServoMovementModeScroll> CLIENT_MOVEMENT_MODE_TYPE = new BehaviourType<>();
	public static final BehaviourType<ServoAngleScroll> CLIENT_MAX_ANGLE_TYPE = new BehaviourType<>();
	public static final BehaviourType<ServoAngleScroll> CLIENT_MIN_ANGLE_TYPE = new BehaviourType<>();

	public static BlockEntityBehaviour<?> speed(ServoMotorBlockEntity be) {
		return new ServoSpeedScroll(be);
	}

	public static BlockEntityBehaviour<?> movementMode(ServoMotorBlockEntity be) {
		return new ServoMovementModeScroll(be);
	}

	public static BlockEntityBehaviour<?> maxAngle(ServoMotorBlockEntity be) {
		return new ServoAngleScroll(be, Component.literal("Max Angle"),
				new DirectionalExtenderScrollOptionSlot(ServoMotorClientBehaviours::isMaxAngleSide),
				CLIENT_MAX_ANGLE_TYPE, ServoMotorBlockEntity.MAX_ANGLE_TYPE, false);
	}

	public static BlockEntityBehaviour<?> minAngle(ServoMotorBlockEntity be) {
		return new ServoAngleScroll(be, Component.literal("Min Angle"),
				new DirectionalExtenderScrollOptionSlot(ServoMotorClientBehaviours::isMinAngleSide),
				CLIENT_MIN_ANGLE_TYPE, ServoMotorBlockEntity.MIN_ANGLE_TYPE, true);
	}

	private static boolean isModeSide(BlockState state, Direction side) {
		Axis facingAxis = state.getValue(ServoMotorBlock.FACING).getAxis();
		Axis sideAxis = side.getAxis();
		if (facingAxis == Axis.Y)
			return sideAxis == Axis.Z;
		return sideAxis != facingAxis && sideAxis != Axis.Y;
	}

	private static boolean isMaxAngleSide(BlockState state, Direction side) {
		if (state.getValue(ServoMotorBlock.FACING).getAxis() == Axis.Y)
			return side == Direction.EAST;
		return side == Direction.UP;
	}

	private static boolean isMinAngleSide(BlockState state, Direction side) {
		if (state.getValue(ServoMotorBlock.FACING).getAxis() == Axis.Y)
			return side == Direction.WEST;
		return side == Direction.DOWN;
	}

	public static class ServoSpeedScroll extends KineticScrollValueBehaviour {

		public ServoSpeedScroll(ServoMotorBlockEntity be) {
			super(CreateLang.translateDirect("generic.speed"), be, new CenteredSideValueBoxTransform(
					(state, side) -> state.getValue(ServoMotorBlock.FACING) == side.getOpposite()));
		}

		@Override
		public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
			return new ValueSettingsBoard(label, behaviour.getMax(), 32, ImmutableList.of(
					Component.literal("⟳").withStyle(ChatFormatting.BOLD),
					Component.literal("⟲").withStyle(ChatFormatting.BOLD)),
					new ValueSettingsFormatter(this::formatSettings));
		}
	}

	public static class ServoMovementModeScroll extends ScrollOptionBehaviour<RotationMode> {

		public ServoMovementModeScroll(ServoMotorBlockEntity be) {
			super(ServoRotationModeIcon.class, ServoRotationModeIcon::from,
					CreateLang.translateDirect("contraptions.movement_mode"), be,
					new DirectionalExtenderScrollOptionSlot(ServoMotorClientBehaviours::isModeSide));
			behaviour = be.getBehaviour(ServoMotorBlockEntity.MOVEMENT_MODE_TYPE);
		}

		@Override
		public void initialize() {
			behaviour = blockEntity.getBehaviour(ServoMotorBlockEntity.MOVEMENT_MODE_TYPE);
		}

		@Override
		public BehaviourType<?> getType() {
			return CLIENT_MOVEMENT_MODE_TYPE;
		}
	}

	public static class ServoAngleScroll extends ScrollValueBehaviour<SmartBlockEntity, ServoMotorBlockEntity.ServoAngleLimit> {

		private final BehaviourType<ServoAngleScroll> clientType;
		private final BehaviourType<ServoMotorBlockEntity.ServoAngleLimit> serverType;
		private final boolean negated;

		public ServoAngleScroll(ServoMotorBlockEntity be, Component label, ValueBoxTransform slot,
				BehaviourType<ServoAngleScroll> clientType,
				BehaviourType<ServoMotorBlockEntity.ServoAngleLimit> serverType, boolean negated) {
			super(label, be, slot);
			this.clientType = clientType;
			this.serverType = serverType;
			this.negated = negated;
			behaviour = be.getBehaviour(serverType);
		}

		@Override
		public void initialize() {
			behaviour = blockEntity.getBehaviour(serverType);
		}

		@Override
		public String formatValue() {
			int value = behaviour.getValue();
			return (negated ? -value : value) + "°";
		}

		@Override
		public BehaviourType<?> getType() {
			return clientType;
		}

		@Override
		public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
			return new ValueSettingsBoard(label, 180, 5, ImmutableList.of(Component.literal("°")),
					new ValueSettingsFormatter(settings -> Component.literal(
							(negated ? -settings.value() : settings.value()) + "°")));
		}
	}

	private enum ServoRotationModeIcon implements INamedIconOptions {
		ROTATE_PLACE(AllIcons.I_ROTATE_PLACE),
		ROTATE_PLACE_RETURNED(AllIcons.I_ROTATE_PLACE_RETURNED),
		ROTATE_NEVER_PLACE(AllIcons.I_ROTATE_NEVER_PLACE);

		private final String translationKey;
		private final AllIcons icon;

		ServoRotationModeIcon(AllIcons icon) {
			this.icon = icon;
			this.translationKey = "create.contraptions.movement_mode." + Lang.asId(name());
		}

		public static ServoRotationModeIcon from(RotationMode mode) {
			return switch (mode) {
				case ROTATE_PLACE -> ROTATE_PLACE;
				case ROTATE_PLACE_RETURNED -> ROTATE_PLACE_RETURNED;
				case ROTATE_NEVER_PLACE -> ROTATE_NEVER_PLACE;
			};
		}

		@Override
		public AllIcons getIcon() {
			return icon;
		}

		@Override
		public String getTranslationKey() {
			return translationKey;
		}
	}
}
