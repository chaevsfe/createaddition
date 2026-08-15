package com.mrh0.createaddition.blocks.liquid_blaze_burner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerRenderer.LiquidBlazeBurnerRenderState;
import com.mrh0.createaddition.index.CAPartials;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Quaternionf;

public class LiquidBlazeBurnerRenderer implements BlockEntityRenderer<LiquidBlazeBurnerBlockEntity, LiquidBlazeBurnerRenderState> {
    public LiquidBlazeBurnerRenderer(Context context) {
    }

    @Override
    public LiquidBlazeBurnerRenderState createRenderState() {
        return new LiquidBlazeBurnerRenderState();
    }

    @Override
    public void extractRenderState(
        LiquidBlazeBurnerBlockEntity be,
        LiquidBlazeBurnerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        state.data = null;
        HeatLevel heatLevel = be.getHeatLevelForRender();
        if (heatLevel == HeatLevel.NONE) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        state.blockState = be.getBlockState();
        state.data = getLiquidBlazeBurnerRenderData(
            be.getLevel(),
            state.blockState,
            heatLevel,
            be.headAnimation.getValue(tickProgress) * 0.175f,
            AngleHelper.rad(be.headAngle.getValue(tickProgress)),
            heatLevel.isAtLeast(HeatLevel.FADING),
            be.goggles,
            be.hashCode()
        );
    }

