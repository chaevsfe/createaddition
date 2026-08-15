package com.mrh0.createaddition.blocks.modular_accumulator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorRenderer.ModularAccumulatorRenderState;
import com.mrh0.createaddition.index.CAPartials;
import com.zurrtum.create.catnip.data.Iterate;
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

import java.util.ArrayList;
import java.util.List;

public class ModularAccumulatorRenderer implements BlockEntityRenderer<ModularAccumulatorBlockEntity, ModularAccumulatorRenderState> {
    private static final float DIAL_PIVOT_Y = 6 / 16f;
    private static final float DIAL_PIVOT_Z = 8 / 16f;

    public ModularAccumulatorRenderer(Context context) {
    }

    @Override
    public ModularAccumulatorRenderState createRenderState() {
        return new ModularAccumulatorRenderState();
    }

    @Override
    public boolean shouldRender(ModularAccumulatorBlockEntity be, Vec3 cameraPosition) {
        return be.isController() && BlockEntityRenderer.super.shouldRender(be, cameraPosition);
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public void extractRenderState(
        ModularAccumulatorBlockEntity be,
        ModularAccumulatorRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        state.dials = null;
        if (!be.isController()) {
            return;
        }
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        BlockState blockState = be.getBlockState();
        float width = be.getWidth();
        float centerY = be.getHeight() - 0.5f;
        float radius = width / 2f - 6 / 16f;
        float progress = be.gauge.getValue(tickProgress);
        List<DialRenderState> dials = new ArrayList<>(Iterate.horizontalDirections.length);
        for (Direction direction : Iterate.horizontalDirections) {
            float yRot = direction.toYRot();
            SuperByteBufferRenderState gauge = CachedBuffers.partial(CAPartials.ACCUMULATOR_GUAGE, blockState)
                .translate(width / 2f, centerY, width / 2f).rotateYDegrees(yRot).uncenter().translate(radius, 0, 0)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            SuperByteBufferRenderState dial = CachedBuffers.partial(CAPartials.ACCUMULATOR_DIAL, blockState)
                .translate(width / 2f, centerY, width / 2f).rotateYDegrees(yRot).uncenter().translate(radius, 0, 0)
                .translate(0, DIAL_PIVOT_Y, DIAL_PIVOT_Z).rotateXDegrees(-180 * progress)
                .translate(0, -DIAL_PIVOT_Y, -DIAL_PIVOT_Z).cardinalLighting(cardinalLighting)
                .light(state.lightCoords).extractRenderState();
            dials.add(new DialRenderState(gauge, dial));
        }
        state.dials = dials;
    }

    @Override
    public void submit(
        ModularAccumulatorRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.dials == null) {
            return;
        }
        for (DialRenderState dial : state.dials) {
            dial.gauge().submit(matrices, queue);
            dial.dial().submit(matrices, queue);
        }
    }

    public record DialRenderState(SuperByteBufferRenderState gauge, SuperByteBufferRenderState dial) {
    }

    public static class ModularAccumulatorRenderState extends BlockEntityRenderState {
        public @Nullable List<DialRenderState> dials;
    }
}
