package com.mrh0.createaddition.blocks.portable_energy_interface;

import com.mrh0.createaddition.index.CABlocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrh0.createaddition.index.CAPartials;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.render.ContraptionMatrices;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class PortableEnergyInterfaceRenderer extends SafeBlockEntityRenderer<PortableEnergyInterfaceBlockEntity> {

	public PortableEnergyInterfaceRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	protected void renderSafe(PortableEnergyInterfaceBlockEntity be, float partialTicks, PoseStack ms,
							  MultiBufferSource buffer, int light, int overlay) {
		if (VisualizationManager.supportsVisualization(be.getLevel())) return;

		BlockState blockState = be.getBlockState();
		float progress = be.getExtensionDistance(partialTicks);
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		render(blockState, be.isConnected(), progress, null, sbb -> sbb.light(light)
				.renderInto(ms, vb));
	}

	public static void renderInContraption(MovementContext context, VirtualRenderWorld renderWorld,
										   ContraptionMatrices matrices, MultiBufferSource buffer) {
		BlockState blockState = context.state;
		VertexConsumer vb = buffer.getBuffer(RenderType.solid());
		float renderPartialTicks = AnimationTickHolder.getPartialTicks();

		LerpedFloat animation = PortableEnergyInterfaceMovement.getAnimation(context);
		float progress = animation.getValue(renderPartialTicks);
		boolean lit = animation.settled();
		render(blockState, lit, progress, matrices.getModel(),
				sbb -> sbb.light(LevelRenderer.getLightColor(renderWorld, context.localPos))
						.useLevelLight(context.world, matrices.getWorld())
						.renderInto(matrices.getViewProjection(), vb));
	}

	private static void render(BlockState blockState, boolean lit, float progress,@Nullable PoseStack local,
							   Consumer<SuperByteBuffer> drawCallback) {
		SuperByteBuffer middle = CachedBuffers.partial(getMiddleForState(blockState, lit), blockState);
		SuperByteBuffer top = CachedBuffers.partial(getTopForState(blockState), blockState);

		if (local != null) {
			middle.transform(local);
			top.transform(local);
		}
		Direction facing = blockState.getValue(PortableEnergyInterfaceBlock.FACING);
		rotateToFacing(middle, facing);
		rotateToFacing(top, facing);
		middle.translate(0, progress * 0.5f + 0.375f, 0);
		top.translate(0, progress, 0);

		drawCallback.accept(middle);
		drawCallback.accept(top);
	}

	private static void rotateToFacing(SuperByteBuffer buffer, Direction facing) {
		buffer.center()
				.rotateYDegrees(AngleHelper.horizontalAngle(facing))
				.rotateXDegrees(facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90)
				.uncenter();
	}

	@Nullable
	static PortableEnergyInterfaceBlockEntity getTargetPSI(MovementContext context) {
		String _workingPos_ = PortableEnergyInterfaceMovement._workingPos_;
		if (!context.data.contains(_workingPos_)) return null;

		BlockPos pos = NbtUtils.readBlockPos(context.data, _workingPos_).orElse(BlockPos.ZERO); // Should never else
		BlockEntity blockEntity = context.world.getBlockEntity(pos);
		if (!(blockEntity instanceof PortableEnergyInterfaceBlockEntity psi)) return null;

		if (!psi.isTransferring()) return null;
		return psi;
	}

	static PartialModel getMiddleForState(BlockState state, boolean lit) {
        CABlocks.PORTABLE_ENERGY_INTERFACE.has(state);
        return lit ? CAPartials.PORTABLE_ENERGY_INTERFACE_MIDDLE_POWERED
				: CAPartials.PORTABLE_ENERGY_INTERFACE_MIDDLE;
	}

	static PartialModel getTopForState(BlockState state) {
		return CAPartials.PORTABLE_ENERGY_INTERFACE_TOP;
	}
}
