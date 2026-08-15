package com.mrh0.createaddition.blocks.connector;

import com.mrh0.createaddition.blocks.connector.base.AbstractConnectorBlock;
import com.mrh0.createaddition.index.CABlockEntities;
import com.mrh0.createaddition.shapes.CAShapes;
import com.zurrtum.create.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SmallConnectorBlock extends AbstractConnectorBlock<SmallConnectorBlockEntity> {
    public static final VoxelShaper CONNECTOR_SHAPE = CAShapes.shape(6, 0, 6, 10, 5, 10).forDirectional();
    public SmallConnectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Class<SmallConnectorBlockEntity> getBlockEntityClass() {
        return SmallConnectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SmallConnectorBlockEntity> getBlockEntityType() {
        return CABlockEntities.SMALL_CONNECTOR;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return CONNECTOR_SHAPE.get(state.getValue(FACING).getOpposite());
    }
}
