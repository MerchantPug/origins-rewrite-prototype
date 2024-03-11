package com.github.apace100.origins;

import com.github.apace100.apoli.command.PowerCommand;
import com.github.apace100.apoli.command.ResourceCommand;
import com.github.apace100.calio.api.GlobalIdentifierAlias;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Origins implements ModInitializer {
	public static final String MODID = "origins";
    public static final Logger LOGGER = LoggerFactory.getLogger("Origins");

	@Override
	public void onInitialize() {
		GlobalIdentifierAlias.INSTANCE.addNamespaceAlias("origins", "apoli");

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LiteralCommandNode<ServerCommandSource> originsNode = createOriginsNode(dispatcher);
			PowerCommand.register(registryAccess, originsNode);
			ResourceCommand.register(registryAccess, originsNode);
		});

		LOGGER.info("Origins loaded!");
	}

	public static Identifier identifier(String path) {
		return new Identifier(Origins.MODID, path);
	}

	private static LiteralCommandNode<ServerCommandSource> createOriginsNode(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> originsNode = CommandManager
				.literal("origins")
				.requires(c -> c.hasPermissionLevel(2))
				.build();
		dispatcher.getRoot().addChild(originsNode);
		return originsNode;
	}

}