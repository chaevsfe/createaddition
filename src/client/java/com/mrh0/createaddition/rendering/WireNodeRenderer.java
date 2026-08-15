package com.mrh0.createaddition.rendering;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.energy.IWireNode;
import com.mrh0.createaddition.energy.WireType;
import com.mrh0.createaddition.index.CAPartials;
import com.mrh0.createaddition.item.WireSpool;
import com.mrh0.createaddition.util.Util;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import org.joml.Matrix4f;

public class WireNodeRenderer<T extends BlockEntity> implements BlockEntityRenderer<T, WireNodeRenderer.WireNodeRenderState> {

	private static final float HANG = 0.5f;
	private static final int SEGMENTS = 24;
	private static final float LIGHT_Y_OFFSET = -0.03f;
	private static final float WIRE_WIDTH = 0.025f;
	private static final float VERTICAL_WIRE_WIDTH = 0.015f;
	private static final int BOTH = 0;
	private static final int FIRST_ONLY = 1;
	private static final int SECOND_ONLY = 2;
	private static final Color[] LIGHT_COLORS = {Color.RED, Color.GREEN, new Color(0f, 0f, 1f, 1f)};

	public WireNodeRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public WireNodeRenderState createRenderState() {
		return new WireNodeRenderState();
	}

	@Override
	public void extractRenderState(T be, WireNodeRenderState state, float tickProgress, Vec3 cameraPos, CrumblingOverlay crumblingOverlay) {
		BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
		state.wires = null;

		Level level = be.getLevel();
		if (level == null || !(be instanceof IWireNode node))
			return;

		BlockState blockState = be.getBlockState();
		BlockPos pos = node.getPos();
		List<WireRenderState> wires = new ArrayList<>();

		for (int i = 0; i < node.getNodeCount(); i++) {
			if (!node.hasConnection(i))
				continue;
			IWireNode remote = node.getWireNode(i);
			if (remote == null)
				continue;
			BlockPos remotePos = node.getNodePos(i);
			WireType type = node.getNodeType(i);
			if (remotePos == null || type == null)
				continue;

			Vec3 localOffset = node.getNodeOffset(i);
			Vec3 remoteOffset = remote.getNodeOffset(node.getOtherNodeIndex(i));
			if (localOffset == null || remoteOffset == null)
				continue;

			float tx = remotePos.getX() - pos.getX();
			float ty = remotePos.getY() - pos.getY();
			float tz = remotePos.getZ() - pos.getZ();

			wires.add(buildWire(
					level, blockState, pos, remotePos, type,
					tx + 0.5f + (float) remoteOffset.x(),
					ty + 0.5f + (float) remoteOffset.y(),
					tz + 0.5f + (float) remoteOffset.z(),
					-tx - (float) remoteOffset.x() + (float) localOffset.x(),
					-ty - (float) remoteOffset.y() + (float) localOffset.y(),
					-tz - (float) remoteOffset.z() + (float) localOffset.z(),
					lengthFromZero(tx, ty, tz)));
		}

		WireRenderState held = buildHeldWire(level, blockState, node, pos, tickProgress);
		if (held != null)
			wires.add(held);

		if (!wires.isEmpty())
			state.wires = wires;
	}

	@Override
	public void submit(WireNodeRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
		if (state.wires == null)
			return;
		RenderType layer = CARenderType.wire();
		for (WireRenderState wire : state.wires) {
			matrices.pushPose();
			matrices.translate(wire.ox, wire.oy, wire.oz);
			queue.submitCustomGeometry(matrices, layer, wire);
			if (wire.lights != null)
				for (SuperByteBufferRenderState light : wire.lights)
					light.submit(matrices, queue);
			matrices.popPose();
		}
	}

	@Override
	public boolean shouldRenderOffScreen() {
		return true;
	}

	@Override
	public int getViewDistance() {
		return 256;
	}

	private static WireRenderState buildHeldWire(Level level, BlockState blockState, IWireNode node, BlockPos pos, float tickProgress) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return null;
		ItemStack stack = player.getInventory().getSelectedItem();
		if (stack.isEmpty() || WireSpool.isRemover(stack.getItem()))
			return null;
		Util.Triple<BlockPos, Integer, WireType> held = Util.getWireNodeOfSpools(stack);
		if (held == null || !held.a.equals(pos))
			return null;

		Vec3 localOffset = node.getNodeOffset(held.b);
		if (localOffset == null)
			return null;

		Vec3 playerPos = player.getPosition(tickProgress);
		float tx = (float) playerPos.x - pos.getX();
		float ty = (float) playerPos.y - pos.getY();
		float tz = (float) playerPos.z - pos.getZ();

