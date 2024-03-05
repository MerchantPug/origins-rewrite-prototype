package com.github.apace100.apoli;

import com.github.apace100.apoli.power.Power;
import com.github.apace100.apoli.power.type.PowerTypes;
import com.github.apace100.apoli.registry.ApoliRegistryKeys;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.Registry;
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

}