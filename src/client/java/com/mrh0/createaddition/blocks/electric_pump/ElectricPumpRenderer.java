package com.mrh0.createaddition.blocks.electric_pump;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrh0.createaddition.blocks.electric_pump.ElectricPumpRenderer.ElectricPumpRenderState;
import com.mrh0.createaddition.index.CAPartials;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;

public class ElectricPumpRenderer implements BlockEntityRenderer<ElectricPumpBlockEntity, ElectricPumpRenderState> {

	public ElectricPumpRenderer(Context context) {
	}

	@Override
	public ElectricPumpRenderState createRenderState() {
		return new ElectricPumpRenderState();
	}

	@Override
	public void extractRenderState(
		ElectricPumpBlockEntity be,
		ElectricPumpRenderState state,
		float tickProgress,
		Vec3 cameraPos,
		@Nullable CrumblingOverlay crumblingOverlay
	) {
		Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
		CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
		BlockState blockState = be.getBlockState();
		Direction facing = blockState.getValue(ElectricPumpBlock.FACING);
		float renderTime = AnimationTickHolder.getRenderTime(level);

		state.rotation = ElectricPumpBlockEntity.getFacingRotation(facing);
		for (int i = 0; i < ElectricPumpBlockEntity.PARTIAL_OFFSETS.length; i++) {
			state.partials[i] = CachedBuffers.partial(CAPartials.ELECTRIC_PUMP_PARTIAL, blockState)
				.cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
			state.scales[i] = ElectricPumpBlockEntity.getPulseScale(
				be.isActive(), be.getPumpSpeed(), renderTime, ElectricPumpBlockEntity.PARTIAL_PHASE_OFFSETS[i]);
		}
	}

	@Override
	public void submit(
		ElectricPumpRenderState state,
		PoseStack matrices,
		SubmitNodeCollector queue,
		CameraRenderState cameraState
	) {
		for (int i = 0; i < state.partials.length; i++) {
			SuperByteBufferRenderState partial = state.partials[i];
			if (partial == null) continue;
			float[] offset = ElectricPumpBlockEntity.PARTIAL_OFFSETS[i];
			float scale = state.scales[i];
			matrices.pushPose();
			matrices.translate(0.5f, 0.5f, 0.5f);
			matrices.mulPose(state.rotation);
			matrices.translate(-0.5f, -0.5f, -0.5f);
			matrices.translate(offset[0], offset[1], offset[2]);
			matrices.scale(scale, scale, scale);
			matrices.translate(-offset[0], -offset[1], -offset[2]);
			partial.submit(matrices, queue);
			matrices.popPose();
		}
	}

	public static class ElectricPumpRenderState extends BlockEntityRenderState {
		public final SuperByteBufferRenderState[] partials =
			new SuperByteBufferRenderState[ElectricPumpBlockEntity.PARTIAL_OFFSETS.length];
		public final float[] scales = new float[ElectricPumpBlockEntity.PARTIAL_OFFSETS.length];
		public @UnknownNullability Quaternionf rotation;
	}
}
