package com.mrh0.createaddition.sound;

import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import com.mrh0.createaddition.sound.CASoundScapes.AmbienceGroup;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;

public class CAFlatAudioBehaviour<T extends SmartBlockEntity> extends CAAudioBehaviour<T> {

	public CAFlatAudioBehaviour(T be, AmbienceGroup group, Predicate<T> active, ToDoubleFunction<T> pitchSource) {
		super(be, group, active, pitchSource);
	}

	@Override
	protected float mapPitch(double pitch) {
		return (float) pitch;
	}
}
