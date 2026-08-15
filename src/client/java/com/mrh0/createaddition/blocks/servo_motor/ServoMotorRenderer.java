package com.mrh0.createaddition.blocks.servo_motor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrh0.createaddition.blocks.servo_motor.ServoMotorRenderer.ServoMotorRenderState;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
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
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getEastRotateAngle;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getRadiansRotateAngle;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getTintColor;
import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.getUpRotateAngle;

public class ServoMotorRenderer implements BlockEntityRenderer<ServoMotorBlockEntity, ServoMotorRenderState> {
    public ServoMotorRenderer(Context context) {
    }

    @Override
    public ServoMotorRenderState createRenderState() {
        return new ServoMotorRenderState();
    }

    @Override
    public void extractRenderState(
        ServoMotorBlockEntity be,
        ServoMotorRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.blockState = be.getBlockState();
        Direction facing = state.blockState.getValue(BlockStateProperties.FACING);
        Axis axis = facing.getAxis();
        state.topAngle = getRadiansRotateAngle(
            be.getInterpolatedAngle(tickProgress - 1) * Mth.DEG_TO_RAD,
            axis.getPositive()
        );
        state.upAngle = axis == Axis.Y ? null : getUpRotateAngle(AngleHelper.horizontalAngle(facing.getOpposite()));
        state.eastAngle = getEastRotateAngle(-90 - AngleHelper.verticalAngle(facing));
        state.top = CachedBuffers.partial(AllPartialModels.BEARING_TOP, state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).color(getTintColor(be)).extractRenderState();
    }

    @Override
    public void submit(
        ServoMotorRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.topAngle != null) {
            matrices.rotateAround(state.topAngle, 0.5f, 0.5f, 0.5f);
        }
        if (state.upAngle != null) {
            matrices.rotateAround(state.upAngle, 0.5f, 0.5f, 0.5f);
        }
        if (state.eastAngle != null) {
            matrices.rotateAround(state.eastAngle, 0.5f, 0.5f, 0.5f);
        }
        state.top.submit(matrices, queue);
    }

    public static class ServoMotorRenderState extends BlockEntityRenderState {
        public @UnknownNullability BlockState blockState;
        public @UnknownNullability SuperByteBufferRenderState top;
        public @Nullable Quaternionf topAngle;
        public @Nullable Quaternionf upAngle;
        public @Nullable Quaternionf eastAngle;
    }
}
