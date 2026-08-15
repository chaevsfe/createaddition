package com.mrh0.createaddition.energy;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;

public class NodeMovementBehaviour extends MovementBehaviour {

	@Override
	public void startMoving(MovementContext context) {
		if (context.blockEntityData == null) return;
		context.blockEntityData.putBoolean("contraption", true);
	}
}
