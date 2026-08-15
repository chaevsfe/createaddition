package com.mrh0.createaddition.blocks.connector;

import com.mrh0.createaddition.blocks.connector.base.AbstractConnectorBlock;
import com.mrh0.createaddition.blocks.connector.base.AbstractConnectorBlockEntity;
import com.mrh0.createaddition.config.CACommonConfig;
import com.mrh0.createaddition.energy.network.EnergyNetwork;
import com.mrh0.createaddition.index.CABlockEntities;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.List;

public class SmallLightConnectorBlockEntity extends AbstractConnectorBlockEntity {

    private final static float OFFSET_HEIGHT = 5.5f;
    public final static Vec3 OFFSET_DOWN = new Vec3(0f, -OFFSET_HEIGHT/16f, 0f);
    public final static Vec3 OFFSET_UP = new Vec3(0f, OFFSET_HEIGHT/16f, 0f);
    public final static Vec3 OFFSET_NORTH = new Vec3(0f, 0f, -OFFSET_HEIGHT/16f);
    public final static Vec3 OFFSET_WEST = new Vec3(-OFFSET_HEIGHT/16f, 0f, 0f);
    public final static Vec3 OFFSET_SOUTH = new Vec3(0f, 0f, OFFSET_HEIGHT/16f);
    public final static Vec3 OFFSET_EAST = new Vec3(OFFSET_HEIGHT/16f, 0f, 0f);

    private int posTimeOffset = 0;

    public SmallLightConnectorBlockEntity(BlockEntityType<?> blockEntityTypeIn, BlockPos pos, BlockState state) {
        super(blockEntityTypeIn, pos, state);

        posTimeOffset = 10 + (Math.abs(pos.getX()*31 + pos.getY()*45 + pos.getZ()*33) % 7) * 3;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                CABlockEntities.SMALL_LIGHT_CONNECTOR.get(),
                (be, context) -> be.internal
        );
    }

    @Override
    public void read(CompoundTag nbt, HolderLookup.Provider registries, boolean clientPacket) {
        tickToggleTimer = nbt.getInt("tick_toggle_timer");
        super.read(nbt, registries, clientPacket);
    }

    @Override
    public void writeSafe(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putInt("tick_toggle_timer", tickToggleTimer);
        super.writeSafe(nbt, registries);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> list) {}

    @Override
    public int getMaxIn() {
        return CACommonConfig.COMMON.SMALL_CONNECTOR_MAX_INPUT.get();
    }

    @Override
    public int getMaxOut() {
        return CACommonConfig.COMMON.SMALL_CONNECTOR_MAX_OUTPUT.get();
    }

    @Override
    public int getNodeCount() {
        return 4;
    }

    @Override
    public Vec3 getNodeOffset(int node) {
        return switch (getBlockState().getValue(AbstractConnectorBlock.FACING)) {
            case DOWN -> OFFSET_DOWN;
            case UP -> OFFSET_UP;
            case NORTH -> OFFSET_NORTH;
            case WEST -> OFFSET_WEST;
            case SOUTH -> OFFSET_SOUTH;
            case EAST -> OFFSET_EAST;
        };
    }

    @Override
    public ConnectorType getConnectorType() {
        return ConnectorType.Small;
    }

    public int getMaxWireLength() {
        return CACommonConfig.COMMON.SMALL_CONNECTOR_MAX_LENGTH.get();
    }

    private int tickToggleTimer = 0;
    @Override
    protected void specialTick() {
        if(getLevel() == null) return;
        if(level.isClientSide()) return;
        EnergyNetwork network = getNetwork(0);
        if (network != null) network.demand(1);
        boolean hasEnergy = network != null && network.pull(CACommonConfig.COMMON.SMALL_LIGHT_CONNECTOR_CONSUMPTION.get(), false) > 0;
        tickToggleTimer = tickToggleTimer + (hasEnergy ? 1 : -1);

        if (tickToggleTimer >= posTimeOffset) {
            tickToggleTimer = posTimeOffset;
            if (!getBlockState().getValue(SmallLightConnectorBlock.POWERED))
                getLevel().setBlockAndUpdate(getBlockPos(), getBlockState()
                        .setValue(SmallLightConnectorBlock.POWERED, true));
        }

        if (tickToggleTimer <= -posTimeOffset) {
            tickToggleTimer = -posTimeOffset;
            if (getBlockState().getValue(SmallLightConnectorBlock.POWERED))
                getLevel().setBlockAndUpdate(getBlockPos(), getBlockState()
                        .setValue(SmallLightConnectorBlock.POWERED, false));
        }
    }
}
