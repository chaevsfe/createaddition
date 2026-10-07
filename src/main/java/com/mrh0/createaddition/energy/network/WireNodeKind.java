package com.mrh0.createaddition.energy.network;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.mrh0.createaddition.index.CABlockEntities;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

public enum WireNodeKind {
	CONNECTOR,
	RELAY;

	private static Map<Identifier, WireNodeKind> byBlockEntityId;

	public int portOf(int node) {
		return this == RELAY && node >= 4 ? 1 : 0;
	}

	public int portCount() {
		return this == RELAY ? 2 : 1;
	}

	public boolean isBridge() {
		return this == RELAY;
	}

	public boolean isPowered(@Nullable BlockState state) {
		return isBridge() && state != null && state.hasProperty(BlockStateProperties.POWERED) && state.getValue(BlockStateProperties.POWERED);
	}

	public String getSerializedName() {
		return name().toLowerCase(Locale.ROOT);
	}

	@Nullable
	public static WireNodeKind byName(String name) {
		for (WireNodeKind kind : values())
			if (kind.getSerializedName().equals(name)) return kind;
		return null;
	}

	@Nullable
	public static WireNodeKind fromBlockEntityId(String id) {
		if (byBlockEntityId == null) {
			Map<Identifier, WireNodeKind> map = new HashMap<>();
			map.put(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CABlockEntities.SMALL_CONNECTOR), CONNECTOR);
			map.put(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CABlockEntities.SMALL_LIGHT_CONNECTOR), CONNECTOR);
			map.put(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CABlockEntities.LARGE_CONNECTOR), CONNECTOR);
			map.put(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(CABlockEntities.REDSTONE_RELAY), RELAY);
			byBlockEntityId = map;
		}
		Identifier key = Identifier.tryParse(id);
		return key == null ? null : byBlockEntityId.get(key);
	}
}
