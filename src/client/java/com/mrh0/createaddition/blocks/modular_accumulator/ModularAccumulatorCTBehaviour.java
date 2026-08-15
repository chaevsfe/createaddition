package com.mrh0.createaddition.blocks.modular_accumulator;

import com.mrh0.createaddition.index.CASpriteShifts;
import com.zurrtum.create.api.connectivity.ConnectivityHandler;
import com.zurrtum.create.client.foundation.block.connected.HorizontalCTBehaviour;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ModularAccumulatorCTBehaviour extends HorizontalCTBehaviour {

    public ModularAccumulatorCTBehaviour() {
        super(CASpriteShifts.ACCUMULATOR, CASpriteShifts.ACCUMULATOR_TOP);
    }

    @Override
    public boolean connectsTo(
        BlockState state,
        BlockState other,
        BlockAndTintGetter reader,
        BlockPos pos,
        BlockPos otherPos,
        Direction face
    ) {
        return state.getBlock() == other.getBlock() && ConnectivityHandler.isConnected(reader, pos, otherPos);
    }
}
