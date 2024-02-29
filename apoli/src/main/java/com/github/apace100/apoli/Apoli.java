package com.github.apace100.apoli;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Apoli implements ModInitializer {
	public static final String MOD_ID = "apoli";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Apoli loaded!");
	}
}