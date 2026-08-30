package com.mrh0.createaddition.sound;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import com.mrh0.createaddition.sound.CASoundScapes.AmbienceGroup;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;

public class CAAudioBehaviour<T extends SmartBlockEntity> extends BlockEntityBehaviour<T> {

	public static final BehaviourType<CAAudioBehaviour<?>> TYPE = new BehaviourType<>();

	private final AmbienceGroup group;
	private final Predicate<T> active;
	private final ToDoubleFunction<T> pitchSource;

	public CAAudioBehaviour(T be, AmbienceGroup group, Predicate<T> active, ToDoubleFunction<T> pitchSource) {
		super(be);
		this.group = group;
		this.active = active;
		this.pitchSource = pitchSource;
	}

	@Override
	public void tick() {
		super.tick();
		if (!active.test(blockEntity))
			return;
		CASoundScapes.play(group, getPos(), mapPitch(pitchSource.applyAsDouble(blockEntity)));
	}

	protected float mapPitch(double speed) {
		float abs = (float) Math.abs(speed);
		if (abs == 0)
			return 1;
		return (float) Math.min(2f, Math.max(0.5f, abs / 256f + 0.75f));
	}

	@Override
	public BehaviourType<?> getType() {
		return TYPE;
	}
}
