package com.github.apace100.apoli.client;

import com.github.apace100.apoli.network.ApoliPackets;
import com.github.apace100.apoli.registry.ApoliRegistryKeys;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ApoliClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        ApoliPackets.registerS2C();
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {

            client.world.getRegistryManager().get(ApoliRegistryKeys.POWER).forEach(power -> power.init(client.world.getRegistryManager()));

        });
    }
}
