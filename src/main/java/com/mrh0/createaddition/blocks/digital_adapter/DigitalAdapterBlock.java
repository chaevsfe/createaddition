package com.mrh0.createaddition.blocks.digital_adapter;

import com.mrh0.createaddition.index.CABlockEntities;
import com.zurrtum.create.content.equipment.wrench.IWrenchable;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class DigitalAdapterBlock extends Block implements IBE<DigitalAdapterBlockEntity>, IWrenchable {
    public DigitalAdapterBlock(Properties props) {
        super(props);
    }

    @Override
    public Class<DigitalAdapterBlockEntity> getBlockEntityClass() {
        return DigitalAdapterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DigitalAdapterBlockEntity> getBlockEntityType() {
        return CABlockEntities.DIGITAL_ADAPTER;
    }
}
