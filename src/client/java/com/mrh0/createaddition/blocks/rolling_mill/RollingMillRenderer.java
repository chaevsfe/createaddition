package com.mrh0.createaddition.blocks.rolling_mill;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillRenderer.RollingMillRenderState;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;

public class RollingMillRenderer implements BlockEntityRenderer<RollingMillBlockEntity, RollingMillRenderState> {
	private static final float ROLLER_OFFSET = 4f / 16f;

	public RollingMillRenderer(Context context) {
	}

	@Override
	public RollingMillRenderState createRenderState() {
		return new RollingMillRenderState();
	}

	@Override
	public void extractRenderState(
		RollingMillBlockEntity be,
		RollingMillRenderState state,
		float partialTicks,
		Vec3 cameraPosition,
		@Nullable CrumblingOverlay breakProgress
	) {
		Level level = SmartBlockEntityRenderer.extractBase(be, state, breakProgress);
		BlockState blockState = be.getBlockState();
		Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(blockState);
		Direction direction = axis.getPositive();
		int color = KineticBlockEntityRenderer.getTintColor(be);

		state.bottomRoller = CachedBuffers.partialFacingVertical(AllPartialModels.SHAFT, blockState, direction)
			.cardinalLighting(level).light(state.lightCoords).color(color).extractRenderState();
		state.topRoller = CachedBuffers.partialFacingVertical(AllPartialModels.SHAFT, blockState, direction)
			.cardinalLighting(level).light(state.lightCoords).color(color).extractRenderState();

		float offset = KineticBlockEntityVisual.rotationOffset(blockState, axis, be.getBlockPos())
			+ be.getRotationAngleOffset(axis);
		float progress = KineticBlockEntityRenderer.getProgress(be, level);
		state.bottomAngle = KineticBlockEntityRenderer.getRotateAngle(progress, offset, direction);
		state.topAngle = KineticBlockEntityRenderer.getRotateAngle(-progress, offset, direction);
	}

	@Override
	public void submit(
		RollingMillRenderState state,
		PoseStack matrices,
		SubmitNodeCollector queue,
		CameraRenderState camera
	) {
		matrices.pushPose();
		if (state.bottomAngle != null) {
			matrices.rotateAround(state.bottomAngle, 0.5f, 0.5f, 0.5f);
		}
		state.bottomRoller.submit(matrices, queue);
		matrices.popPose();

		matrices.pushPose();
		matrices.translate(0f, ROLLER_OFFSET, 0f);
		if (state.topAngle != null) {
			matrices.rotateAround(state.topAngle, 0.5f, 0.5f, 0.5f);
		}
		state.topRoller.submit(matrices, queue);
		matrices.popPose();
	}

	public static class RollingMillRenderState extends BlockEntityRenderState {
		public @UnknownNullability SuperByteBufferRenderState bottomRoller;
		public @UnknownNullability SuperByteBufferRenderState topRoller;
		public @Nullable Quaternionf bottomAngle;
		public @Nullable Quaternionf topAngle;
	}
}
