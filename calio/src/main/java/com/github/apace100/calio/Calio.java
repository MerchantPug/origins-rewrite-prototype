package com.github.apace100.calio;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Calio implements ModInitializer {
	public static final String MODID = "calio";
    public static final Logger LOGGER = LoggerFactory.getLogger("Calio");

	@Override
	public void onInitialize() {
		LOGGER.info("Calio loaded!");
	}
}