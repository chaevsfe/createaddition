package com.mrh0.createaddition;

import com.mrh0.createaddition.blocks.alternator.AlternatorRenderer;
import com.mrh0.createaddition.blocks.alternator.AlternatorVisual;
import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorRenderer;
import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorVisual;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerRenderer;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerVisual;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorCTBehaviour;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorRenderer;
import com.mrh0.createaddition.blocks.portable_energy_interface.PEIActorVisual;
import com.mrh0.createaddition.blocks.portable_energy_interface.PEIVisual;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceRenderer;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillRenderer;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillVisual;
import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorScrollValueBehaviour;
import com.mrh0.createaddition.blocks.servo_motor.ServoMotorClientBehaviours;
import com.mrh0.createaddition.blocks.servo_motor.ServoMotorRenderer;
import com.mrh0.createaddition.blocks.servo_motor.ServoMotorVisual;
import com.mrh0.createaddition.rendering.WireNodeRenderer;
import com.mrh0.createaddition.trains.schedule.condition.EnergyThresholdConditionRender;
import com.mrh0.createaddition.client.goggles.AlternatorTooltipBehaviour;
import com.mrh0.createaddition.client.goggles.ConnectorTooltipBehaviour;
import com.mrh0.createaddition.client.goggles.ElectricMotorTooltipBehaviour;
import com.mrh0.createaddition.client.goggles.LiquidBlazeBurnerTooltipBehaviour;
import com.mrh0.createaddition.client.goggles.ModularAccumulatorTooltipBehaviour;
import com.mrh0.createaddition.client.goggles.RedstoneRelayTooltipBehaviour;
import com.mrh0.createaddition.client.goggles.TeslaCoilTooltipBehaviour;
import com.mrh0.createaddition.event.ClientEventHandler;
import com.mrh0.createaddition.index.CABlockEntities;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CAFluids;
import com.mrh0.createaddition.index.CAPartials;
import com.mrh0.createaddition.network.EnergyNetworkPacketPayload;
import com.mrh0.createaddition.network.TimeRemainingPacketPayload;
import com.mrh0.createaddition.blocks.alternator.AlternatorBlockEntity;
import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorBlockEntity;
import com.mrh0.createaddition.blocks.servo_motor.ServoMotorBlockEntity;
import com.mrh0.createaddition.ponder.CAPonderPlugin;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorBlockEntity;
import com.mrh0.createaddition.blocks.tesla_coil.TeslaCoilBlockEntity;
import com.mrh0.createaddition.sound.CAAudioBehaviour;
import com.mrh0.createaddition.sound.CAFlatAudioBehaviour;
import com.mrh0.createaddition.sound.RollingMillAudioBehaviour;
import com.mrh0.createaddition.sound.CASoundScapes.AmbienceGroup;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.AllItemTooltips;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.AllScheduleRenders;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.GeneratingKineticTooltipBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.KineticTooltipBehaviour;
import com.zurrtum.create.client.infrastructure.model.CTModel;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;

