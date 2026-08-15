package com.mrh0.createaddition;

import com.mrh0.createaddition.blocks.alternator.AlternatorVisual;
import com.mrh0.createaddition.blocks.electric_motor.ElectricMotorVisual;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerRenderer;
import com.mrh0.createaddition.blocks.liquid_blaze_burner.LiquidBlazeBurnerVisual;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorCTBehaviour;
import com.mrh0.createaddition.blocks.modular_accumulator.ModularAccumulatorRenderer;
import com.mrh0.createaddition.blocks.portable_energy_interface.PEIVisual;
import com.mrh0.createaddition.blocks.portable_energy_interface.PortableEnergyInterfaceRenderer;
import com.mrh0.createaddition.blocks.rolling_mill.RollingMillVisual;
import com.mrh0.createaddition.blocks.servo_motor.ServoMotorRenderer;
import com.mrh0.createaddition.blocks.servo_motor.ServoMotorVisual;
import com.mrh0.createaddition.client.goggles.ConnectorTooltipBehaviour;
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
import com.mrh0.createaddition.ponder.CAPonderPlugin;
import com.zurrtum.create.client.AllBlockEntityBehaviours;
import com.zurrtum.create.client.AllBlockEntityRenders;
import com.zurrtum.create.client.AllFluidConfigs;
import com.zurrtum.create.client.AllModels;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleBlockEntityVisualizer;
import com.zurrtum.create.client.infrastructure.model.CTModel;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class CreateAdditionClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		CAPartials.init();
		registerModels();
		registerBlockEntityRenders();
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

		SimpleBlockEntityVisualizer.builder(CABlockEntities.ELECTRIC_MOTOR).factory(ElectricMotorVisual::new).apply();
		SimpleBlockEntityVisualizer.builder(CABlockEntities.ALTERNATOR).factory(AlternatorVisual::new).apply();
		SimpleBlockEntityVisualizer.builder(CABlockEntities.ROLLING_MILL).factory(RollingMillVisual::new).apply();
	}

	private static void registerBlockEntityBehaviours() {
		AllBlockEntityBehaviours.add(CABlockEntities.SMALL_CONNECTOR, ConnectorTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.SMALL_LIGHT_CONNECTOR, ConnectorTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.LARGE_CONNECTOR, ConnectorTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.REDSTONE_RELAY, RedstoneRelayTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.TESLA_COIL, TeslaCoilTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.LIQUID_BLAZE_BURNER, LiquidBlazeBurnerTooltipBehaviour::new);
		AllBlockEntityBehaviours.add(CABlockEntities.MODULAR_ACCUMULATOR, ModularAccumulatorTooltipBehaviour::new);
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
