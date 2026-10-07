package com.mrh0.createaddition.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mrh0.createaddition.energy.network.WireGraph;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class WireGraphCommand {
	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(Commands.literal("cca_wires").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
			.then(action("stats", (source, graph) -> {
				source.sendSuccess(graph::describe, false);
				return 1;
			}))
			.then(action("verify", (source, graph) -> {
				Component problems = graph.describeProblems();
				int count = graph.verifyAll();
				source.sendSuccess(() -> Component.literal("Checking " + count + " wire nodes against the world; ").append(problems), true);
				return count;
			}))
			.then(action("rebuild", (source, graph) -> {
				graph.rebuild();
				source.sendSuccess(() -> Component.literal("Rebuilt the wire graph"), true);
				return 1;
			})));
	}

	private interface Action {
		int run(CommandSourceStack source, WireGraph graph);
	}

	private static LiteralArgumentBuilder<CommandSourceStack> action(String name, Action action) {
		return Commands.literal(name).executes(context -> {
			CommandSourceStack source = context.getSource();
			WireGraph graph = WireGraph.get(source.getLevel());
			if (graph == null) {
				source.sendFailure(Component.literal("This dimension has no wire graph"));
				return 0;
			}
			return action.run(source, graph);
		});
	}
}
