package com.mrh0.createaddition.blocks.portable_energy_interface;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceRenderer.PortableEnergyInterfaceRenderState;
import com.mrh0.createaddition.index.CAPartials;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
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
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;
import org.jetbrains.annotations.Nullable;

public class PortableEnergyInterfaceRenderer implements BlockEntityRenderer<PortableEnergyInterfaceBlockEntity, PortableEnergyInterfaceRenderState> {
    public PortableEnergyInterfaceRenderer(Context context) {
    }

    @Override
    public PortableEnergyInterfaceRenderState createRenderState() {
        return new PortableEnergyInterfaceRenderState();
    }

    @Override
    public void extractRenderState(
        PortableEnergyInterfaceBlockEntity be,
        PortableEnergyInterfaceRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.blockState = be.getBlockState();
        state.middle = CachedBuffers.partial(getMiddleForState(state.blockState, be.isConnected()), state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        state.top = CachedBuffers.partial(getTopForState(state.blockState), state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        Direction facing = state.blockState.getValue(PortableEnergyInterfaceBlock.FACING);
        state.yRot = KineticBlockEntityRenderer.getYRotateAngle(AngleHelper.horizontalAngle(facing));
        if (facing != Direction.UP) {
            state.xRot = KineticBlockEntityRenderer.getXRotateAngle(facing == Direction.DOWN ? 180 : 90);
        } else {
            state.xRot = null;
        }
        float offset = be.getExtensionDistance(tickProgress) * 0.5f;
        state.middleOffset = offset + 0.375f;
        state.topOffset = offset - 0.375f;
    }

    @Override
    public void submit(
        PortableEnergyInterfaceRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.yRot != null || state.xRot != null) {
            matrices.translate(0.5f, 0.5f, 0.5f);
            if (state.yRot != null) {
                matrices.mulPose(state.yRot);
            }
            if (state.xRot != null) {
                matrices.mulPose(state.xRot);
            }
            matrices.translate(-0.5f, -0.5f, -0.5f);
        }
        matrices.translate(0, state.middleOffset, 0);
        state.middle.submit(matrices, queue);
        matrices.translate(0, state.topOffset, 0);
        state.top.submit(matrices, queue);
    }

    public static PartialModel getMiddleForState(BlockState state, boolean lit) {
        return lit ? CAPartials.PORTABLE_ENERGY_INTERFACE_MIDDLE_POWERED : CAPartials.PORTABLE_ENERGY_INTERFACE_MIDDLE;
    }

    public static PartialModel getTopForState(BlockState state) {
        return CAPartials.PORTABLE_ENERGY_INTERFACE_TOP;
    }

    public static class PortableEnergyInterfaceRenderState extends BlockEntityRenderState {
        public @UnknownNullability BlockState blockState;
        public @UnknownNullability SuperByteBufferRenderState middle;
        public @UnknownNullability SuperByteBufferRenderState top;
        public @Nullable Quaternionf yRot;
        public @Nullable Quaternionf xRot;
        public float middleOffset;
        public float topOffset;
    }
}
