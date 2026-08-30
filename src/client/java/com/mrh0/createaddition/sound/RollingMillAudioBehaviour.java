package com.mrh0.createaddition.sound;

import com.mrh0.createaddition.blocks.rolling_mill.RollingMillBlockEntity;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.audio.KineticAudioBehaviour;
import com.zurrtum.create.client.foundation.sound.SoundScapes;
import net.minecraft.util.Mth;

public class RollingMillAudioBehaviour extends KineticAudioBehaviour<RollingMillBlockEntity> {

	public RollingMillAudioBehaviour(RollingMillBlockEntity be) {
		super(be);
	}

	@Override
	public void tickAudio() {
		super.tickAudio();

		float speed = blockEntity.getSpeed();
		if (speed == 0) return;
		if (blockEntity.inputInv.getItem(0).isEmpty()) return;

		float pitch = Mth.clamp((Math.abs(speed) / 256f) + .45f, .85f, 1f);
		SoundScapes.play(SoundScapes.AmbienceGroup.MILLING, blockEntity.getBlockPos(), pitch);
	}
}
