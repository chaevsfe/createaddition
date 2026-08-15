package com.mrh0.createaddition.blocks.portable_energy_interface;

import java.util.function.Consumer;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class PEIInstance {
	private final InstancerProvider instancerProvider;
	private final BlockState blockState;
	private final BlockPos instancePos;
	private final float angleX;
	private final float angleY;

	private boolean lit;
	TransformedInstance middle;
	TransformedInstance top;

	public PEIInstance(InstancerProvider instancerProvider, BlockState blockState, BlockPos instancePos, boolean lit) {
		this.instancerProvider = instancerProvider;
		this.blockState = blockState;
		this.instancePos = instancePos;
		Direction facing = blockState.getValue(PortableEnergyInterfaceBlock.FACING);
		angleX = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
		angleY = AngleHelper.horizontalAngle(facing);
		this.lit = lit;

		middle = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(PortableEnergyInterfaceRenderer.getMiddleForState(blockState, lit)))
				.createInstance();
		top = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(PortableEnergyInterfaceRenderer.getTopForState(blockState)))
				.createInstance();
	}

	public void beginFrame(float progress) {
		middle.setIdentityTransform()
				.translate(instancePos)
				.center()
				.rotateYDegrees(angleY)
				.rotateXDegrees(angleX)
				.uncenter();

		top.setIdentityTransform()
				.translate(instancePos)
				.center()
				.rotateYDegrees(angleY)
				.rotateXDegrees(angleX)
				.uncenter();

		middle.translate(0, progress * 0.5f + 0.375f, 0);
		top.translate(0, progress, 0);

		middle.setChanged();
		top.setChanged();
	}

	public void tick(boolean lit) {
		if (this.lit != lit) {
			this.lit = lit;
			instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(PortableEnergyInterfaceRenderer.getMiddleForState(blockState, lit)))
					.stealInstance(middle);
		}
	}

	public void remove() {
		middle.delete();
		top.delete();
	}

	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(middle);
		consumer.accept(top);
	}
}