		return buildWire(
				level, blockState, pos, player.blockPosition(), held.c,
				tx + 0.5f, ty + 0.5f, tz + 0.5f,
				-tx + (float) localOffset.x(),
				-ty + (float) localOffset.y(),
				-tz + (float) localOffset.z(),
				lengthFromZero(tx, ty, tz));
	}

	private static WireRenderState buildWire(Level level, BlockState blockState, BlockPos pos, BlockPos remotePos, WireType type,
			float ox, float oy, float oz, float x, float y, float z, float dis) {
		WireRenderState wire = new WireRenderState();
		wire.ox = ox;
		wire.oy = oy;
		wire.oz = oz;
		wire.x = x;
		wire.y = y;
		wire.z = z;
		wire.dis = dis;
		wire.red = type.getRed();
		wire.green = type.getGreen();
		wire.blue = type.getBlue();
		wire.blockLightStart = level.getBrightness(LightLayer.BLOCK, remotePos);
		wire.blockLightEnd = level.getBrightness(LightLayer.BLOCK, pos);
		wire.skyLightStart = level.getBrightness(LightLayer.SKY, remotePos);
		wire.skyLightEnd = level.getBrightness(LightLayer.SKY, pos);
		if (type.isFestive())
			wire.lights = buildLights(blockState, x, y, z, dis);
		return wire;
	}

	private static List<SuperByteBufferRenderState> buildLights(BlockState blockState, float x, float y, float z, float dis) {
		List<SuperByteBufferRenderState> lights = new ArrayList<>();
		boolean main = x + y + z > 0;
		for (int index = 3; index < SEGMENTS; index += 3) {
			float part = (float) index / (float) SEGMENTS;
			Color color = LIGHT_COLORS[main ? 2 - (index / 3) % 3 : (index / 3) % 3];
			lights.add(CachedBuffers.partial(CAPartials.SMALL_LIGHT, blockState)
					.color(color)
					.light(LightCoordsUtil.FULL_BRIGHT)
					.translate(x * part, catenary(y, part) + hang(part, dis) + LIGHT_Y_OFFSET, z * part)
					.extractRenderState());
		}
		return lights;
	}

	private static float catenary(float y, float part) {
		return y > 0.0f ? y * part * part : y - y * (1.0f - part) * (1.0f - part);
	}

	private static float hang(float part, float dis) {
		return (float) Math.sin(-part * (float) Math.PI) * (HANG * dis / (float) CACommonConfig.COMMON.SMALL_CONNECTOR_MAX_LENGTH.get());
	}

	public static float lengthFromZero(float x, float y, float z) {
		return (float) Math.sqrt(x * x + y * y + z * z);
	}

	public static class WireNodeRenderState extends BlockEntityRenderState {
		public List<WireRenderState> wires;
	}

	public static class WireRenderState implements CustomGeometryRenderer {
		public float ox;
		public float oy;
		public float oz;
		public float x;
		public float y;
		public float z;
		public float dis;
		public int red;
		public int green;
		public int blue;
		public int blockLightStart;
		public int blockLightEnd;
		public int skyLightStart;
		public int skyLightEnd;
		public List<SuperByteBufferRenderState> lights;

		@Override
		public void render(Pose pose, VertexConsumer consumer) {
			Matrix4f matrix = pose.pose();
			float scalar = Mth.invSqrt(x * x + z * z) * WIRE_WIDTH / 2.0f;
			float o1 = z * scalar;
			float o2 = x * scalar;
			boolean vertical = Math.abs(x) + Math.abs(z) < Math.abs(y);

			emitPair(consumer, matrix, 0, WIRE_WIDTH, WIRE_WIDTH, o1, o2, vertical, FIRST_ONLY);
			for (int index = 0; index <= SEGMENTS; index++)
				emitPair(consumer, matrix, index, WIRE_WIDTH, WIRE_WIDTH, o1, o2, vertical, BOTH);
			for (int index = SEGMENTS; index >= 0; index--)
				emitPair(consumer, matrix, index, WIRE_WIDTH, 0.0f, o1, o2, vertical, BOTH);
			emitPair(consumer, matrix, 0, WIRE_WIDTH, 0.0f, o1, o2, vertical, SECOND_ONLY);
		}

		private void emitPair(VertexConsumer consumer, Matrix4f matrix, int index, float a, float b, float o1, float o2,
				boolean vertical, int mode) {
			float part = (float) index / (float) SEGMENTS;
			int light = LightCoordsUtil.pack(
					(int) Mth.lerp(part, blockLightStart, blockLightEnd),
					(int) Mth.lerp(part, skyLightStart, skyLightEnd));

			int cr = red;
			int cg = green;
			int cb = blue;
			if (index % 2 == 0) {
				cr = (int) (cr * 0.7f);
				cg = (int) (cg * 0.7f);
				cb = (int) (cb * 0.7f);
			}

			float fx = x * part;
			float fy = catenary(y, part) + hang(part, dis);
			float fz = z * part;

			float x1, y1, z1, x2, y2, z2;
			if (vertical) {
				boolean p = b > 0;
				x1 = fx + VERTICAL_WIRE_WIDTH;
				y1 = fy;
				z1 = fz + (p ? VERTICAL_WIRE_WIDTH : -VERTICAL_WIRE_WIDTH);
				x2 = fx - VERTICAL_WIRE_WIDTH;
				y2 = fy;
				z2 = fz + (p ? -VERTICAL_WIRE_WIDTH : VERTICAL_WIRE_WIDTH);
			} else {
				x1 = fx + o1;
				y1 = fy + a - b;
				z1 = fz - o2;
				x2 = fx - o1;
				y2 = fy + b;
				z2 = fz + o2;
			}

			if (mode != SECOND_ONLY)
				consumer.addVertex(matrix, x1, y1, z1).setColor(cr, cg, cb, 255).setLight(light);
			if (mode != FIRST_ONLY)
				consumer.addVertex(matrix, x2, y2, z2).setColor(cr, cg, cb, 255).setLight(light);
		}
	}
}