import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CreateAdditionClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		CAPartials.init();
		registerModels();
		registerBlockEntityRenders();
		registerMovementRenders();
		registerScheduleRenders();
		registerItemTooltips();
		registerBlockEntityBehaviours();
		registerFluidConfigs();
		registerClientReceivers();
		ClientEventHandler.register();
		PonderIndex.addPlugin(new CAPonderPlugin());
	}

	private static void registerModels() {
		AllModels.register(CABlocks.MODULAR_ACCUMULATOR, CTModel.of(new ModularAccumulatorCTBehaviour()));
	}

	private static void registerBlockEntityRenders() {
		AllBlockEntityRenders.visual(CABlockEntities.SERVO_MOTOR, ServoMotorRenderer::new, ServoMotorVisual::new);
		AllBlockEntityRenders.visual(CABlockEntities.LIQUID_BLAZE_BURNER, LiquidBlazeBurnerRenderer::new, LiquidBlazeBurnerVisual::new);
		AllBlockEntityRenders.visual(CABlockEntities.PORTABLE_ENERGY_INTERFACE, PortableEnergyInterfaceRenderer::new, PEIVisual::new);
		AllBlockEntityRenders.render(CABlockEntities.MODULAR_ACCUMULATOR, ModularAccumulatorRenderer::new);

		AllBlockEntityRenders.visual(CABlockEntities.ELECTRIC_MOTOR, ElectricMotorRenderer::new, ElectricMotorVisual::new);
		AllBlockEntityRenders.visual(CABlockEntities.ALTERNATOR, AlternatorRenderer::new, AlternatorVisual::new);
		AllBlockEntityRenders.visual(CABlockEntities.ROLLING_MILL, RollingMillRenderer::new, RollingMillVisual::new);

		AllBlockEntityRenders.render(CABlockEntities.SMALL_CONNECTOR, WireNodeRenderer::new);
		AllBlockEntityRenders.render(CABlockEntities.SMALL_LIGHT_CONNECTOR, WireNodeRenderer::new);
		AllBlockEntityRenders.render(CABlockEntities.LARGE_CONNECTOR, WireNodeRenderer::new);
		AllBlockEntityRenders.render(CABlockEntities.REDSTONE_RELAY, WireNodeRenderer::new);
	}

	private static void registerMovementRenders() {
		MovementBehaviour behaviour = MovementBehaviour.REGISTRY.get(CABlocks.PORTABLE_ENERGY_INTERFACE);
		if (behaviour != null)
			behaviour.attachRender = new MovementRenderBehaviour() {
				@Override
				public ActorVisual createVisual(VisualizationContext context, VirtualRenderWorld world, MovementContext movementContext) {
					return new PEIActorVisual(context, world, movementContext);
				}
			};
	}

	private static void registerScheduleRenders() {
		AllScheduleRenders.ALL.put(CreateAddition.asResource("energy_threshold"), new EnergyThresholdConditionRender());
	}

	private static void registerItemTooltips() {
		for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
			if (entry.getKey().identifier().getNamespace().equals(CreateAddition.MODID))
				AllItemTooltips.register(entry.getValue());
		}
	}

	private static void registerBlockEntityBehaviours() {
		AllBlockEntityBehaviours.add(CABlockEntities.SMALL_CONNECTOR, ConnectorTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.SMALL_LIGHT_CONNECTOR, ConnectorTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.LARGE_CONNECTOR, ConnectorTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.REDSTONE_RELAY, RedstoneRelayTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.TESLA_COIL, TeslaCoilTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.LIQUID_BLAZE_BURNER, LiquidBlazeBurnerTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.MODULAR_ACCUMULATOR, ModularAccumulatorTooltipBehaviour::new);

		AllBlockEntityBehaviours.add(
			CABlockEntities.ELECTRIC_MOTOR,
			ElectricMotorScrollValueBehaviour::new,
			ElectricMotorTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(
			CABlockEntities.SERVO_MOTOR,
			ServoMotorClientBehaviours::speed,
			ServoMotorClientBehaviours::movementMode,
			ServoMotorClientBehaviours::maxAngle,
			ServoMotorClientBehaviours::minAngle,
			GeneratingKineticTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.ALTERNATOR, AlternatorTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.ROLLING_MILL, KineticTooltipBehaviour::new);

		registerAmbientAudio();
	}

	private static void registerAmbientAudio() {
		AllBlockEntityBehaviours.add(
			CABlockEntities.ELECTRIC_MOTOR,
			be -> new CAAudioBehaviour<>(be, AmbienceGroup.DYNAMO, b -> b.getSpeed() != 0, ElectricMotorBlockEntity::getSpeed));
		AllBlockEntityBehaviours.add(
			CABlockEntities.ALTERNATOR,
			be -> new CAAudioBehaviour<>(be, AmbienceGroup.DYNAMO, b -> b.getSpeed() != 0, AlternatorBlockEntity::getSpeed));
		AllBlockEntityBehaviours.add(
			CABlockEntities.SERVO_MOTOR,
			be -> new CAAudioBehaviour<>(be, AmbienceGroup.DYNAMO, b -> b.getSpeed() != 0, ServoMotorBlockEntity::getSpeed));
		AllBlockEntityBehaviours.add(CABlockEntities.ROLLING_MILL, RollingMillAudioBehaviour::new);
		AllBlockEntityBehaviours.add(
			CABlockEntities.TESLA_COIL,
			be -> new CAFlatAudioBehaviour<>(be, AmbienceGroup.TESLA, TeslaCoilBlockEntity::isPoweredState, b -> 1f));
		AllBlockEntityBehaviours.add(
			CABlockEntities.MODULAR_ACCUMULATOR,
			be -> new CAFlatAudioBehaviour<>(be, AmbienceGroup.CHARGE, ModularAccumulatorBlockEntity::isEnergyChanging,
				ModularAccumulatorBlockEntity::audioPitch));
	}

	private static void registerFluidConfigs() {
		AllFluidConfigs.model(CAFluids.SEED_OIL);
		AllFluidConfigs.model(CAFluids.BIOETHANOL);
	}

	private static void registerClientReceivers() {
		ClientPlayNetworking.registerGlobalReceiver(
			EnergyNetworkPacketPayload.TYPE,
			(payload, context) -> EnergyNetworkPacketPayload.updateClientCache(payload.pos(), payload.demand(), payload.buff()));
		ClientPlayNetworking.registerGlobalReceiver(
			TimeRemainingPacketPayload.TYPE,
			(payload, context) -> TimeRemainingPacketPayload.updateClientCache(payload.timeRemaining()));
	}
}
