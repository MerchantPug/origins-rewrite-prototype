package com.github.apace100.origins;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Origins implements ModInitializer {
	public static final String MODID = "origins";
    public static final Logger LOGGER = LoggerFactory.getLogger("Origins");

	@Override
	public void onInitialize() {
		LOGGER.info("Origins loaded!");
	}
}