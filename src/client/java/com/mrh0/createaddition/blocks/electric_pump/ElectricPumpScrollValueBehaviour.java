package com.mrh0.createaddition.blocks.electric_pump;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.CenteredSideValueBoxTransform;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import com.zurrtum.create.client.foundation.utility.CreateLang;

public class ElectricPumpScrollValueBehaviour extends ScrollValueBehaviour {

	public ElectricPumpScrollValueBehaviour(ElectricPumpBlockEntity be) {
		super(CreateLang.translateDirect("generic.speed"), be, new CenteredSideValueBoxTransform(
				(state, side) -> side.getAxis() != state.getValue(ElectricPumpBlock.FACING).getAxis()));
	}
}
