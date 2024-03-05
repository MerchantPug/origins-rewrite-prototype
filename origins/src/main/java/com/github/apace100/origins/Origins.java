package com.github.apace100.origins;

import com.github.apace100.calio.Calio;
import com.github.apace100.calio.api.IdentifierAlias;
import net.fabricmc.api.ModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Origins implements ModInitializer {
	public static final String MODID = "origins";
    public static final Logger LOGGER = LoggerFactory.getLogger("Origins");

	@Override
	public void onInitialize() {
		IdentifierAlias.GLOBAL.addNamespaceAlias("origins", "apoli");
		LOGGER.info("Origins loaded!");
	}

	public static Identifier identifier(String path) {
		return new Identifier(Calio.MODID, path);
	}

}