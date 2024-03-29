package com.github.apace100.apoli;

import com.github.apace100.apoli.command.ApollArgumentTypes;
import com.github.apace100.apoli.command.PowerCommand;
import com.github.apace100.apoli.command.ResourceCommand;
import com.github.apace100.apoli.network.ApoliPackets;
import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.power.type.PowerTypes;
import com.github.apace100.apoli.attachment.ApoliAttachmentTypes;
import com.github.apace100.apoli.attachment.ApoliEntityApis;
import com.github.apace100.apoli.registry.ApoliRegistryKeys;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Apoli implements ModInitializer {

	public static final String MODID = "apoli";
    public static final Logger LOGGER = LoggerFactory.getLogger("Apoli");

	@Override
	public void onInitialize() {

		PowerTypes.register();
		DynamicRegistries.registerSynced(ApoliRegistryKeys.POWER, Power.CODEC, DynamicRegistries.SyncOption.SKIP_WHEN_EMPTY);

		ApoliAttachmentTypes.register();
		ApoliEntityApis.register();
		ApollArgumentTypes.register();
		ApoliPackets.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			LiteralCommandNode<ServerCommandSource> apoliNode = createApoliNode(dispatcher);
			PowerCommand.register(registryAccess, apoliNode);
			ResourceCommand.register(registryAccess, apoliNode);
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server ->
				server.getRegistryManager().get(ApoliRegistryKeys.POWER).forEach(power ->
				power.init(server.getRegistryManager())));

		CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {

			if (client) {
				return;
			}

			Registry<Power> powerRegistry = registries.get(ApoliRegistryKeys.POWER);

			if (powerRegistry != null) {
				LOGGER.info("Loaded " + powerRegistry.stream().count() + " powers");
				LOGGER.info("Loaded " + powerRegistry.streamTags().count() + " power tags");
			}

		});

		LOGGER.info("Apoli loaded!");

	}

	public static Identifier identifier(String path) {
		return new Identifier(Apoli.MODID, path);
	}

	private static LiteralCommandNode<ServerCommandSource> createApoliNode(CommandDispatcher<ServerCommandSource> dispatcher) {
		LiteralCommandNode<ServerCommandSource> apoliNode = CommandManager
				.literal("apoli")
				.requires(c -> c.hasPermissionLevel(2))
				.build();
		dispatcher.getRoot().addChild(apoliNode);
		return apoliNode;
	}

}