    @Override
    public void submit(
        LiquidBlazeBurnerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.data != null) {
            state.data.submit(matrices, queue);
        }
    }

    public static PartialModel getBlazeModel(HeatLevel heatLevel, boolean blockAbove) {
        if (heatLevel.isAtLeast(HeatLevel.SEETHING)) {
            return blockAbove ? AllPartialModels.BLAZE_SUPER_ACTIVE : AllPartialModels.BLAZE_SUPER;
        }
        if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            return blockAbove && heatLevel.isAtLeast(HeatLevel.KINDLED) ? AllPartialModels.BLAZE_ACTIVE :
                AllPartialModels.BLAZE_IDLE;
        }
        return AllPartialModels.BLAZE_INERT;
    }

    public static void tickAnimation(LiquidBlazeBurnerBlockEntity be) {
        if (be.getHeatLevelFromBlock().isAtLeast(HeatLevel.FADING) && be.isValidBlockAbove()) {
            be.headAngle.chase(
                (AngleHelper.horizontalAngle(be.getBlockState()
                    .getValueOrElse(LiquidBlazeBurnerBlock.FACING, Direction.SOUTH)) + 180) % 360,
                0.125f,
                Chaser.EXP
            );
            be.headAnimation.chase(1, 0.25f, Chaser.exp(0.25f));
        } else {
            float target = 0;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isInvisible()) {
                double x;
                double z;
                if (be.isVirtual()) {
                    x = -4;
                    z = -10;
                } else {
                    x = player.getX();
                    z = player.getZ();
                }
                double dx = x - (be.getBlockPos().getX() + 0.5);
                double dz = z - (be.getBlockPos().getZ() + 0.5);
                target = AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
            }
            target = be.headAngle.getValue() + AngleHelper.getShortestAngleDiff(be.headAngle.getValue(), target);
            be.headAngle.chase(target, 0.25f, Chaser.exp(5));
            be.headAnimation.chase(0, 0.25f, Chaser.exp(0.25f));
        }
        be.headAngle.tickChaser();
        be.headAnimation.tickChaser();
    }

    public static LiquidBlazeBurnerRenderData getLiquidBlazeBurnerRenderData(
        @Nullable Level level,
        BlockState blockState,
        HeatLevel heatLevel,
        float animation,
        float horizontalAngle,
        boolean canDrawFlame,
        boolean drawGoggles,
        int hashCode
    ) {
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        LiquidBlazeBurnerRenderData data = new LiquidBlazeBurnerRenderData();
        boolean blockAbove = animation > 0.125f;
        float time = AnimationTickHolder.getRenderTime(level);
        float renderTick = time / 16.0f + hashCode % 13;
        float offset = Mth.sin(renderTick % Mth.TWO_PI) / (heatLevel.isAtLeast(HeatLevel.FADING) ? 64 : 16);
        PartialModel blazeModel = getBlazeModel(heatLevel, blockAbove);
        data.headY = offset - animation * 0.75f;
        data.horizontalAngle = KineticBlockEntityRenderer.getUpRadiansRotateAngle(horizontalAngle);
        data.blaze = CachedBuffers.partial(blazeModel, blockState).cardinalLighting(cardinalLighting)
            .light(LightCoordsUtil.FULL_BRIGHT).extractRenderState();
        if (drawGoggles) {
            PartialModel gogglesModel =
                blazeModel == AllPartialModels.BLAZE_INERT ? AllPartialModels.BLAZE_GOGGLES_SMALL :
                    AllPartialModels.BLAZE_GOGGLES;
            data.goggles = CachedBuffers.partial(gogglesModel, blockState).cardinalLighting(cardinalLighting)
                .light(LightCoordsUtil.FULL_BRIGHT).extractRenderState();
        }
        data.hat = CachedBuffers.partial(CAPartials.LIQUID_HAT, blockState).cardinalLighting(cardinalLighting)
            .light(LightCoordsUtil.FULL_BRIGHT).extractRenderState();
        data.hatScale = blazeModel == AllPartialModels.BLAZE_INERT;
        data.hatAngle = new Quaternionf().setAngleAxis(horizontalAngle + Mth.PI, 0, 1, 0);
        if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            if (heatLevel == HeatLevel.SEETHING) {
                data.rods = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_SUPER_RODS, blockState)
                    .cardinalLighting(cardinalLighting).light(LightCoordsUtil.FULL_BRIGHT).extractRenderState();
                data.rods2 = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_SUPER_RODS_2, blockState)
                    .cardinalLighting(cardinalLighting).light(LightCoordsUtil.FULL_BRIGHT).extractRenderState();
            } else {
                data.rods = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_RODS, blockState)
                    .cardinalLighting(cardinalLighting).light(LightCoordsUtil.FULL_BRIGHT).extractRenderState();
                data.rods2 = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_RODS_2, blockState)
                    .cardinalLighting(cardinalLighting).light(LightCoordsUtil.FULL_BRIGHT).extractRenderState();
            }
            data.rodsY = Mth.sin((float) ((renderTick + Math.PI) % Mth.TWO_PI)) / 64 + animation + 0.125f;
            data.rods2Y = Mth.sin((renderTick + Mth.HALF_PI) % Mth.TWO_PI) / 64 + animation - 0.1875f;
        }
        if (canDrawFlame && blockAbove) {
            SpriteShiftEntry spriteShift =
                heatLevel == HeatLevel.SEETHING ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;
            TextureAtlasSprite target = spriteShift.getTarget();
            float spriteWidth = target.getU1() - target.getU0();
            float spriteHeight = target.getV1() - target.getV0();
            float speed = 0.03125f + 0.015625f * heatLevel.ordinal();
            double vScroll = speed * time;
            vScroll = vScroll - Math.floor(vScroll);
            vScroll = vScroll * spriteHeight / 2;
            double uScroll = speed * time / 2;
            uScroll = uScroll - Math.floor(uScroll);
            uScroll = uScroll * spriteWidth / 2;
            data.flame = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, blockState)
                .cardinalLighting(cardinalLighting).light(LightCoordsUtil.FULL_BRIGHT)
                .shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll).extractRenderState();
        }
        return data;
    }

    public static class LiquidBlazeBurnerRenderState extends BlockEntityRenderState {
        public @UnknownNullability BlockState blockState;
        public @Nullable LiquidBlazeBurnerRenderData data;
    }

    public static class LiquidBlazeBurnerRenderData {
        public SuperByteBufferRenderState blaze;
        public @Nullable SuperByteBufferRenderState goggles;
        public SuperByteBufferRenderState hat;
        public @Nullable SuperByteBufferRenderState rods;
        public @Nullable SuperByteBufferRenderState rods2;
        public @Nullable SuperByteBufferRenderState flame;
        public float headY;
        public @Nullable Quaternionf horizontalAngle;
        public boolean hatScale;
        public @Nullable Quaternionf hatAngle;
        public float rodsY;
        public float rods2Y;

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            if (horizontalAngle != null) {
                matrices.rotateAround(horizontalAngle, 0.5f, 0.5f, 0.5f);
            }
            if (flame != null) {
                flame.submit(matrices, queue);
            }
            matrices.translate(0, headY, 0);
            blaze.submit(matrices, queue);
            if (goggles != null) {
                matrices.translate(0, 0.5f, 0);
                goggles.submit(matrices, queue);
            }
            matrices.popPose();
            matrices.pushPose();
            matrices.translate(0, headY, 0);
            if (hatScale) {
                matrices.translate(0, 0.5f, 0);
                SuperByteBuffer.scaleAround(matrices.last(), 0.75f, 0.5f, 0.5f, 0.5f);
            } else {
                matrices.translate(0, 0.75f, 0);
            }
            if (hatAngle != null) {
                matrices.rotateAround(hatAngle, 0.5f, 0.5f, 0.5f);
            }
            matrices.translate(0.5f, 0, 0.5f);
            hat.submit(matrices, queue);
            matrices.popPose();
            if (rods != null && rods2 != null) {
                matrices.pushPose();
                matrices.translate(0, rodsY, 0);
                rods.submit(matrices, queue);
                matrices.popPose();
                matrices.pushPose();
                matrices.translate(0, rods2Y, 0);
                rods2.submit(matrices, queue);
                matrices.popPose();
            }
        }
    }
}
